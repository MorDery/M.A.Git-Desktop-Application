package LogicEngine;

import com.sun.org.apache.regexp.internal.REProgram;

import java.util.HashMap;
import java.util.Map;

public class Repository {

    private String m_Location;
    private String m_Name = null;
    private final Map<String,Blob> m_Blobs = new HashMap<>();
    private final Map<String,Folder> m_Folders = new HashMap<>();
    private final Map<String,Commit> m_Commits = new HashMap<>();
    private final Map<String,Branch> m_Branches = new HashMap<>();
    private String m_RemoteRepositoryLocation = null;


    public Repository(String i_Location,String i_Name) {
        m_Location = i_Location;
        m_Name = i_Name;
    }

    public Repository(String i_Location) {
        m_Location = i_Location;
    }

    public String getM_Location() {
        return m_Location;
    }

    public  Map<String,Blob>  getM_Blobs() {
        return m_Blobs;
    }

    public  Map<String,Folder>  getM_Folders() {
        return m_Folders;
    }

    public Map<String, Commit> getM_Commits() {
        return m_Commits;
    }

    public Map<String, Branch> getM_Branches() {
        return m_Branches;
    }

    public String getM_Name() {
        return m_Name;
    }

    public String getM_RemoteRepositoryLocation() {
        return m_RemoteRepositoryLocation;
    }

    public void setM_Name(String i_Name){
        m_Name = i_Name;
    }

    public void setM_Location(String m_Location) {
        this.m_Location = m_Location;
    }

    public void setM_RemoteRepository(String i_RemoteRepositoryLocation){
        m_RemoteRepositoryLocation = i_RemoteRepositoryLocation;
    }

    public static Repository CopyRepository(Repository i_RepositoryToCopy) {
        Repository cloneRepository = new Repository(i_RepositoryToCopy.getM_Location());
        cloneRepository.copyBranches(i_RepositoryToCopy);
        cloneRepository.copyCommits(i_RepositoryToCopy);
        cloneRepository.copyBlobs(i_RepositoryToCopy);
        cloneRepository.copyFolders(i_RepositoryToCopy);
        cloneRepository.setM_Name(i_RepositoryToCopy.getM_Name());
        return cloneRepository;
    }

    private void copyFolders(Repository i_RepositoryToCopy) {
        for(Map.Entry<String,Folder> folderEntry : i_RepositoryToCopy.getM_Folders().entrySet()) {
            Folder oldFolder = folderEntry.getValue();
            Folder folderToAdd = new Folder(oldFolder.isM_IsRoot());
            for(Map.Entry<String,Folder.Item> itemEntry : oldFolder.getM_Items().entrySet()){
                Folder.Item itemValue = itemEntry.getValue();
                Folder.Item newItemToAttach = new Folder.Item(itemValue.getM_Name(),itemValue.getM_Id(),itemValue.getM_Type(),itemValue.getLastUpdater(),itemValue.getLastUpdateDate());
                folderToAdd.getM_Items().put(itemEntry.getKey(),newItemToAttach);
            }
            m_Folders.put(folderEntry.getKey(),folderToAdd);
        }
    }

    private void copyBlobs(Repository i_RepositoryToCopy) {
        for(Map.Entry<String,Blob> bloblEntry : i_RepositoryToCopy.getM_Blobs().entrySet()){
            Blob blolbToAdd = new Blob(bloblEntry.getValue().getM_Content());
            this.getM_Blobs().put(bloblEntry.getKey(),blolbToAdd);
        }
    }

    private void copyCommits(Repository i_RepositoryToCopy) {
        for(Map.Entry<String,Commit> commitEntry : i_RepositoryToCopy.getM_Commits().entrySet()){
            Commit commitToCopy = commitEntry.getValue();
            Commit commitToAdd = new Commit(commitToCopy.getM_RootFolderId(),commitToCopy.getM_Message(),commitToCopy.getM_Author(),commitToCopy.getM_DateOfCreation());
            for(String precendingCommit : commitToCopy.getM_PrecedingCommitId()){
                commitToAdd.getM_PrecedingCommitId().add(precendingCommit);
            }
            this.getM_Commits().put(commitEntry.getKey(),commitToAdd);
        }
    }

    private void copyBranches(Repository i_RepositoryToCopy) {
        for(Map.Entry<String,Branch> branchEntry : i_RepositoryToCopy.getM_Branches().entrySet()){
            Branch branchToCopy = branchEntry.getValue();
            Branch branchToAdd = new Branch(branchToCopy.getM_Name(),branchToCopy.getM_PointedCommitId(),branchToCopy.getM_TrackingAfter(),branchToCopy.getM_IsRemote(),branchToCopy.getM_Tracking());
            if(branchToCopy.isM_IsHead()){
                branchToAdd.setM_IsHead(true);
            }
            this.getM_Branches().put(branchEntry.getKey(),branchToAdd);
        }
    }

}
