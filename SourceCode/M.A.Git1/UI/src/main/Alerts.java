package main;

import javafx.scene.control.Alert;

import java.util.List;

public class Alerts {
    public static void showAlertToUser(String i_AlertMessage){
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error!");
        errorAlert.setContentText(i_AlertMessage);
        errorAlert.show();
    }
    public static void showMessageToUser(String i_AlertMessage){
        Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
        errorAlert.setTitle("Success!");
        errorAlert.setContentText(i_AlertMessage);
        errorAlert.show();
    }
}

