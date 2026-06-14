package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwshared.networking.NetworkMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @brief Controller JavaFX per la gestione della schermata di attesa e del matchmaking.
 */
public class WaitingViewController implements Initializable {
    /** @brief Etichetta che mostra lo stato corrente del matchmaking. */
    @FXML
    private Label lblStatus;
    
    /** @brief Indicatore di caricamento attivo durante l'attesa del matchmaking. */
    @FXML
    private ProgressIndicator progressIndicator;
    
    /** @brief Pulsante per annullare il matchmaking e tornare alla lobby. */
    @FXML
    private Button btnCancel;

    /** @brief Riferimento alla connessione verso il server. */
    private ClientConnection connection;
    
    /** @brief Identificativo dell'utente. */
    private String username;

    /**
     * @brief Configura la connessione di rete del client.
     * @param[in] connection Istanza attiva di ClientConnection.
     */
    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * @brief Imposta lo username dell'utente in sessione.
     * @param[in] username Nome utente corrente.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @brief Gestore dell'evento click sul pulsante di annullamento per tornare alla lobby.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onCancel(ActionEvent event) {
        try {
            if (connection != null) {
                connection.send(new NetworkMessage.ClientDisconnect());
            }
            
            // Tornare indietro alla Lobby
            LobbyViewController ctrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
            ctrl.setConnection(connection);
            ctrl.setUsername(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Inizializza il controller della schermata di attesa.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto radice.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * @brief Aggiorna la UI per mostrare che l'avversario è stato trovato.
     * @details Viene chiamato da ClientConnectionCreator quando arriva MATCH_FOUND.
     */
    public void showMatchFound() {
        if (lblStatus != null) {
            lblStatus.setText("Avversario trovato! Preparazione partita...");
        }
        if (btnCancel != null) {
            btnCancel.setDisable(true); // Da questo momento, impossibile annullare
        }
    }
}
