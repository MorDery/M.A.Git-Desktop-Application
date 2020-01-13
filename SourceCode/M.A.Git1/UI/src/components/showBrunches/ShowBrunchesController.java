package components.showBrunches;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import main.MainAppController;

public class ShowBrunchesController {
    @FXML
    private ListView <String> showBrunchesList;
    @FXML
    private MainAppController m_MainController;
    private Stage m_MyStage;
    @FXML
    private void initialize() {
    }


    public void setM_MainController(MainAppController m_MainController) {
        this.m_MainController = m_MainController;
    }
    public void setM_MyStage(Stage m_MyStage) {
        this.m_MyStage = m_MyStage;
    }
    public void setShowBrunches(ObservableList<String> i_Brunches){
        showBrunchesList.setItems(i_Brunches);
    }
    public ListView<String> getShowBrunches() {
        return showBrunchesList;
    }
}
