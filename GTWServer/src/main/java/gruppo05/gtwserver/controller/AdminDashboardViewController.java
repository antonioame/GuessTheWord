package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.PlayerDAO;
import gruppo05.gtwserver.db.ConcretePlayerDAO;
import gruppo05.gtwserver.model.Player;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.networking.ServerConnection;
import gruppo05.gtwserver.networking.ServerConnectionCreator;
import gruppo05.gtwserver.sourcemanager.api.SourceManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller JavaFX per il pannello di controllo dell'amministratore lato Server (Server Admin Dashboard).
 * Gestisce l'interfaccia grafica per il caricamento e la rimozione delle sorgenti dei documenti,
 * l'avanzamento dell'analisi delle parole e la visualizzazione della classifica dei giocatori.
 */
public class AdminDashboardViewController implements Initializable {
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblConnectedClients;
    @FXML
    private ListView<String> lstSources;
    @FXML
    private Button btnAddSource;
    @FXML
    private Button btnRemoveSource;
    @FXML
    private ProgressBar progressAnalysis;
    @FXML
    private Label lblAnalysisStatus;
    @FXML
    private TableView<Player> tblPlayers;
    @FXML
    private TableColumn<Player, String> colUsername;
    @FXML
    private TableColumn<Player, Integer> colWins;
    @FXML
    private TableColumn<Player, Integer> colPlayed;

    private ServerConnection connection;
    private SourceManager sourceManager;
    private final List<Source> loadedSources = new ArrayList<>();

    /**
     * @brief   Configura la connessione di rete e registra le callback evento per aggiornare
     *          il contatore dei client connessi (con meccanismo event-driven).
     * @details Alla chiamata:
     *          <ol>
     *            <li>Legge subito il numero di canali già attivi (client connessi prima del login).</li>
     *            <li>Registra {@code setOnClientConnected} su {@link ServerConnection} per ricevere
     *                notifiche sulle nuove connessioni TCP.</li>
     *            <li>Registra {@code setUiDisconnectCallback} su {@link ServerConnectionCreator}
     *                per ricevere notifiche sulle disconnessioni.</li>
     *          </ol>
     * @param[in] connection        Istanza attiva di ServerConnection.
     * @param[in] connectionCreator Istanza del creator che gestisce la logica di disconnessione.
     */
    public void setConnection(ServerConnection connection, ServerConnectionCreator connectionCreator) {
        this.connection = connection;

        // FASE 1) Lettura iniziale: catturare i client già connessi prima dell'attivazione della dashboard
        updateConnectedClients(connection.getActiveChannelCount());

        // FASE 2) Callback event-driven per le nuove connessioni TCP
        connection.setOnClientConnected(
            index -> updateConnectedClients(connection.getActiveChannelCount())
        );

        // FASE 3) Callback event-driven per le disconnessioni
        connectionCreator.setUiDisconnectCallback(
            index -> updateConnectedClients(connection.getActiveChannelCount())
        );
    }

    /**
     * @brief Configura il gestore delle sorgenti documentali per l'analisi dei testi.
     * @param[in] sourceManager Istanza del gestore delle sorgenti.
     */
    public void setSourceManager(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
    }

    /**
     * @brief Aggiorna graficamente il numero di client attualmente connessi al server.
     * @param[in] count Numero di client connessi.
     */
    public void updateConnectedClients(int count) {
        Platform.runLater(() -> lblConnectedClients.setText("Client connessi: " + count));
    }

    /**
     * @brief Gestore dell'evento click sul pulsante per aggiungere una sorgente documentale TXT.
     * Apre un selettore di file e avvia il caricamento e l'analisi asincrona del testo scelto.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onAddSource(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona File Documento TXT");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File di testo", "*.txt"));
        File file = fileChooser.showOpenDialog(SceneNavigator.getStage());

        if (file != null) {
            progressAnalysis.setVisible(true);
            progressAnalysis.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblAnalysisStatus.setText("Analisi e caricamento in corso...");

            Source source = new Source(file.toPath());

            if (sourceManager != null) {
                sourceManager.addSource(source,
                    () -> Platform.runLater(() -> {
                        loadedSources.add(source);
                        lstSources.getItems().add(file.getName());
                        progressAnalysis.setVisible(false);
                        progressAnalysis.setProgress(0);
                        lblAnalysisStatus.setText("Sorgente aggiunta con successo.");
                    }),
                    (exception) -> Platform.runLater(() -> {
                        progressAnalysis.setVisible(false);
                        progressAnalysis.setProgress(0);
                        lblAnalysisStatus.setText("Errore durante l'analisi.");
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore: " + exception.getMessage());
                        alert.showAndWait();
                    })
                );
            }
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante per rimuovere la sorgente documentale selezionata.
     * Rimuove la sorgente dall'elenco e dal gestore asincrono.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onRemoveSource(ActionEvent event) {
        int selectedIndex = lstSources.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Source source = loadedSources.get(selectedIndex);

            progressAnalysis.setVisible(true);
            progressAnalysis.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblAnalysisStatus.setText("Rimozione in corso...");

            if (sourceManager != null) {
                sourceManager.removeSource(source,
                    () -> Platform.runLater(() -> {
                        loadedSources.remove(selectedIndex);
                        lstSources.getItems().remove(selectedIndex);
                        progressAnalysis.setVisible(false);
                        progressAnalysis.setProgress(0);
                        lblAnalysisStatus.setText("Sorgente rimossa.");
                    }),
                    (exception) -> Platform.runLater(() -> {
                        progressAnalysis.setVisible(false);
                        progressAnalysis.setProgress(0);
                        lblAnalysisStatus.setText("Errore durante la rimozione.");
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Errore: " + exception.getMessage());
                        alert.showAndWait();
                    })
                );
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona una sorgente da rimuovere.");
            alert.showAndWait();
        }
    }

    /**
     * @brief Inizializza i componenti grafici del controller, configurando la tabella dei giocatori e i valori di default.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto root.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colUsername.setCellValueFactory(cellData -> {
            Player p = cellData.getValue();
            if (p != null) {
                return new SimpleStringProperty(p.getUsername());
            }
            return new SimpleStringProperty("");
        });
        colWins.setCellValueFactory(new PropertyValueFactory<>("totalGamesWon"));
        colPlayed.setCellValueFactory(new PropertyValueFactory<>("totalGamesPlayed"));

        try {
            List<Player> players = new ConcretePlayerDAO().selectAll();
            tblPlayers.setItems(FXCollections.observableArrayList(players));
        } catch (Exception e) {
            e.printStackTrace();
        }

        lblStatus.setText("In ascolto sulla porta: 5050");
        progressAnalysis.setVisible(false);
        lblAnalysisStatus.setText("Pronto");
    }
}
