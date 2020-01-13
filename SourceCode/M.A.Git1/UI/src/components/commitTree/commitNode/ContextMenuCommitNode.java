package components.commitTree.commitNode;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ContextMenuCommitNode {

    private  ContextMenu contextMenu ;

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public ContextMenuCommitNode(){
        contextMenu = new ContextMenu();
    }

    public void addSubMenuItem(Menu parent,MenuItem menuItem){
        parent.getItems().add(menuItem);
    }

    public MenuItem createMenuItem(String content, Consumer consumer){
        MenuItem menuItem = new MenuItem(content);
        menuItem.setOnAction(v->consumer.accept(v));
        return  menuItem;
    }

    public void mainMenuCreator(MenuItem menuItem){
        contextMenu.getItems().add(menuItem);
    }

}
