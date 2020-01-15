package com.az.gitember.misc;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class GitemberUITool {

    public static Optional<ButtonType> showResult(
            final String title, final String text, final Alert.AlertType alertType) {

        final GridPane gridPane = new GridPane();
        final TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.setMaxHeight(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);

        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);


        final Alert alert = new Alert(alertType);
        alert.setWidth(Const.ALERT_WIDTH);
        alert.setTitle(title);
        alert.getDialogPane().setContent(gridPane);

        return alert.showAndWait();
    }

    public static void showException(String title, Throwable ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        showResult(title, sw.toString(), Alert.AlertType.ERROR);
    }
    public static void showException(Throwable ex) {
        showException("Error", ex);
    }

    public static Optional<ButtonType>  showResult(final String text, final Alert.AlertType alertType) {
        return showResult("Code", text, alertType);
    }

    public static void showResult(final String text) {
        showResult("Code", text, Alert.AlertType.INFORMATION);
    }

}
