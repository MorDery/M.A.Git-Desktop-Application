package components.checkoutHeadBrunch;

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

public class CeckoutHeadBrunchController {

    @FXML
    private Button ceckoutHeadBrunch;
    @FXML
    private ListView<String> showBrunches;
    @FXML
    private MainAppController m_MainController;
    private Stage m_MyStage;

    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }

    public void setShowBrunches(ObservableList<String> i_Brunches){
        showBrunches.setItems(i_Brunches);
    }

    public ListView<String> getShowBrunches() {
        return showBrunches;
    }

    @FXML
    private void initialize() {
    }

    @FXML
    void OnCheckoutHeadBrunchClicked(ActionEvent event){
        String resultOfSubmit = showBrunches.getSelectionModel().getSelectedItem();
        if(resultOfSubmit == null){
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("Not seleceted brunch!Please try again!");
            errorAlert.showAndWait();
        }
        else if(m_MainController.isRemoteBranchOnBranchSelectedName(resultOfSubmit)){
            m_MainController.OnCheckoutToRemoteBranch(resultOfSubmit);
            m_MyStage.close();
        }
        else{
            try {
                m_MainController.changeHeadBrunchAndSpanTheObjects(resultOfSubmit);
            } catch (IOException e) {
                Alerts.showAlertToUser("Wasn't able to change the head brunch!Please try again.");
            }
            m_MyStage.close();
        }
    }
}
