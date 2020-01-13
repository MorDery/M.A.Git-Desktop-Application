package components.newCommit;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;

import java.io.IOException;

public class NewCommitController {
    private String m_Message;
    @FXML
    private Button createCommit;

    @FXML
    private TextField messageTextField;


    @FXML
    private MainAppController m_MainController;
    private Stage m_MyStage;
    private String m_PrevMergeCommit = null;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setM_PrevMergeCommit(String m_PrevMergeCommit) {
        this.m_PrevMergeCommit = m_PrevMergeCommit;
    }

    @FXML
    private void initialize() {
    }
    @FXML
    void OnCreateCommitClicked(ActionEvent event) {
        m_Message = messageTextField.getText();
        if(m_Message.isEmpty() || m_Message == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("Empty message was entered!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            m_MainController.createCommit(m_Message,m_PrevMergeCommit);
            m_PrevMergeCommit = null;
            m_MyStage.close();
        }
    }

}
