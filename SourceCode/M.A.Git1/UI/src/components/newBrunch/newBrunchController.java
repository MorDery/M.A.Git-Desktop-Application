package components.newBrunch;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import main.Alerts;
import main.MainAppController;

import java.io.IOException;
import java.util.regex.Pattern;

public class newBrunchController {

    private String m_NewBrunchName;
    private String m_CommitSha;
    private String m_RemoteBranchToTrackAfter;

    @FXML
    private Button createBrunch;
    @FXML
    private TextField newBrunchNameTextField;
    @FXML
    private Button noButton;
    @FXML
    private Button yesButton;
    @FXML
    private ComboBox<String> chooseCommitSha;
    @FXML
    private Button yesTypeButton;
    @FXML
    private Button noTypeButton;
    @FXML
    private Button branchToTrackAfterButton;
    @FXML
    private ComboBox<String> chooseBranchComboBox;
    @FXML
    private Button yesCheckoutRemoteBranchButton;
    @FXML
    private Button noCheckoutRemoteBranchButton;
    @FXML
    private TextField newTrackingAfterBranch;
    @FXML
    private Button createNewTrackingAfter;
    @FXML
    private Button createBrunchFromContextMenu;



    @FXML
    private MainAppController m_MainController;

    private Stage m_MyStage;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }

    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setM_CommitSha(String i_CommitSha) {
        this.m_CommitSha = i_CommitSha;
    }

    public void setM_NewBrunchName(String i_Name){ m_NewBrunchName = i_Name; }

    public void setM_RemoteBranchToTrackAfter(String i_Name){ m_RemoteBranchToTrackAfter = i_Name; }

    public void setChooseCommitSha(ObservableList<String> i_List) {
        this.chooseCommitSha.setItems(i_List);
    }

    public void setChooseRemoteBranches(ObservableList<String> i_RemoteBranchesList){
        this.chooseBranchComboBox.setItems(i_RemoteBranchesList);
    }

    @FXML
    private void initialize() {
    }
    @FXML
    void OnCreateBrunchClicked(ActionEvent event) {
        m_NewBrunchName = newBrunchNameTextField.getText();
        m_CommitSha = chooseCommitSha.getValue();
        if(m_NewBrunchName == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("Empty name was entered!Please try again!");
            errorAlert.showAndWait();
        }
        else if(m_MainController.isBrunchNameExist(m_NewBrunchName)){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("The name is exist!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            if(m_CommitSha == null){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Input not valid");
                errorAlert.setContentText("Not selected commit!Please try again!");
                errorAlert.showAndWait();
            }
            else if(m_MainController.isRemoteBranchOnCommitId(m_CommitSha)){
                m_MainController.OnCreateBranchAsRemoteTrackingOrRegular(m_NewBrunchName,m_CommitSha);
                m_MyStage.close();
            }
            else {
                m_MainController.OnCheckoutNewBrunchButton(m_NewBrunchName,m_CommitSha);
                m_MyStage.close();
            }
        }
    }
    @FXML
    void OnYesClicked(ActionEvent event){
        try {
            m_MainController.checkIfThereAreOpenChanges(m_NewBrunchName);
            m_MainController.InitCommitTree();
            Alerts.showMessageToUser("Creation was done!");
            m_MyStage.close();
        } catch (IOException e) {
            Alerts.showAlertToUser("Wasn't able to write into the request file!Please try again.");
        }
    }
    @FXML
    void OnNoClicked(ActionEvent event){
        Alerts.showMessageToUser("Creation was done!");
        m_MainController.InitCommitTree();
        m_MyStage.close();
    }
    @FXML
    void OnNoTypeClicked(ActionEvent event) {
        m_MainController.OnCheckoutNewBrunchButton(m_NewBrunchName,m_CommitSha);
        m_MyStage.close();
    }
    @FXML
    void OnYesTypeClicked(ActionEvent event) {
        m_MainController.OnCheckoutNewRemoteTrackingBranchButton(m_NewBrunchName,m_CommitSha);
        m_MyStage.close();
    }
    @FXML
    void OnBranchToTrackAfterButtonClick(ActionEvent event) {
        m_RemoteBranchToTrackAfter = chooseBranchComboBox.getValue();
        if(m_RemoteBranchToTrackAfter.isEmpty() || m_RemoteBranchToTrackAfter == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("No Branch was selected!Please try again!");
            errorAlert.showAndWait();
        }
        else{
            m_MainController.createTrackingAfterBranch(m_NewBrunchName,m_RemoteBranchToTrackAfter);
            m_MainController.OnCheckoutNewBrunchButton(m_NewBrunchName,m_MainController.getM_MyMagit().getM_Repositories().getM_Branches().get(m_RemoteBranchToTrackAfter).getM_PointedCommitId());
            m_MyStage.close();
        }
    }
    @FXML
    void OnNoCheckOutRemoteClicked(ActionEvent event) {
        Alerts.showAlertToUser("Can't checkout to Remote Branch!");
        m_MyStage.close();
    }
    @FXML
    void OnYesCheckOutRemoteClicked(ActionEvent event) {
        if(m_NewBrunchName == null){
            String separator = "\\";
            String [] spllitedCont = m_RemoteBranchToTrackAfter.split(Pattern.quote(separator));
            try {
                m_NewBrunchName = spllitedCont[1];
            }
            catch (Exception ex){
                m_NewBrunchName = m_MainController.getM_MyMagit().extractRepositoryNameFromPath(m_RemoteBranchToTrackAfter);
            }
        }
        m_MainController.createTrackingAfterBranch(m_NewBrunchName,m_RemoteBranchToTrackAfter);
        try {
            m_MainController.changeHeadBrunchAndSpanTheObjects(m_NewBrunchName);
        } catch (IOException e) {
            Alerts.showAlertToUser(e.toString());
        }
        finally {
            m_MyStage.close();
        }
    }
    @FXML
    void OnCreateBrunchFromContextClicked(ActionEvent event) {
        m_NewBrunchName = newBrunchNameTextField.getText();
        if (m_NewBrunchName.isEmpty() || m_NewBrunchName == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("Empty name was entered!Please try again!");
            errorAlert.showAndWait();
        } else if (m_MainController.isBrunchNameExist(m_NewBrunchName)) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("The name is exist!Please try again!");
            errorAlert.showAndWait();
        } else {
            if (m_MainController.isRemoteBranchOnCommitId(m_CommitSha)) {
                m_MainController.OnCreateBranchAsRemoteTrackingOrRegular(m_NewBrunchName, m_CommitSha);
                m_MyStage.close();
            } else {
                m_MainController.OnCheckoutNewBrunchButton(m_NewBrunchName, m_CommitSha);
                m_MyStage.close();
            }
        }
    }

}
