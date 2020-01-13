package LogicEngine;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.nashorn.internal.ir.BreakableNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class MAGit {

    private String m_ActiveUserName;
    private Repository m_CurrentRepository;
    private SaverToXml m_SaveRepositoryToXML = null;

    public MAGit(String m_ActiveUserName, Repository i_CurrentRep) {
        this.m_ActiveUserName = m_ActiveUserName;
        m_CurrentRepository = i_CurrentRep;
    }

    public MAGit(String m_ActiveUserName) {
        this.m_ActiveUserName = m_ActiveUserName;
        m_CurrentRepository = null;
    }

    public String getM_ActiveUserName() {
        return m_ActiveUserName;
    }

    public void setM_ActiveUserName(String m_ActiveUserName) {
        this.m_ActiveUserName = m_ActiveUserName;
    }

    public void setM_CurrentRepository(Repository m_CurrentRepository) {
        this.m_CurrentRepository = m_CurrentRepository;
    }

    public Repository getM_Repositories() {
        return m_CurrentRepository;
    }

    public void setM_Repositories(Repository m_Repositories) {
        this.m_CurrentRepository = m_Repositories;
    }

    public void spanHeadBranchToOurObjects(String i_RepositoryFullPath) throws FileNotFoundException, IOException {

        File file = new File(i_RepositoryFullPath + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String activeBranchName = br.readLine();
        file = new File(i_RepositoryFullPath + "/.magit/branches/" + activeBranchName);
        br = new BufferedReader(new FileReader(file));
        String pointedCommitId = br.readLine();
        file = new File(i_RepositoryFullPath + "//.magit//RemoteRepository.txt");
        String remoteRepoLocation = "";
        if(file.exists()) {
            br = new BufferedReader(new FileReader(file));
            remoteRepoLocation = br.readLine();
        }
        br.close();

        if (m_CurrentRepository != null) {
            clearRepository();
        }
        m_CurrentRepository = new Repository(i_RepositoryFullPath);
        if(!remoteRepoLocation.isEmpty() && remoteRepoLocation != null && remoteRepoLocation != "null"){
            m_CurrentRepository.setM_RemoteRepository(remoteRepoLocation);
        }
        m_CurrentRepository.getM_Branches().put(activeBranchName, new Branch(activeBranchName, pointedCommitId, true));
        if(pointedCommitId != null) {
            spanCommitToOurObjects(i_RepositoryFullPath, pointedCommitId);
        }
        String nameRepository = extractRepositoryNameFromPath(i_RepositoryFullPath);
        this.getM_Repositories().setM_Name(nameRepository);
    }

    public void clearRepository() {
        m_CurrentRepository.getM_Blobs().clear();
        m_CurrentRepository.getM_Folders().clear();
        m_CurrentRepository.getM_Commits().clear();
        m_CurrentRepository.getM_Branches().clear();
        m_CurrentRepository.setM_Name("N/A");
        m_CurrentRepository.setM_Location("N/A");
    }

    public void spanCommitToOurObjects(String i_RepositoryFullPath, String i_CommitId) throws FileNotFoundException, IOException {

        String commitContent = FileZipper.unZip(i_RepositoryFullPath + "/.magit/objects/" + i_CommitId);
        String[] splittedCommitContent = commitContent.split(";");
        String rootFolderId = splittedCommitContent[0];
        String precedingCommitId = splittedCommitContent[1];
        String message = splittedCommitContent[2];
        String author = splittedCommitContent[3];
        String dateOfCreation = splittedCommitContent[4].trim();

        m_CurrentRepository.getM_Commits().put(i_CommitId, new Commit(rootFolderId, message, author, dateOfCreation, precedingCommitId));

        m_CurrentRepository.getM_Folders().put(rootFolderId, new Folder(true));

        spanFolderToOurObjectsRec(m_CurrentRepository.getM_Folders().get(rootFolderId), i_RepositoryFullPath, rootFolderId);
    }

    private void spanFolderToOurObjectsRec(Folder i_currentFolder, String i_RepositoryFullPath, String i_FolderId) throws IOException {

        String folderContent = FileZipper.unZip(i_RepositoryFullPath + "/.magit/objects/" + i_FolderId);
        String[] lines = folderContent.split("\n");
        // add items to list
        for (int i = 0; i < lines.length-1; i++) {
            String[] splittedItemInFolderContent = lines[i].split(";");
            String name = splittedItemInFolderContent[0];
            String id = splittedItemInFolderContent[1];
            String type = splittedItemInFolderContent[2];
            String lastUpdater = splittedItemInFolderContent[3];
            String lastUpdateDate = splittedItemInFolderContent[4].trim();
            i_currentFolder.getM_Items().put(id, new Folder.Item(name, id, Folder.Item.eItemType.valueOf(type.toUpperCase()), lastUpdater, lastUpdateDate));
            if(type.equals("Folder")) {
                spanFolderToOurObjectsRec(i_currentFolder,i_RepositoryFullPath + "/.magit/objects/"+id,id);
            }
        }


        for (Folder.Item itemData : i_currentFolder.getM_Items().values()) {
            if (itemData.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Folder nextFolder = new Folder(false);
                m_CurrentRepository.getM_Folders().put(itemData.getM_Id(), nextFolder);
                spanFolderToOurObjectsRec(nextFolder, i_RepositoryFullPath, itemData.getM_Id());
            } else {
                spanBlobToOurObjects(i_RepositoryFullPath, itemData.getM_Id());
            }
        }
    }

    private void spanBlobToOurObjects(String i_RepositoryFullPath, String i_BlobId) throws IOException {
        String blobContent = FileZipper.unZip(i_RepositoryFullPath + "/.magit/objects/" + i_BlobId).trim();
        m_CurrentRepository.getM_Blobs().put(i_BlobId, new Blob(blobContent));
    }

    public boolean isRepositoryExists(String i_RepositoryFullPath) {
        File file = new File(i_RepositoryFullPath + "/.magit");
        return file.exists();
    }

    public void ShowCurrentCommitFilesFromRepository(List<String> i_ItemsToDisplay) throws IOException {
        String folderSHA = null;
        String commitSHA = null;
        Map<String, Folder.Item> items = null;
        Path generalPath = Paths.get(this.getM_Repositories().getM_Location());

        String headBranch = getHeadBranchFromRepository();
        commitSHA = m_CurrentRepository.getM_Branches().get(headBranch).getM_PointedCommitId();

        if (commitSHA != null && !(commitSHA.equals(""))) {
            folderSHA = this.getM_Repositories().getM_Commits().get(commitSHA).getM_RootFolderId();
        }

        if (folderSHA != null) {
            generalPath = Paths.get(generalPath + "/" + folderSHA);
            items = this.getM_Repositories().getM_Folders().get(folderSHA).getM_Items();
            if (items.size() != 0) {
                showItemsFromCommit(items, generalPath,i_ItemsToDisplay);
            }
        }
    }

    private void showItemsFromCommit(Map<String, Folder.Item> i_MyItems, Path i_FullPath,List<String> i_ItemsToDisplay) {

        for (Map.Entry<String, Folder.Item> entry : i_MyItems.entrySet()) {
            Folder.Item v = entry.getValue();
            if (v.getM_Type() == Folder.Item.eItemType.BLOB) {
                finAndPrintBlob(v, Paths.get(i_FullPath + "/" + v.getM_Name()),i_ItemsToDisplay);
            }
            else {
                findFolder(v, Paths.get(i_FullPath + "/" + v.getM_Name()),i_ItemsToDisplay);
            }
        }
    }

    private void finAndPrintBlob(Folder.Item i_BlobItem, Path i_FullPath,List<String> i_ItemsToDisplay) {
        Blob blobToFind = this.getM_Repositories().getM_Blobs().get(i_BlobItem.getM_Id());

            if (blobToFind != null) {
                i_ItemsToDisplay.add(i_FullPath.toString());
                i_ItemsToDisplay.add("Blob");
                i_ItemsToDisplay.add(i_BlobItem.getM_Id());
                i_ItemsToDisplay.add(i_BlobItem.getLastUpdater());
                i_ItemsToDisplay.add(i_BlobItem.getLastUpdateDate());
            }
        }

    private void findFolder(Folder.Item i_FolderItem, Path i_FullPath,List<String> i_ItemsToDisplay) {
        Folder folderToFind = this.getM_Repositories().getM_Folders().get(i_FolderItem.getM_Id());

        if (folderToFind != null) {
            i_ItemsToDisplay.add(i_FullPath.toString());
            i_ItemsToDisplay.add("Folder");
            i_ItemsToDisplay.add(i_FolderItem.getM_Id());
            i_ItemsToDisplay.add(i_FolderItem.getLastUpdater());
            i_ItemsToDisplay.add(i_FolderItem.getLastUpdateDate());
            showItemsFromCommit(folderToFind.getM_Items(), i_FullPath, i_ItemsToDisplay);
        }
    }

    public String getHeadBranchFromRepository() throws IOException {
        File file = new File(m_CurrentRepository.getM_Location() + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String activeBranchName = br.readLine();
        file = null ;
        br.close();
        return activeBranchName;
    }

    public String getHeadBranchFromRepository(String i_RepositoryFullPath) throws IOException {
        File file = new File(i_RepositoryFullPath + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String activeBranchName = br.readLine();
        file = null ;
        br.close();
        return activeBranchName;
    }

    public void readAllBranches(List<String> i_BranchesToPrint) throws IOException,NullPointerException {
        BufferedReader br = null;
        String pathBranches = (this.getM_Repositories().getM_Location() + "/.magit/branches");
        File folder = new File(pathBranches);
        File headFolder = new File(this.getM_Repositories().getM_Location() + "/.magit/branches/Head.txt");
        br = new BufferedReader(new FileReader(headFolder));
        String activeBranchName = br.readLine();
        br.close();
        if(activeBranchName == null)
        {
            throw new NullPointerException("No branches available on this repository!");
        }

        for (File fileEntry : folder.listFiles()) {
            if (!(fileEntry.getName().toLowerCase().equals("head.txt")) && !(fileEntry.isDirectory()) ) {
                if (activeBranchName.equals(fileEntry.getName())) {
                    i_BranchesToPrint.add("That's the head active branch: ");
                }
                addBranchInformationToPrint(i_BranchesToPrint,fileEntry,false);
            }
            else if(fileEntry.isDirectory()){
                for(File remoteRepoFileEntry : fileEntry.listFiles()){
                    addBranchInformationToPrint(i_BranchesToPrint,remoteRepoFileEntry,true);
                }
            }
        }
    }

    private void addBranchInformationToPrint(List<String> i_BranchesToPrint,File i_FileEntry,boolean i_IsRemoteFile) throws IOException {
        String noCommitMessge = "no commit in the brunch";
        String pointedCommitSha1 = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(i_FileEntry));
        if(i_IsRemoteFile){
            sb.append("Remote Branch: ");
        }
        sb.append(i_FileEntry.getName());
        i_BranchesToPrint.add(sb.toString());
        pointedCommitSha1 = br.readLine();
        if (pointedCommitSha1 != null && !(pointedCommitSha1.equals(""))) {
            br.close();
            i_BranchesToPrint.add(pointedCommitSha1);
            String commitContent = FileZipper.unZip(this.getM_Repositories().getM_Location() + "/.magit/objects/" + pointedCommitSha1);
            String[] splittedCommitContent = commitContent.split(";");
            String message = splittedCommitContent[2];
            i_BranchesToPrint.add(message);
        }
        else{
            br.close();
            i_BranchesToPrint.add(noCommitMessge);
        }
    }

    public void createBrunch(String i_NewBranchToAdd,String i_CommitSha) throws IOException,NullPointerException {
        String commitSHA = null;
        String brunchName = i_NewBranchToAdd;
        if(i_CommitSha == null) {
            commitSHA = m_CurrentRepository.getM_Branches().get(getHeadBranchFromRepository()).getM_PointedCommitId();
        }
        else{
            commitSHA = i_CommitSha;
        }

        if (commitSHA != null && !(commitSHA.equals(""))) {
            Branch newBranch = new Branch(brunchName, commitSHA, false);
            m_CurrentRepository.getM_Branches().put(brunchName, newBranch);
            Files.write(Paths.get(this.getM_Repositories().getM_Location() + "/.magit/branches/" + brunchName), newBranch.getM_PointedCommitId().getBytes());
        }
        else {
            throw new NullPointerException("No commits in the repository! please try again!");
        }
    }

    public void deleteBrunch(String i_BranchToDelete) throws IOException,NullPointerException {
        if(getM_Repositories().getM_Branches() == null)
        {
            throw new NullPointerException("No branches available on this repository!");
        }
        File folder = new File(this.getM_Repositories().getM_Location() + "/.magit/branches/" + i_BranchToDelete);
        if(folder.exists()) {
            if(isHeadBranch(i_BranchToDelete)) {
                System.out.println("this branch is head brunch,can't delete it");
                return;
            }
            if(isLoadedToBranchObejcts(i_BranchToDelete)) {
                m_CurrentRepository.getM_Branches().remove(i_BranchToDelete);
            }
                deleteBranch(i_BranchToDelete);
        }
        else
        {
            throw new FileNotFoundException("Branch can't be found on this repository!");
        }
    }

    public List<String> showActiveBranchHistory() throws IOException {
        String activeBranchName = this.getHeadBranchFromRepository();
        Branch activeBranch = this.m_CurrentRepository.getM_Branches().get(activeBranchName);
        List<String> formatToPrint = new ArrayList<>();
        showCommitsHistoryRec(activeBranch.getM_PointedCommitId(), formatToPrint);
        return formatToPrint;
    }

    private void showCommitsHistoryRec(String i_CommitId, List<String> i_FormatToPrint) {
        String[] commitContent = commitDetailsFormatGetter(this.m_CurrentRepository.getM_Location(), i_CommitId);
        if (commitContent[1] == null || commitContent[1].isEmpty()) {
            i_FormatToPrint.add(i_CommitId);
            addToList(i_FormatToPrint,commitContent);
        }
        else {
            i_FormatToPrint.add(i_CommitId);
            addToList(i_FormatToPrint,commitContent);
            showCommitsHistoryRec(commitContent[1], i_FormatToPrint);
        }
    }

    private String[] commitDetailsFormatGetter(String i_RepositoryFullPath, String i_CommitId) {
        String commitContent = FileZipper.unZip(i_RepositoryFullPath + "/.magit/objects/" + i_CommitId);
        String[] splittedCommitContent = commitContent.split(";");
        return splittedCommitContent;
    }

    private void addToList(List<String> i_OutPutList, String[] i_ContentToAdd){
        i_OutPutList.add(i_ContentToAdd[2]);
        i_OutPutList.add(i_ContentToAdd[3]);
        i_OutPutList.add(i_ContentToAdd[4]);
    }

    public void showWorkingCopyStatus(List<String> i_ChangeFile,List<String> i_DeleteFile,List<String> i_CreateFile) throws IOException,NullPointerException {
        try {
            if( !(this.m_CurrentRepository == null)) {
                changesInCurrentWC(Paths.get(this.m_CurrentRepository.getM_Location() + "/WC"), i_ChangeFile, i_DeleteFile, i_CreateFile);
            }
            else
            {
                throw new NullPointerException("No repository is loaded!Can't show the status!");
            }
        } catch (IOException e) {
            throw new IOException("Can't find the repository path!Please try again.");
        }
    }

    public void changesInCurrentWC(Path i_WcFullPath,List<String> i_ChangeFile,List<String> i_DeleteFile,List<String> i_CreateFile) throws IOException {
        List<String> changeFile = new ArrayList<>();
        List<String> deleteFile = new ArrayList<>();
        List<String> createFile = new ArrayList<>();
        String commitSHA = null;
        String headFolderSHA = null;

        File folder = new File(i_WcFullPath.toString());

        for (Map.Entry<String, Branch> branch : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = branch.getValue();
            if (v.isM_IsHead()) {
                commitSHA = v.getM_PointedCommitId();
                break;
            }
        }
        if (commitSHA != null && !(commitSHA.equals(""))) {
            headFolderSHA = this.getM_Repositories().getM_Commits().get(commitSHA).getM_RootFolderId();
            Folder headFolder = this.getM_Repositories().getM_Folders().get(headFolderSHA);
            Map<String, Folder.Item> itemHeadFolder = headFolder.getM_ItemsClone();
            checkItems(folder.listFiles(),itemHeadFolder,changeFile,deleteFile,createFile);
        }
        else{
            newFolderInWC(folder.listFiles(),i_CreateFile);
        }



        copyListToOther(changeFile,i_ChangeFile);
        copyListToOther(deleteFile,i_DeleteFile);
        copyListToOther(createFile,i_CreateFile);

    }

    private void checkItems(File[] i_FileList, Map<String, Folder.Item> i_Items, List<String> i_ChangeFile, List<String> i_DeleteFile, List<String> i_CreateFile) throws IOException {
        boolean isExist = false;
        for (File fileEntry : i_FileList) {
            for (Map.Entry<String, Folder.Item> entry : i_Items.entrySet()) {
                String k = entry.getKey();
                Folder.Item v = entry.getValue();
                if (v.getM_Name().equals(fileEntry.getName())) {
                    isExist = true;
                    if (v.getM_Type() == Folder.Item.eItemType.FOLDER) {
                        Folder checkFolder = this.getM_Repositories().getM_Folders().get(k);
                        checkItems(fileEntry.listFiles(),checkFolder.getM_ItemsClone(), i_ChangeFile,i_DeleteFile,i_CreateFile);
                    } else {
                        byte[] fileContent = Files.readAllBytes(Paths.get(fileEntry.getPath()));
                        Blob checkBlob = this.getM_Repositories().getM_Blobs().get(k);

                        if(!(new String (fileContent).trim().equals(checkBlob.getM_Content().trim()))){
                            i_ChangeFile.add(v.getM_Name());
                        }
                    }
                    i_Items.remove(k);
                    break;
                }
            }
            if (!isExist) {
                i_CreateFile.add(fileEntry.getName());
                if(!fileEntry.isFile()){
                    newFolderInWC(fileEntry.listFiles(),i_CreateFile);
                }

            }
            isExist = false;
        }


        if(i_Items.size()>0){
            i_Items.forEach((k, v) ->
            {
                i_DeleteFile.add(v.getM_Name());
                if(v.getM_Type() == Folder.Item.eItemType.FOLDER){
                    Map<String, Folder.Item> deleteFoldeItems = this.getM_Repositories().getM_Folders().get(k).getM_Items();
                    deleteFolderInWc(deleteFoldeItems,i_DeleteFile);
                }
            });
        }

    }

    public void deleteFolderInWc(Map<String, Folder.Item> i_DeleteFolderItems,  List<String> i_DeleteFile) {

        i_DeleteFolderItems.forEach((k, v) ->
        {
            i_DeleteFile.add(v.getM_Name());
            if (v.getM_Type() == Folder.Item.eItemType.FOLDER) {
                Map<String, Folder.Item> deleteFoldeItems = this.getM_Repositories().getM_Folders().get(k).getM_Items();
                deleteFolderInWc(deleteFoldeItems, i_DeleteFile);
            }
        });

    }

    public void newFolderInWC(File [] i_File, List<String> i_CreateFile){
        for(File fileEntry : i_File){
            i_CreateFile.add(fileEntry.getName());
            if(!fileEntry.isFile()){
                newFolderInWC(fileEntry.listFiles(),i_CreateFile);
            }
        }

    }

    public List<String> getAllBranchesToString(){
        List<String> branchesNamesList = new ArrayList<>();
        String pathBranches = (this.getM_Repositories().getM_Location() + "/.magit/branches");
        File folder = new File(pathBranches);

        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!(fileEntry.getName().toLowerCase().equals("head.txt"))) {
                branchesNamesList.add(fileEntry.getName());
                }
            }
        return branchesNamesList;
    }

    public void spanCheckoutBranchToOurObjects(String i_RepositoryFullPath,String i_SelectedBranch) throws FileNotFoundException, IOException {

        File file = new File(i_RepositoryFullPath + "/.magit/branches/" + i_SelectedBranch);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String pointedCommitId = br.readLine();
        br.close();

        if (m_CurrentRepository != null) {
            clearRepository();
        }
        m_CurrentRepository = new Repository(i_RepositoryFullPath);

        m_CurrentRepository.getM_Branches().put(i_SelectedBranch, new Branch(i_SelectedBranch, pointedCommitId, true));
        spanCommitToOurObjects(i_RepositoryFullPath, pointedCommitId);
    }

    public boolean checkIfThereAreOpenChanges(){
        boolean resultOfTest = false;
        try {
            resultOfTest = openChangesBeforeCheckout(Paths.get(this.m_CurrentRepository.getM_Location()+"/WC"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultOfTest;
    }

    public boolean openChangesBeforeCheckout(Path i_WcFullPath) throws IOException {

        boolean isExist = false;
        boolean isOpenChanges = false;
        String commitSHA = null;
        String headFolderSHA = null;

        BufferedReader br = null;
        List<String> changeFile = new ArrayList<>();
        List<String> deleteFile = new ArrayList<>();
        List<String> createFile = new ArrayList<>();

        File folder = new File(i_WcFullPath.toString());
        for (Map.Entry<String, Branch> branch : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = branch.getValue();
            if (v.isM_IsHead()) {
                commitSHA = v.getM_PointedCommitId();
                break;
            }
        }

        if (commitSHA != null && !(commitSHA.equals(""))) {
            headFolderSHA = this.getM_Repositories().getM_Commits().get(commitSHA).getM_RootFolderId();
            Folder headFolder = this.getM_Repositories().getM_Folders().get(headFolderSHA);
            Map<String, Folder.Item> itemHeadFolder = headFolder.getM_ItemsClone();
            checkItems(folder.listFiles(),itemHeadFolder,changeFile,deleteFile,createFile);
        }
        else{
            newFolderInWC(folder.listFiles(),createFile);
        }


        if(changeFile.size()>0 || deleteFile.size()>0 || createFile.size()>0)
        {
            isOpenChanges = true;
        }
        return isOpenChanges;
    }

    private void copyListToOther(List<String> copyFrom , List<String> copyTo) {
        for(String str : copyFrom)
        {
            copyTo.add(str);
        }
    }

    public void createCommit() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the commit description:");
        String messege = scanner.nextLine();
        String author = this.m_ActiveUserName;
        String dateOfCreation = formatter.format(new Date());

        String prevCommitId = null;
        String headBrunch = null;
        String headFolder = null;

        for (Map.Entry<String, Branch> branch : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = branch.getValue();
            if (v.isM_IsHead()) {
                prevCommitId = v.getM_PointedCommitId();
                headBrunch = v.getM_Name();
                break;
            }
        }

        try {
            headFolder = creatHeadFolderFromWC();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (headFolder == null && prevCommitId !=null) {
            headFolder = this.m_CurrentRepository.getM_Commits().get(prevCommitId).getM_RootFolderId();
        }
        if(headFolder == null && prevCommitId == null){
            File folder = new File(this.m_CurrentRepository.getM_Location()+"/WC");
            try {
                Folder head = new Folder(true);
                creatNewHeadFolder(folder.listFiles(),head);
                headFolder = DigestUtils.sha1Hex(head.toString());
                FileTime date = Files.getLastModifiedTime(folder.toPath());
                this.getM_Repositories().getM_Folders().put(headFolder, head);
                Folder.Item newItem = new Folder.Item(folder.getName(), headFolder, Folder.Item.eItemType.FOLDER, this.m_ActiveUserName, formatter.format(date.toMillis()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Commit newCommit = new Commit(headFolder,messege,author,dateOfCreation,prevCommitId);
        String newCommitId = DigestUtils.sha1Hex(newCommit.ToStringForSha1());
        this.getM_Repositories().getM_Commits().put(newCommitId,newCommit);
        this.getM_Repositories().getM_Branches().get(headBrunch).setM_PointedCommitId(newCommitId);
        try {
            Deleter.deleteDir(this.m_CurrentRepository.getM_Location());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.createRepository();
        Creator.createWC(this);
    }

    public String creatHeadFolderFromWC() throws IOException {
      String headFolderId = null;
      String commitSHA = null;
      String headFolderSha;
        File folder = new File(this.m_CurrentRepository.getM_Location()+"/WC");
        for (Map.Entry<String, Branch> branch : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = branch.getValue();
            if (v.isM_IsHead()) {
                commitSHA = v.getM_PointedCommitId();
                break;
            }
        }
        if (commitSHA != null && !commitSHA.isEmpty()) {
            headFolderSha = this.getM_Repositories().getM_Commits().get(commitSHA).getM_RootFolderId();
            if(headFolderSha!= null && !headFolderSha.isEmpty()) {
                Folder headFolder = this.getM_Repositories().getM_Folders().get(headFolderSha);
                Map<String, Folder.Item> itemHeadFolder = headFolder.getM_ItemsClone();
                Folder newHeadFolder = updateRepostory(folder.listFiles(), itemHeadFolder);
                if (newHeadFolder != null) {
                    headFolderId = DigestUtils.sha1Hex(newHeadFolder.toString());
                    this.getM_Repositories().getM_Folders().put(headFolderId, newHeadFolder);
                }
            }

        }
      return headFolderId;
    }

    private Folder updateRepostory(File[] i_FileList, Map<String, Folder.Item> i_Items) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        boolean isExist = false;
        String sha1;
        Blob newBlob;
        FileTime date;
        Folder.Item newItem = null;
        Folder newFolder = null;
        byte[] fileContent;
        for (File fileEntry : i_FileList) {
            for (Map.Entry<String, Folder.Item> entry : i_Items.entrySet()) {
                String k = entry.getKey();
                Folder.Item v = entry.getValue();
                if (v.getM_Name().equals(fileEntry.getName())) {
                    isExist = true;
                    if (v.getM_Type() == Folder.Item.eItemType.FOLDER) {
                        Folder checkFolder = this.getM_Repositories().getM_Folders().get(k);
                        Folder newInsideFolder = updateRepostory(fileEntry.listFiles(),checkFolder.getM_Items());
                        if(newInsideFolder != null){
                            sha1 = DigestUtils.sha1Hex(newInsideFolder.toString());
                            date = Files.getLastModifiedTime(fileEntry.toPath());
                            this.getM_Repositories().getM_Folders().put(sha1, newInsideFolder);
                            newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.FOLDER, this.m_ActiveUserName, formatter.format(date.toMillis()));
                        }
                        else{
                            newItem = v;
                        }
                        if (newFolder == null) {
                            newFolder = new Folder(false);
                        }
                        newFolder.getM_Items().put(newItem.getM_Id(), newItem);
                    } else {
                        fileContent = Files.readAllBytes(Paths.get(fileEntry.getPath()));
                        Blob checkBlob = this.getM_Repositories().getM_Blobs().get(k);

                        if (!(Arrays.equals(fileContent , checkBlob.getM_Content().getBytes()))){
                            sha1 = DigestUtils.sha1Hex(fileContent);
                            newBlob = new Blob(new String(fileContent));
                            date = Files.getLastModifiedTime(fileEntry.toPath());
                            this.getM_Repositories().getM_Blobs().put(sha1,newBlob);
                            newItem = new Folder.Item(fileEntry.getName(),sha1, Folder.Item.eItemType.BLOB,this.m_ActiveUserName,formatter.format(date.toMillis()));
                            if(newFolder == null){
                                newFolder = new Folder(false);
                            }
                            newFolder.getM_Items().put(newItem.getM_Id(),newItem);
                        }
                    }
                    break;
                }
            }
            if (!isExist) {
                if (fileEntry.isFile()) {
                    fileContent = Files.readAllBytes(Paths.get(fileEntry.getPath()));
                    sha1 = DigestUtils.sha1Hex(new String (fileContent));
                    newBlob = new Blob(new String(fileContent));
                    date = Files.getLastModifiedTime(fileEntry.toPath());
                    this.getM_Repositories().getM_Blobs().put(sha1, newBlob);
                    newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.BLOB, this.m_ActiveUserName, formatter.format(date.toMillis()));
                }
                else{
                    Folder newInsideFolder = creatNewInsideFolder(fileEntry.listFiles());
                    if(newInsideFolder!= null) {
                        sha1 = DigestUtils.sha1Hex(newInsideFolder.toString());
                        date = Files.getLastModifiedTime(fileEntry.toPath());
                        this.getM_Repositories().getM_Folders().put(sha1, newInsideFolder);
                        newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.FOLDER, this.m_ActiveUserName, formatter.format(date.toMillis()));
                    }

                }
                if (newFolder == null) {
                    newFolder = new Folder(false);
                }
                if(newItem != null) {
                    newFolder.getM_Items().put(newItem.getM_Id(), newItem);
                }
            }

            isExist = false;
        }

        if(newFolder != null)
        {
            boolean toAdd = true;
            for (File fileEntry : i_FileList) {
                toAdd = true;
                for (Map.Entry<String, Folder.Item> entry : newFolder.getM_Items().entrySet()) {
                    Folder.Item v = entry.getValue();
                    if ((fileEntry.getName().equals(v.getM_Name()))) {
                        toAdd = false;
                    }
                }

                if (toAdd) {

                    for (Map.Entry<String, Folder.Item> e : i_Items.entrySet()) {
                        String key = e.getKey();
                        Folder.Item valueToAdd = e.getValue();
                        Folder.Item value = e.getValue();
                        if (fileEntry.getName().equals(valueToAdd.getM_Name())) {
                            newFolder.getM_Items().put(key, value);
                        }

                    }


                }
            }
        }

        return newFolder;
    }

    public Folder creatNewInsideFolder(File[] i_Files) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        String sha1;
        Blob newBlob;
        FileTime date;
        Folder.Item newItem;
        byte[] fileContent;
        Folder newFolder= null;
        for (File fileEntry : i_Files) {
            if (fileEntry.isFile()) {
                fileContent = Files.readAllBytes(Paths.get(fileEntry.getPath()));
                sha1 = DigestUtils.sha1Hex(fileContent);
                newBlob = new Blob(new String(fileContent));
                date = Files.getLastModifiedTime(fileEntry.toPath());
                this.getM_Repositories().getM_Blobs().put(sha1, newBlob);
                newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.BLOB, this.m_ActiveUserName, formatter.format(date.toMillis()));
                if (newFolder == null) {
                    newFolder = new Folder(false);
                }
                newFolder.getM_Items().put(newItem.getM_Id(), newItem);
            }
            else{
                Folder newInsideFolder = creatNewInsideFolder(fileEntry.listFiles());
            }
        }
        return newFolder;
    }

    public void creatNewHeadFolder(File[] i_Files, Folder i_Folder) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        String sha1;
        Blob newBlob;
        FileTime date;
        Folder.Item newItem;
        byte[] fileContent;
        for (File fileEntry : i_Files) {
            if (fileEntry.isFile()) {
                fileContent = Files.readAllBytes(Paths.get(fileEntry.getPath()));
                sha1 = DigestUtils.sha1Hex(fileContent);
                newBlob = new Blob(new String(fileContent));
                date = Files.getLastModifiedTime(fileEntry.toPath());
                this.getM_Repositories().getM_Blobs().put(sha1, newBlob);
                newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.BLOB, this.m_ActiveUserName, formatter.format(date.toMillis()));
                i_Folder.getM_Items().put(newItem.getM_Id(), newItem);
            }
            else{
                Folder newInsideFolder = new Folder(false);
                creatNewHeadFolder(fileEntry.listFiles(),newInsideFolder);
                sha1 = DigestUtils.sha1Hex(newInsideFolder.toString());
                date = Files.getLastModifiedTime(fileEntry.toPath());
                this.getM_Repositories().getM_Folders().put(sha1, newInsideFolder);
                newItem = new Folder.Item(fileEntry.getName(), sha1, Folder.Item.eItemType.FOLDER, this.m_ActiveUserName, formatter.format(date.toMillis()));

            }
        }

    }

    public  void createRepository() {
        String pathObjects = ".magit/objects";
        String pathBranches = ".magit/branches";
        String pathMagit = ".magit";
        String pathRemoteRepository = null ;
        Path objectsPath = Paths.get(this.getM_Repositories().getM_Location() + "/" + pathObjects);
        Path branchesPath = Paths.get(this.getM_Repositories().getM_Location() + "/" + pathBranches);
        Path magitPath = Paths.get(this.getM_Repositories().getM_Location() + "/" + pathMagit);
        Path remoteRepositoryBranchesPath = null;
        boolean isRemoteRepository = false ;

        if(this.getM_Repositories().getM_RemoteRepositoryLocation() != null){
            isRemoteRepository = true ;
            pathRemoteRepository = extractRepositoryNameFromPath(this.m_CurrentRepository.getM_RemoteRepositoryLocation());
            remoteRepositoryBranchesPath = Paths.get( branchesPath + "/" + pathRemoteRepository);
        }

        try {
            Files.createDirectories(objectsPath);
            Files.createDirectories(branchesPath);
            if(isRemoteRepository){
                Files.createDirectories(remoteRepositoryBranchesPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.getM_Repositories().getM_Blobs().forEach((k, v) ->
        {
            FileZipper.Zip(Paths.get(objectsPath + "/" + k), Writer.writeBlob(v));
        });

        this.getM_Repositories().getM_Folders().forEach((k, v) ->
        {
            FileZipper.Zip(Paths.get(objectsPath + "/" + k), Writer.writeFolder(v));
        });

        this.getM_Repositories().getM_Commits().forEach((k, v) ->
        {
            FileZipper.Zip(Paths.get(objectsPath + "/" + k), Writer.writeCommit(v));
        });
        boolean finalIsRemoteRepository = isRemoteRepository;
        Path finalRemoteRepositoryBranchesPath = remoteRepositoryBranchesPath;
        this.getM_Repositories().getM_Branches().forEach((k, v) ->
        {
            try {
                if(v.isM_IsHead()) {
                    Files.write(Paths.get(branchesPath + "/" + "Head.txt"), k.getBytes());
                }
                try {
                    Files.write(Paths.get(branchesPath + "/" + k), v.getM_PointedCommitId().getBytes());
                }
                catch (Exception ex){
                    if(finalIsRemoteRepository){
                        Files.write(Paths.get(finalRemoteRepositoryBranchesPath + "/" + extractNameFromGeneralString(k)), v.getM_PointedCommitId().getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try{
            if(finalIsRemoteRepository){
                Files.write(Paths.get(magitPath + "/" + "RemoteRepository.txt"), this.m_CurrentRepository.getM_RemoteRepositoryLocation().getBytes());
            }
        }
         catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initializeNewRepository(String i_LocationOfrepository,String i_RepositoryName) throws Exception {

        String repositoryLocation = i_LocationOfrepository;
        Path repositoryPath = Paths.get(repositoryLocation + "/" + i_RepositoryName);
        if(Files.exists(repositoryPath))
        {
            throw new Exception("There is another file on that Location with identical name!");
        }
        String magitFolder = ".magit";
        String pathObjects = "objects";
        String pathBranches = "branches";
        String workingCopy = "WC";
        String headFile = "Head.txt";
        Branch masterBranch = new Branch("master",null,true);
        Path magitPath = Paths.get(repositoryPath + "/" + magitFolder);
        Path workingCopyPath = Paths.get(repositoryPath + "/" + workingCopy);
        Path objectsPath = Paths.get(magitPath + "/" + pathObjects);
        Path branchesPath = Paths.get(magitPath + "/" + pathBranches);
        Path headFilePath = Paths.get(branchesPath + "/" + headFile);
        File masterFile = new File(Paths.get(String.valueOf(repositoryPath),
                ".magit", "branches", "master").toString());
        try {
            Files.createDirectories(repositoryPath);
            Files.createDirectories(magitPath);
            Files.createDirectories(workingCopyPath);
            Files.createDirectories(objectsPath);
            Files.createDirectories(branchesPath);
            Files.createFile(headFilePath);
            Writer.WriteToTextFile(headFilePath,masterBranch.getM_Name());
            masterFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkIfFileExistsInObjectsBySha1(String i_Sha1) throws Exception {
        if(m_CurrentRepository != null) {
            String objectsLocation = (this.m_CurrentRepository.getM_Location() + "/.magit/objects");
            Path obejctPath = Paths.get(objectsLocation + "/" + i_Sha1);
            if(!Files.exists(obejctPath))
            {
                throw new Exception("This Sha1 can't be found on objects folder!");
            }
        }
        else
        {
            throw new Exception("No repoistory is loaded!Therefore can't find this sha1 on objects!");
        }

    }

    public void resetActiveBranch(String i_NewCommitSha1) throws IOException {
        String activeBranchName = getHeadBranchFromRepository();
        writeNewSha1ToBranchFile(i_NewCommitSha1,activeBranchName);
        m_CurrentRepository.getM_Branches().get(activeBranchName).setM_PointedCommitId(i_NewCommitSha1);
        Deleter.deleteDir(m_CurrentRepository.getM_Location() + "//WC");
        Creator.createWC(this);
    }

    private  void writeNewSha1ToBranchFile(String i_NewCommitSha1,String i_ActiveBranchName) throws IOException {
        String branchesFile = (m_CurrentRepository.getM_Location() + "/.magit/branches/" + i_ActiveBranchName);
        Path activeBranchPath = Paths.get(branchesFile);
        Deleter.deleteTextFileContent(activeBranchPath);
        Writer.WriteToTextFile(activeBranchPath,i_NewCommitSha1);
    }

    public void changeTheActiveBranchActivity(String i_NewActiveBranchName) throws IOException {

        for(Map.Entry<String,Branch> entry: m_CurrentRepository.getM_Branches().entrySet())
        {
            entry.getValue().setM_IsHead(false);
        }
        File file = new File(m_CurrentRepository.getM_Location() + "/.magit/branches/" + i_NewActiveBranchName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String pointedCommitId = br.readLine();
        br.close();
        try {
            m_CurrentRepository.getM_Branches().get(i_NewActiveBranchName).setM_IsHead(true);
        }
        catch (Exception ex) //if the branch isn't loaded to our objects
        {
            Branch addBranch = new Branch(i_NewActiveBranchName,pointedCommitId,true);
            m_CurrentRepository.getM_Branches().put(i_NewActiveBranchName,addBranch);
            String [] commitContent = commitDetailsFormatGetter(m_CurrentRepository.getM_Location(),pointedCommitId);
            m_CurrentRepository.getM_Commits().put(pointedCommitId,Creator.createCommitFromSplittedCOntent(commitContent));
        }
        Path headFile = Paths.get(m_CurrentRepository.getM_Location() + "/.magit/branches/Head.txt");
        Deleter.deleteTextFileContent(headFile);
        Writer.WriteToTextFile(headFile,i_NewActiveBranchName);
    }

    public void isRepositoryNull() throws NullPointerException {
        if(this.getM_Repositories() == null)
            throw new NullPointerException("No repository is loaded!please try again!");
    }

    public boolean isThereAnyBranch() {
        boolean result = true;
        int size = this.getM_Repositories().getM_Branches().size();
        if (size == 0) {
            System.out.println("No branches available!");
            result = false ;
        }
        return result;
    }

    public void SaveRepositoryToXml(String i_XmlFullPath) throws IOException {
        spreadAllBranchesIntoOurObjects();
        m_SaveRepositoryToXML = new SaverToXml(m_CurrentRepository);
        m_SaveRepositoryToXML.SaveRepositoryToXml(i_XmlFullPath);
    }

    public void spreadAllBranchesIntoOurObjects() throws IOException {

        File file = new File(m_CurrentRepository.getM_Location() + "//.magit//branches");
        File[] files = file.listFiles();
        assert files != null;
        List<File> RRFolder = Arrays.stream(files).filter(File::isDirectory).collect(Collectors.toList());

        if (!RRFolder.isEmpty()) {
            File remoteFolder = RRFolder.get(0);
            File[] filesInRemoteFolder = remoteFolder.listFiles();
            assert filesInRemoteFolder != null;
            for (File f : filesInRemoteFolder) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String pointedCommitId = br.readLine();
                br.close();
                Branch remoteBranch = new Branch(remoteFolder.getName() + "\\" + f.getName(),pointedCommitId,null,true,false);
                m_CurrentRepository.getM_Branches().put(remoteBranch.getM_Name(), remoteBranch);
                if (pointedCommitId != null && !pointedCommitId.equals("")) {
                    spanCommitToOurObjects(m_CurrentRepository.getM_Location(), pointedCommitId);
                }
            }
        }

        BufferedReader br = null ;
        Branch branch = null ;
        List<String> branchesNames = Creator.getFileNamesOfFilesInAFolder(m_CurrentRepository.getM_Location() + "/.magit/branches");
        branchesNames.remove(branchesNames.indexOf("Head.txt"));

        for(String branchName: branchesNames) {
                file = new File(m_CurrentRepository.getM_Location() + "/.magit/branches/" + branchName);
                br = new BufferedReader(new FileReader(file));
                String pointedCommitId = br.readLine();
                br.close();

                if (!RRFolder.isEmpty()) {
                    File remoteFolder = RRFolder.get(0);
                    if (m_CurrentRepository.getM_Branches().containsKey(remoteFolder.getName() + "\\" + branchName)) {
                        branch = new Branch(branchName,pointedCommitId,remoteFolder.getName() + "\\" + branchName,false,true);
                    } else {
                         branch = new Branch(branchName,pointedCommitId,null,false,false);
                    }
                }
                else{
                    branch = new Branch(branchName,pointedCommitId,null,false,false);
                }

                m_CurrentRepository.getM_Branches().put(branchName, branch);
                if (pointedCommitId != null && !pointedCommitId.equals("")) {
                    spanCommitToOurObjects(m_CurrentRepository.getM_Location(), pointedCommitId);
                }
        }
        String headBranch = getHeadBranchFromRepository();
        m_CurrentRepository.getM_Branches().get(headBranch).setM_IsHead(true);
    }

    public void spanAllNonPointedCommitsToOurObjects(List<String> i_NonPointedCommitsSha1) throws IOException {
        for(String commitSha1 : i_NonPointedCommitsSha1) {
            addAndCreateCommitsHistoryRec(commitSha1);
        }
    }

    public void deleteBranch(String i_BranchNameToDelete) {
        File branchToDelete = new File(String.valueOf(Paths.get(m_CurrentRepository.getM_Location(),
                ".magit", "branches", i_BranchNameToDelete)));
        FileUtils.deleteQuietly(branchToDelete);
    }

    private boolean isHeadBranch(String i_OtherBranchName) throws IOException {
        return this.getHeadBranchFromRepository().equals(i_OtherBranchName);
    }

    private boolean isLoadedToBranchObejcts(String i_ValueToSearch){
        return m_CurrentRepository.getM_Branches().containsKey(i_ValueToSearch);
    }

    public String extractRepositoryNameFromPath(String i_RepositoryFullPath){
        String name = i_RepositoryFullPath;
        int indexName = name.lastIndexOf("/");
        if(indexName == -1){
            indexName = name.lastIndexOf("\\");
        }
        return name.substring(indexName + 1);
    }

    private void addAndCreateCommitsHistoryRec(String i_CommitId) throws IOException {
        String[] commitContent = commitDetailsFormatGetter(this.m_CurrentRepository.getM_Location(), i_CommitId);
        if (commitContent[1] == null || commitContent[1].isEmpty()) {
            spanCommitToOurObjects(this.m_CurrentRepository.getM_Location(),i_CommitId);
        }
        else {
            spanCommitToOurObjects(this.m_CurrentRepository.getM_Location(),i_CommitId);
            addAndCreateCommitsHistoryRec(commitContent[1]);
        }
    }

    public void createNewCommit(String i_Messege, String i_MergePervCommit) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:sss");
        String author = this.m_ActiveUserName;
        String dateOfCreation = formatter.format(new Date());

        String prevCommitId = null;
        String headBrunch = null;
        String headFolder = null;

        for (Map.Entry<String, Branch> branch : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = branch.getValue();
            if (v.isM_IsHead()) {
                prevCommitId = v.getM_PointedCommitId();
                headBrunch = v.getM_Name();
                break;
            }
        }

        try {
            headFolder = creatHeadFolderFromWC();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (headFolder == null && prevCommitId !=null && !prevCommitId.isEmpty()) {
            headFolder = this.m_CurrentRepository.getM_Commits().get(prevCommitId).getM_RootFolderId();
        }
        if(headFolder == null && prevCommitId == null){
            File folder = new File(this.m_CurrentRepository.getM_Location()+"/WC");
            try {
                Folder head = new Folder(true);
                creatNewHeadFolder(folder.listFiles(),head);
                headFolder = DigestUtils.sha1Hex(head.toString());
                FileTime date = Files.getLastModifiedTime(folder.toPath());
                this.getM_Repositories().getM_Folders().put(headFolder, head);
                Folder.Item newItem = new Folder.Item(folder.getName(), headFolder, Folder.Item.eItemType.FOLDER, this.m_ActiveUserName, formatter.format(date.toMillis()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Commit newCommit;
        if(i_MergePervCommit != null) {
            List<String> prevCommits = new ArrayList<>();
            prevCommits.add(prevCommitId);
            prevCommits.add(i_MergePervCommit);
             newCommit = new Commit(headFolder,i_Messege,author,dateOfCreation,prevCommits);
        }
        else {
             newCommit = new Commit(headFolder, i_Messege, author, dateOfCreation, prevCommitId);
        }
        String newCommitId = DigestUtils.sha1Hex(newCommit.ToStringForSha1());
        this.getM_Repositories().getM_Commits().put(newCommitId,newCommit);
        this.getM_Repositories().getM_Branches().get(headBrunch).setM_PointedCommitId(newCommitId);
        try {
            Deleter.deleteDir(this.m_CurrentRepository.getM_Location());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.createRepository();
        Creator.createWC(this);
    }

    public void LoadRepositoryToOurObjects(String i_RepositoryPath) throws Exception {
        try {
            if (this.isRepositoryExists(i_RepositoryPath)) {
                if (this.getM_Repositories() != null) {
                    this.clearRepository();
                }
                this.spanHeadBranchToOurObjects(i_RepositoryPath);
                this.spreadAllBranchesIntoOurObjects();
                this.findAndSpanAllLeavesInCommitLogicTree();
            }
        } catch (Exception e) {
            throw new Exception("Can't find repository on that location!");
        }
    }

    public void findAndSpanAllLeavesInCommitLogicTree(){
        Map<String, Commit> allPrecendingCommits = new HashMap<>();
        this.gatherAllPrecendingCommits(allPrecendingCommits);
        List<String> allTopLeavesCommitsSha1 = new ArrayList<>();
        this.gatherAllTheTopLeavesCommitsInTreeToList(allTopLeavesCommitsSha1, allPrecendingCommits);
        try {
            this.spanAllNonPointedCommitsToOurObjects(allTopLeavesCommitsSha1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gatherAllPrecendingCommits(Map<String, Commit> allPrecendingCommits) {
        for (Map.Entry<String, Commit> commitEntry : this.getM_Repositories().getM_Commits().entrySet()) {
            for (String str : commitEntry.getValue().getM_PrecedingCommitId()) {
                if (!allPrecendingCommits.containsKey(this.getM_Repositories().getM_Commits().get(str))) {
                    allPrecendingCommits.put(str, this.getM_Repositories().getM_Commits().get(str));
                }
            }
        }
    }

    public void gatherAllTheTopLeavesCommitsInTreeToMap(Map<String, Commit> allTopLeavesCommits, Map<String, Commit> allPrecendingCommits) {
        for (Map.Entry<String, Commit> commitEntry : this.getM_Repositories().getM_Commits().entrySet()) {
            if (!allPrecendingCommits.containsKey(commitEntry.getKey())) {
                allTopLeavesCommits.put(commitEntry.getKey(), this.getM_Repositories().getM_Commits().get(commitEntry.getKey()));
            }
        }
    }

    public void gatherAllTheTopLeavesCommitsInTreeToList(List<String> allTopLeavesCommitsSha1, Map<String, Commit> allPrecendingCommits) {
        for (Map.Entry<String, Commit> commitEntry : this.getM_Repositories().getM_Commits().entrySet()) {
            if (!allPrecendingCommits.containsKey(commitEntry.getKey())) {
                allTopLeavesCommitsSha1.add(commitEntry.getKey());
            }
        }
    }

    public String getDifferenceBetweenTwoCommits(String i_CurrentCommitId, String i_parentCommitId) {
        String result = "";

        List<Folder.Item> deletedFilesItems = new ArrayList<>();
        List<Folder.Item> addedFilesItems = new ArrayList<>();
        List<Folder.Item> changedFilesItems = new ArrayList<>();

        if(m_CurrentRepository.getM_Commits().containsKey(i_CurrentCommitId) && m_CurrentRepository.getM_Commits().containsKey(i_parentCommitId) ) {
            compareTwoCommits(i_CurrentCommitId, i_parentCommitId, deletedFilesItems, addedFilesItems, changedFilesItems);
            result += "Deleted files:\n" + getStringInfoFromItemDataList(deletedFilesItems);
            result += "Added files:\n" + getStringInfoFromItemDataList(addedFilesItems);
            result += "Changed files:\n" + getStringInfoFromItemDataList(changedFilesItems);
        }
        return result;
    }

    public void compareTwoCommits(String i_currentCommitId, String i_parentCommitId, List<Folder.Item> i_DeletedFilesItems,
                                  List<Folder.Item> i_AddedFilesItems, List<Folder.Item> i_ChangedFilesItems) {
        List<FileFullPathAndSha1> currentCommitDetails = getCommitFileDetails(i_currentCommitId);
        List<FileFullPathAndSha1> otherCommitDetails = getCommitFileDetails(i_parentCommitId);

        for (FileFullPathAndSha1 fileDetails : currentCommitDetails) {
            List<FileFullPathAndSha1> sameFiles = otherCommitDetails.stream().filter(v -> v.getFullPath().equals(fileDetails.getFullPath())).collect(Collectors.toList());
            if (sameFiles.isEmpty()) {
                i_AddedFilesItems.add(fileDetails.getItemData());
            } else {
                if (!sameFiles.get(0).getSha1().equals(fileDetails.getSha1())) {
                    i_ChangedFilesItems.add(fileDetails.getItemData());
                }
                otherCommitDetails.remove(sameFiles.get(0));
            }
        }

        for (FileFullPathAndSha1 fileDetailsInOtherList : otherCommitDetails) {
            i_DeletedFilesItems.add(fileDetailsInOtherList.getItemData());
        }
    }

    private List<FileFullPathAndSha1> getCommitFileDetails(String i_CommitSha1) {
        List<FileFullPathAndSha1> commitFileDetails = new ArrayList<>();
        Commit currentCommit = m_CurrentRepository.getM_Commits().get(i_CommitSha1);
        Folder rootFolder = m_CurrentRepository.getM_Folders().get(currentCommit.getM_RootFolderId());

        getCurrentFolderFilesDetailsRec(commitFileDetails, rootFolder, m_CurrentRepository.getM_Location());

        return commitFileDetails;
    }

    private void getCurrentFolderFilesDetailsRec(List<FileFullPathAndSha1> i_CommitFileDetails, Folder i_CurrentFolder, String i_FullPath) {
        List<Folder.Item> items = mapToList(i_CurrentFolder.getM_Items());
        Folder nextFolder;

        if (checkIfAllTypesInAFolderAreBlobs(items)) {
            for (Folder.Item itemData : items) {
                FileFullPathAndSha1 newFileDetails = new FileFullPathAndSha1(i_FullPath + "\\" + itemData.getM_Name(), itemData.getM_Id(), itemData);
                i_CommitFileDetails.add(newFileDetails);
            }
        } else {
            for (Folder.Item itemData : items) {
                if (itemData.getM_Type() == Folder.Item.eItemType.BLOB) {
                    FileFullPathAndSha1 newFileDetails = new FileFullPathAndSha1(i_FullPath + "\\" + itemData.getM_Name(), itemData.getM_Id(), itemData);
                    i_CommitFileDetails.add(newFileDetails);
                } else {
                    FileFullPathAndSha1 newFileDetails = new FileFullPathAndSha1(i_FullPath + "\\" + itemData.getM_Name(), itemData.getM_Id(), itemData);
                    i_CommitFileDetails.add(newFileDetails);
                    nextFolder = m_CurrentRepository.getM_Folders().get(itemData.getM_Id());
                    getCurrentFolderFilesDetailsRec(i_CommitFileDetails, nextFolder, i_FullPath + "\\" + itemData.getM_Name());
                }
            }
        }
    }

    private List<Folder.Item> mapToList(Map<String, Folder.Item> m_items) {
        List<Folder.Item> resultList = new ArrayList<>();

        for (Map.Entry<String, Folder.Item> entry : m_items.entrySet()) {
            resultList.add(entry.getValue());
        }
        return resultList;
    }

    private boolean checkIfAllTypesInAFolderAreBlobs(List<Folder.Item> i_Items) {
        boolean checkIfAllTypesAreBlobs = true;

        for (Folder.Item itemData : i_Items) {
            if (itemData.getM_Type() == Folder.Item.eItemType.FOLDER) {
                checkIfAllTypesAreBlobs = false;
            }
        }

        return checkIfAllTypesAreBlobs;
    }

    private String getStringInfoFromItemDataList(List<Folder.Item> i_ItemsData) {
        String itemsDataInfo = "";
        for (Folder.Item itemeData : i_ItemsData) {
            itemsDataInfo += itemeData.toStringForConsole();
        }

        return itemsDataInfo;
    }

    public void CloneRepository(String i_fullPathFromUser, String i_cloneFullPathFromUser,String i_CloneName) {
        String remoteRepositoryName = extractNameFromGeneralString(i_fullPathFromUser);
        this.SwitchRepository(i_fullPathFromUser);
        this.remoteBranchesChanger(remoteRepositoryName);
        this.addTrackingAfterBranches();
        this.m_CurrentRepository.setM_Location(i_cloneFullPathFromUser + "//" + i_CloneName);
        this.m_CurrentRepository.setM_Name(i_CloneName);
        this.m_CurrentRepository.setM_RemoteRepository(i_fullPathFromUser);
        this.createRepository();
        Creator.createWC(this);
    }

    public void SwitchRepository(String i_RepositoryFullPath){
        if (this.getM_Repositories() != null) {
            this.clearRepository();
        }
        try {
            this.spanHeadBranchToOurObjects(i_RepositoryFullPath);
            this.spreadAllBranchesIntoOurObjects();
            this.findAndSpanAllLeavesInCommitLogicTree();
            this.getM_Repositories().setM_Name(this.extractNameFromGeneralString(i_RepositoryFullPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void remoteBranchesChanger(String i_RepositoryName){

        List<Branch> remoteBranchesToAdd = new ArrayList<>();
        List<String> branchesToRemove = new ArrayList<>();
        for(Map.Entry<String,Branch> currentBranchEntryPoint : m_CurrentRepository.getM_Branches().entrySet()){
            branchesToRemove.add(currentBranchEntryPoint.getKey());
            Branch currentBranch = currentBranchEntryPoint.getValue();
            currentBranch.setM_IsRemote(true);
            currentBranch.setM_Name(i_RepositoryName + "/" +  currentBranch.getM_Name());
            remoteBranchesToAdd.add(currentBranch);
        }

        for(Branch currentBranch : remoteBranchesToAdd){
            m_CurrentRepository.getM_Branches().put(currentBranch.getM_Name(),currentBranch);
        }
        for(String currentBranchToRemove : branchesToRemove){
            m_CurrentRepository.getM_Branches().remove(currentBranchToRemove);
        }

    }

    private void addTrackingAfterBranches(){
        boolean isHead = false ;
        List<Branch> newBranchesToAdd = new ArrayList<>();
        for(Map.Entry<String,Branch> currentBranchEntryPoint : m_CurrentRepository.getM_Branches().entrySet()){
            Branch oldBranch = currentBranchEntryPoint.getValue();
            isHead = oldBranch.isM_IsHead();
            if (isHead){
                String fixedBranchName = extractBranchNameFromRemoteBranch(oldBranch.getM_Name());
                Branch newTrackingAfterBranch = new Branch(fixedBranchName,oldBranch.getM_PointedCommitId());
                newTrackingAfterBranch.setM_IsRemote(false);
                newTrackingAfterBranch.setM_Tracking(true);
                newTrackingAfterBranch.setM_IsHead(oldBranch.isM_IsHead());
                newTrackingAfterBranch.setM_TrackingAfter(oldBranch.getM_Name());
                newBranchesToAdd.add(newTrackingAfterBranch);
                oldBranch.setM_IsHead(false);
            }
        }

        for(Branch currentBranch : newBranchesToAdd){
            m_CurrentRepository.getM_Branches().put(currentBranch.getM_Name(),currentBranch);
        }
    }

    private String extractBranchNameFromRemoteBranch(String i_OldBranchName) {
        String separator = "/";
        String[] splittedBranchNameContent = i_OldBranchName.split(Pattern.quote(separator));
        return splittedBranchNameContent[splittedBranchNameContent.length - 1 ];
    }

    private String extractNameFromGeneralString(String i_CloneFullPathFromUser) {
        String separator = "\\";
        String[] splittedPathContent = i_CloneFullPathFromUser.split(Pattern.quote(separator));
        return splittedPathContent[splittedPathContent.length - 1 ];
    }

    public void FetchRemoteRepository() throws IOException {
        Repository RemoteCopyRepository = remoteRepositoryLoadingProcess();
        LoadFromRR(RemoteCopyRepository.getM_Blobs(),this.m_CurrentRepository.getM_Blobs());
        LoadFromRR(RemoteCopyRepository.getM_Folders(),this.m_CurrentRepository.getM_Folders());
        LoadFromRR(RemoteCopyRepository.getM_Commits(),this.m_CurrentRepository.getM_Commits());
        LoadBranchesFromRR(RemoteCopyRepository.getM_Branches(),extractNameFromGeneralString(RemoteCopyRepository.getM_Location()));
        Deleter.deleteDir(m_CurrentRepository.getM_Location() + "/.magit");
        this.createRepository();
    }

    private Repository remoteRepositoryLoadingProcess(){
        String remoteRepositoryLocation = m_CurrentRepository.getM_RemoteRepositoryLocation();
        String localRepositoryLocation = m_CurrentRepository.getM_Location();
        this.SwitchRepository(remoteRepositoryLocation);
        Repository RemoteCopyRepository = Repository.CopyRepository(this.getM_Repositories());
        this.SwitchRepository(localRepositoryLocation);
        return RemoteCopyRepository;
    }

    private void LoadBranchesFromRR(Map<String, Branch> i_RemoteBranches,String remoteRepoName) throws IOException {
        for (Map.Entry<String,Branch> entryPoint : i_RemoteBranches.entrySet()) {
            Branch tempBranch = entryPoint.getValue();
            if (!m_CurrentRepository.getM_Branches().containsKey(remoteRepoName + "\\" + tempBranch.getM_Name())) {
                tempBranch.setM_Name((remoteRepoName + "\\" + tempBranch.getM_Name()));
                tempBranch.setM_Tracking(false);
                tempBranch.setM_IsRemote(true);
                tempBranch.setM_TrackingAfter(null);
                tempBranch.setM_IsHead(false);
                m_CurrentRepository.getM_Branches().put(tempBranch.getM_Name(), tempBranch);
                //Branch remoteTrackingBranch = new Branch(tempBranch.getM_Name(),tempBranch.getM_PointedCommitId(),remoteRepoName + "\\" + tempBranch.getM_Name(),false,true);
                //m_CurrentRepository.getM_Branches().put(remoteTrackingBranch.getM_Name(),remoteTrackingBranch);
            } else {
                Branch remoteBranchInLRObjects = m_CurrentRepository.getM_Branches().get(remoteRepoName + "\\" + tempBranch.getM_Name());
                remoteBranchInLRObjects.UpdateNewSettings(tempBranch.getM_PointedCommitId());
                Path branchPath = Paths.get(this.m_CurrentRepository.getM_Location() + "//.magit//branches//" + remoteRepoName + "//" + tempBranch.getM_Name());
                Writer.WriteBranchSHA1ToFile(branchPath,remoteBranchInLRObjects.getM_PointedCommitId());
            }
        }
    }

    private <T> void LoadFromRR(Map<String, T> i_RemoteRepoCollection,Map<String, T> i_LocalRepoCollection) {
        for(Map.Entry<String,T> entryPoint : i_RemoteRepoCollection.entrySet()){
            if(!i_LocalRepoCollection.containsKey(entryPoint.getKey())){
                i_LocalRepoCollection.put(entryPoint.getKey(),entryPoint.getValue());
            }
        }
    }

    public boolean isHeadBranchTrackingAfter() {
        boolean result = false;
        Branch headBranch = null;
        try {
            headBranch = m_CurrentRepository.getM_Branches().get(getHeadBranchFromRepository());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(headBranch.getM_Tracking()){
            result = true;
        }
        return result;
    }

    public void PullFromRemoteRepository() throws IOException {
        Branch headBranchInLR = m_CurrentRepository.getM_Branches().get(getHeadBranchFromRepository());
        Repository remoteRepository = remoteRepositoryLoadingProcess();
        Branch branchInRR = remoteRepository.getM_Branches().get(headBranchInLR.getM_Name());
        String commitSha1 = branchInRR.getM_PointedCommitId();
        addAllBranchData(commitSha1,remoteRepository, m_CurrentRepository);
        this.resetActiveBranch(commitSha1);
        this.m_CurrentRepository.getM_Branches().get(headBranchInLR.getM_TrackingAfter()).setM_PointedCommitId(commitSha1);
        deleteMagitFileAndCreateItBack(m_CurrentRepository.getM_Location());
    }

    private void addAllBranchData(String commitSha1, Repository dataSupplierRepo, Repository receivingDataRepo) throws IOException {
        if (!receivingDataRepo.getM_Commits().containsKey(commitSha1)) {
            spanCommitToOurObjects(dataSupplierRepo.getM_Location(),commitSha1);
            Commit currentCommit = receivingDataRepo.getM_Commits().get(commitSha1);
            List<String> prevCommitsList = currentCommit.getM_PrecedingCommitId();
            for (String prevCommitSha1 : prevCommitsList) {
                if (!receivingDataRepo.getM_Commits().containsKey(prevCommitSha1)) {
                    addAllBranchData(prevCommitSha1, dataSupplierRepo, receivingDataRepo);
                }
            }
        }
    }

    public void PushToRemoteRepository() throws IllegalAccessException, IOException {
        String remoteRepositoryLocation = m_CurrentRepository.getM_RemoteRepositoryLocation();
        String localRepositoryLocation = m_CurrentRepository.getM_Location();
        String remoteRepositoryName = extractRepositoryNameFromPath(remoteRepositoryLocation);
        Repository LocalCopyRepository = Repository.CopyRepository(this.getM_Repositories());
        String localRepositoryActiveBranch = this.getHeadBranchFromRepository();
        String localRepositoryPointedCommitByActiveBranchSha1 = m_CurrentRepository.getM_Branches().get(localRepositoryActiveBranch).getM_PointedCommitId();
        String str = remoteRepositoryName + "\\" + localRepositoryActiveBranch;
        Branch localRepositoryRemoteBranch = LocalCopyRepository.getM_Branches().get(str);
        if(localRepositoryRemoteBranch == null){
            localRepositoryRemoteBranch = findRemoteBranch(str);
        }
        String PointedCommitSha1 = localRepositoryRemoteBranch.getM_PointedCommitId();
        this.SwitchRepository(remoteRepositoryLocation);
        Branch remoteHeadBranch = m_CurrentRepository.getM_Branches().get(localRepositoryActiveBranch);
        String remoteRepositoryCommitToCompare = remoteHeadBranch.getM_PointedCommitId();
        if(checkIfThereAreOpenChanges()){
            this.SwitchRepository(localRepositoryLocation);
            throw new IllegalAccessException("There are open changes on the remote repository.Can't Push");
        }
        if(!PointedCommitSha1.equals(remoteRepositoryCommitToCompare)){
            this.SwitchRepository(localRepositoryLocation);
            throw new IllegalAccessException("Remote tracking branch pointed to different commit compare to this branch in Remote repository");
        }
        addAllBranchData(localRepositoryPointedCommitByActiveBranchSha1,LocalCopyRepository,m_CurrentRepository);
        Branch localBranch = m_CurrentRepository.getM_Branches().get(localRepositoryActiveBranch);
        localBranch.setM_PointedCommitId(localRepositoryPointedCommitByActiveBranchSha1);
        deleteMagitFileAndCreateItBack(m_CurrentRepository.getM_Location());
        if(localBranch.getM_Name().equals(getHeadBranchFromRepository())){
            deleteWcFolderAndCreateItBack(m_CurrentRepository.getM_Location());
        }
        this.SwitchRepository(localRepositoryLocation);
        Branch remoteBranch = m_CurrentRepository.getM_Branches().get(remoteRepositoryName + "\\" + localRepositoryActiveBranch);
        remoteBranch.setM_PointedCommitId(localRepositoryPointedCommitByActiveBranchSha1);
        Path remoteBranchPath = Paths.get(m_CurrentRepository.getM_Location() + "//.magit//branches//" + extractRepositoryNameFromPath(m_CurrentRepository.getM_RemoteRepositoryLocation()) + "//" + remoteHeadBranch.getM_Name());
        try {
            changeTextFileContent(remoteBranchPath, localRepositoryPointedCommitByActiveBranchSha1);
        } catch (IOException e) {
            throw new IOException("Can't find remote branch file on file system!");
        }

    }

    private void changeTextFileContent(Path locationInMagitFileSystem,String i_NewContent) throws IOException {
        Deleter.deleteTextFileContent(locationInMagitFileSystem);
        Writer.WriteToTextFile(locationInMagitFileSystem,i_NewContent);
    }

    private Branch findRemoteBranch(String i_BranchName) {
        String separator = "\\";
        String [] spllitedCont = i_BranchName.split(Pattern.quote(separator));
        for(Map.Entry<String,Branch> entryBranch: m_CurrentRepository.getM_Branches().entrySet()){
            if(entryBranch.getKey().contains(spllitedCont[0]) && entryBranch.getKey().contains(spllitedCont[1]) )
                return entryBranch.getValue();
        }
        return null;
    }

    private void deleteMagitFileAndCreateItBack(String i_RepositoryFullPath) throws IOException {
        Deleter.deleteDir(i_RepositoryFullPath + "//.magit");
        this.createRepository();
    }

    private void deleteWcFolderAndCreateItBack(String i_RepositoryFullPath) throws IOException {
        Deleter.deleteDir(i_RepositoryFullPath + "//WC");
        Creator.createWC(this);
    }

    public boolean isRemoteBranchOnCommitIdInCurrentCommitTree(String i_CommitSha) {
        boolean result = false;
        for(Map.Entry<String,Branch> entryBranch : m_CurrentRepository.getM_Branches().entrySet()){
            Branch tempBranch = entryBranch.getValue();
            if(tempBranch.getM_PointedCommitId().equals(i_CommitSha) && tempBranch.getM_IsRemote()){
                result = true;
            }
        }
        return result ;
    }

    public ObservableList<String> getAllRemoteBranchesOnThatCommit(String i_SelectedCommitSha1) {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Map.Entry<String, Branch> entry : m_CurrentRepository.getM_Branches().entrySet()) {
            if(entry.getValue().getM_PointedCommitId().equals(i_SelectedCommitSha1) && entry.getValue().getM_IsRemote()){
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public void createTrackingAfterBranch(String i_newBrunchName, String i_remoteBranchToTrackAfter) {
        String pointedCommit = m_CurrentRepository.getM_Branches().get(i_remoteBranchToTrackAfter).getM_PointedCommitId();
        String trackingAfter = m_CurrentRepository.getM_Branches().get(i_remoteBranchToTrackAfter).getM_Name();
        Branch newBranchToAdd = new Branch(i_newBrunchName,pointedCommit,trackingAfter,false,true);
        m_CurrentRepository.getM_Branches().put(i_newBrunchName,newBranchToAdd);
    }

    public boolean isBranchPointingToThatCommit(String i_SelectedCommitId) {
        boolean result = false ;
        for(Map.Entry<String,Branch> branchEntry : m_CurrentRepository.getM_Branches().entrySet()){
            if(branchEntry.getValue().getM_PointedCommitId().equals(i_SelectedCommitId)){
                result = true;
                break;
            }
        }
        return result;
    }

    public ObservableList<String> gatherAllBranchesOnCommit(String i_SelectedCommit) throws IOException {
        ObservableList<String> list = FXCollections.observableArrayList();
        File headFolder = new File(this.getM_Repositories().getM_Location() + "/.magit/branches/Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(headFolder));
        String activeBranchName = br.readLine();
        list.removeAll();
        for (Map.Entry<String, Branch> entry : this.getM_Repositories().getM_Branches().entrySet()) {
            Branch v = entry.getValue();
            if (!(v.getM_Name().equals("Head")) && !(v.getM_Name().equals(activeBranchName))) {
                if(v.getM_PointedCommitId().equals(i_SelectedCommit)) {
                    list.add(v.getM_Name());
                }
            }
        }
        return list;
    }
}

