package gruppo05.gtwserver.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Utility Class per Routing e Navigazione della Finestra Server.
 */
public class SceneNavigator {
    private static Stage primaryStage;

    /**
     * @brief Inizializza lo stage primario per la navigazione.
     * @param[in] stage Stage primario dell'applicazione.
     */
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /**
     * @brief Restituisce lo stage primario attualmente in uso.
     * @return Stage primario dell'applicazione.
     */
    public static Stage getStage() {
        return primaryStage;
    }

    /**
     * @brief Carica ed imposta una nuova scena a partire dal file FXML indicato.
     * @param[in] fxmlPath Percorso della risorsa FXML nel classpath.
     * @throws IOException In caso di problemi nel caricamento del file FXML.
     */
    public static void navigateTo(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * @brief Imposta una nuova scena a partire dal file FXML e ne restituisce il controller associato.
     * @param[in] fxmlPath Percorso della risorsa FXML nel classpath.
     * @return Controller tipizzato rispetto alla schermata caricata.
     * @throws IOException In caso di problemi nel caricamento del file FXML.
     */
    public static <T> T navigateAndGetController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        return loader.getController();
    }
}
