package components.merge;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import main.MainAppController;

public class MergeController {
    @FXML
    private Button brunchToMerge;
    @FXML
    private ListView<String> showBrunches;
    @FXML
    private Button brunchOnCommitToMergeSubmit;
    @FXML
    private Button chooseConflict;
    @FXML
    private ListView<String> showConflicts;

    @FXML
    private TextArea oursText;
    @FXML
    private TextArea AncestorText;
    @FXML
    private TextArea theirsText;
    @FXML
    private TextArea resultText;
    @FXML
    private Button chooseFixConflict;

    @FXML
    private MainAppController m_MainController;
    private Stage m_MyStage;
    private String m_NameBrunchToMerge;
    private String m_NameConflict;
    private ObservableList<String[]> m_ConflictsList;
    private String m_AncestorName;

    @FXML
    private void initialize() {
    }

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }

    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public Stage getM_MyStage(){
        return m_MyStage;
    }

    public void setShowBrunches(ObservableList<String> i_Brunches){
        showBrunches.setItems(i_Brunches);
    }

    public void setAncestorText(String text) {
        AncestorText.setText(text);
    }

    public void setOursText(String text) {
       oursText.setText(text);
    }

    public void setResultText(String text) {
        resultText.setText(text);

    }

    public void setTheirsText(String text) {
        theirsText.setText(text);
    }

    public TextArea getResultText() {
        return resultText;
    }

    public ListView<String> getShowBrunches() {
        return showBrunches;
    }

    public void setShowConflicts(ObservableList<String[]> i_Conflicts){
        m_ConflictsList = i_Conflicts;
        ObservableList<String> conflictsList =  FXCollections.observableArrayList();
        for (String[] item : i_Conflicts) {
            conflictsList.add(item[1]);
        }
        showConflicts.setItems(conflictsList);
    }

    public ListView<String> getShowConflicts() {
        return showConflicts;
    }

    public void setM_AncestorName(String m_AncestorName) {
        this.m_AncestorName = m_AncestorName;
    }

    public String getM_AncestorName(){
        return m_AncestorName;
    }

    public String getM_NameBrunchToMerge(){
        return m_NameBrunchToMerge;
    }

    public String getM_NameConflict(){
        return m_NameConflict;
    }

    public ObservableList<String[]> getM_ConflictsList(){
        return m_ConflictsList;
    }

    @FXML
    void OnBrunchToMergeClicked(ActionEvent event){
        m_NameBrunchToMerge = showBrunches.getSelectionModel().getSelectedItem();
        if(m_NameBrunchToMerge == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Not selected brunch!Please try again!");
            errorAlert.showAndWait();
        }
        else{
            m_MainController.OnBrunchToMerge(m_NameBrunchToMerge);
            m_MyStage.close();
        }

    }

    @FXML
    void OnConflictClicked(ActionEvent event) {
        m_NameConflict = showConflicts.getSelectionModel().getSelectedItem();
        if (m_NameConflict == null) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Not selected conflict to fix!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            m_MainController.OnFixConflict(m_NameConflict);
        }


    }

    @FXML
    void OnFixConflictClicked() {
        m_MainController.OnUpdateConflicts(resultText.getText());
        m_MyStage.close();
    }

    @FXML
    void OnBrunchOnCommitToMergeClick(ActionEvent event) {
        m_NameBrunchToMerge = showBrunches.getSelectionModel().getSelectedItem();
        if(m_NameBrunchToMerge == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Not selected brunch!Please try again!");
            errorAlert.showAndWait();
        }
        else{
            m_MainController.MergeFromContextMenu(m_NameBrunchToMerge);
            m_MyStage.close();
        }
    }


}
