package components.resetBrunch;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;

import java.io.IOException;

public class ResetBrunchController {
    private String m_commitSha;
    @FXML
    private Button resetBrunchButton;
    @FXML
    private ListView<String> showCommitSHA;
    @FXML
    private Button noButton;
    @FXML
    private Button yesButton;

    @FXML
    private MainAppController m_MainController;
    private Stage m_MyStage;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }
    public void setM_commitSha(String m_commitSha) {
        this.m_commitSha = m_commitSha;
    }
    public void setShowCommitSHA(ObservableList<String> i_ShowCommitSHA) {
        showCommitSHA.setItems(i_ShowCommitSHA);
    }

    @FXML
    private void initialize() {
    }
    @FXML
    void OnResetBrunchClicked(ActionEvent event) {
        m_commitSha = showCommitSHA.getSelectionModel().getSelectedItem();
        if(m_commitSha == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("No selected commit SHA-1!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            m_MainController.OnResetBrunch(m_commitSha);
            m_MyStage.close();
        }
    }
    @FXML
    void OnYesClicked(ActionEvent event){
        try {
            m_MainController.resetBrunch(m_commitSha);

        } catch (IOException e) {
            Alerts.showAlertToUser("Wasn't able to change the pointed commit!Please try again.");
        }
        m_MyStage.close();
    }
    @FXML
    void OnNoClicked(ActionEvent event){
        m_MyStage.close();
    }

}
