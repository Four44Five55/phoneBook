package ru.example.phonebook.controller;

import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import ru.example.phonebook.Main;
import ru.example.phonebook.interfaces.impls.SQLitePhoneBook;
import ru.example.phonebook.objects.Person;
import ru.example.phonebook.utils.DialogManager;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;


// контроллер - обработчик действий пользователя на главном окне приложения
public class MainController implements Initializable {

    // для доступа к данным используем реализацию на основе БД SQLite
    private SQLitePhoneBook phoneBookImpl = new SQLitePhoneBook();

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Person> tablePhoneBook;

    @FXML
    private TableColumn<Person, String> columnFIO;

    @FXML
    private TableColumn<Person, String> columnPhone;

    @FXML
    private TableColumn<Person, String> columnDepartment;

    @FXML
    private TableColumn<Person, String> columnNotes;

    @FXML
    private Label labelCount;

    @FXML
    private Button btnTheme;

    @FXML
    private Button btnImport;

    private Parent fxmlEdit;

    private boolean isDarkTheme = true;

    private EditDialogController editDialogController;

    private Stage editDialogStage;


    // вызывается автоматически при загрузке окна
    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        fillData(); // начальная загрузка данных
        initListeners(); // слушатели изменений данных (можем обновлять компоненты)
        initLoaders(); // инициализируем все другие окна, которые участвуют в приложении

