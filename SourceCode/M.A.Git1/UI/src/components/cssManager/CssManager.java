package components.cssManager;

import java.util.ArrayList;
import java.util.List;

public class CssManager {
    private List<String> cssFiles;
    private int currentIndex;

    public CssManager(){
        cssFiles=new ArrayList<>();
        currentIndex=0;
    }
    public void addCssSheet(String path){
        cssFiles.add(path);
    }
    public void nextCss(){
        currentIndex=(currentIndex+1)%(cssFiles.size());
    }

    public String getCurrentCss() {
        return cssFiles.get(currentIndex);
    }
}
