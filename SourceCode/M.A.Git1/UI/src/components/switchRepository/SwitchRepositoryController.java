package components.switchRepository;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;

import java.io.IOException;

public class SwitchRepositoryController {

    @FXML
    private TextField repositoryPathTextField;
    @FXML
    private Button repositoryPathSubmitButton;

    private MainAppController m_MainAppFormController;
    private Stage m_MyStage;
    private SimpleStringProperty m_FullPathProperty;

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setMainController(MainAppController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    @FXML
    private void initialize() {

    }

    @FXML
    void OnRepositoryPathSubmitButtonClick(ActionEvent event) {
        String repositoryFullPath = repositoryPathTextField.getText();
        if (repositoryFullPath.isEmpty() || repositoryFullPath == null) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty path");
        } else {
            if (m_MainAppFormController.getM_MyMagit().isRepositoryExists(repositoryFullPath)) {
                    m_MainAppFormController.EndOfChangeRepositoryProcess(repositoryFullPath);
                    m_MyStage.close();
                }
            else {
                Alerts.showAlertToUser("This repository does not exist!");
            }
        }
    }
}
