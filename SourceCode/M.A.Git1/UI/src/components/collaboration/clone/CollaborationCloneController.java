package components.collaboration.clone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CollaborationCloneController {

    @FXML
    private TextField fullPathTextField;
    @FXML
    private TextField cloneFullPathTextField;
    @FXML
    private TextField cloneNewNameTextField;
    @FXML
    private Button cloneRepositorySubmitButton;

    private MainAppController m_MainController;
    private Stage m_MyStage;

    public void setMainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    @FXML
    private void initialize() {
    }
    @FXML
    void OnCloneRepositorySubmitButtonClicked(ActionEvent event) {
        String fullPathFromUser = fullPathTextField.getText();
        String cloneFullPathFromUser = cloneFullPathTextField.getText();
        String cloneRepositoryName = cloneNewNameTextField.getText();
        if (fullPathFromUser.isEmpty() || fullPathFromUser == null ) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty path");
        } else if (cloneFullPathFromUser.isEmpty() || cloneFullPathFromUser == null) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty path");
        }else if (cloneRepositoryName.isEmpty() || cloneRepositoryName == null) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty name");
        }
        else if(!m_MainController.getM_MyMagit().isRepositoryExists(fullPathFromUser)){
            Alerts.showAlertToUser("This repository does not exist!");
        }
        else if(m_MainController.getM_MyMagit().isRepositoryExists(cloneFullPathFromUser + "//" + cloneRepositoryName)){
            Alerts.showAlertToUser("There's a repository on that location. Can't clone.");
        }
        else{
            if (Files.exists(Paths.get(cloneFullPathFromUser))) {
                if (m_MainController.getM_MyMagit().isRepositoryExists(fullPathFromUser)) {
                    try {
                        m_MainController.CollaborationCloneRepository(fullPathFromUser, cloneFullPathFromUser,cloneRepositoryName);
                        m_MyStage.close();
                    } catch (Exception e) {
                        Alerts.showAlertToUser(e.getMessage());
                    }
                }
                else{
                        Alerts.showAlertToUser("There is not repository on that Location,therefore it can't be cloned.");
                    }
                }
            else {
                Alerts.showAlertToUser("The given path does not exist / The given path is not a folder");
            }
        }

    }

}
