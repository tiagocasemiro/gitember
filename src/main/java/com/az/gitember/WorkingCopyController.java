package com.az.gitember;

import com.az.gitember.misc.*;
import com.az.gitember.scm.exception.GEScmAPIException;
import com.az.gitember.service.Context;
import com.az.gitember.ui.AutoCompleteTextField;
import com.az.gitember.ui.StatusCellValueFactory;
import com.jcraft.jsch.IO;
import com.sun.javafx.binding.StringConstant;
import difflib.DiffUtils;
import difflib.Patch;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 23.12.2016.
 */
public class WorkingCopyController implements Initializable {

    private final static Logger log = Logger.getLogger(MainViewController.class.getName());

    public TableView workingCopyTableView;
    public TableColumn<ScmItem, FontIcon> statusTableColumn;
    public TableColumn<ScmItem, Boolean> selectTableColumn;
    public TableColumn<ScmItem, String> itemTableColumn;
    public TableColumn<ScmItem, String> itemTableColumnColorStatus;
    public Button stashBtn;
    public Button commitBtn;
    public Button stageAllBtn;
    public Button unStageAllBtn;
    public Button refreshBtn;
    //public Button searchButton;
    public Pane spacerPane;
    public AutoCompleteTextField searchText;
    public Label searchLabel;
    public Menu workingCopyMenu;

    public ContextMenu scmItemContextMenu;
    public MenuItem revertMenuItem;
    public MenuItem stageFileMenuItem;
    public MenuItem unstageFileMenuItem;
    public MenuItem openFileMenuItem;
    public MenuItem showHistoryMenuItem;
    public MenuItem showDiffMenuItem;

    public MenuItem conflictResolveUsingMy;
    public MenuItem conflictResolveUsingTheir;
    public MenuItem conflictResolved;


    private ScmBranch branch;

