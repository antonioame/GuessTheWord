package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.Difficulty;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller JavaFX per la gestione della schermata principale della Lobby.
 * Gestisce i re-indirizzamenti e i comandi principali dell'utente, come
 * l'avvio della partita (matchmaking), la consultazione dello storico ed il logout.
 */
public class LobbyViewController implements Initializable {
    @FXML
    private Label lblWelcome;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnHistory;
    @FXML
    private Button btnExit;

    private ClientConnection connection;    
    private String username;

    /**
     * @brief Configura la connessione di rete del client.
     * @param[in] connection Istanza attiva di ClientConnection.
     */
    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * @brief Imposta lo username dell'utente loggato nella sessione corrente ed aggiorna l'interfaccia grafica.
     * @param[in] username Nome utente corrente.
     */
    public void setUsername(String username) {
        this.username = username;
        if (lblWelcome != null) {
            lblWelcome.setText("Benvenuto, " + username + "!");
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante "Gioca Partita" per avviare il matchmaking.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onPlay(ActionEvent event) {
        try {
            if (connection != null) {
                connection.send(new NetworkMessage.PlayRequest(Difficulty.NORMAL));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante "Storico Partite" per recuperare lo storico delle partite.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onHistory(ActionEvent event) {
        try {
            if (connection != null) {
                connection.send(new NetworkMessage.HistoryRequest());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante "Esci" per disconnettersi ed uscire dall'applicazione.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onExit(ActionEvent event) {
        try {
            if (connection != null) {
                connection.send(new NetworkMessage.ClientDisconnect());
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * @brief Inizializza l'interfaccia grafica impostando il messaggio di benvenuto.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto root.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (username != null) {
            lblWelcome.setText("Benvenuto, " + username + "!");
        }
    }
}
