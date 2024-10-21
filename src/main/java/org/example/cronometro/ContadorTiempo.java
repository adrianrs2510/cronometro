package org.example.cronometro;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ContadorTiempo extends Application {
    private TextField inputField;
    private ProgressBar progressBar;
    private Label tiempoLabel;
    private Button iniciarButton;
    private Button cancelarButton;
    private int tiempoTotal;
    private int tiempoActual;
    private boolean contando;
    private List<Integer> tiemposPredefinidos = new ArrayList<>();
    private ComboBox<Integer> tiemposComboBox = new ComboBox<>();
    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Contador de Tiempo");

        // Crear el layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Elementos de la interfaz
        Label instruccionLabel = new Label("Introduce el tiempo en segundos:");
        inputField = new TextField();
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        tiempoLabel = new Label("Tiempo: 0 segundos");
        iniciarButton = new Button("Iniciar");
        cancelarButton = new Button("Cancelar");
        cancelarButton.setDisable(true);

        // Cargar tiempos predefinidos
        cargarTiemposPredefinidos();

        // ComboBox para seleccionar tiempos predefinidos
        tiemposComboBox.getItems().addAll(tiemposPredefinidos);
        tiemposComboBox.setPromptText("Seleccionar tiempo");
        tiemposComboBox.setOnAction(e -> inputField.setText(tiemposComboBox.getValue().toString()));

        // ComboBox para cambiar de tema
        ComboBox<String> temaComboBox = new ComboBox<>();
        temaComboBox.getItems().addAll("Claro", "Oscuro");
        temaComboBox.setValue("Claro"); // Tema por defecto
        temaComboBox.setOnAction(e -> cambiarTema(temaComboBox.getValue()));

        // Añadir todos los elementos al layout
        root.getChildren().addAll(instruccionLabel, inputField, tiemposComboBox, progressBar, tiempoLabel, iniciarButton, cancelarButton, temaComboBox);

        // Crear la escena y aplicar tema por defecto
        scene = new Scene(root, 300, 350);
        cambiarTema("Claro"); // Inicia con el tema claro

        // Configurar la ventana principal
        primaryStage.setScene(scene);
        primaryStage.show();

        // Configurar acciones de los botones
        iniciarButton.setOnAction(e -> iniciarContador());
        cancelarButton.setOnAction(e -> cancelarContador());
    }

    // Método para cambiar el tema
    private void cambiarTema(String tema) {
        if (tema.equals("Oscuro")) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
        }
    }

    private void iniciarContador() {
        try {
            tiempoTotal = Integer.parseInt(inputField.getText());
            if (tiempoTotal <= 0) {
                throw new NumberFormatException();
            }
            tiempoActual = 0;
            contando = true;
            progressBar.setProgress(0);
            iniciarButton.setDisable(true);
            cancelarButton.setDisable(false);
            inputField.setDisable(true);
            new Thread(() -> {
                while (contando && tiempoActual < tiempoTotal) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    tiempoActual++;
                    Platform.runLater(this::actualizarUI);
                }
                if (tiempoActual >= tiempoTotal) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Tiempo completado");
                        alert.setHeaderText(null);
                        alert.setContentText("¡El tiempo ha finalizado! ¿Deseas guardar este tiempo?");
                        ButtonType guardar = new ButtonType("Guardar");
                        ButtonType noGuardar = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(guardar, noGuardar);

                        alert.showAndWait().ifPresent(response -> {
                            if (response == guardar) {
                                guardarTiempo();
                            }
                            reiniciarUI();
                        });
                    });
                }
            }).start();
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, introduce un número válido mayor que cero.");
            alert.showAndWait();
        }
    }

    private void cancelarContador() {
        contando = false;
        reiniciarUI();
    }

    private void actualizarUI() {
        double progreso = (double) tiempoActual / tiempoTotal;
        progressBar.setProgress(progreso);
        tiempoLabel.setText("Tiempo: " + formatearTiempo(tiempoActual));
    }

    private String formatearTiempo(int segundos) {
        int horas = segundos / 3600;
        int minutos = (segundos % 3600) / 60;
        int seg = segundos % 60;
        return String.format("%02d:%02d:%02d", horas, minutos, seg);
    }

    private void reiniciarUI() {
        iniciarButton.setDisable(false);
        cancelarButton.setDisable(true);
        inputField.setDisable(false);
        progressBar.setProgress(0);
        tiempoLabel.setText("Tiempo: 0 segundos");
    }

    private void guardarTiempo() {
        if (!tiemposPredefinidos.contains(tiempoTotal)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("tiempos_predefinidos.txt", true))) {
                // Guardar solo si el tiempo no existe
                writer.write(tiempoTotal + "\n");

                // Añadir el tiempo a la lista en memoria
                tiemposPredefinidos.add(tiempoTotal);

                // Actualizar el ComboBox con el nuevo tiempo
                tiemposComboBox.getItems().add(tiempoTotal);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Mostrar mensaje si el tiempo ya está guardado
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Tiempo duplicado");
            alert.setHeaderText(null);
            alert.setContentText("El tiempo ya está guardado en los predefinidos.");
            alert.showAndWait();
        }
    }

    private void cargarTiemposPredefinidos() {
        try (BufferedReader reader = new BufferedReader(new FileReader("tiempos_predefinidos.txt"))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                tiemposPredefinidos.add(Integer.parseInt(linea));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
