package ru.example.phonebook.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.example.phonebook.objects.Person;
import ru.example.phonebook.utils.DialogManager;
import javafx.scene.control.TextFormatter;

// контроллер - обработчик действий пользователя диалогового окна редактирования/создания

public class EditDialogController {

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;

    @FXML
    private TextField txtFIO;

    @FXML
    private TextField txtPhone1;

    @FXML
    private TextField txtPhone2;

    @FXML
    private TextField txtDepartment;

    @FXML
    private TextArea txtNotes;

    private Person person;

    private boolean saveClicked = false;// для определения нажатой кнопки

    // Форматирует один номер телефона в формат 8-XXX-XXX-XX-XX
    private String formatPhoneNumber(String input) {
        // Удаляем все нецифровые символы
        String digits = input.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return "";
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

    // Инициализация форматирования телефона
    @FXML
    public void initialize() {
        // TextFormatter для первого телефона
        txtPhone1.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }

            String formatted = formatPhoneNumber(change.getControlNewText());

            if (!formatted.isEmpty() && !formatted.equals(change.getControlNewText())) {
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                change.setAnchor(formatted.length());
                change.setCaretPosition(formatted.length());
            }

            return change;
        }));

        // TextFormatter для второго телефона
        txtPhone2.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().isEmpty()) {
                return change;
            }

            String formatted = formatPhoneNumber(change.getControlNewText());

            if (!formatted.isEmpty() && !formatted.equals(change.getControlNewText())) {
                change.setText(formatted);
                change.setRange(0, change.getControlText().length());
                change.setAnchor(formatted.length());
                change.setCaretPosition(formatted.length());
            }

            return change;
        }));
    }

    // Разбивает строку с телефонами на два номера
    private void splitPhones(String phoneString) {
        if (phoneString == null || phoneString.trim().isEmpty()) {
            txtPhone1.setText("");
            txtPhone2.setText("");
            return;
        }

        // Разбиваем по разделителю ;
        String[] parts = phoneString.split(";");
        txtPhone1.setText(parts[0].trim());

        if (parts.length > 1) {
            txtPhone2.setText(parts[1].trim());
        } else {
            txtPhone2.setText("");
        }
    }

    // Объединяет два телефона в одну строку
    private String joinPhones() {
        String phone1 = txtPhone1.getText().trim();
        String phone2 = txtPhone2.getText().trim();

        if (phone1.isEmpty()) {
            return phone2;
        }
        if (phone2.isEmpty()) {
            return phone1;
        }
        return phone1 + "; " + phone2;
    }

    // объект, который редактируем/создаем в данный момент
    public void setPerson(Person person) {
        if (person == null){
            return;
        }
        saveClicked = false;
        this.person = person;
        txtFIO.setText(person.getFio());
        splitPhones(person.getPhone());
        txtDepartment.setText(person.getDepartment());
        txtNotes.setText(person.getNotes());
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
        person.setPhone(joinPhones());
        person.setDepartment(txtDepartment.getText());
        person.setNotes(txtNotes.getText());
        saveClicked = true;
        actionClose(actionEvent);
    }

    // проверка на заполнение всех полей
    private boolean checkValues() {
        if (txtFIO.getText().trim().length()==0 || txtPhone1.getText().trim().length()==0){
            DialogManager.showInfoDialog("Ошибка", "Заполните обязательные поля");
            return false;
        }

        return true;
    }


    // если нажали save, а не просто закрыли окно, значит есть изменения (условие проверяется в родительском окне)
    public boolean isSaveClicked() {
        return saveClicked;
    }
}
