package components.loadRepositoryFromXML;

import LogicEngine.Creator;
import LogicEngine.MAGit;
import LogicEngine.Repository;
import LogicEngine.SchemaBasedJAXB;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class LoadRepositoryFromXMLTask extends Task<Boolean> {

    private String m_FullXMLPath;
    private String m_ResultMessage;
    private List<String> m_ErrorList = new ArrayList<>();
    private LoadRepositoryFromXMLController m_CurrentController;
    private Runnable onFinished;
    private Runnable onError;
    private SimpleBooleanProperty m_DeleteOrRemainInput = new SimpleBooleanProperty();


    public LoadRepositoryFromXMLTask(String i_FullXMLPath, Runnable i_OnError, LoadRepositoryFromXMLController i_ActiveController, Runnable i_OnFinished,SimpleBooleanProperty i_DeleteOrRemainInput) {
        m_FullXMLPath = i_FullXMLPath;
        onError =  i_OnError;
        m_CurrentController = i_ActiveController;
        onFinished = i_OnFinished;
        i_DeleteOrRemainInput.bind(m_DeleteOrRemainInput);
    }

    @Override
    protected Boolean call() throws Exception {
        SchemaBasedJAXB converterFromXML = new SchemaBasedJAXB(m_FullXMLPath);
        try {
            Repository newRepository = converterFromXML.deserializeFromXML();
            m_CurrentController.getMainAppFormController().getM_MyMagit().setM_Repositories(newRepository);
        } catch (FileNotFoundException e) {
            m_ResultMessage = "File wasn't found!Please try again!";
            updateMessage(m_ResultMessage);
            Platform.runLater(onError);
            return Boolean.FALSE;
        } catch (JAXBException e) {
            m_ResultMessage = "Can't use the Schema XSD to deserialize!Please try again!";
            updateMessage(m_ResultMessage);
            Platform.runLater(onError);
            return Boolean.FALSE;
        }

        boolean testerAnswer = checkIfAnswerIsValid();

        if (!testerAnswer) {
            //the xml isn't valid - let's print the errors
            m_ErrorList = converterFromXML.getM_XmlTester().getErrorMessage();
            updateMessage(attachErrorList());
            Platform.runLater(onError);
            return Boolean.FALSE;
        }
        else{
            MAGit currentMagit = m_CurrentController.getMainAppFormController().getM_MyMagit();
            boolean isExists = currentMagit.isRepositoryExists(currentMagit.getM_Repositories().getM_Location());
            if (!isExists) {
                //we need to create a repository based on the XML
                currentMagit.createRepository();
                Creator.createWC(currentMagit);
            }
            else{
                m_DeleteOrRemainInput.setValue(true);
                Platform.runLater(()->m_CurrentController.askTheUserProcess());
            }
            Platform.runLater(()->m_CurrentController.updateRepositoryLocationFromTask());
            Platform.runLater(onFinished);
            return Boolean.TRUE;
        }
    }

    private boolean checkIfAnswerIsValid() {
        if( m_CurrentController.getMainAppFormController().getM_MyMagit().getM_Repositories() == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private String attachErrorList() {
        StringBuilder sb = new StringBuilder();
        for(String str : m_ErrorList){
            sb.append(str + ".");
        }
        return sb.toString();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }
}
