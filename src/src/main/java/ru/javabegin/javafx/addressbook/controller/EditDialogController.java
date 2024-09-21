package ru.javabegin.javafx.addressbook.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.javabegin.javafx.addressbook.objects.Person;
import ru.javabegin.javafx.addressbook.utils.DialogManager;

// контроллер - обработчик действий пользователя диалогового окна редактирования/создания

public class EditDialogController {

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;

    @FXML
    private TextField txtFIO;

    @FXML
    private TextField txtPhone;

    private Person person;

    private boolean saveClicked = false;// для определения нажатой кнопки

    // объект, который редактируем/создаем в данный момент
    public void setPerson(Person person) {
        if (person == null){
            return;
        }
        saveClicked = false;
        this.person = person;
        txtFIO.setText(person.getFio());
        txtPhone.setText(person.getPhone());
    }

    // после редактирования/создания - сможем получить обновленные данные
    public Person getPerson() {
        return person;
    }

    // закрывает окно
    public void actionClose(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.hide();// скрываем внешний контейнер диалогового окна (где находится Scene с UI)
    }


    // сохранение изменений и закрытие окна
    public void actionSave(ActionEvent actionEvent) {
        if (!checkValues()){
            return;
        }
        person.setFio(txtFIO.getText());
        person.setPhone(txtPhone.getText());
        saveClicked = true;
        actionClose(actionEvent);
    }

    // проверка на заполнение всех полей
    private boolean checkValues() {
        if (txtFIO.getText().trim().length()==0 || txtPhone.getText().trim().length()==0){
            DialogManager.showInfoDialog("Ошибка", "Заполните поле");
            return false;
        }

        return true;
    }


    // если нажали save, а не просто закрыли окно, значит есть изменения (условие проверяется в родительском окне)
    public boolean isSaveClicked() {
        return saveClicked;
    }
}
