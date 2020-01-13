package components.loadRepositoryFromXML;

import LogicEngine.Creator;
import LogicEngine.Deleter;
import LogicEngine.MAGit;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class LoadRepositoryFromXMLController {

    @FXML
    private Button fullXMLPathSubmitButton;

    private MainAppController m_MainAppFormController;
    private RepositoryFoundOnThatLocationController m_RepositoryFoundController;
    private Stage m_MyStage;
    private SimpleStringProperty taskMessageProperty = new SimpleStringProperty();
    private SimpleStringProperty newRepositoryLocationIfTaskCreatedIt = new SimpleStringProperty();
    private SimpleStringProperty newRepositoryLocation = new SimpleStringProperty();
    private SimpleBooleanProperty repositoryFoundProperty = new SimpleBooleanProperty();
    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }
    public void setMainController(MainAppController m_MainController) {
        this.m_MainAppFormController = m_MainController;
    }

    public MainAppController getMainAppFormController() {
        return m_MainAppFormController;
    }

    public SimpleStringProperty getNewRepositoryLocationProperty(){return newRepositoryLocation;}

    public SimpleStringProperty getNewRepositoryLocationIfTaskCreatedItProperty(){return newRepositoryLocationIfTaskCreatedIt;}

    @FXML
    private void initialize() {

    }

    @FXML
    void OnfullXMLPathSubmitButtonClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(m_MyStage);
        if(selectedFile != null) {
            String repositoryFullPath = selectedFile.getAbsolutePath();
            if (repositoryFullPath.isEmpty() || repositoryFullPath == null) {
                Alerts.showAlertToUser("Invalid input! You can't enter empty path");
            } else {
                LoadRepositoryFromXMLTask loadRepositoryFromXMLTask = new LoadRepositoryFromXMLTask(repositoryFullPath, () -> printErrors(), this, () -> onceTaskIsEnded(), repositoryFoundProperty);
                taskMessageProperty.bind(loadRepositoryFromXMLTask.messageProperty());
                m_MyStage.close();
                new Thread(loadRepositoryFromXMLTask).start();
            }
        }
    }

    public void printErrors(){
        Alerts.showAlertToUser(taskMessageProperty.get());
    }

    public void askTheUserProcess(){
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/loadRepositoryFromXML/RepositoryFoundOnThatLocation.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_RepositoryFoundController = loader.getController();
            m_RepositoryFoundController.setMainController(this);
            Stage stage = createStage("Repository Exists",false,root);
            m_RepositoryFoundController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remainOrDeleteMaker(boolean i_RemainOrDelete){
        MAGit myMagitRef = m_MainAppFormController.getM_MyMagit();
        if(i_RemainOrDelete){ // we need to remain with the repo
            try {
                myMagitRef.LoadRepositoryToOurObjects(myMagitRef.getM_Repositories().getM_Location());
                m_MainAppFormController.InitCommitTree();
            }
            catch (Exception ex){
                Alerts.showAlertToUser(ex.getMessage());
                return;
            }
        }
        else{//we need to delete the repo
            try {
                Deleter.deleteDir(myMagitRef.getM_Repositories().getM_Location());
            }
            catch (Exception ex)
            {
                Alerts.showAlertToUser(ex.getMessage());
                return;
            }
            myMagitRef.createRepository();
            Creator.createWC(myMagitRef);
        }
        newRepositoryLocation.set(myMagitRef.getM_Repositories().getM_Location());
    }

    public void updateRepositoryLocationFromTask(){
        newRepositoryLocationIfTaskCreatedIt.setValue(m_MainAppFormController.getM_MyMagit().getM_Repositories().getM_Location());
    }

    private void onceTaskIsEnded(){
        taskMessageProperty.unbind();
        newRepositoryLocationIfTaskCreatedIt.unbind();
        m_MainAppFormController.InitCommitTree();
        m_MyStage.close();
    }

    private Stage createStage(String i_Title, boolean i_IsResizable, Parent i_Root){
        Stage returnStage = new Stage();
        returnStage.setTitle(i_Title);
        returnStage.setScene(new Scene(i_Root));
        returnStage.setResizable(i_IsResizable);

        return returnStage;
    }

}
