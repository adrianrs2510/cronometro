module org.example.cronometro {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.cronometro to javafx.fxml;
    exports org.example.cronometro;
}