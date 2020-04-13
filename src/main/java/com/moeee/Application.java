package com.moeee;

import com.moeee.controller.RootController;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Title: Application<br>
 * Description: 程序启动类<br>
 * Create DateTime: 2020年04月07日<br>
 *
 * @author MoEee
 */
public class Application extends javafx.application.Application {

    public static final String APP_NAME = "Bilibili Native Downloader";
    public static final String APP_VERSION = "1.2";

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/fxml/root.fxml"));
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent root = fxmlLoader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.setTitle(String.join(" ", APP_NAME, APP_VERSION));
            RootController controller = fxmlLoader.getController();
            controller.init();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
