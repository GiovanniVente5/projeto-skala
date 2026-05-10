package org.example.projeto_skala;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SkalaApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(SkalaApplication.class.getResource("MenuView.fxml"));
        Scene cena = new Scene(fxmlLoader.load(), 700,700);
        stage.setTitle("Skala Convertor");
        stage.setScene(cena);
        stage.show();
    }
}
