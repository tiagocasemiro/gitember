package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.GitemberSettings;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.service.GitemberServiceImpl;
import com.az.gitember.service.SettingsServiceImpl;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.storage.file.WindowCacheConfig;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.LogManager;


public class GitemberApp extends Application {

    private static Stage mainStage = null;

    public static StringProperty currentRepositoryPath = new SimpleStringProperty();
    public static StringProperty remoteUrl = new SimpleStringProperty();
    public static ObjectProperty<ScmBranch> workingBranch = new SimpleObjectProperty<ScmBranch>();

    //todo
    public static GitRepositoryService repositoryService = new GitRepositoryService();
    private static SettingsServiceImpl settingsService = new SettingsServiceImpl();
    private static GitemberServiceImpl gitemberService = new GitemberServiceImpl();
    private static  WorkingCopyController workingCopyController = null;

    public final static SortedSet<String> entries = new TreeSet<>();


    public static void setWorkingBranch(ScmBranch workingBranch) throws Exception {
        GitemberApp.workingBranch.setValue(workingBranch);
        String head = getRepositoryService().getHead().getFirst();
        GitemberApp.setTitle(Const.TITLE + getCurrentRepositoryPathWOGit() + " " + head);

    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static String getCurrentRepositoryPathWOGit() {
        if (currentRepositoryPath != null) {
            return FilenameUtils.getFullPathNoEndSeparator(currentRepositoryPath.getValue());
        }
        return null;
    }

    public static GitRepositoryService getRepositoryService() {
        return repositoryService;
    }



    public static GitemberServiceImpl getGitemberService() {
        return gitemberService;
    }

    public static void setTitle(String title) {
        mainStage.setTitle(title);
    }

    public static SettingsServiceImpl getSettingsService() {
        return settingsService;
    }


    public static void applySettings(GitemberSettings newGitemberSettings) {

    }


    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();

        try (InputStream is = WorkingCopyController.class.getResource("/fxml/Scene.fxml").openStream()) {
            Parent root = fxmlLoader.load(is);
            FXMLController controller = fxmlLoader.getController();
            gitemberService.setProgressBar(controller.progressBar);
            gitemberService.setOperationProgressBar(controller.operationProgressBar);
            gitemberService.setOperationName(controller.operationName);
            applySettings(getSettingsService().getGitemberSettings());


            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int minus = 100;
            int width = gd.getDisplayMode().getWidth() - minus;
            int height = gd.getDisplayMode().getHeight() - minus;


            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Const.DEFAULT_CSS);


            mainStage = stage;
            setTitle(Const.TITLE);
            stage.setScene(scene);
            stage.getIcons().add(new Image(GitemberApp.class.getResourceAsStream(Const.ICON)));
            stage.show();

            stage.setOnCloseRequest(
                    e -> GitRepositoryService.cleanUpTempFiles()
            );

            stage.focusedProperty().addListener(new ChangeListener<Boolean>()  {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown)       {

                    if(onShown  && (workingCopyController instanceof  WorkingCopyController) ) {
                        ((WorkingCopyController) workingCopyController).refreshBtnHandler(null);
                    }

                }
            });

        }

    }

    public static Optional<ButtonType> showResult(String text, Alert.AlertType alertTypet) {
        GridPane gridPane = null;
        if (StringUtils.isNotBlank(text)) {
            TextArea textArea = new TextArea(text);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            gridPane = new GridPane();
            gridPane.setMaxWidth(Double.MAX_VALUE);
            gridPane.add(textArea, 0, 0);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane.setFillWidth(textArea, true);

        }

        Alert alert = new Alert(alertTypet);
        alert.setWidth(600);
        alert.setTitle("Result");
        //alert.setContentText(text);
        if (StringUtils.isNotBlank(text)) {
            alert.getDialogPane().setContent(gridPane);
        }

        return alert.showAndWait();
    }

    public static void showException(String text, Throwable ex) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setWidth(600);
        alert.setTitle("Result");
        alert.setContentText(text);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();

    }

    public static void setWorkingCopyController(WorkingCopyController workingCopyController) {
        GitemberApp.workingCopyController = workingCopyController;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            LogManager.getLogManager().readConfiguration(GitemberApp.class.getResourceAsStream("/log.properties"));
            WindowCacheConfig c = new WindowCacheConfig();
            c.install();
        } catch (IOException e) {
            e.printStackTrace();

        }
        launch(args);
    }

}
