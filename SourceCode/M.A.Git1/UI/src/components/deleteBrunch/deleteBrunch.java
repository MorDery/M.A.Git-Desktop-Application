package components.deleteBrunch;


import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import main.MainAppController;

public class deleteBrunch {
    @FXML
    private Button deleteBrunch;
    @FXML
    private ListView <String> showBrunches;
    @FXML
    private MainAppController m_MainController;


    private Stage m_MyStage;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    @FXML
    private void initialize() {
    }

    public void setShowBrunches(ObservableList<String> i_Brunches){
        showBrunches.setItems(i_Brunches);
    }

    public ListView<String> getShowBrunches() {
        return showBrunches;
    }

    @FXML
    void OnDeleteBrunchClicked(ActionEvent event){
        String resultOfSubmit = showBrunches.getSelectionModel().getSelectedItem();
        if(resultOfSubmit == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Not selected brunch!Please try again!");
            errorAlert.showAndWait();
        }
        else {
            m_MainController.updateBrunches(resultOfSubmit);
            showBrunches.getItems().remove(resultOfSubmit);
            m_MainController.InitCommitTree();
            m_MyStage.close();
        }
    }
}
