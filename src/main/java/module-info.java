module org.example.projeto_skala {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.dlsc.fxmlkit;
    requires org.apache.poi.poi;
    requires com.google.gson;

    opens org.example.projeto_skala to javafx.fxml;
    opens org.example.projeto_skala.objetos to com.google.gson;

    exports org.example.projeto_skala;
}