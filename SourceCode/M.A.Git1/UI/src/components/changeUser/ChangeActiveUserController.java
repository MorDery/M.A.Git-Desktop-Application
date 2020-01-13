package components.changeUser;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import main.MainAppController;


public class ChangeActiveUserController {

    @FXML
    private TextField newUserNameTextField;
    @FXML
    private Button newActiveUserSubmitButton;
    private MainAppController m_MainController;
    private Stage m_MyStage;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    @FXML
    private void initialize() {
    }

    @FXML
    void OnNewActiveUserSubmitButtonClicked(ActionEvent event) {
        String resultOfSubmit = newUserNameTextField.getText();
        if(resultOfSubmit.isEmpty() || resultOfSubmit == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("Empty name was entered!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            m_MainController.updateNewActiveUser(resultOfSubmit);
            m_MyStage.close();
        }
    }

}
