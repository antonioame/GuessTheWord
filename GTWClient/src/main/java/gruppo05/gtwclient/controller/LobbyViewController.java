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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Toggle;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @brief Controller JavaFX per la gestione della schermata principale della Lobby.
 * 
 * @details Gestisce i re-indirizzamenti e i comandi principali dell'utente, come
 *          l'avvio della partita (matchmaking), la consultazione dello storico ed il logout.
 */
public class LobbyViewController implements Initializable {
    /** @brief Etichetta per mostrare il messaggio di benvenuto. */
    @FXML
    private Label lblWelcome;
    
    /** @brief Pulsante per avviare il matchmaking. */
    @FXML
    private Button btnPlay;
    
    /** @brief Pulsante per accedere allo storico partite. */
    @FXML
    private Button btnHistory;
    
    /** @brief Pulsante per uscire dall'applicazione. */
    @FXML
    private Button btnExit;
    
    /** @brief Pulsante di selezione per la difficoltà facile. */
    @FXML
    private ToggleButton btnEasy;
    
    /** @brief Pulsante di selezione per la difficoltà normale. */
    @FXML
    private ToggleButton btnNormal;
    
    /** @brief Pulsante di selezione per la difficoltà difficile. */
    @FXML
    private ToggleButton btnHard;

    /** @brief Gruppo di pulsanti per la selezione esclusiva della difficoltà. */
    private ToggleGroup difficultyGroup;

    /** @brief Connessione di rete del client verso il server. */
    private ClientConnection connection;    
    
    /** @brief Username dell'utente loggato. */
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
                Difficulty selectedDifficulty = getSelectedDifficulty();
                connection.send(new NetworkMessage.PlayRequest(selectedDifficulty));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Recupera la difficoltà attualmente selezionata dall'utente.
     * @return Difficoltà scelta (EASY, NORMAL, o HARD). Se nulla è selezionato, restituisce NORMAL come fallback.
     */
    private Difficulty getSelectedDifficulty() {
        if (difficultyGroup == null) return Difficulty.NORMAL;
        Toggle selected = difficultyGroup.getSelectedToggle();
        if (selected == btnEasy)   return Difficulty.EASY;
        if (selected == btnHard)   return Difficulty.HARD;
        return Difficulty.NORMAL;
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

        difficultyGroup = new ToggleGroup();
        if (btnEasy != null) btnEasy.setToggleGroup(difficultyGroup);
        if (btnNormal != null) {
            btnNormal.setToggleGroup(difficultyGroup);
            btnNormal.setSelected(true);
        }
        if (btnHard != null) btnHard.setToggleGroup(difficultyGroup);

        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                // Previene che non sia selezionato alcun Toggle
                oldToggle.setSelected(true);
            }
        });
    }
}
