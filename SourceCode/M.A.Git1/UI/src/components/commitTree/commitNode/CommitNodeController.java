package components.commitTree.commitNode;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import main.Alerts;
import main.MainAppController;

import javax.xml.soap.Text;
import java.io.IOException;
import java.text.ParseException;

public class CommitNodeController {

    @FXML
    private GridPane commitNodeGridPane;
    @FXML
    private Circle CommitCircle;
    @FXML
    private Label commitTimeStampLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Label committerLabel;
    @FXML
    private Label pointingBranchLabel;

    private CommitNode m_CommitNode;
    private MainAppController m_MainAppController;
    private  String m_DifferenceBetweenLastCommit;


    @FXML
    private void initialize() {

    }

    public void setCommitTimeStamp(String i_TimeStamp) {
        commitTimeStampLabel.setText(i_TimeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(i_TimeStamp));
    }

    public void setCommitter(String i_CommitterName) {
        committerLabel.setText(i_CommitterName);
        committerLabel.setTooltip(new Tooltip(i_CommitterName));
    }

    public void setCommitMessage(String i_CommitMessage) {
        messageLabel.setText(i_CommitMessage);
        messageLabel.setTooltip(new Tooltip(i_CommitMessage));
    }
    public void setCommitPointingBranchName(String i_BranchName) {
        pointingBranchLabel.setText(i_BranchName);
        pointingBranchLabel.setTooltip(new Tooltip(i_BranchName));
    }
    public void setDifferenceBetweenLastCommit(String i_DifferenceBetweenLastCommit) {
        this.m_DifferenceBetweenLastCommit = i_DifferenceBetweenLastCommit;
    }

    @FXML
    void OnCommitNodeClicked(MouseEvent event) {
        m_MainAppController.setCurrentCommitSha1Property(m_CommitNode.getCommitId());
        m_MainAppController.setCurrentCommitAuthorProperty(m_CommitNode.getCommitter());
        m_MainAppController.setCurrentCommitMessageProperty(m_CommitNode.getCommitMessage());
        m_MainAppController.setCurrentCommitDateProperty(m_CommitNode.getTimestamp());
        m_MainAppController.setCurrentCommitPrecendingProperty(m_CommitNode.getCommitDirectParents());
        m_MainAppController.setCurrentCommitBranchNameProperty(m_CommitNode.getPointingBranchName());
        m_MainAppController.setDifferenceBetweenLastCommit(m_DifferenceBetweenLastCommit);
    }


    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

    public void setCommitLogic(CommitNode i_CommitNode) {
        m_CommitNode = i_CommitNode;
    }

    public void setMainAppController(MainAppController i_MainAppController) {
        m_MainAppController = i_MainAppController;
    }

    public void setRadius(int newRadius) {
        CommitCircle.setRadius(newRadius);
    }

    public void setColor(Color newColor) {
        CommitCircle.setFill(newColor);
    }

    public void changeAttributes(CommitNode commitNode,int raduis,Color color){
        commitNode.getM_CommitNodeController().setRadius(raduis);
        commitNode.getM_CommitNodeController().setColor(color);
        if(!commitNode.getCommitDirectParents().isEmpty()){
            for(String cn:commitNode.getCommitDirectParents()) {
                if(!cn.isEmpty()) {
                    CommitNode prevCommitNode = m_MainAppController.getM_CommitNodesMap().get(cn);
                    changeAttributes(prevCommitNode, raduis, color);
                }
            }
        }
    }

    public void AddResetHeadBranchMenuItem(ContextMenuCommitNode i_ContextMenuCommitNode) {
        MenuItem resetHeadBranchItem = i_ContextMenuCommitNode.createMenuItem("Reset head branch to here", (v) -> {
            try {
                m_MainAppController.resetBrunch(m_CommitNode.getCommitId());
            } catch (Exception ex) {
                Alerts.showAlertToUser(ex.getMessage());
            }
        });
        i_ContextMenuCommitNode.mainMenuCreator(resetHeadBranchItem);
    }

    public void AddCreateNewBranchMenuItem(ContextMenuCommitNode i_ContextMenuCommitNode) {
        MenuItem createNewBranchMenuItem = i_ContextMenuCommitNode.createMenuItem("Create Branch on that Commit", (v) -> {
            try {
                m_MainAppController.CreateBranchFromContextMenu(m_CommitNode.getCommitId());
            } catch (Exception ex) {
                Alerts.showAlertToUser(ex.getMessage());
            }
        });
        i_ContextMenuCommitNode.mainMenuCreator(createNewBranchMenuItem);
    }

    public void AddMergeMenuItem(ContextMenuCommitNode i_ContextMenuCommitNode) {
        MenuItem MmrgeMenuItem = i_ContextMenuCommitNode.createMenuItem("Merge Branch on that Commit", (v) -> {
            try {
                m_MainAppController.DisplayBranchesOnCommit(m_CommitNode.getCommitId());
            } catch (Exception ex) {
                Alerts.showAlertToUser(ex.getMessage());
            }
        });
        i_ContextMenuCommitNode.mainMenuCreator(MmrgeMenuItem);
    }

    public void AddDeleteBranchMenuItem(ContextMenuCommitNode i_ContextMenuCommitNode) {
        MenuItem MergeMenuItem = i_ContextMenuCommitNode.createMenuItem("Delete Branch on that Commit", (v) -> {
            try {
                m_MainAppController.DeleteBranchFromContextMenu(m_CommitNode.getCommitId());
            } catch (Exception ex) {
                Alerts.showAlertToUser(ex.getMessage());
            }
        });
        i_ContextMenuCommitNode.mainMenuCreator(MergeMenuItem);
    }

}
