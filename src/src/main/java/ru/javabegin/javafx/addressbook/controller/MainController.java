package ru.javabegin.javafx.addressbook.controller;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.javabegin.javafx.addressbook.Main;
import ru.javabegin.javafx.addressbook.interfaces.impls.SQLiteAddressBook;
import ru.javabegin.javafx.addressbook.objects.Person;
import ru.javabegin.javafx.addressbook.utils.DialogManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


// контроллер - обработчик действий пользователя на главном окне приложения
public class MainController implements Initializable {

    // для доступа к данным используем реализацию на основе БД SQLite
    private SQLiteAddressBook addressBookImpl = new SQLiteAddressBook();

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private TableView tableAddressBook;

    @FXML
    private TableColumn<Person, String> columnFIO;

    @FXML
    private TableColumn<Person, String> columnPhone;

    @FXML
    private Label labelCount;

    private Parent fxmlEdit;

    private EditDialogController editDialogController;

    private Stage editDialogStage;


    // вызывается автоматически при загрузке окна
    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        fillData(); // начальная загрузка данных
        initListeners(); // слушатели изменений данных (можем обновлять компоненты)
        initLoaders(); // инициализируем все другие окна, которые участвуют в приложении
    }


    // заполняет таблицу и обновляет счетчик
    private void fillData() {
        fillTable();
        updateCountLabel();
    }

    // заполняет таблицу данными из БД
    private void fillTable() {
        // чтобы tableView понимал какие поля брать из Person и в какие столбцы подставлять значение
        columnFIO.setCellValueFactory(new PropertyValueFactory<Person, String>("fio"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<Person, String>("phone"));
        tableAddressBook.setItems(addressBookImpl.getPersonList());
    }


    // слушатели различных действий - чтобы среагировать на них
    private void initListeners() {

        // слушает изменения в коллекции для обновления надписи "Кол-во"
        addressBookImpl.getPersonList().addListener(new ListChangeListener<Person>() {
            @Override
            public void onChanged(Change<? extends Person> c) {
                updateCountLabel();
            }
        });


        // слушает двойное нажатие для редактирования записи
        tableAddressBook.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {  // если нажатие было двойное
                    btnEdit.fire(); // имитируем нажатие на кнопку редактирования
                }
            }
        });


    }

    // какие другие окна будут загружаться
    private void initLoaders() {
        try {
            FXMLLoader editFxmlLoader = new FXMLLoader(Main.class.getResource("edit.fxml")); // окно редактирования
            fxmlEdit = editFxmlLoader.load();
            editDialogController = editFxmlLoader.getController(); // это важно получить, чтобы установить person для редактирования/создания

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // обновляет счетчик
    private void updateCountLabel() {
        labelCount.setText("Кол-во: " + addressBookImpl.getPersonList().size());
    }

    // обработка нажатой кнопки
    public void buttonActionPressed(ActionEvent actionEvent) {

        Object source = actionEvent.getSource(); // кто источник действия

        // если нажата не кнопка - выходим из метода (вдруг нечаянно "подвязали" этот метод для другого контрола)
        if (!(source instanceof Button)) {
            return;
        }

        // какой объект пользователь выбрал из tableView
        Person selectedPerson = (Person) tableAddressBook.getSelectionModel().getSelectedItem();

        // была точно нажата кнопка - поэтому работаем в объектом как с кнопкой
        Button clickedButton = (Button) source;

        // нужно ли будет заново производить поиск (после редактирования), вдруг не изменились данные
        boolean research = false;

        // определяем какая кнопка была нажата
        switch (clickedButton.getId()) {
            case "btnAdd":
                editDialogController.setPerson(new Person()); // передаем новый объект, который будем заполнять в диалог. окне
                showDialog(); // показать диалог. окно для редактирования

                if (editDialogController.isSaveClicked()) { // если в диалог. окне нажали ОК, а не отмена
                    addressBookImpl.add(editDialogController.getPerson()); // получаем заполненного person
                    research = true; // флаг, что есть изменения и потребуется обновление tableView
                }


                break;

            case "btnEdit":
                if (!personIsSelected(selectedPerson)) {
                    return;
                }

                // selectedPerson всегда ссылается на один и тот же объект, который перед редактированием и после
                // Т.е. selectedPerson будет содержать измененные данные

                editDialogController.setPerson(selectedPerson);
                showDialog(); // показать диалог. окно для редактирования

                if (editDialogController.isSaveClicked()) { // если в диалог. окне нажали ОК, а не отмена
                    // коллекция в addressBookImpl и так обновляется, т.к. мы ее редактируем в диалоговом окне и сохраняем при нажатии на ОК
                    addressBookImpl.update(selectedPerson);
                    research = true; // флаг, что есть изменения и потребуется обновление tableView
                }

                break;

            case "btnDelete":
                if (!personIsSelected(selectedPerson) || !(confirmDelete())) {
                    return;
                }

                research = true; // флаг, что есть изменения и потребуется обновление tableView
                addressBookImpl.delete(selectedPerson);
                break;
        }


        // заново обновляем tableView
        if (research) {
            actionSearch(actionEvent);
        }

    }

    // диалоговое окно подтверждения удаления
    private boolean confirmDelete() {
        if (DialogManager.showConfirmDialog("Подтверждение", "Удалить?").get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }

    }

    // если пытаемся редактировать, но не выбрали объект из tableView
    private boolean personIsSelected(Person selectedPerson) {
        if (selectedPerson == null) {
            DialogManager.showInfoDialog("Ошибка", "Выберите значение");
            return false;
        }
        return true;
    }


    // отображение диалог. окна
    private void showDialog() {

        if (editDialogStage == null) {
            editDialogStage = new Stage();
            editDialogStage.setTitle("Редактировать");
            editDialogStage.setMinHeight(150);
            editDialogStage.setMinWidth(300);
            editDialogStage.setResizable(false);
            editDialogStage.setScene(new Scene(fxmlEdit));
            editDialogStage.initModality(Modality.WINDOW_MODAL);
        }

        editDialogStage.showAndWait(); // для ожидания закрытия окна

    }


    // поиск по адресной книге
    public void actionSearch(ActionEvent actionEvent) {

        if (txtSearch.getText().trim().length() == 0) {
            addressBookImpl.findAll();
        } else {
            addressBookImpl.find(txtSearch.getText());
        }

    }
}
