
package components.commitInformation;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.MainAppController;
import sun.reflect.generics.tree.Tree;

public class CommitInformationController {

    @FXML
    private Button chooseCommitSha1Submit;
    @FXML
    private ComboBox<String> chooseCommitSha1;
    @FXML
    private BorderPane emptyBotderPane;
    @FXML
    private MainAppController m_MainController;

    private TreeView<ViewMagitFile> m_CommitInfromationTree;
    private Stage m_MyStage;
    private String m_CommitSha;
    private String m_SelectedCommitSha1;


    @FXML
    private void initialize() {
    }
    public void setM_CommitInfromationTree(TreeView<ViewMagitFile> m_CommitInfromationTree) {
        this.m_CommitInfromationTree = m_CommitInfromationTree;
    }

    public void setChooseCommitSha1(ObservableList<String> commitsListOfSha1) {
        chooseCommitSha1.setItems(commitsListOfSha1);
    }

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }

    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }
    @FXML
    void OnChooseCommitSha1Submit(ActionEvent event) {
        m_SelectedCommitSha1 = chooseCommitSha1.getValue();
        if(m_SelectedCommitSha1.isEmpty() || m_SelectedCommitSha1 == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText("No Commit was selected!Please try again!");
            errorAlert.showAndWait();
        }
        else{
            m_MainController.createTheSelectedCommitInformationTree(m_SelectedCommitSha1);
            m_MyStage.close();
        }

    }

    public void ShowCommitInfromation() {
        m_CommitInfromationTree.setPrefHeight(300);
        m_CommitInfromationTree.setMaxHeight(400);
        emptyBotderPane.setCenter(m_CommitInfromationTree);
    }

}
