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


    private static  WorkingCopyController workingCopyController = null;

    public final static SortedSet<String> entries = new TreeSet<>();


    public static Stage getMainStage() {
        return mainStage;
    }



    @Override
    public void start(Stage stage) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();

        try (InputStream is = WorkingCopyController.class.getResource("/fxml/MainViewPane.fxml").openStream()) {
            Parent root = fxmlLoader.load(is);

            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int minus = 100;
            int width = gd.getDisplayMode().getWidth() - minus;
            int height = gd.getDisplayMode().getHeight() - minus;


            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Const.DEFAULT_CSS);


            mainStage = stage;
            mainStage.setTitle(Const.TITLE);
            stage.setScene(scene);
            stage.getIcons().add(new Image(GitemberApp.class.getResourceAsStream(Const.ICON)));
            stage.show();



            stage.focusedProperty().addListener(new ChangeListener<Boolean>()  {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown)       {

                    System.out.println("Got focus");

                }
            });

        }

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
            (new WindowCacheConfig()).install();
        } catch (IOException e) {
            e.printStackTrace();
        }
        launch(args);
    }


    public static void setWorkingCopyController(WorkingCopyController workingCopyController) {
        GitemberApp.workingCopyController = workingCopyController;
    }

}
