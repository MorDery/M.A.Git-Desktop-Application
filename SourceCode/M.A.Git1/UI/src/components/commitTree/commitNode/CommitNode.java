package components.commitTree.commitNode;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import main.MainAppController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommitNode extends AbstractCell {

    private String m_Timestamp;
    private String m_Committer;
    private String m_CommitMessage;
    private List<String> m_PointingBranchName = new ArrayList<String>();
    private String m_CommitId;
    private List<String> m_CommitDirectParents ;
    private MainAppController m_MainAppController;
    private CommitNodeController m_CommitNodeController;


    public CommitNode(String timestamp, String committer, String message, String PointingBranchName, String i_CommitId, MainAppController i_MainAppController, List<String> i_CommitDirectParents) {
        this.m_Timestamp = timestamp.toString();
        this.m_Committer = committer;
        this.m_CommitMessage = message;
        this.m_PointingBranchName.add(PointingBranchName);
        m_CommitId = i_CommitId;
        m_MainAppController = i_MainAppController;
        m_CommitDirectParents = i_CommitDirectParents;
    }

    @Override
    public Region getGraphic(Graph graph) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/commitTree/commitNode/CommitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load(url.openStream());
            root.setOnMouseEntered((event -> biggerCircle(this,12, Color.DEEPPINK)));
            root.setOnMouseExited(event -> biggerCircle(this,10,Color.valueOf("087fee")));
            m_CommitNodeController = fxmlLoader.getController();
            m_CommitNodeController.setCommitMessage(m_CommitMessage);
            m_CommitNodeController.setCommitter(m_Committer);
            m_CommitNodeController.setCommitTimeStamp(m_Timestamp);
            m_CommitNodeController.setCommitPointingBranchName(getPointingBranchName());
            m_CommitNodeController.setCommitLogic(this);
            m_CommitNodeController.setMainAppController(m_MainAppController);
            ContextMenuCommitNode contextMenuCommitNode = new ContextMenuCommitNode();
            m_CommitNodeController.AddResetHeadBranchMenuItem(contextMenuCommitNode);
            m_CommitNodeController.AddCreateNewBranchMenuItem(contextMenuCommitNode);
            m_CommitNodeController.AddMergeMenuItem(contextMenuCommitNode);
            m_CommitNodeController.AddDeleteBranchMenuItem(contextMenuCommitNode);
            EventHandler<MouseEvent> mouseRightClickEventHandler = (MouseEvent event) -> {
                if(event.getButton() == MouseButton.SECONDARY ) {
                    contextMenuCommitNode.getContextMenu().show(root,event.getScreenX(),event.getScreenY());
                    }
                };
            root.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseRightClickEventHandler);
            for(String precedingCommit : m_CommitDirectParents) {
                m_CommitNodeController.setDifferenceBetweenLastCommit(m_MainAppController.getM_MyMagit().getDifferenceBetweenTwoCommits(m_CommitId, precedingCommit));
            }
            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(m_CommitNodeController.getCircleRadius());
    }

    public String getTimestamp() {
        return m_Timestamp;
    }

    public String getPointingBranchName() {
        StringBuilder sb = new StringBuilder();

        if(m_PointingBranchName.size() < 2) {
            for (String str : m_PointingBranchName) {
                sb.append(str);
            }
        }
        else{
            for (String str : m_PointingBranchName) {
                sb.append(str).append(", ");
            }
            int index = sb.lastIndexOf(", ");
            sb.replace(index,index+1 ,"");
        }

        return sb.toString();
    }

    public String getCommitId() {
        return m_CommitId;
    }

    public String getCommitter() {
        return m_Committer;
    }

    public String getCommitMessage() {
        return m_CommitMessage;
    }

    public List<String> getCommitDirectParents() {
        return m_CommitDirectParents;
    }

    public void addPointingBranchName(String i_PointingBranchName) {
        if(m_PointingBranchName.contains("Not pointed Commit")){
            m_PointingBranchName.clear();
        }
        m_PointingBranchName.add(i_PointingBranchName);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitNode)) return false;
        CommitNode that = (CommitNode) o;
        return m_Timestamp.equals(that.m_Timestamp) &&
                m_Committer.equals(that.m_Committer) &&
                m_CommitMessage.equals(that.m_CommitMessage) &&
                Objects.equals(m_PointingBranchName, that.m_PointingBranchName) &&
                Objects.equals(m_CommitNodeController, that.m_CommitNodeController);
    }

    public CommitNodeController getM_CommitNodeController() {
        return m_CommitNodeController;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_Timestamp, m_Committer, m_CommitMessage, m_PointingBranchName, m_CommitNodeController);
    }

    private void biggerCircle(CommitNode commitNode,int radius,Color color){
        m_CommitNodeController.changeAttributes(commitNode,radius,color);
    }
}
