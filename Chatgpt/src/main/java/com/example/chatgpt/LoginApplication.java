package com.example.chatgpt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 875, 450);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();

        LoginController controller = fxmlLoader.getController();
        controller.testDatabaseConnection();

    }

    public static void main(String[] args) {
        launch();
    }
}
