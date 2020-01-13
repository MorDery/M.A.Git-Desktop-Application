package components.loadRepositoryFromXML;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;
import main.Alerts;

public class RepositoryFoundOnThatLocationController {

    @FXML
    private RadioButton deleteAndCreateOption;
    @FXML
    private RadioButton remainWithCurrentOption;
    @FXML
    private Button userSubmitButton;
    private Stage m_MyStage;
    public SimpleBooleanProperty userInput = new SimpleBooleanProperty();
    private LoadRepositoryFromXMLController m_LoadFromXMLFormController;

    public void setMyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setMainController(LoadRepositoryFromXMLController m_MainController) {
        this.m_LoadFromXMLFormController = m_MainController;
    }
    @FXML
    private void initialize() {

    }
    @FXML
    void OnDeleteAndCreateOptionClick(ActionEvent event) {
        if(deleteAndCreateOption.isSelected()){
            remainWithCurrentOption.disarm();
        }
    }

    @FXML
    void OnRemainWithCurrentOptionClick(ActionEvent event) {
        if(remainWithCurrentOption.isSelected()){
            remainWithCurrentOption.disarm();
        }
    }

    @FXML
    void OnUserSubmitButtonClicked(ActionEvent event) {
        if(deleteAndCreateOption.isSelected() && remainWithCurrentOption.isSelected()){
            Alerts.showAlertToUser("Please select one option.");
        }
        else if(deleteAndCreateOption.isSelected()) {
            userInput.set(false);
            m_LoadFromXMLFormController.remainOrDeleteMaker(userInput.get());
            m_MyStage.close();
        }
        else if(remainWithCurrentOption.isSelected()){
            userInput.set(true);
            m_LoadFromXMLFormController.remainOrDeleteMaker(userInput.get());
            m_MyStage.close();
        }
        else{
            Alerts.showAlertToUser("Please select one option.");
        }

    }

}
