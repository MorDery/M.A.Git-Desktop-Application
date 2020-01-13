package components.showOpenChanges;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import main.MainAppController;

public class ShowOpenChangesController {
    @FXML
    private ListView<String> showOpenChangesList;
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
    public void setShowOpenChangesList(ObservableList<String> i_Brunches){
        showOpenChangesList.setItems(i_Brunches);
    }
    public ListView<String> getShowBrunches() {
        return showOpenChangesList;
    }
}