    private Consumer<Object> onStashCreated;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        itemTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getShortName())
        );

        itemTableColumnColorStatus.setCellValueFactory(
                c -> StringConstant.valueOf(" ")
        );

        itemTableColumnColorStatus.setCellFactory(

                new Callback<TableColumn<ScmItem, String>,
                        TableCell<ScmItem, String>>() {

                    @Override
                    public TableCell<ScmItem, String> call(TableColumn<ScmItem, String> param) {
                        return new TableCell<ScmItem, String> () {

                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (!empty) {

                                    int currentIndex = indexProperty()
                                            .getValue() < 0 ? 0
                                            : indexProperty().getValue();
                                    ScmItem currItem = param
                                            .getTableView().getItems()
                                            .get(currentIndex);
                                    if (currItem.getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)) {
                                        setStyle("-fx-background-color: #49ee52;");
                                    } else if (currItem.getAttribute().getStatus().contains(ScmItemStatus.MISSED)) {
                                        setStyle("-fx-background-color: #a3a3a3;");
                                    } else if (currItem.getAttribute().getStatus().contains(ScmItemStatus.CONFLICT)) {
                                        setStyle("-fx-background-color: #dd37d4;");
                                    } else if (currItem.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED_FOLDER)) {
                                        setStyle("-fx-background-color: #f0262b;");
                                    } else if (currItem.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED)) {
                                        setStyle("-fx-background-color: #f0262b;");
                                    } else if (
                                            (currItem.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED) && currItem.getAttribute().getStatus().size() == 1)
                                            || (currItem.getAttribute().getStatus().contains(ScmItemStatus.REMOVED) && currItem.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED))
                                            || (currItem.getAttribute().getStatus().contains(ScmItemStatus.ADDED) && currItem.getAttribute().getStatus().contains(ScmItemStatus.CHANGED))

                                            ) {
                                        setStyle("-fx-background-color: #517de6;");
                                    }
                                }
                            }
                        };
                    }


                }

        );

        statusTableColumn.setCellValueFactory(
                c -> new StatusCellValueFactory(c.getValue().getAttribute().getStatus())
        ); //TODO add tooltip

        selectTableColumn.setCellValueFactory(
                c -> new ReadOnlyBooleanWrapper(!isUnstaged(c.getValue()))
        );

        selectTableColumn.setCellFactory(p -> new CheckBoxTableCell<ScmItem, Boolean>());

        workingCopyTableView.setRowFactory(
                tr -> {
                    return new TableRow<ScmItem>() {

                        private String calculateStyle(final ScmItem scmItem) {
                            StringBuilder sb = new StringBuilder();
                            if (scmItem != null) {
                                if (WorkingCopyController.this.searchText.getText() != null
                                        && WorkingCopyController.this.searchText.getText().length() > Const.SEARCH_LIMIT_CHAR) {
                                    if (scmItem.getShortName().toLowerCase().contains(
                                            WorkingCopyController.this.searchText.getText().toLowerCase())) {
                                        //sb.append("-fx-font-weight: bold;");
                                        sb.append("-fx-font-weight: bold; ");
                                        sb.append("-fx-background-color: linear-gradient(#9fbed6 0%, #d0fad0 100%);");
                                    }

                                }

                            }
                            return  sb.toString();

                        }

                        @Override
                        protected void updateItem(ScmItem item, boolean empty) {
                            super.updateItem(item, empty);
                            setStyle(calculateStyle(item));
                            if (!empty) {
                                setContextMenu(scmItemContextMenu);
                                setOnContextMenuRequested(event -> {
                                    boolean isConflict = item.getAttribute().getStatus().contains(ScmItemStatus.CONFLICT);
                                    conflictResolveUsingMy.setVisible(isConflict);
                                    conflictResolveUsingTheir.setVisible(isConflict);
                                    conflictResolved.setVisible(isConflict);

                                    stageFileMenuItem.setDisable(!isUnstaged(item));
                                    unstageFileMenuItem.setDisable(isUnstaged(item));

                                    revertMenuItem.setVisible(!isConflict);
                                    showDiffMenuItem.setVisible(!isConflict);

                                });
                            }
                        }
                    };
                }
        );

        stageAllBtn = new Button("Stage all");
        stageAllBtn.setOnAction(this::stageAllBtnHandler);
        stageAllBtn.setId(Const.MERGED);

        unStageAllBtn = new Button("Unstage all");
        unStageAllBtn.setOnAction(this::unstageAllBtnHandler);
        unStageAllBtn.setId(Const.MERGED);

        commitBtn = new Button("Commit ...");
        commitBtn.setOnAction(this::commitHandler);
        commitBtn.setId(Const.MERGED);

        stashBtn = new Button("Move to stash");
        stashBtn.setOnAction(this::stashBtnHandler);
        stashBtn.setId(Const.MERGED);

        refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(this::refreshBtnHandler);
        refreshBtn.setId(Const.MERGED);
        refreshBtn.setGraphic(
                new FontIcon(FontAwesome.REFRESH)
        );

        spacerPane = new Pane();
        HBox.setHgrow(spacerPane, Priority.ALWAYS);
        spacerPane.setId(Const.MERGED);

        searchLabel =  new Label();
        searchLabel.setId(Const.MERGED);
        searchLabel.setText("Search");

        searchText = new AutoCompleteTextField();
        searchText.setId(Const.MERGED);
        searchText.getEntries().addAll(GitemberApp.entries);
        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    workingCopyTableView.refresh();
                    if (oldValue != null && newValue != null && newValue.length() > oldValue.length() && newValue.contains(oldValue)) {
                        GitemberApp.entries.remove(oldValue);
                        GitemberApp.entries.add(newValue);
                    }
                }
        );


        workingCopyMenu = new Menu("Working copy");
        workingCopyMenu.setId(Const.MERGED);

        MenuItem mi = new MenuItem("Checkout ...");
        mi.setOnAction(this::checkoutRevision);
        workingCopyMenu.getItems().add(mi);

        mi = new MenuItem("Merge ...");
        mi.setOnAction(this::mergeBranch);
        workingCopyMenu.getItems().add(mi);

        mi = new MenuItem("Rebase ...");
        mi.setOnAction(this::rebaseIntoWorkingCopy);
        workingCopyMenu.getItems().add(mi);

        mi = new MenuItem("Commit ...");
        mi.setOnAction(this::commitHandler);
        workingCopyMenu.getItems().add(mi);


        //----------------- item context menu

        revertMenuItem = new MenuItem("Revert changes ...");
        revertMenuItem.setOnAction(this::revertEventHandler);

        stageFileMenuItem = new MenuItem("Add item to stage");
        stageFileMenuItem.setOnAction(this::stageMiEventHandler);

        unstageFileMenuItem = new MenuItem("Unstage");
        unstageFileMenuItem.setOnAction(this::untageMiEventHandler);


        openFileMenuItem = new MenuItem("Open item");
        openFileMenuItem.setOnAction(this::openEventHandler);

        showHistoryMenuItem = new MenuItem("History of changes");
        showHistoryMenuItem.setOnAction(this::historyEventHandler);

        showDiffMenuItem = new MenuItem("Diff with the same version from repository");
        showDiffMenuItem.setOnAction(this::diffEventHandler);


        conflictResolveUsingMy = new MenuItem("Resolve using my changes");
        conflictResolveUsingMy.setOnAction(this::resolveUsingMyChanges);

        conflictResolveUsingTheir = new MenuItem("Resolve using their changes");
        conflictResolveUsingTheir.setOnAction(this::resolveUsingTheirChanges);

        conflictResolved = new MenuItem("Mark resolved");
        conflictResolved.setOnAction(this::stageMiEventHandler);


        scmItemContextMenu = new ContextMenu(
                conflictResolveUsingMy,
                conflictResolveUsingTheir,
                conflictResolved,
                revertMenuItem,
                stageFileMenuItem,
                unstageFileMenuItem,
                new SeparatorMenuItem(),
                openFileMenuItem,
                new SeparatorMenuItem(),
                showHistoryMenuItem,
                showDiffMenuItem
        );


    }

    private void mergeBranch(ActionEvent actionEvent) {
        Context.getGitemberService().makeBranchOperation(
                "Merge",
                "Please select branch to merge with current",
                s -> Context.getGitemberService().mergeToHead(s));
    }

    /**
     * Show dialogs with revisions to checkout.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    private void checkoutRevision(ActionEvent actionEvent) {
        Context.getGitemberService().makeBranchOperation(
                "Checkout",
                "Please select branch to checkout",
                s -> Context.getGitemberService().checkout(s, null));
    }

    /**
     * Show dialogs with revisions to checkout.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    private void rebaseIntoWorkingCopy(ActionEvent actionEvent) {
        Context.getGitemberService().makeBranchOperation(
                "Rebase",
                "Please select branch to integrate into your working copy",
                s -> Context.getGitemberService().rebase(s));
    }


    public void open(final ScmBranch branch, final String path) {
        this.branch = branch;
        GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);
        Task<List<ScmItem>> longTask = new Task<List<ScmItem>>() {
            @Override
            protected List<ScmItem> call() throws Exception {
                return Context.getGitemberService().getStatuses(path);
            }
        };

        longTask.setOnSucceeded(z -> Platform.runLater(
                () -> {
                    List<ScmItem> list = longTask.getValue();
                    if (path == null) {
                        workingCopyTableView.setItems(FXCollections.observableArrayList(list));
                    } else {
                        if (list.size() == 1) {
                            //in case of delete on changed file. but not such operation atm.
                            ScmItem item = list.get(0);
                            workingCopyTableView.getItems().replaceAll(o -> {
                                if (((ScmItem) o).getShortName().endsWith(path)) {
                                    return item;
                                }
                                return o;
                            });
                        } else {
                            //revert operation
                            workingCopyTableView.getItems().removeIf(o -> ((ScmItem) o).getShortName().equals(path));
                        }
                    }
                    GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                    workingCopyTableView.refresh();
                }
                )
        );

        longTask.setOnFailed(z -> Platform.runLater(
                () -> {
                    GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                    Throwable e = z.getSource().getException();
                    log.log(Level.SEVERE, "Cannot load item statuses from repository", e);
                    GitemberUITool.showException("Cannot open working copy. ", e);
                })
        );

        Platform.runLater(
                () -> {
                    Thread th = new Thread(longTask);
                    th.start();

                }
        );

    }

    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    ToolBar handlers       -------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//


    /**
     * Stage all changes for commit.
     *
     * @param actionEvent event
     */
    @SuppressWarnings({"unchecked", "unused"})
    public void stageAllBtnHandler(ActionEvent actionEvent) {
        workingCopyTableView.getItems().stream()
                .filter(i -> (isUnstaged((ScmItem) i)))
                .forEach(i -> stageUnstageItem((ScmItem) i));
        workingCopyTableView.refresh();
    }

    /**
     * Unstage all changes
     *
     * @param actionEvent event
     */
    @SuppressWarnings({"unchecked", "unused"})
    public void unstageAllBtnHandler(ActionEvent actionEvent) {
        workingCopyTableView.getItems().stream()
                .filter(i -> (!isUnstaged((ScmItem) i)))
                .forEach(i -> stageUnstageItem((ScmItem) i));
        workingCopyTableView.refresh();
    }


    /**
     * Commit all staged changes.
     *
     * @param actionEvent event
     * @throws Exception in case of errors
     */
    @SuppressWarnings("unused")
    public void commitHandler(ActionEvent actionEvent) {
        if (Context.getGitemberService().commit(branch)) {
            open(branch, null);
        }
    }


    /**
     * Refresh changes from disk
     *
     * @param actionEvent event
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public void refreshBtnHandler(ActionEvent actionEvent) {
        open(branch, null);
    }

    /**
     * Move changes to stash.
     *
     * @param actionEvent event
     * @throws Exception
     */
    @SuppressWarnings("unused")
    public void stashBtnHandler(ActionEvent actionEvent) {
        try {
            Context.getGitemberService().stash();
            open(branch, null);
            onStashCreated.accept(null);
        } catch (GEScmAPIException e) {
            GitemberUITool.showResult("Changes not moved to stash, because: " + e.getMessage(), Alert.AlertType.ERROR);
            log.log(Level.SEVERE, "Cannot move to stash", e);
        }
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Item context menu item -------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//


    /**
     * Add file to stage from context menu.
     *
     * @param event event
     */
    @SuppressWarnings("unused")
    public void stageMiEventHandler(Event event) {
        ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            stageItem(item);
            workingCopyTableView.refresh();
        }
    }

    /**
     * Add file to stage from context menu.
     *
     * @param event event
     */
    @SuppressWarnings("unused")
    public void untageMiEventHandler(Event event) {
        ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            unstageItem(item);
            workingCopyTableView.refresh();
        }
    }

    /**
     * Open file.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void openEventHandler(ActionEvent actionEvent) {
        final ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            final FileViewController fileViewController = new FileViewController();
            try {
                fileViewController.openFile(
                        "Context.getCurrentRepositoryPathWOGit()" + File.separator + item.getShortName(),
                        item.getShortName());
            } catch (Exception e) {
                String msg = String.format("Cannot open file %s", item.getShortName());
                GitemberUITool.showResult(msg, Alert.AlertType.WARNING);
                log.log(Level.WARNING, msg, e);
            }
        }
    }

    /**
     * Revert file changes.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void revertEventHandler(ActionEvent actionEvent) {
        final ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            Optional<ButtonType> result = GitemberUITool.showResult(
                    "Revert " + item.getShortName() + " changes ?",
                    Alert.AlertType.CONFIRMATION);
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Context.getGitemberService().checkoutFile(item.getShortName(), null);
                open(branch, item.getShortName());
            }
        }
    }

    /**
     * Resolve conflict using their changes.
     *
     * @param event event
     */
    public void resolveUsingTheirChanges(Event event) {
        resolveConflict(Stage.THEIRS);

    }

    /**
     * Resolve conflict using my changes.
     *
     * @param event event
     */
    public void resolveUsingMyChanges(Event event) {
        resolveConflict(Stage.OURS);
    }

    private void resolveConflict(Stage stage) {
        final ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            Context.getGitemberService().checkoutFile(item.getShortName(), stage);
            try {
                Context.getGitemberService().addFileToCommitStage(item.getShortName());
                open(branch, item.getShortName());
            } catch (Exception e) {
                GitemberUITool.showException("Cannot resolve conflict adding file " + item.getShortName(), e);
            }
        }
    }


    /**
     * Show different with last version from repository.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void diffEventHandler(ActionEvent actionEvent) {
        final ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {

            try {
                final String fileName = item.getShortName();
                final Pair<String, String> head = Context.getGitemberService().getHead();
                final String oldFile = Context.getGitemberService().saveFile(
                        head.getFirst(),
                        head.getSecond(), fileName);
                final String newFile = "GitemberApp.getCurrentRepositoryPathWOGit()" + File.separator + fileName;

                List<String> newFileLines = Files.readAllLines(Paths.get(newFile));
                List<String> oldFileLines = Files.readAllLines(Paths.get(oldFile));
                Patch<String> pathc = DiffUtils.diff(oldFileLines, newFileLines);

                final DiffViewController fileViewController = new DiffViewController();
                fileViewController.openFile(
                        new File(fileName).getName(),
                        oldFile, head.getSecond(),
                        newFile, "On disk",
                        pathc);

            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot get head", e);
            }
        }
    }

    /**
     * Open hisotry
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void historyEventHandler(ActionEvent actionEvent) {
        final ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
        if (item != null) {
            try {
                HistoryViewController.openHistoryWindow(item.getShortName(), branch.getFullName());
            } catch (Exception e) {
                log.log(Level.SEVERE, String.format("Cannot open history for %s %s", item.getShortName(), branch.getFullName()));
            }
        }
    }


    private boolean isStaged(ScmItem scmItem) {
        return scmItem.getAttribute().getStatus().contains(ScmItemStatus.ADDED)
                || scmItem.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED);
    }

    private boolean isUnstaged(ScmItem scmItem) {
        return scmItem.getAttribute().getStatus().contains(ScmItemStatus.MODIFIED)
                || scmItem.getAttribute().getStatus().contains(ScmItemStatus.MISSED)
                || scmItem.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED);
    }


    /**
     * Add file to stage.
     *
     * @param event event
     */
    @SuppressWarnings("unused")
    public void addItemToStageEventHandler(Event event) {
        if (event.getTarget() instanceof CheckBoxTableCell) {
            CheckBoxTableCell cell = (CheckBoxTableCell) event.getTarget();
            if (cell.getTableColumn() == this.selectTableColumn) {
                ScmItem item = (ScmItem) workingCopyTableView.getSelectionModel().getSelectedItem();
                stageUnstageItem(item);
                workingCopyTableView.refresh();
            }
        }
    }


    private void stageItem(ScmItem item) {
        try {
            if (item != null) {
                if (isUnstaged(item)) {
                    if (item.getAttribute().getStatus().contains(ScmItemStatus.MISSED)) {
                        Context.getGitemberService().removeMissedFile(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.MISSED);
                        item.getAttribute().getStatus().add(ScmItemStatus.REMOVED);
                    } else if (item.getAttribute().getStatus().contains(ScmItemStatus.UNTRACKED)) {
                        Context.getGitemberService().addFileToCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.UNTRACKED);
                        item.getAttribute().getStatus().add(ScmItemStatus.ADDED);
                        item.getAttribute().getStatus().add(ScmItemStatus.CHANGED);
                        item.getAttribute().getStatus().add(ScmItemStatus.UNCOMMITED);
                    } else {
                        Context.getGitemberService().addFileToCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.MODIFIED);
                    }
                }
            }
        } catch (Exception e) {
            GitemberUITool.showException("Cannot add item " + item.getShortName() + " to stage", e);
        }
    }


    private void unstageItem(ScmItem item) {
        try {
            if (item != null) {
                if (isStaged(item)) {
                    if (item.getAttribute().getStatus().contains(ScmItemStatus.REMOVED)
                            && item.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED)
                            && item.getAttribute().getStatus().size() == 2) {
                        Context.getGitemberService().removeFileFromCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.REMOVED);
                        item.getAttribute().getStatus().add(ScmItemStatus.MISSED);
                    } else if (item.getAttribute().getStatus().contains(ScmItemStatus.ADDED)
                            && item.getAttribute().getStatus().contains(ScmItemStatus.CHANGED)
                            && item.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED)
                            && item.getAttribute().getStatus().size() == 3) {
                        Context.getGitemberService().removeFileFromCommitStage(item.getShortName());
                        item.getAttribute().getStatus().clear();
                        item.getAttribute().getStatus().add(ScmItemStatus.UNTRACKED);

                    } else if (item.getAttribute().getStatus().contains(ScmItemStatus.UNCOMMITED)
                            && item.getAttribute().getStatus().size() == 1) {
                        Context.getGitemberService().removeFileFromCommitStage(item.getShortName());
                        item.getAttribute().getStatus().add(ScmItemStatus.MODIFIED);
                    }
                }
            }
        } catch (Exception e) {
            GitemberUITool.showException("Cannot unstage item " + item.getShortName(), e);
        }
    }

    private void stageUnstageItem(ScmItem item) {
        try {
            if (item != null) {
                if (isUnstaged(item)) {
                    stageItem(item);
                } else if (isStaged(item)) {
                    unstageItem(item);
                } else if (item.getAttribute().getStatus().contains(ScmItemStatus.CONFLICT)) {

                    // mark resolved. so nothing meaningful, just delete of add as it even.
                    if (ScmItemStatus.CONFLICT_DELETED_BY_THEM.equals(item.getAttribute().getSubstatus())
                            || ScmItemStatus.CONFLICT_DELETED_BY_US.equals(item.getAttribute().getSubstatus())
                            || ScmItemStatus.CONFLICT_BOTH_DELETED.equals(item.getAttribute().getSubstatus())
                    ) {
                        if (Files.exists(Paths.get(item.getShortName()))) {
                            Context.getGitemberService().addFileToCommitStage(item.getShortName());
                            item.getAttribute().getStatus().remove(ScmItemStatus.CONFLICT);
                            item.getAttribute().getStatus().add(ScmItemStatus.CHANGED);
                            item.getAttribute().getStatus().add(ScmItemStatus.UNCOMMITED);
                        } else {
                            Context.getGitemberService().removeMissedFile(item.getShortName());
                            item.getAttribute().getStatus().remove(ScmItemStatus.MISSED);
                            item.getAttribute().getStatus().add(ScmItemStatus.REMOVED);
                        }
                    } else if (ScmItemStatus.CONFLICT_ADDED_BY_US.equals(item.getAttribute().getSubstatus())
                            || ScmItemStatus.CONFLICT_ADDED_BY_THEM.equals(item.getAttribute().getSubstatus())
                            || ScmItemStatus.CONFLICT_BOTH_ADDED.equals(item.getAttribute().getSubstatus())
                            || ScmItemStatus.CONFLICT_BOTH_MODIFIED.equals(item.getAttribute().getSubstatus())) {
                        Context.getGitemberService().addFileToCommitStage(item.getShortName());
                        item.getAttribute().getStatus().remove(ScmItemStatus.CONFLICT);
                        item.getAttribute().getStatus().add(ScmItemStatus.CHANGED);
                        item.getAttribute().getStatus().add(ScmItemStatus.UNCOMMITED);
                    }

                }
            }
        } catch (Exception e) {
            GitemberUITool.showException("Cannot add item " + item.getShortName() + " to stage", e);
        }
    }

    @SuppressWarnings("unused")
    public static Parent openWorkingCopyHandler(ScmBranch branch,
                                                MenuBar menuBar,
                                                ToolBar toolBar,
                                                Consumer<Object> onStashCreated) {

        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = WorkingCopyController.class.getResource("/fxml/WorkingCopyPane.fxml").openStream()) {
            final Parent workCopyView = fxmlLoader.load(is);
            final WorkingCopyController workingCopyController = fxmlLoader.getController();
            workingCopyController.onStashCreated = onStashCreated;
            workingCopyController.open(branch, null);

            menuBar.getMenus().add(2, workingCopyController.workingCopyMenu);


            toolBar.getItems().add(workingCopyController.stageAllBtn);
            toolBar.getItems().add(workingCopyController.unStageAllBtn);
            toolBar.getItems().add(workingCopyController.commitBtn);
            toolBar.getItems().add(workingCopyController.stashBtn);
            toolBar.getItems().add(workingCopyController.refreshBtn);
            toolBar.getItems().add(workingCopyController.spacerPane);
            toolBar.getItems().add(workingCopyController.searchLabel);
            toolBar.getItems().add(workingCopyController.searchText);
            // toolBar.getItems().add(workingCopyController.searchButton);

            GitemberApp.setWorkingCopyController(workingCopyController);


            return workCopyView;
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Cannot open working copy view", ioe.getMessage());
            GitemberUITool.showException("Cannot open working copy view", ioe);
        }

        return null;


    }


}