        // Загружаем сохранённую ширину столбцов
        Platform.runLater(() -> {
            loadColumnWidths();
            applyTheme(true);
        });
    }

    // Настраивает сохранение ширины при закрытии окна
    private void setupColumnWidthSaving() {
        // Будет вызвано из Main.java при закрытии
    }

    // Получает файл настроек
    private File getSettingsFile() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".phonebook-settings.properties");
    }

    // Сохраняет ширину столбцов (вызывается извне)
    public void saveColumnWidths() {
        try {
            try (PrintWriter writer = new PrintWriter(new FileWriter(getSettingsFile()))) {
                writer.println("columnFIO=" + columnFIO.getWidth());
                writer.println("columnPhone=" + columnPhone.getWidth());
                writer.println("columnDepartment=" + columnDepartment.getWidth());
                writer.println("columnNotes=" + columnNotes.getWidth());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Загружает сохранённую ширину столбцов
    private void loadColumnWidths() {
        File settingsFile = getSettingsFile();
        System.out.println("Settings file: " + settingsFile.getAbsolutePath());
        System.out.println("Settings file exists: " + settingsFile.exists());

        if (!settingsFile.exists()) {
            return;
        }

        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                props.load(fis);
            }

            // Считываем сохранённые ширины
            final double fioWidth = Double.parseDouble(props.getProperty("columnFIO", "250"));
            final double phoneWidth = Double.parseDouble(props.getProperty("columnPhone", "60"));
            final double departmentWidth = Double.parseDouble(props.getProperty("columnDepartment", "7"));
            final double notesWidth = Double.parseDouble(props.getProperty("columnNotes", "75"));

            System.out.println("Loaded widths: FIO=" + fioWidth + ", Phone=" + phoneWidth + ", Dept=" + departmentWidth + ", Notes=" + notesWidth);

            // Используем UNCONSTRAINED_RESIZE_POLICY для возможности изменения ширины
            tablePhoneBook.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            Platform.runLater(() -> {
                columnFIO.setMinWidth(10);
                columnFIO.setPrefWidth(fioWidth);
                columnFIO.setMaxWidth(10000);

                columnPhone.setMinWidth(10);
                columnPhone.setPrefWidth(phoneWidth);
                columnPhone.setMaxWidth(10000);

                columnDepartment.setMinWidth(10);
                columnDepartment.setPrefWidth(departmentWidth);
                columnDepartment.setMaxWidth(10000);

                columnNotes.setMinWidth(10);
                columnNotes.setPrefWidth(notesWidth);
                columnNotes.setMaxWidth(10000);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        columnDepartment.setCellValueFactory(new PropertyValueFactory<Person, String>("department"));
        columnNotes.setCellValueFactory(new PropertyValueFactory<Person, String>("notes"));
        tablePhoneBook.setItems(phoneBookImpl.getPersonList());
    }


    // слушатели различных действий - чтобы среагировать на них
    private void initListeners() {

        // слушает изменения в коллекции для обновления надписи "Кол-во"
        phoneBookImpl.getPersonList().addListener(new ListChangeListener<Person>() {
            @Override
            public void onChanged(Change<? extends Person> c) {
                updateCountLabel();
            }
        });


        // слушает двойное нажатие для редактирования записи
        tablePhoneBook.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {  // если нажатие было двойное
                    btnEdit.fire(); // имитируем нажатие на кнопку редактирования
                }
            }
        });

        // автоматическая фильтрация при вводе текста в поле поиска
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                phoneBookImpl.findAll();
            } else {
                phoneBookImpl.find(newValue);
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
        labelCount.setText("Кол-во: " + phoneBookImpl.getPersonList().size());
    }

    // обработка нажатой кнопки
    public void buttonActionPressed(ActionEvent actionEvent) {

        Object source = actionEvent.getSource(); // кто источник действия

        // если нажата не кнопка - выходим из метода (вдруг нечаянно "подвязали" этот метод для другого контрола)
        if (!(source instanceof Button)) {
            return;
        }

        // какой объект пользователь выбрал из tableView
        Person selectedPerson = (Person) tablePhoneBook.getSelectionModel().getSelectedItem();

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
                    phoneBookImpl.add(editDialogController.getPerson()); // получаем заполненного person
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
                    // коллекция в phoneBookImpl и так обновляется, т.к. мы ее редактируем в диалоговом окне и сохраняем при нажатии на ОК
                    phoneBookImpl.update(selectedPerson);
                    research = true; // флаг, что есть изменения и потребуется обновление tableView
                }

                break;

            case "btnDelete":
                if (!personIsSelected(selectedPerson) || !(confirmDelete())) {
                    return;
                }

                research = true; // флаг, что есть изменения и потребуется обновление tableView
                phoneBookImpl.delete(selectedPerson);
                break;
        }


        // заново обновляем tableView на основе текущего текста поиска
        if (research) {
            String searchText = txtSearch.getText().trim();
            if (searchText.isEmpty()) {
                phoneBookImpl.findAll();
            } else {
                phoneBookImpl.find(searchText);
            }
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
            editDialogStage.setMinWidth(400);
            editDialogStage.setResizable(false);
            Scene scene = new Scene(fxmlEdit);
            editDialogStage.setScene(scene);
            editDialogStage.initModality(Modality.WINDOW_MODAL);

            // Применяем текущую тему к диалоговому окну
            String themePath = isDarkTheme ? "/ru/example/phonebook/dark-theme.css" : "/ru/example/phonebook/light-theme.css";
            scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
        }

        editDialogStage.showAndWait(); // для ожидания закрытия окна

    }


    // переключение темы оформления
    public void toggleTheme(ActionEvent actionEvent) {
        isDarkTheme = !isDarkTheme;
        applyTheme(isDarkTheme);
    }

    // применение темы
    private void applyTheme(boolean dark) {
        String themePath = dark ? "/ru/example/phonebook/dark-theme.css" : "/ru/example/phonebook/light-theme.css";
        String buttonIcon = dark ? "☀️" : "🌙";

        if (btnTheme != null) {
            btnTheme.setText(buttonIcon);
        }

        // Применяем тему к главному окну
        Scene scene = tablePhoneBook.getScene();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
    }

    // Импорт данных из CSV
    public void importFromCSV(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите CSV файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

        File file = fileChooser.showOpenDialog(tablePhoneBook.getScene().getWindow());

        if (file != null) {
            importCSV(file);
        }
    }

    // Экспорт данных в CSV
    public void exportToCSV(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить CSV файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        fileChooser.setInitialFileName("phonebook_export.csv");

        File file = fileChooser.showSaveDialog(tablePhoneBook.getScene().getWindow());

        if (file != null) {
            exportCSV(file);
        }
    }

    // Экспорт данных в CSV файл
    private void exportCSV(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // BOM для правильного отображения кириллицы в Excel
            writer.write("\uFEFF");
            // Заголовок
            writer.write("ФИО;Отдел;Телефон;Примечание");
            writer.newLine();

            // Данные (экспортируем все записи из БД, а не только отфильтрованные)
            List<Person> allPeople = phoneBookImpl.findAllPeople();
            for (Person person : allPeople) {
                // Экранируем точки с запятой в данных
                String fio = escapeCSV(person.getFio());
                String department = escapeCSV(person.getDepartment());
                String phone = escapeCSV(person.getPhone());
                String notes = escapeCSV(person.getNotes());

                writer.write(fio + ";" + department + ";" + phone + ";" + notes);
                writer.newLine();
            }

            DialogManager.showInfoDialog("Экспорт", "Экспортировано записей: " + allPeople.size());

        } catch (Exception e) {
            DialogManager.showInfoDialog("Ошибка", "Не удалось сохранить файл: " + e.getMessage());
        }
    }

    // Экранирование данных для CSV
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Заменяем точки с запятой на запятые, чтобы не ломать формат
        return value.replace(";", ",");
    }

    // Импорт данных из CSV файла
    private void importCSV(File file) {
        int imported = 0;
        int duplicates = 0;
        int errors = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            // Пропускаем заголовки
            String line = reader.readLine();
            if (line == null) {
                DialogManager.showInfoDialog("Ошибка", "Файл пуст");
                return;
            }

            // Читаем данные построчно
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // пропускаем пустые строки
                }

                String[] parts = line.split(";", -1); // -1 чтобы сохранить пустые поля в конце
                if (parts.length < 3) {
                    errors++;
                    continue;
                }

                try {
                    // Нормализация данных (порядок: ФИО;Отдел;Телефон;Примечание)
                    // Удаляем BOM если он есть в первом поле
                    String fio = parts[0].trim().replace("\uFEFF", "");
                    String department = parts[1].trim();
                    String phone = normalizePhone(parts[2].trim());
                    String notes = parts.length > 3 ? parts[3].trim() : "";

                    if (fio.isEmpty() || phone.isEmpty()) {
                        errors++;
                        continue;
                    }

                    // Проверка на дубликаты по ФИО и телефону
                    boolean isDuplicate = phoneBookImpl.getPersonList().stream()
                            .anyMatch(p -> p.getFio().equalsIgnoreCase(fio) && p.getPhone().equals(phone));

                    if (isDuplicate) {
                        duplicates++;
                        continue;
                    }

                    // Создаём и добавляем запись
                    Person person = new Person();
                    person.setFio(fio);
                    person.setPhone(phone);
                    person.setDepartment(department);
                    person.setNotes(notes);

                    phoneBookImpl.add(person);
                    imported++;

                } catch (Exception e) {
                    errors++;
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            DialogManager.showInfoDialog("Ошибка", "Не удалось прочитать файл: " + e.getMessage());
            return;
        }

        // Обновляем отображение
        fillData();

        // Показываем результат
        StringBuilder result = new StringBuilder();
        result.append("Импорт завершён!\n\n");
        result.append("Добавлено: ").append(imported).append("\n");
        result.append("Пропущено дубликатов: ").append(duplicates).append("\n");
        if (errors > 0) {
            result.append("Ошибок: ").append(errors);
        }

        DialogManager.showInfoDialog("Результат импорта", result.toString());
    }

    // Нормализация телефонного номера
    private String normalizePhone(String input) {
        // Удаляем все нецифровые символы
        String digits = input.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return input;
        }

        // Если номер начинается с 7 или 8, заменяем на 8
        if (digits.charAt(0) == '7') {
            digits = "8" + digits.substring(1);
        } else if (digits.charAt(0) != '8') {
            // Если не начинается с 7 или 8, добавляем 8
            digits = "8" + digits;
        }

        // Форматируем номер
        if (digits.length() <= 1) {
            return digits;
        } else if (digits.length() <= 4) {
            return digits.substring(0, 1) + "-" + digits.substring(1);
        } else if (digits.length() <= 7) {
            return digits.substring(0, 1) + "-" + digits.substring(1, 4) + "-" + digits.substring(4);
        } else if (digits.length() <= 9) {
            return digits.substring(0, 1) + "-" + digits.substring(1, 4) + "-" + digits.substring(4, 7) + "-" + digits.substring(7);
        } else {
            return digits.substring(0, 1) + "-" + digits.substring(1, 4) + "-" + digits.substring(4, 7) + "-" + digits.substring(7, 9) + "-" + digits.substring(9, Math.min(11, digits.length()));
        }
    }
}
