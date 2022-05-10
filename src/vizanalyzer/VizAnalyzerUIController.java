/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vizanalyzer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;

/**
 * FXML Controller class
 *
 * @author KH9132
 */
public class VizAnalyzerUIController implements Initializable {

    @FXML
    private TextField projectPathField;
    @FXML
    private Button browseButton;
    
    
    
    private File projectDirFile;
    private int logLineNumber;
    @FXML
    private TextArea console;
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private Accordion optionsAccordian;
    @FXML
    private ListView<String> skinList;
    @FXML
    private Button scanProjectButton;

    private HashMap skinNames;
    private HashMap actionNames;
    @FXML
    private TextField searchBoxField;
    private Label kuid_label;
    @FXML
    private Label kuid_Label;
    @FXML
    private ListView<String> fromsList;
    @FXML
    private TextArea skinCode;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logLineNumber=0;
        console.setText("initializing tool....");
        // TODO
        projectDirFile=null;
        
        mainPanel.setId("flxMain");
        mainPanel.disableProperty().set(true);
        
        optionsAccordian.setId("flxOptions");
                
        scanProjectButton.disableProperty().set(true);
        
        skinNames=new HashMap();
        actionNames=new HashMap();
        
        skinList.setOnMouseClicked(new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            
            showLog("clicked on "+skinList.getSelectionModel().getSelectedItem());
            String kuid=getKeyofSkin(skinList.getSelectionModel().getSelectedItem());
            if(kuid!=null){
            showLog("kuid of Skin is "+kuid);
            kuid_Label.setText("skin :{ name:\""+skinList.getSelectionModel().getSelectedItem()+"\" kuid: \""+kuid+"\" }");
            searchSkinInForms(kuid);
            
            skinCode.setText(getContentOfFile(new File(getFilePathOfSkin(kuid))));
            
            }
        }
        });
        
        console.appendText("\nfinished loading tool.");
    }
    private void searchSkinInForms(String kuid)
    {
        showLog("searching skin using kuid --> "+kuid+" <-- in forms");
        String formsDirPath=projectDirFile.getPath()+"/forms/mobile";
        File formsDirFile=new File(formsDirPath);
        if(formsDirFile.exists() && formsDirFile.isDirectory())
        {
            ObservableList<String> items =FXCollections.observableArrayList ();
            for(File formsIndex:formsDirFile.listFiles())
            {
                if(formsIndex.isDirectory())
                {
                    for(File widgetIndex:formsIndex.listFiles())
                    {
                        String contentOfWidget=getContentOfFile(widgetIndex);
                        if(contentOfWidget.contains(kuid))
                        {
                            items.add(formsIndex.getName()+"---->"+widgetIndex.getName());
                        }
                    }
                }
            }
     
            fromsList.setItems(items);
            
        }else{
            showLog("condition failed");
        }
    }

    @FXML
    private void browseButtonOnClickAction(ActionEvent event) {
               
        projectDirFile=browseProject();
        if(fileIsNull(projectDirFile))
        {
            showLog("you selected nothing");
            showAlert("Please select any project path!!!");
            return;
        }
        showLog("selected :"+projectDirFile.toPath());
        if(!isVizProject(projectDirFile))
        {
            showLog("is not a viz project");
            showAlert("please select viz project root only!!!");
            return;
        }
        projectPathField.setText(projectDirFile.getPath());
        scanProjectButton.disableProperty().set(false);
    }
    private boolean isVizProject(File directory)
    {
        return (new File(projectDirFile.getPath()+"/projectproperties.json").exists());
    }
    private boolean fileIsNull(File file)
    {
        if(file==null)
        {
            return true;
        }
        return false;
    }
    private File browseProject()
    {
        File f;
        DirectoryChooser dirWindow=new DirectoryChooser();
        showLog("browsing project...");
        f=dirWindow.showDialog(VizAnalyzer.mainWindow);
        return f;
    }
    private void showAlert(String message)
    {
        Alert myAlert=new Alert(Alert.AlertType.INFORMATION,message,ButtonType.OK);
        myAlert.show();
    }
    private void showLog(String content)
    {
       logLineNumber++;
       console.appendText("\n"+logLineNumber+". "+content);
    }

    @FXML
    private void scanProjectOnCLickAction(ActionEvent event) throws IOException {
        mainPanel.disableProperty().set(true);
        
        populateSkins();
        showLog("total "+skinNames.size()+" skins found");
        setSkinList();
        
        populateActions();
        showLog("total "+actionNames.size()+" actions found");
        mainPanel.disableProperty().set(false);
        setActionList();        
    }
    private void setActionList()
    {
       ObservableList<String> items =FXCollections.observableArrayList ();
        for (Iterator it = actionNames.keySet().iterator(); it.hasNext();) {
            Object actionName = it.next();
            String temp=skinNames.get(actionName).toString();
            items.add(temp.substring(temp.lastIndexOf("\\")+1, temp.length()));
        }
        SortedList<String> sortedList = new SortedList(items);
        skinList.setItems(items);
        showLog("skins loaded sucessfully...."); 
    }
    private void populateActions() throws IOException
    {
        showLog("scanning project for Actions");
        
        String actionsDirPath=projectPathField.getText()+"\\studioactions\\mobile";
        File actionsDir=new File(actionsDirPath);
        if(!(actionsDir.exists() || actionsDir.isDirectory()) )
        {
            showAlert("destination project is curropted: missing skins");
            showLog("error: directory not found "+actionsDirPath);
            return;
        }
        for(File indexFile:actionsDir.listFiles()){
            if(!indexFile.getPath().contains("json"))
            {
                actionNames.put(indexFile.getCanonicalPath(),indexFile.getPath());
                showLog("action file :"+indexFile.getPath());
            }
        }
    }
    private void setSkinList()
    {
        ObservableList<String> items =FXCollections.observableArrayList ();
        for (Iterator it = skinNames.keySet().iterator(); it.hasNext();) {
            Object kuid = it.next();
            String temp=skinNames.get(kuid).toString();
            items.add(temp.substring(temp.lastIndexOf("\\")+1, temp.length()));
        }
        SortedList<String> sortedList = new SortedList(items);
        skinList.setItems(items);
        showLog("skins loaded sucessfully....");
    }
    private void populateSkins()
    {
        showLog("scanning project for skins");
        
        String skinDirPath=projectPathField.getText()+"\\themes\\defaultTheme";
        File skinDir=new File(skinDirPath);
        if(!(skinDir.exists() || skinDir.isDirectory()) )
        {
            showAlert("destination project is curropted: missing skins");
            showLog("error: directory not found "+skinDirPath);
            return;
        }
        for(File indexFile:skinDir.listFiles()){
            skinNames.put(getKUIDof(indexFile),indexFile.getPath());
            showLog("skin file :"+indexFile.getPath());
        }
    }
    private String getKUIDof(File skinFile)
    {
        String kuid=null;
        String content=getContentOfFile(skinFile);
        for(String line:content.split("\n"))
        {
            if(!line.contains("kuid"))
            {
                continue;
            }
            String[] keyValuePairOfKUID=line.split(":");
            kuid=keyValuePairOfKUID[1].substring(2,keyValuePairOfKUID[1].length()-3);
            break;
        }
        return kuid;
    }
    private String getContentOfFile(File f)
    {
        String content="";
        try{
         FileReader fr=new FileReader(f);
         int asciiChar=fr.read();
         while(asciiChar!=-1)
         {
             content+=(char)asciiChar;
             asciiChar=fr.read();
         }
        }catch(Exception e){
            showLog("Exception :"+e.getMessage());
        }
        return content;
    }

    @FXML
    private void searchingAction(KeyEvent event) {
        
        String selectedOption=optionsAccordian.getExpandedPane().getText();
        if(selectedOption.equals("Skins"))
        {
            searchSkniNames(searchBoxField.getText());
        }
    }
    private void searchSkniNames(String pattern)
    {
        showLog("searching :"+pattern);
        
        ObservableList<String> items =FXCollections.observableArrayList ();
        for (Iterator it = skinNames.keySet().iterator(); it.hasNext();) {
            Object kuid = it.next();
            String temp=skinNames.get(kuid).toString();
            if(temp.contains(pattern))
            {
             items.add(temp.substring(temp.lastIndexOf("\\")+1, temp.length()));
            }
        }
        SortedList<String> sortedList = new SortedList(items);
        skinList.setItems(items);
    }
    private String getKeyofSkin(String value)
    {
        String key="";
        for (Iterator it = skinNames.keySet().iterator(); it.hasNext();) {
                Object kuid = it.next();
                String temp=skinNames.get(kuid).toString();
                if(temp.substring(temp.lastIndexOf("\\")+1, temp.length()).equals(value))
                {
                    key=kuid.toString();
                }
            }
        return key;
    }
    private String getFilePathOfSkin(String Rkuid)
    {
        String path="";
        for (Iterator it = skinNames.keySet().iterator(); it.hasNext();) {
                Object kuid = it.next();
                if(kuid.toString().equals(Rkuid))
                {
                    path=skinNames.get(kuid).toString();
                    break;
                }              
            }
        return path;
    }
}