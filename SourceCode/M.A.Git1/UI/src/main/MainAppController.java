package main;

import LogicEngine.*;
import LogicEngine.Writer;
import com.fxgraph.graph.Graph;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import components.changeUser.ChangeActiveUserController;
import components.addNewRepository.AddNewRepositoryController;
import components.checkoutHeadBrunch.CeckoutHeadBrunchController;
import components.collaboration.clone.CollaborationCloneController;
import components.commitInformation.CommitInformationController;
import components.commitInformation.PopUpWindowWithBtn;
import components.commitInformation.ViewMagitFile;
import components.commitTree.commitGraphLayout.CommitTreeLayout;
import components.commitTree.commitNode.CommitNode;
import components.cssManager.CssManager;
import components.deleteBrunch.deleteBrunch;
import components.loadRepositoryFromXML.LoadRepositoryFromXMLController;
import components.merge.MergeController;
import components.newBrunch.newBrunchController;
import components.newCommit.NewCommitController;
import components.resetBrunch.ResetBrunchController;
import components.showBrunches.ShowBrunchesController;
import components.showOpenChanges.ShowOpenChangesController;
import components.switchRepository.SwitchRepositoryController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainAppController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private MenuItem changeActiveUserButton;
    @FXML
    private MenuItem loadRepositoryFromXMLButton;
    @FXML
    private MenuItem InitiallizeNewRepositoryButton;
    @FXML
    private MenuItem ChangeRepositoryButton;
    @FXML
    private MenuItem ShowWorkingCopyStatusButton;
    @FXML
    private MenuItem CommitButton;
    @FXML
    private MenuItem ShowAvailableBranchesButton;
    @FXML
    private MenuItem CreateBranchButton;
    @FXML
    private MenuItem DeleteBranchButton;
    @FXML
    private MenuItem CheckoutBranchButton;
    @FXML
    private MenuItem ResetBranchButton;
    @FXML
    private Label RepositoryLocationLabel;
    @FXML
    private Label RepositoryNameLabel;
    @FXML
    private TextArea CurrentCommitSha1TextField;
    @FXML
    private Label CurrentCommitMessageLabel;
    @FXML
    private Label CurrentCommitAuthorLabel;
    @FXML
    private Label CurrentCommitDateLabel;
    @FXML
    private AnchorPane CurrentDifferenceScrollPane;
    @FXML
    private ScrollPane CommitTreeScrollPane;
    @FXML
    private MenuItem CreateMerge;
    @FXML
    private TextArea CurrentCommitPrecendingTextArea;
    @FXML
    private Label CurrentCommitBranchLabel;
    @FXML
    private Label differenceBetweenLastCommitLable;
    @FXML
    private MenuItem CollaborationClone;
    @FXML
    private MenuItem commitInformationMenuItem;
    @FXML
    private MenuItem CollaborationFetch;

    @FXML
    private MenuItem CollaborationPull;

    @FXML
    private MenuItem CollaborationPush;
    @FXML
    private MenuItem changeSkinButton;



    private MAGit m_MyMagit;
    private Stage m_PrimaryStage;
    private Map<String, CommitNode> m_CommitNodesMap;
    private final String DATE_FORMAT = "dd.MM.yyyy-hh:mm:ss:sss";


    @FXML
    private ChangeActiveUserController m_ChangeActiveUserControllerController;
    private AddNewRepositoryController m_AddNewRepositoryController;
    private SwitchRepositoryController m_SwitchRepositoryController;
    private deleteBrunch m_DeleteBrunch;
    private newBrunchController m_NewBrunchController;
    private ShowBrunchesController m_ShowBrunches;
    private CeckoutHeadBrunchController m_CeckoutHeadBrunchController;
    private ResetBrunchController m_ResetBrunchController;
    private ShowOpenChangesController m_ShowOpenChangesController;
    private NewCommitController m_NewCommitController;
    private CommitInformationController m_CommitInformationController;
    private LoadRepositoryFromXMLController m_LoadFromXMLController;
    private MergeController m_MergeBrunchController;
    private MergeController m_MergeConflictsController;
    private MergeController m_MergeShowConflictsController;
    private MergeController m_MergeFixConflictController;
    private CollaborationCloneController m_CollaborationCloneController;
    private String m_AncestorName;
    private ObservableList<String[]> m_ConflictsList;
    private String m_NameFixConflict;
    private String m_NameBrunchToMerge;
    private CssManager m_CssStyles;

    private SimpleStringProperty activerUserNameProperty;
    private SimpleStringProperty activeRepositoryLocationProperty;
    private SimpleStringProperty CurrentCommitSha1Property;
    private SimpleStringProperty CurrentCommitMessageProperty;
    private SimpleStringProperty CurrentCommitAuthorProperty;
    private SimpleStringProperty CurrentCommitDateProperty;
    private SimpleStringProperty CurrentCommitPrecendingProperty;
    private SimpleStringProperty CurrentCommitBranchNameProperty;
    private SimpleStringProperty m_DifferenceBetweenLastCommit;

    final Image FOLDER_ICON = new Image(getClass().getResourceAsStream("/components/commitInformation/FolderIcon.png"));
    final Image TEXT_ICON = new Image(getClass().getResourceAsStream("/components/commitInformation/TextFileIcon.png"));


    public MainAppController() {
        m_MyMagit = new MAGit("Administrator");
        m_CommitNodesMap = new LinkedHashMap<>();
        activerUserNameProperty = new SimpleStringProperty();
        activeRepositoryLocationProperty = new SimpleStringProperty();
        CurrentCommitSha1Property = new SimpleStringProperty();
        CurrentCommitMessageProperty = new SimpleStringProperty();
        CurrentCommitAuthorProperty = new SimpleStringProperty();
        CurrentCommitDateProperty = new SimpleStringProperty();
        CurrentCommitPrecendingProperty = new SimpleStringProperty();
        CurrentCommitBranchNameProperty = new SimpleStringProperty();
        m_DifferenceBetweenLastCommit  = new SimpleStringProperty();
        activerUserNameProperty.setValue("Administrator");
        activeRepositoryLocationProperty.set("N/A");
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.m_PrimaryStage = primaryStage;
    }

    public void setDifferenceBetweenLastCommit(String i_DifferenceBetweenLastCommit) {
        this.m_DifferenceBetweenLastCommit.set(i_DifferenceBetweenLastCommit);
    }

    public void updateNewActiveUser(String i_InputFroUser) {
        m_MyMagit.setM_ActiveUserName(i_InputFroUser);
        activerUserNameProperty.setValue(i_InputFroUser);
    }

    public void updateBrunches(String i_NameBrunch) {
        m_MyMagit.deleteBranch(i_NameBrunch);
        m_MyMagit.getM_Repositories().getM_Branches().remove(i_NameBrunch);
    }

    public void setCurrentCommitSha1Property(String currentCommitSha1Property) {
        this.CurrentCommitSha1Property.set(currentCommitSha1Property);
    }

    public void setCurrentCommitMessageProperty(String currentCommitMessageProperty) {
        this.CurrentCommitMessageProperty.set(currentCommitMessageProperty);
    }

    public void setCurrentCommitAuthorProperty(String currentCommitAuthorProperty) {
        this.CurrentCommitAuthorProperty.set(currentCommitAuthorProperty);
    }

    public void setCurrentCommitDateProperty(String currentCommitDateProperty) {
        this.CurrentCommitDateProperty.set(currentCommitDateProperty);
    }

    public void setCurrentCommitPrecendingProperty(List<String> i_CurrentCommitPrecendingProperty) {
        StringBuilder sb = new StringBuilder();
        for(String commitSha1 : i_CurrentCommitPrecendingProperty){
            sb.append(commitSha1).append(" ");
        }
        this.CurrentCommitPrecendingProperty.set(sb.toString());
    }

    public void setCurrentCommitBranchNameProperty(String i_CurrentCommitBranchProperty) {
        this.CurrentCommitBranchNameProperty.set(i_CurrentCommitBranchProperty);
    }

    public Map<String,CommitNode> getM_CommitNodesMap(){return m_CommitNodesMap;}

    public MAGit getM_MyMagit() {
        return m_MyMagit;
    }

    @FXML
    private void initialize() {
        RepositoryLocationLabel.textProperty().bind(activeRepositoryLocationProperty);
        RepositoryNameLabel.textProperty().bind(activerUserNameProperty);
        CurrentCommitSha1TextField.textProperty().bind(CurrentCommitSha1Property);
        CurrentCommitMessageLabel.textProperty().bind(CurrentCommitMessageProperty);
        CurrentCommitAuthorLabel.textProperty().bind(CurrentCommitAuthorProperty);
        CurrentCommitDateLabel.textProperty().bind(CurrentCommitDateProperty);
        CurrentCommitPrecendingTextArea.textProperty().bind(CurrentCommitPrecendingProperty);
        CurrentCommitBranchLabel.textProperty().bind(CurrentCommitBranchNameProperty);
        differenceBetweenLastCommitLable.textProperty().bind(m_DifferenceBetweenLastCommit);
        m_CssStyles = new CssManager();
        m_CssStyles.addCssSheet("/components/cssManager/ActionBarStyle.css");
        m_CssStyles.addCssSheet("/components/cssManager/ActionBarStyle2.css");
        m_CssStyles.addCssSheet("/components/cssManager/ActionBarStyle3.css");

    }
    @FXML
    public void OnChangeActiveUserButton(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/changeUser/ChangeActiveUser.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            m_ChangeActiveUserControllerController = fxmlLoader.getController();
            m_ChangeActiveUserControllerController.setM_MainController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Change user name");
            m_ChangeActiveUserControllerController.setM_MyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void OnChangeRepositoryButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/switchRepository/SwitchRepository.fxml");
            loader.setLocation(mainFXML);
            HBox root = loader.load();
            m_SwitchRepositoryController = loader.getController();
            m_SwitchRepositoryController.setMainController(this);
            Stage stage = createStage("Switch repository", false, root);
            m_SwitchRepositoryController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void OnCheckoutBranchButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else if (checkIfThereAreOpenChanges()) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setContentText("There is open changes in WC, couldn't change the head brunch");
            errorAlert.showAndWait();
        } else {
            try {
                ObservableList<String> brunchesList;
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/checkoutHeadBrunch/checkoutHeadBrunch.fxml");
                fxmlLoader.setLocation(url);
                brunchesList = createBrunchesList();
                BorderPane root = fxmlLoader.load(url.openStream());
                m_CeckoutHeadBrunchController = fxmlLoader.getController();
                m_CeckoutHeadBrunchController.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_CeckoutHeadBrunchController.setM_MyStage(stage);
                m_CeckoutHeadBrunchController.setShowBrunches(brunchesList);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnCommitButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            if(checkIfThereAreOpenChanges()) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    URL url = getClass().getResource("/components/newCommit/newCommit.fxml");
                    fxmlLoader.setLocation(url);
                    BorderPane root = fxmlLoader.load(url.openStream());
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    m_NewCommitController = fxmlLoader.getController();
                    m_NewCommitController.setM_MainController(this);
                    m_NewCommitController.setM_MyStage(stage);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Alerts.showAlertToUser("there is no changes in the commit, you can't create new commit");
            }
        }
    }
    @FXML
    void OnCommitInformationClick(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> commitList = createCommitList();
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/commitInformation/commitInformation.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_CommitInformationController = fxmlLoader.getController();
                m_CommitInformationController.setM_MainController(this);
                m_CommitInformationController.setM_MyStage(stage);
                m_CommitInformationController.setChooseCommitSha1(commitList);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnCreateBranchButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> commitsShaList = FXCollections.observableArrayList();
                commitsShaList = createShowCommitsShaList();
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/newBrunch/newBrunch.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_NewBrunchController = fxmlLoader.getController();
                m_NewBrunchController.setM_MainController(this);
                m_NewBrunchController.setM_MyStage(stage);
                m_NewBrunchController.setChooseCommitSha(commitsShaList);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnDeleteBranchButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> brunchesList;
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/deleteBrunch/deleteBrunch.fxml");
                fxmlLoader.setLocation(url);
                brunchesList = createBrunchesList();
                BorderPane root = fxmlLoader.load(url.openStream());
                m_DeleteBrunch = fxmlLoader.getController();
                m_DeleteBrunch.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_DeleteBrunch.setM_MyStage(stage);
                if (m_DeleteBrunch.getShowBrunches().getItems().isEmpty()) {
                    m_DeleteBrunch.setShowBrunches(brunchesList);
                }
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnInitiallizeNewRepositoryButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/addNewRepository/AddNewRepository.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_AddNewRepositoryController = loader.getController();
            m_AddNewRepositoryController.setMainController(this);
            Stage stage = createStage("Add new repositroy", false, root);
            m_AddNewRepositoryController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void OnResetBranchButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> commitShaList;
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/resetBrunch/resetBrunch.fxml");
                fxmlLoader.setLocation(url);
                commitShaList = createShowCommitsShaList();
                BorderPane root = fxmlLoader.load(url.openStream());
                m_ResetBrunchController = fxmlLoader.getController();
                m_ResetBrunchController.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_ResetBrunchController.setM_MyStage(stage);
                m_ResetBrunchController.setShowCommitSHA(commitShaList);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnShowAvailableBranchesButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> brunchesList;
                List<String> itemsToDisplay = new ArrayList<>();
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/showBrunches/showBrunches.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                m_ShowBrunches = fxmlLoader.getController();
                m_ShowBrunches.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_ShowBrunches.setM_MyStage(stage);
                m_MyMagit.readAllBranches(itemsToDisplay);
                brunchesList = createShowBrunchesList(itemsToDisplay);
                m_ShowBrunches.setShowBrunches(brunchesList);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnShowWorkingCopyStatusButton(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                ObservableList<String> showWcList;
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/showOpenChanges/showOpenChanges.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                m_ShowOpenChangesController = fxmlLoader.getController();
                m_ShowOpenChangesController.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_ShowOpenChangesController.setM_MyStage(stage);
                showWcList = createOpenWorkingList();
                m_ShowOpenChangesController.setShowOpenChangesList(showWcList);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    void OnloadRepositoryFromXMLButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/loadRepositoryFromXML/LoadRepositoryFromXML.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_LoadFromXMLController = loader.getController();
            m_LoadFromXMLController.setMainController(this);
            Stage stage = createStage("Load from XML", false, root);
            m_LoadFromXMLController.setMyStage(stage);
            activeRepositoryLocationProperty.bind(m_LoadFromXMLController.getNewRepositoryLocationProperty());
            activeRepositoryLocationProperty.bind(m_LoadFromXMLController.getNewRepositoryLocationIfTaskCreatedItProperty());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void OnMerge() {
        if (!checkIfThereAreOpenChanges()) {
            try {
                ObservableList<String> brunchesList;
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/merge/selectBrunchToMerge.fxml");
                fxmlLoader.setLocation(url);
                brunchesList = createBrunchesList();

                BorderPane root = fxmlLoader.load(url.openStream());
                m_MergeBrunchController = fxmlLoader.getController();
                m_MergeBrunchController.setM_MainController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                m_MergeBrunchController.setShowBrunches(brunchesList);
                m_MergeBrunchController.setM_MyStage(stage);
                if(brunchesList != null && !brunchesList.isEmpty()) {
                    stage.show();
                }
                else{
                    Alerts.showAlertToUser("You don't have brunches to merge");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alerts.showAlertToUser("You have open changes, can't doing merge");
        }

    }
    @FXML
    void OnCollaborationCloneClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            // load main fxml
            URL mainFXML = getClass().getResource("/components/collaboration/clone/CollaborationClone.fxml");
            loader.setLocation(mainFXML);
            BorderPane root = loader.load();
            m_CollaborationCloneController = loader.getController();
            m_CollaborationCloneController.setMainController(this);
            Stage stage = createStage("Clone Repository", false, root);
            m_CollaborationCloneController.setMyStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    void OnCollaborationFetchClicked(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        }
        else if (!isRemoteRepositoryLoaded()){
            Alerts.showAlertToUser("Please load remote repository first.");
        }
        else {
            try {
                m_MyMagit.FetchRemoteRepository();
                this.InitCommitTree();
            } catch (IOException e) {
                Alerts.showAlertToUser(e.toString());
            }

        }
    }
    @FXML
    void OnCollaborationPullClicked(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        }
        else if (!isRemoteRepositoryLoaded()){
            Alerts.showAlertToUser("Please load remote repository first.");
        }
        else if(m_MyMagit.checkIfThereAreOpenChanges()){
            Alerts.showAlertToUser("There are open changes. Can't do the Pull action");
        }
        else if(!m_MyMagit.isHeadBranchTrackingAfter()){
            Alerts.showAlertToUser("The Head branch isn't remote tracking branch.");
        }
        else{
            try {
                m_MyMagit.PullFromRemoteRepository();
                this.InitCommitTree();
            } catch (IOException e) {
                Alerts.showAlertToUser("Can't extract one of the files in the remote repository");
            }
        }

    }
    @FXML
    void OnCollaborationPushClicked(ActionEvent event) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        }
        else if (!isRemoteRepositoryLoaded()){
            Alerts.showAlertToUser("Please load remote repository first.");
        }
        else if(!m_MyMagit.isHeadBranchTrackingAfter()){
            Alerts.showAlertToUser("The Head branch isn't remote tracking branch.");
        }
        else{
            try {
                m_MyMagit.PushToRemoteRepository();
                this.InitCommitTree();
            } catch (IOException e) {
                Alerts.showAlertToUser("Can't extract one of the files in the remote repository");
            }catch (IllegalAccessException ex){
                Alerts.showAlertToUser(ex.getMessage());
            }
        }
    }
    @FXML
    void OnChangeSkinButtonClick(ActionEvent event) {
        m_CssStyles.nextCss();
        BorderPane bp = (BorderPane)m_PrimaryStage.getScene().lookup("#mainBorderPane");
        bp.getStylesheets().clear();
        bp.getStylesheets().add(m_CssStyles.getCurrentCss());
    }

    public void CollaborationCloneRepository(String i_FullPathFromUser, String i_CloneFullPathFromUser,String i_CloneName) {
        m_MyMagit.CloneRepository(i_FullPathFromUser,i_CloneFullPathFromUser,i_CloneName);
        updateActiveRepositoryLocation(i_CloneFullPathFromUser + "\\" + i_CloneName);
        this.InitCommitTree();
    }

    private ObservableList<String> createShowBrunchesList(List<String> i_ListBrunches) {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.addAll(i_ListBrunches);
        return list;
    }

    public boolean isBrunchNameExist(String i_Name) {
        boolean isExist = false;
        if (m_MyMagit.getM_Repositories().getM_Branches().get(i_Name) != null) {
            isExist = true;
        }
        return isExist;
    }

    public void OnCheckoutNewBrunchButton(String i_Name,String i_CommitSha) {
        try {
            m_MyMagit.createBrunch(i_Name,i_CommitSha);
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/newBrunch/checkoutNewBrunch.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            m_NewBrunchController = fxmlLoader.getController();
            m_NewBrunchController.setM_MainController(this);
            m_NewBrunchController.setM_MyStage(stage);
            m_NewBrunchController.setM_NewBrunchName(i_Name);
            m_NewBrunchController.setM_CommitSha(i_CommitSha);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkIfThereAreOpenChanges(String i_BranchName) throws IOException {
        boolean isWcChange = false;
        if (m_MyMagit.checkIfThereAreOpenChanges()) {
            isWcChange = true;
        } else {
            changeHeadBrunchAndSpanTheObjects(i_BranchName);
        }
        return isWcChange;
    }

    public void EndOfChangeRepositoryProcess(String i_RepositoryFullPath){
        this.updateActiveRepositoryLocation(i_RepositoryFullPath);
        this.m_MyMagit.SwitchRepository(i_RepositoryFullPath);
        this.InitCommitTree();
    }

    private Stage createStage(String i_Title, boolean i_IsResizable, Parent i_Root) {
        Stage returnStage = new Stage();
        returnStage.setTitle(i_Title);
        returnStage.setScene(new Scene(i_Root));
        returnStage.setResizable(i_IsResizable);

        return returnStage;
    }

    public void changeHeadBrunchAndSpanTheObjects(String i_NewHeadBranch) throws IOException {

        m_MyMagit.changeTheActiveBranchActivity(i_NewHeadBranch);
        m_MyMagit.spanHeadBranchToOurObjects(m_MyMagit.getM_Repositories().getM_Location());
        m_MyMagit.spreadAllBranchesIntoOurObjects();
        Map<String, Commit> allPrecendingCommits = new HashMap<>();
        m_MyMagit.gatherAllPrecendingCommits(allPrecendingCommits);
        List<String> allTopLeavesCommitsSha1 = new ArrayList<>();
        m_MyMagit.gatherAllTheTopLeavesCommitsInTreeToList(allTopLeavesCommitsSha1, allPrecendingCommits);
        m_MyMagit.spanAllNonPointedCommitsToOurObjects(allTopLeavesCommitsSha1);
        Deleter.deleteDir(m_MyMagit.getM_Repositories().getM_Location() + "/WC");
        Creator.createWC(m_MyMagit);
        InitCommitTree();
    }

    public boolean checkIfThereAreOpenChanges() {
        boolean isWcChange = false;
        if (m_MyMagit.checkIfThereAreOpenChanges()) {
            isWcChange = true;
        }
        return isWcChange;
    }

    public void OnResetBrunch(String i_CommitSha) {
        try {
            if (!checkIfThereAreOpenChanges()) {
                resetBrunch(i_CommitSha);
            }
            else {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/resetBrunch/resetBrunchMessege.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_ResetBrunchController = fxmlLoader.getController();
                m_ResetBrunchController.setM_MainController(this);
                m_ResetBrunchController.setM_MyStage(stage);
                m_ResetBrunchController.setM_commitSha(i_CommitSha);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void resetBrunch(String i_CommitSha) throws IOException {
        String nameHeadBrunch;
        File headFolder = new File(m_MyMagit.getM_Repositories().getM_Location() + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(headFolder));
        nameHeadBrunch = br.readLine();
        br.close();
        m_MyMagit.getM_Repositories().getM_Branches().get(nameHeadBrunch).setM_PointedCommitId(i_CommitSha);
        Path branchPath = Paths.get(m_MyMagit.getM_Repositories().getM_Location() + "/.magit/branches/" + nameHeadBrunch);
        Path wcPath = Paths.get(m_MyMagit.getM_Repositories().getM_Location() + "/WC");
        Deleter.deleteTextFileContent(branchPath);
        Writer.WriteToTextFile(branchPath,i_CommitSha);
        Deleter.deleteDir(wcPath.toString());
        Creator.createWC(m_MyMagit);
        InitCommitTree();
    }

    private ObservableList<String> createShowCommitsShaList() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Map.Entry<String, Commit> entry : m_MyMagit.getM_Repositories().getM_Commits().entrySet()) {
            String k = entry.getKey();
            list.add(k);
        }
        return list;
    }

    private ObservableList<String> createOpenWorkingList() throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();
        List<String> changeFile = new ArrayList<>();
        List<String> deleteFile = new ArrayList<>();
        List<String> createFile = new ArrayList<>();

        m_MyMagit.showWorkingCopyStatus(changeFile, deleteFile, createFile);

        //print data
        list.add("Repository name:" + this.m_MyMagit.getM_Repositories().getM_Name());
        list.add("Repository location:" + this.m_MyMagit.getM_Repositories().getM_Location());
        list.add("User name:" + this.m_MyMagit.getM_ActiveUserName());

        list.add("Files that were changed:");
        if (changeFile != null) {
            changeFile.forEach((k) ->
            {
                list.add(k);
            });
        }

        if (createFile != null) {
            list.add("Files that are new:");
            createFile.forEach((k) ->
            {
                list.add(k);
            });
        }

        if (deleteFile != null) {
            list.add("Files that were deleted:");
            deleteFile.forEach((k) ->
            {
                list.add(k);
            });
        }
        return list;
    }

    public void createCommit(String i_Messege, String i_MergePervCommit) {
        m_MyMagit.createNewCommit(i_Messege, i_MergePervCommit);
        InitCommitTree();
    }

    public void InitCommitTree() {
        try {
            m_CommitNodesMap.clear();
            Graph tree = new Graph();
            Model model = tree.getModel();
            tree.beginUpdate();
            createCommitNodesMapAndTree(model);
            tree.endUpdate();
            tree.layout(new CommitTreeLayout());
            Scene i_Scene = m_PrimaryStage.getScene();
            ScrollPane scrollPane = (ScrollPane) i_Scene.lookup("#CommitTreeScrollPane");
            PannableCanvas canvas = tree.getCanvas();
            scrollPane.setContent(canvas);
            Platform.runLater(() -> {
                tree.getUseViewportGestures().set(false);
                tree.getUseNodeGestures().set(false);
            });
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }
    }

    private void createCommitNodesMapAndTree(Model treeModel) {
        Map<String, Commit> allPrecendingCommits = new HashMap<>();
        m_MyMagit.gatherAllPrecendingCommits(allPrecendingCommits);
        Map<String, Commit> allTopLeavesCommits = new HashMap<>();
        m_MyMagit.gatherAllTheTopLeavesCommitsInTreeToMap(allTopLeavesCommits, allPrecendingCommits);
        CommitNode activeCommitNode;
        for (Map.Entry<String, Commit> commitEntry : allTopLeavesCommits.entrySet()) {
            String activeCommitId = commitEntry.getKey();
            Commit activeCommitObject = commitEntry.getValue();
            activeCommitNode = new CommitNode(activeCommitObject.getM_DateOfCreation(), activeCommitObject.getM_Author(), activeCommitObject.getM_Message(), "Not pointed Commit", activeCommitId,this,activeCommitObject.getM_PrecedingCommitId());
            if (!m_CommitNodesMap.containsKey(activeCommitId)) {
                this.m_CommitNodesMap.put(activeCommitId, activeCommitNode);
                treeModel.addCell(activeCommitNode);
            }
            createAndInsertNodesToTreeRec(activeCommitNode, treeModel);
        }
        attachBranchesToCommitNodes();
        Comparator<ICell> comparator = (o1, o2) -> {
            CommitNode firstNode = (CommitNode) o1;
            CommitNode secondNode = (CommitNode) o2;
            SimpleDateFormat general = new SimpleDateFormat(DATE_FORMAT);
            try {
                Date firstDate = general.parse(firstNode.getTimestamp());
                Date secondDate = general.parse(secondNode.getTimestamp());
                if (firstDate.after(secondDate))
                {
                    return -1;
                } else if (firstDate.before(secondDate)) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        };

        treeModel.getAddedCells().sort(comparator);
    }

    private void createAndInsertNodesToTreeRec(CommitNode i_CurrentActiveCommitNode, Model treeModel) {
        List<String> currentPrecedingCommitsIds = this.m_MyMagit.getM_Repositories().getM_Commits().get(i_CurrentActiveCommitNode.getCommitId()).getM_PrecedingCommitId();

        if ((currentPrecedingCommitsIds.isEmpty() || currentPrecedingCommitsIds == null)) {
            this.m_CommitNodesMap.put(i_CurrentActiveCommitNode.getCommitId(), i_CurrentActiveCommitNode);
        }
        else if(currentPrecedingCommitsIds.get(0).equals("") || currentPrecedingCommitsIds.get(0).isEmpty()){
            this.m_CommitNodesMap.put(i_CurrentActiveCommitNode.getCommitId(), i_CurrentActiveCommitNode);
        }
        else {
            for (String commitId : currentPrecedingCommitsIds) {
                Commit temp = this.m_MyMagit.getM_Repositories().getM_Commits().get(commitId);
                CommitNode newActiveCommitNode = null;
                if (!m_CommitNodesMap.containsKey(commitId)) {
                    newActiveCommitNode = new CommitNode(temp.getM_DateOfCreation(), temp.getM_Author(), temp.getM_Message(), "Not pointed Commit", commitId,this,temp.getM_PrecedingCommitId());
                    this.m_CommitNodesMap.put(commitId, newActiveCommitNode);
                    treeModel.addCell(newActiveCommitNode);
                } else {//the cell is already in the tree model
                    for (ICell findTheCell : treeModel.getAddedCells()) {
                        CommitNode tempNode = (CommitNode) findTheCell;
                        if (tempNode.getCommitId().equals(commitId)) {
                            newActiveCommitNode = tempNode;
                            break;
                        }
                    }
                }
                Edge edgeToAdd = new Edge(newActiveCommitNode, i_CurrentActiveCommitNode);
                treeModel.addEdge(edgeToAdd);
                createAndInsertNodesToTreeRec(newActiveCommitNode, treeModel);
            }
        }
    }

    private void attachBranchesToCommitNodes() {

        for (Map.Entry<String, Branch> currentBranch : this.m_MyMagit.getM_Repositories().getM_Branches().entrySet()) {
            try {
                m_CommitNodesMap.get(currentBranch.getValue().getM_PointedCommitId()).addPointingBranchName(currentBranch.getKey());
            } catch (Exception ex) {
            }
        }
    }

    private Boolean isRepositoryLoaded() {
        boolean result = true;
        if (m_MyMagit.getM_Repositories() == null) {
            result = false;
        }
        return result;
    }

    private Boolean isRemoteRepositoryLoaded() {
        boolean result = true;
        if (m_MyMagit.getM_Repositories().getM_RemoteRepositoryLocation() == null) {
            result = false;
        }
        return result;
    }

    public void OnBrunchToMerge(String i_NameBrunchToMerge) {
        try {
            ObservableList<String[]> list;
            m_NameBrunchToMerge = i_NameBrunchToMerge;
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/merge/showConflicts.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            m_MergeShowConflictsController = fxmlLoader.getController();
            m_MergeShowConflictsController.setM_MainController(this);
            list = makeMerge(i_NameBrunchToMerge);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            if (list != null && !list.isEmpty()) {
                if(list.get(0)[0]!="not") {
                    m_ConflictsList = list;
                    OnShowConflicts(list);
                }
            }
            else{
                OnEndConflicts();
            }
            m_MergeShowConflictsController.setM_MyStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String[]> makeMerge(String i_NameBrunchToMerge) {

        ObservableList<String[]> list = null;
        String ancestorName;

        String oursName = null;
        try {
            oursName = m_MyMagit.getHeadBranchFromRepository();
            String oursCommit = m_MyMagit.getM_Repositories().getM_Branches().get(oursName).getM_PointedCommitId();
            String oursRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(oursCommit).getM_RootFolderId();
            Folder oursItems = m_MyMagit.getM_Repositories().getM_Folders().get(oursRootFolder);
            List<String[]> oursChangeFile = new ArrayList<>();
            List<String[]> oursDeleteFile = new ArrayList<>();
            List<String[]> oursCreateFile = new ArrayList<>();

            String theirsCommit = m_MyMagit.getM_Repositories().getM_Branches().get(i_NameBrunchToMerge).getM_PointedCommitId();
            String theirsRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(theirsCommit).getM_RootFolderId();
            Folder theirsItems = m_MyMagit.getM_Repositories().getM_Folders().get(theirsRootFolder);
            List<String[]> theirsChangeFile = new ArrayList<>();
            List<String[]> theirsDeleteFile = new ArrayList<>();
            List<String[]> theirsCreateFile = new ArrayList<>();

            ancestorName = getAncestor(oursName, i_NameBrunchToMerge);
            if (ancestorName != null && !ancestorName.isEmpty()) {
                m_MergeShowConflictsController.setM_AncestorName(ancestorName);
                m_AncestorName = ancestorName;
                String ancestorRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(ancestorName).getM_RootFolderId();
                Folder ancestorItems = m_MyMagit.getM_Repositories().getM_Folders().get(ancestorRootFolder);
                changesInItems(m_MyMagit.getM_Repositories().getM_Location() + "/WC", ancestorItems.getM_ItemsClone(), oursItems.getM_ItemsClone(), oursChangeFile, oursDeleteFile, oursCreateFile,oursRootFolder);
               /* System.out.println("ours lists:");
                System.out.println("changes:");
                print(oursChangeFile);
                System.out.println("create:");
                print(oursCreateFile);
                System.out.println("delete:");
                print(oursDeleteFile);*/
                changesInItems(m_MyMagit.getM_Repositories().getM_Location() + "/WC", ancestorItems.getM_ItemsClone(), theirsItems.getM_ItemsClone(), theirsChangeFile, theirsDeleteFile, theirsCreateFile,theirsRootFolder);
                /*System.out.println("theirs lists:");
                System.out.println("changes:");
                print(theirsChangeFile);
                System.out.println("create:");
                print(theirsCreateFile);
                System.out.println("delete:");
                print(theirsDeleteFile);*/
                list = checkItems(oursChangeFile, oursDeleteFile, oursCreateFile, theirsChangeFile, theirsDeleteFile, theirsCreateFile);

            }
            else{
                list = FXCollections.observableArrayList();
                if(!checkIfThereAreOpenChanges()){
                    String[] item = {"not"};
                    list.add(item);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void print(List<String[]> i_Print){
        for (String[] i_Item:i_Print) {
            for (String i_ItemToPrint:i_Item) {
                System.out.println(i_ItemToPrint);
            }
        }
    }

    private String getAncestor(String i_NameOurBrunch, String i_NameTheirsBrunch) {
        boolean isFound = false;
        String ancestorName = null;
        String nameTheirsCommit = m_MyMagit.getM_Repositories().getM_Branches().get(i_NameTheirsBrunch).getM_PointedCommitId();
        String nameOurCommit = m_MyMagit.getM_Repositories().getM_Branches().get(i_NameOurBrunch).getM_PointedCommitId();
        Commit theirsCommit = m_MyMagit.getM_Repositories().getM_Commits().get(nameTheirsCommit);
        Commit oursCommit = m_MyMagit.getM_Repositories().getM_Commits().get(nameOurCommit);

        for (String prevCommit : theirsCommit.getM_PrecedingCommitId()) {
            if (prevCommit.equals(nameOurCommit)) {
                isFound = true;
                m_MyMagit.getM_Repositories().getM_Branches().get(i_NameOurBrunch).setM_PointedCommitId(nameTheirsCommit);
                nameOurCommit = m_MyMagit.getM_Repositories().getM_Branches().get(i_NameOurBrunch).getM_PointedCommitId();
                oursCommit.getM_PrecedingCommitId().add(nameOurCommit);
                break;
            }
        }
        if (!isFound) {
            for (String prevCommit : oursCommit.getM_PrecedingCommitId()) {
                if (prevCommit.equals(nameTheirsCommit)) {
                    isFound = true;
                    Alerts.showAlertToUser("It is all ready marge");
                    break;
                }
            }
        }
        if (!isFound) {
            for (String prevCommitOurs : theirsCommit.getM_PrecedingCommitId()) {
                for (String prevCommitTheirs : theirsCommit.getM_PrecedingCommitId()) {
                    if (prevCommitTheirs.equals(prevCommitOurs)) {
                        ancestorName = prevCommitOurs;
                        break;
                    }
                }
            }
        }
        return ancestorName;
    }

    private void OnShowConflicts(ObservableList<String[]> i_ConflictsList) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/merge/showConflicts.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            m_MergeConflictsController = fxmlLoader.getController();
            m_MergeConflictsController.setM_MainController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            m_MergeConflictsController.setM_MyStage(stage);
            m_MergeConflictsController.setShowConflicts(i_ConflictsList);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void OnFixConflict(String i_NameConflictFix) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/merge/fixConflict.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            m_MergeFixConflictController = fxmlLoader.getController();
            m_MergeFixConflictController.setM_MainController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            m_MergeFixConflictController.setM_MyStage(stage);
            m_NameFixConflict = i_NameConflictFix;
            getTexts(i_NameConflictFix);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTexts(String i_NameConflictFix) throws IOException {

        boolean isFound = false;
        String nameBrunch = m_MergeBrunchController.getM_NameBrunchToMerge();
        String theirsCommit = m_MyMagit.getM_Repositories().getM_Branches().get(nameBrunch).getM_PointedCommitId();
        String theirsRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(theirsCommit).getM_RootFolderId();
        Map<String, Folder.Item> theirsCommitItems = m_MyMagit.getM_Repositories().getM_Folders().get(theirsRootFolder).getM_Items();
        for (Map.Entry<String, Folder.Item> itemEntry : theirsCommitItems.entrySet()) {
            String keyItem = itemEntry.getKey();
            Folder.Item valueItem = itemEntry.getValue();
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Map<String, Folder.Item> Items = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem).getM_Items();
                isFound = foundText(Items, i_NameConflictFix, "Theirs");
                if (isFound) {
                    break;
                }
            } else if (i_NameConflictFix.equals(valueItem.getM_Name())) {
                String text = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItem).getM_Content().trim();
                m_MergeFixConflictController.setTheirsText(text);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            String text = "The file deleted";
            m_MergeFixConflictController.setTheirsText(text);
        }
        isFound = false;
        String oursName = m_MyMagit.getHeadBranchFromRepository();
        String oursCommit = m_MyMagit.getM_Repositories().getM_Branches().get(oursName).getM_PointedCommitId();
        String oursRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(oursCommit).getM_RootFolderId();
        Map<String, Folder.Item> oursCommitItems = m_MyMagit.getM_Repositories().getM_Folders().get(oursRootFolder).getM_Items();
        for (Map.Entry<String, Folder.Item> itemEntry : oursCommitItems.entrySet()) {
            String keyItem = itemEntry.getKey();
            Folder.Item valueItem = itemEntry.getValue();
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Map<String, Folder.Item> Items = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem).getM_Items();
                isFound = foundText(Items, i_NameConflictFix, "Ours");
                if (isFound) {
                    break;
                }
            } else if (i_NameConflictFix.equals(valueItem.getM_Name())) {
                String text = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItem).getM_Content().trim();
                m_MergeFixConflictController.setOursText(text);
                isFound = true;
                break;
            }
        }
        if (!isFound) {
            String text = "The file deleted";
            m_MergeFixConflictController.setOursText(text);
        }
        isFound = false;
        String ancestorRootFolder = m_MyMagit.getM_Repositories().getM_Commits().get(m_AncestorName).getM_RootFolderId();
        Map<String, Folder.Item> ancestorCommitItems = m_MyMagit.getM_Repositories().getM_Folders().get(ancestorRootFolder).getM_Items();
        for (Map.Entry<String, Folder.Item> itemEntry : ancestorCommitItems.entrySet()) {
            String keyItem = itemEntry.getKey();
            Folder.Item valueItem = itemEntry.getValue();
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Map<String, Folder.Item> Items = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem).getM_Items();
                isFound = foundText(Items, i_NameConflictFix, "Ancestor");
                if (isFound) {
                    break;
                }
            } else if (i_NameConflictFix.equals(valueItem.getM_Name())) {
                String text = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItem).getM_Content().trim();
                m_MergeFixConflictController.setAncestorText(text);
                isFound = true;
                break;
            }

        }
        if (!isFound) {
            String text = "The file deleted";
            m_MergeFixConflictController.setAncestorText(text);
        }
    }

    private boolean foundText(Map<String, Folder.Item> i_Items, String i_NameConflictFix, String i_TypeText) {
        boolean isFound = false;
        for (Map.Entry<String, Folder.Item> itemEntry : i_Items.entrySet()) {
            String keyItem = itemEntry.getKey();
            Folder.Item valueItem = itemEntry.getValue();
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Map<String, Folder.Item> Items = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem).getM_Items();
                isFound = foundText(Items, i_NameConflictFix, i_TypeText);
                if (isFound) {
                    break;
                }
            } else if (i_NameConflictFix.equals(valueItem.getM_Name())) {
                String text = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItem).getM_Content().trim();
                if (i_TypeText == "Ancestor") {
                    m_MergeFixConflictController.setAncestorText(text);
                } else if (i_TypeText == "Ours") {
                    m_MergeFixConflictController.setOursText(text);
                } else {
                    m_MergeFixConflictController.setTheirsText(text);
                }
                isFound = true;
                break;
            }

        }
        return isFound;
    }

    private void OnEndConflicts() {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                String prevCommit = m_MyMagit.getM_Repositories().getM_Branches().get(m_NameBrunchToMerge).getM_PointedCommitId();
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/newCommit/newCommit.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_NewCommitController = fxmlLoader.getController();
                m_NewCommitController.setM_PrevMergeCommit(prevCommit);
                m_NewCommitController.setM_MainController(this);
                m_NewCommitController.setM_MyStage(stage);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void OnUpdateConflicts(String i_Text) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        for (String[] conflict : m_ConflictsList) {
            if (m_NameFixConflict.equals(conflict[1])) {
                if (i_Text != null) {
                    Folder folderToPutBlob = m_MyMagit.getM_Repositories().getM_Folders().get(conflict[3]);//get the folder that the blob is in
                    if (m_MyMagit.getM_Repositories().getM_Blobs().get(conflict[0]) != null) {
                        m_MyMagit.getM_Repositories().getM_Blobs().remove((conflict[0]));
                        folderToPutBlob.getM_Items().remove(conflict[0]);
                        deleteItem(conflict[2]);
                    }
                    conflict[0] = DigestUtils.sha1Hex(i_Text.trim());
                    Blob newBlob = new Blob(i_Text);
                    m_MyMagit.getM_Repositories().getM_Blobs().put(conflict[0], newBlob);
                    Folder.Item newItem = new Folder.Item(conflict[1], conflict[0], Folder.Item.eItemType.BLOB, m_MyMagit.getM_ActiveUserName(), formatter.format(new Date().getTime()));
                    folderToPutBlob.getM_Items().put(conflict[0],newItem);
                }else {
                    if (m_MyMagit.getM_Repositories().getM_Blobs().get(conflict[0]) != null) {
                        deleteItem(conflict[2]);
                    }

                }
            }
        }
        m_MergeConflictsController.getShowConflicts().getItems().remove(m_NameFixConflict);
        if (m_MergeConflictsController.getShowConflicts().getItems().isEmpty()) {
            m_MergeConflictsController.getM_MyStage().close();
            OnEndConflicts();
        }
    }

    private void changesInItems(String i_Path, Map<String, Folder.Item> i_ItemsOneBrunch, Map<String, Folder.Item> i_ItemsSecondBrunch, List<String[]> i_ChangeFile, List<String[]> i_DeleteFile, List<String[]> i_CreateFile,String i_FolderId) {
        boolean isExist = false;
        for (Map.Entry<String, Folder.Item> itemEntryOne : i_ItemsOneBrunch.entrySet()) {
            String keyItemOne = itemEntryOne.getKey();
            Folder.Item valueItemOne = itemEntryOne.getValue();

            for (Map.Entry<String, Folder.Item> itemEntrySecond : i_ItemsSecondBrunch.entrySet()) {
                String keyItemSecond = itemEntrySecond.getKey();
                Folder.Item valueItemSecond = itemEntrySecond.getValue();

                if (valueItemSecond.getM_Name().equals(valueItemOne.getM_Name())) {
                    isExist = true;
                    if (valueItemSecond.getM_Type() == Folder.Item.eItemType.FOLDER) {
                        Folder checkFolderOne = m_MyMagit.getM_Repositories().getM_Folders().get(keyItemOne);
                        Folder checkFolderSecond = m_MyMagit.getM_Repositories().getM_Folders().get(keyItemSecond);
                        changesInItems(i_Path + "/" + valueItemOne.getM_Name(), checkFolderOne.getM_ItemsClone(), checkFolderSecond.getM_ItemsClone(), i_ChangeFile, i_DeleteFile, i_CreateFile,keyItemSecond);
                    } else {
                        Blob checkBlobOne = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItemOne);
                        Blob checkBlobSecond = m_MyMagit.getM_Repositories().getM_Blobs().get(keyItemSecond);
                        if (!(checkBlobSecond.getM_Content().trim().equals(checkBlobOne.getM_Content().trim()))) {
                            String[] item = {keyItemSecond, valueItemSecond.getM_Name(), i_Path+"/"+ valueItemSecond.getM_Name(),i_FolderId};
                            i_ChangeFile.add(item);
                        }
                    }
                    i_ItemsSecondBrunch.remove(keyItemSecond);
                    break;
                }
            }
            if (!isExist) {
                String[] item = {keyItemOne, valueItemOne.getM_Name(), i_Path+"/"+valueItemOne.getM_Name(),i_FolderId};
                i_DeleteFile.add(item);
                if (valueItemOne.getM_Type() == Folder.Item.eItemType.FOLDER) {
                    Folder checkFolderOne = m_MyMagit.getM_Repositories().getM_Folders().get(keyItemOne);
                    deleteFolderInItems(checkFolderOne.getM_ItemsClone(), i_DeleteFile, i_Path+"/"+valueItemOne.getM_Name(),keyItemOne);
                }
            }
            isExist = false;
        }

        if (i_ItemsSecondBrunch.size() > 0) {
            i_ItemsSecondBrunch.forEach((k, v) ->
            {
                String[] item = {k, v.getM_Name(), i_Path+"/"+v.getM_Name(),i_FolderId};
                i_CreateFile.add(item);
                 if (v.getM_Type() == Folder.Item.eItemType.FOLDER) {
                   Folder checkFolderOne = m_MyMagit.getM_Repositories().getM_Folders().get(k);
                 newFolderInItems(checkFolderOne.getM_ItemsClone(), i_CreateFile, i_Path+"/"+v.getM_Name(),k);
                }
            });
        }

    }

    private void newFolderInItems(Map<String, Folder.Item> i_Items, List<String[]> i_CreateFile,String i_Path,String i_FolderId) {
        for (Map.Entry<String, Folder.Item> itemEntryOne : i_Items.entrySet()) {
            String keyItem = itemEntryOne.getKey();
            Folder.Item valueItem = itemEntryOne.getValue();
            String[] item = {keyItem, valueItem.getM_Name(),i_Path+"/"+valueItem.getM_Name(),i_FolderId};
            i_CreateFile.add(item);
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Folder checkFolderOne = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem);
                newFolderInItems(checkFolderOne.getM_ItemsClone(), i_CreateFile,i_Path+"/"+valueItem.getM_Name(),i_FolderId);
            }
        }

    }

    private void deleteFolderInItems(Map<String, Folder.Item> i_Items, List<String[]> i_deleteFile,String i_Path,String i_FolderId) {
        for (Map.Entry<String, Folder.Item> itemEntryOne : i_Items.entrySet()) {
            String keyItem = itemEntryOne.getKey();
            Folder.Item valueItem = itemEntryOne.getValue();
            String[] item = {keyItem, valueItem.getM_Name(),i_Path+"/"+valueItem.getM_Name(),i_FolderId};
            i_deleteFile.add(item);
            if (valueItem.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Folder checkFolderOne = m_MyMagit.getM_Repositories().getM_Folders().get(keyItem);
                deleteFolderInItems(checkFolderOne.getM_ItemsClone(), i_deleteFile,i_Path+"/"+valueItem.getM_Name(),i_FolderId);
            }
        }

    }

    private ObservableList<String[]> checkItems(List<String[]> i_OursChangeFile, List<String[]> i_OursDeleteFile, List<String[]> i_OursCreateFile, List<String[]> i_TheirsChangeFile, List<String[]> i_TheirsDeleteFile, List<String[]> i_TheirsCreateFile) {
        ObservableList<String[]> list = FXCollections.observableArrayList();
        Boolean isFound = false;

        for (int i=0 ; i < i_OursChangeFile.size() ; i++) {
            for (int j=0 ; j < i_TheirsChangeFile.size() ; j++) {
                if (i_OursChangeFile.get(i)[1].equals(i_TheirsChangeFile.get(j)[1])) {
                    list.add(i_OursChangeFile.get(i));
                    i_OursChangeFile.remove(i_OursChangeFile.get(i));
                    i_TheirsChangeFile.remove(i_TheirsChangeFile.get(j));
                    i--;
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                for (int j=0 ; j < i_TheirsDeleteFile.size() ; j++) {
                    if (i_OursChangeFile.get(i)[1].equals(i_TheirsDeleteFile.get(j)[1])) {
                        list.add(i_OursChangeFile.get(i));
                        i_OursChangeFile.remove(i_OursChangeFile.get(i));
                        i_TheirsChangeFile.remove(i_TheirsDeleteFile.get(j));
                        break;
                    }
                }
            }
            isFound = false;
        }

        for (int i=0 ; i < i_OursCreateFile.size() ; i++) {

            for (int j=0 ; j < i_TheirsCreateFile.size() ; j++) {
                if (i_OursCreateFile.get(i)[1].equals(i_TheirsCreateFile.get(j)[1])) {
                    list.add(i_OursCreateFile.get(i));
                    i_OursChangeFile.remove(i_OursCreateFile.get(i));
                    i_TheirsChangeFile.remove(i_TheirsCreateFile.get(j));
                    break;
                }
            }

        }

        for (int i=0 ; i < i_OursDeleteFile.size() ; i++) {
            for (int j=0 ; j < i_TheirsChangeFile.size() ; j++) {
                if (i_OursDeleteFile.get(i)[1].equals(i_TheirsChangeFile.get(j)[1])) {
                    list.add(i_OursDeleteFile.get(i));
                    i_OursChangeFile.remove(i_OursDeleteFile.get(i));
                    i_TheirsChangeFile.remove(i_TheirsChangeFile.get(j));
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                for (int j=0 ; j < i_TheirsDeleteFile.size() ; j++) {
                    if (i_OursChangeFile.get(i)[1].equals(i_TheirsDeleteFile.get(j)[1])) {
                        list.add(i_OursChangeFile.get(i));
                        i_OursChangeFile.remove(i_OursChangeFile.get(i));
                        i_TheirsChangeFile.remove(i_TheirsDeleteFile.get(j));
                        break;
                    }
                }
            }
            isFound = false;

        }

        if (!i_TheirsChangeFile.isEmpty()) {
            for (String[] itemEntryTheirs : i_TheirsChangeFile) {
                deleteItem(itemEntryTheirs[2]);
                addItemInWC(itemEntryTheirs);
            }
        }
        if (!i_TheirsDeleteFile.isEmpty()) {
            for (String[] itemEntryTheirs : i_OursDeleteFile) {
                deleteItem(itemEntryTheirs[2]);
            }
        }
        if (!i_TheirsCreateFile.isEmpty()) {
            for (String[] itemEntryTheirs : i_TheirsCreateFile) {
                addItemInWC(itemEntryTheirs);
            }
        }

        return list;
    }

    private void addItemInWC(String[] i_MyItem) {
        if ( m_MyMagit.getM_Repositories().getM_Blobs().get(i_MyItem[0]) != null) {
            findBlob(i_MyItem[0], m_MyMagit.getM_Repositories().getM_Blobs(), Paths.get(i_MyItem[2] ));
        } else {
            findFolder(Paths.get(i_MyItem[2] + "/" + i_MyItem[1]));
        }

    }

    private void findBlob (String i_Id, Map < String, Blob > i_Blobs, Path i_Path ){
        Writer.writeBlob(i_Path, i_Blobs.get(i_Id));
    }

    private void findFolder (Path i_Path ) {
        try {
            Files.createDirectories(i_Path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(String i_Path){
        Path rootPath = Paths.get(i_Path);
        try {
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<String> createBrunchesList() throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();
        File headFolder = new File(m_MyMagit.getM_Repositories().getM_Location() + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(headFolder));
        String activeBranchName = br.readLine();
        list.removeAll();
        for (Map.Entry<String, Branch> entry : m_MyMagit.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = entry.getValue();
            if (!(v.getM_Name().equals("Head")) && !(v.getM_Name().equals(activeBranchName))) {
                list.add(v.getM_Name());
            }

        }
        return list;
    }

    private ObservableList<String> createCommitList() throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();
        list.removeAll();
        for (Map.Entry<String, Commit> entry : m_MyMagit.getM_Repositories().getM_Commits().entrySet()) {
            list.add(entry.getKey());
        }
        return list;
    }

    public boolean isRemoteBranchOnCommitId(String i_CommitSha) {
        return m_MyMagit.isRemoteBranchOnCommitIdInCurrentCommitTree(i_CommitSha);
    }

    public void OnCheckoutNewRemoteTrackingBranchButton(String i_SelectedNewBranchName , String i_SelectedCommitSha1) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/newBrunch/selectRemoteBranchToTrackAfter.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Branch Type");
            m_NewBrunchController = fxmlLoader.getController();
            m_NewBrunchController.setM_MainController(this);
            m_NewBrunchController.setM_MyStage(stage);
            m_NewBrunchController.setM_NewBrunchName(i_SelectedNewBranchName);
            m_NewBrunchController.setChooseRemoteBranches(m_MyMagit.getAllRemoteBranchesOnThatCommit(i_SelectedCommitSha1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void OnCreateBranchAsRemoteTrackingOrRegular(String i_NewBranchName,String i_SelectedCommitSha1) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/newBrunch/branchTypeSelector.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Branch Type");
            m_NewBrunchController = fxmlLoader.getController();
            m_NewBrunchController.setM_MainController(this);
            m_NewBrunchController.setM_MyStage(stage);
            m_NewBrunchController.setM_CommitSha(i_SelectedCommitSha1);
            m_NewBrunchController.setM_NewBrunchName(i_NewBranchName);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTrackingAfterBranch(String i_NewBrunchName, String i_RemoteBranchToTrackAfter) {
        m_MyMagit.createTrackingAfterBranch(i_NewBrunchName,i_RemoteBranchToTrackAfter);
        try {
            m_MyMagit.createBrunch(i_NewBrunchName,m_MyMagit.getM_Repositories().getM_Branches().get(i_NewBrunchName).getM_PointedCommitId());
        } catch (IOException e) {
            Alerts.showAlertToUser(e.toString());
        }
        finally {
            this.InitCommitTree();
        }
    }

    public void updateActiveRepositoryLocation(String i_PathFromUser) {
        activeRepositoryLocationProperty.unbind();
        activeRepositoryLocationProperty.setValue(i_PathFromUser);
    }

    public boolean isRemoteBranchOnBranchSelectedName(String resultOfSubmit) {
        return m_MyMagit.getM_Repositories().getM_Branches().get(resultOfSubmit).getM_IsRemote();
    }

    public void OnCheckoutToRemoteBranch(String i_BranchNameToCheckout) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/components/newBrunch/checkOutToRemoteBranch.fxml");
            fxmlLoader.setLocation(url);
            BorderPane root = fxmlLoader.load(url.openStream());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Select option");
            m_NewBrunchController = fxmlLoader.getController();
            m_NewBrunchController.setM_MainController(this);
            m_NewBrunchController.setM_MyStage(stage);
            m_NewBrunchController.setM_CommitSha(m_MyMagit.getM_Repositories().getM_Branches().get(i_BranchNameToCheckout).getM_PointedCommitId());
            m_NewBrunchController.setM_RemoteBranchToTrackAfter(i_BranchNameToCheckout);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createTheSelectedCommitInformationTree(String i_SelectedCommitSha1) {
        TreeView<ViewMagitFile> tree = buildTreeViewOfCommitFiles(m_MyMagit.getM_Repositories().getM_Commits().get(i_SelectedCommitSha1));
        DisplayTreeView(tree);
    }

    private void DisplayTreeView(TreeView<ViewMagitFile> tree) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/commitInformation/borderPaneToDisplay.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_CommitInformationController = fxmlLoader.getController();
                m_CommitInformationController.setM_MainController(this);
                m_CommitInformationController.setM_MyStage(stage);
                m_CommitInformationController.setM_CommitInfromationTree(tree);
                m_CommitInformationController.ShowCommitInfromation();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private TreeView<ViewMagitFile> buildTreeViewOfCommitFiles(Commit commit) {
        TreeView<ViewMagitFile> treeView = new TreeView<>();
        Folder mainFolder = m_MyMagit.getM_Repositories().getM_Folders().get(commit.getM_RootFolderId());
        ViewMagitFile viewMagitFile = null;
        viewMagitFile = new ViewMagitFile(m_MyMagit.getM_Repositories().getM_Folders().get(commit.getM_RootFolderId()).toString(),m_MyMagit.extractRepositoryNameFromPath(m_MyMagit.getM_Repositories().getM_Location()));
        TreeItem<ViewMagitFile> root = new TreeItem<>(viewMagitFile);
        buildTreeViewOfCommitFilesRec(mainFolder, root);
        treeView.setRoot(root);

        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
            Node node = event.getPickResult().getIntersectedNode();
            if (node instanceof TreeCell) {
                if (event.getClickCount() == 2 && treeView.getSelectionModel().getSelectedItem() != null && (treeView.getSelectionModel().getSelectedItem()).isLeaf()) {
                    TextArea textArea = new TextArea(treeView.getSelectionModel().getSelectedItem().getValue().getM_Content());
                    textArea.setPrefSize(Region.USE_COMPUTED_SIZE,Region.USE_COMPUTED_SIZE);
                    textArea.editableProperty().setValue(false);
                    PopUpWindowWithBtn.popUpWindow(500, 400, "O.K", (v) -> {
                    }, new Object(), textArea);
                }
            }

        };

        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);

        return treeView;
    }

    private void buildTreeViewOfCommitFilesRec(Folder folder, TreeItem<ViewMagitFile> treeItem) {

        ImageView imageView = new ImageView();
        imageView.setFitHeight(20);
        imageView.setFitWidth(25);
        imageView.setImage(FOLDER_ICON);
        treeItem.setGraphic(imageView);
        Map<String,Folder.Item> itemsMap = folder.getM_Items();
        for(Map.Entry<String,Folder.Item> itemsInFolderEntry : itemsMap.entrySet()){
            Folder.Item currentItem = itemsInFolderEntry.getValue();
            if(currentItem.getM_Type().equals(Folder.Item.eItemType.FOLDER)){
                ViewMagitFile viewMagitFile = new ViewMagitFile(m_MyMagit.getM_Repositories().getM_Folders().get(itemsInFolderEntry.getKey()).toString(),currentItem.getM_Name());
                TreeItem<ViewMagitFile> subTreeItem = new TreeItem<>(viewMagitFile);
                treeItem.getChildren().add(subTreeItem);
                buildTreeViewOfCommitFilesRec(m_MyMagit.getM_Repositories().getM_Folders().get(itemsInFolderEntry.getKey()), subTreeItem);
            }
            else{
                ImageView imageView2 = new ImageView();
                imageView2.setFitHeight(25);
                imageView2.setFitWidth(20);
                imageView2.setImage(TEXT_ICON);
                ViewMagitFile viewMagitFile = new ViewMagitFile(m_MyMagit.getM_Repositories().getM_Blobs().get(itemsInFolderEntry.getKey()).getM_Content(), currentItem.getM_Name());
                TreeItem<ViewMagitFile> subTreeItem = new TreeItem<>(viewMagitFile);
                treeItem.getChildren().add(subTreeItem);
                subTreeItem.setGraphic(imageView2);
            }
        }
    }

    public void CreateBranchFromContextMenu(String  i_SelectedCommitId) {
        if (!isRepositoryLoaded()) {
            Alerts.showAlertToUser("Please load repository first.");
        } else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/newBrunch/newBrunchFromContextMenu.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_NewBrunchController = fxmlLoader.getController();
                m_NewBrunchController.setM_MainController(this);
                m_NewBrunchController.setM_MyStage(stage);
                m_NewBrunchController.setM_CommitSha(i_SelectedCommitId);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void DeleteBranchFromContextMenu(String i_SelectedCommitId) {
        if(m_MyMagit.isBranchPointingToThatCommit(i_SelectedCommitId)){
            try {
                ObservableList<String> brunchesToDelete = m_MyMagit.gatherAllBranchesOnCommit(i_SelectedCommitId);
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/deleteBrunch/deleteBrunch.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_DeleteBrunch = fxmlLoader.getController();
                m_DeleteBrunch.setM_MainController(this);
                m_DeleteBrunch.setM_MyStage(stage);
                m_DeleteBrunch.setShowBrunches(brunchesToDelete);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Alerts.showAlertToUser("No Branch on that commit.Can't Delete!");
        }

    }

    public void DisplayBranchesOnCommit(String i_SelectedCommitId){
        if(checkIfThereAreOpenChanges()){
            Alerts.showAlertToUser("The is open changes, can't merge brunches");
        }
        else {
            try {
                ObservableList<String> brunchesToMerge = m_MyMagit.gatherAllBranchesOnCommit(i_SelectedCommitId);
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource("/components/merge/selectedBrunchOnSpecificCommit.fxml");
                fxmlLoader.setLocation(url);
                BorderPane root = fxmlLoader.load(url.openStream());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                m_MergeBrunchController = fxmlLoader.getController();
                m_MergeBrunchController.setM_MainController(this);
                m_MergeBrunchController.setM_MyStage(stage);
                m_MergeBrunchController.setShowBrunches(brunchesToMerge);
                if (brunchesToMerge != null && !brunchesToMerge.isEmpty()) {
                    stage.show();
                } else {
                    Alerts.showAlertToUser("The is not brunch to this commit, can't merge");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void MergeFromContextMenu(String i_SelectedBrunchName) {
        if (i_SelectedBrunchName != null && !i_SelectedBrunchName.isEmpty()) {

            OnBrunchToMerge(i_SelectedBrunchName);
        }

    }
}



