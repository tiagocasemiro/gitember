package com.az.gitember.ui;

import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmRevisionInformation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class ScmItemCellFactory implements Callback<TreeView<Object>, TreeCell<Object>> {

    final ContextMenu contextMenu;
    final ContextMenu tagContextMenu;
    final Consumer<Object> validateContextMenu;



    public ScmItemCellFactory(ContextMenu contextMenu, ContextMenu tagContextMenu, Consumer<Object> validateContextMenu) {
        this.contextMenu = contextMenu;
        this.validateContextMenu = validateContextMenu;
        this.tagContextMenu = tagContextMenu;
    }

    public class ScmItemCell extends TreeCell<Object> {

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setContextMenu(null);
                setGraphic(null);
                setStyle("");
            } else {
                if (item instanceof String) {
                    setText(item.toString());
                    setGraphic(getTreeItem().getGraphic());
                    setStyle("");
                    if ("Tags".equals(item)) {
                        setContextMenu(tagContextMenu);

                    }
                } else if (item instanceof ScmBranch) {
                    ScmBranch scmBranch = (ScmBranch) item;
                    setText(scmBranch.getShortName());
                    setContextMenu(contextMenu);
                    setOnContextMenuRequested(e -> validateContextMenu.accept(scmBranch));

                    if (scmBranch.isHead()) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                    if (scmBranch.getBranchType() == ScmBranch.BranchType.LOCAL) {
                        setGraphic(new FontIcon(FontAwesome.CODE_FORK));
                    } else if (scmBranch.getBranchType() == ScmBranch.BranchType.REMOTE) {
                        setGraphic(new FontIcon(FontAwesome.CODE_FORK));
                    } else if (scmBranch.getBranchType() == ScmBranch.BranchType.TAG) {
                        setGraphic(new FontIcon(FontAwesome.TAG));
                    }

                } else if (item instanceof ScmRevisionInformation) {
                    ScmRevisionInformation ri = (ScmRevisionInformation) item;
                    setText(ri.getShortMessage());
                    setContextMenu(contextMenu);
                    setOnContextMenuRequested(e -> validateContextMenu.accept(ri));
                } else {
                    setText(item.toString());
                }
            }
        }
    }


    @Override
    public TreeCell<Object> call(TreeView<Object> param) {
        return new ScmItemCell();
    }
}
