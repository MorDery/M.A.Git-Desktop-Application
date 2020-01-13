package components.addNewRepository;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddNewRepositoryController {

    @FXML
    private TextField fullPathTextField;
    @FXML
    private TextField repositoryNameTextField;
    @FXML
    private Button addNewRepositorySubmitButton;
    private MainAppController m_MainAppFormController;
    private Stage m_MyStage;

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
    void OnAddNewRepositorySubmitButtonClick(ActionEvent event) {
        String fullPathFromUser = fullPathTextField.getText();
        String newRepositoryName = repositoryNameTextField.getText();

        if (fullPathFromUser.isEmpty() || fullPathFromUser == null) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty path");
        } else if (newRepositoryName.isEmpty() || newRepositoryName == null) {
            Alerts.showAlertToUser("Invalid input! You can't enter empty name");
        } else {
            if (Files.exists(Paths.get(fullPathFromUser))) {
                    try {
                        m_MainAppFormController.getM_MyMagit().initializeNewRepository(fullPathFromUser, newRepositoryName);
                        m_MainAppFormController.EndOfChangeRepositoryProcess(fullPathFromUser + "\\" + newRepositoryName);
                        m_MyStage.close();
                    } catch (IOException e) {
                        Alerts.showAlertToUser(e.getMessage());
                    } catch (Exception e) {
                        Alerts.showAlertToUser(e.getMessage());
                    }
                }
            else {
                Alerts.showAlertToUser("The given path does not exist / The given path is not a folder");
            }
        }
    }
}
