package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwshared.dto.CallbackDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @brief Controller JavaFX per la gestione della schermata dei risultati di gioco.
 */
public class ResultViewController implements Initializable {
    /** @brief Etichetta che mostra l'esito della partita. */
    @FXML
    private Label lblResult;
    
    /** @brief Etichetta che rivela la soluzione. */
    @FXML
    private Label lblCorrectWord;
    
    /** @brief Etichetta che indica lo username del vincitore. */
    @FXML
    private Label lblWinner;
    
    /** @brief Etichetta che mostra il tempo di risposta. */
    @FXML
    private Label lblResponseTime;
    
    /** @brief Pulsante per chiudere i risultati e tornare alla lobby. */
    @FXML
    private Button btnBackToLobby;

    /** @brief Connessione attiva del client. */
    private ClientConnection connection;
    
    /** @brief Username del giocatore corrente. */
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
     * @brief Mostra i risultati finali della sfida.
     * @param[in] dto Dati di callback contenenti i risultati della partita.
     */
    public void showResult(CallbackDTO dto) {
        if (dto.getGameResult() != null) {
            lblResult.setText(dto.getGameResult().toString());
        }
        lblCorrectWord.setText("La parola corretta era: " + dto.getCorrectWord());
        
        if (dto.getWinnerUsername() != null && !dto.getWinnerUsername().isEmpty()) {
            lblWinner.setText("Vincitore: " + dto.getWinnerUsername());
            lblResponseTime.setText(String.format("Tempo di risposta: %.2f s", dto.getResponseTime() / 1000.0));
        } else {
            lblWinner.setText("Pareggio!");
            lblResponseTime.setText("Nessun vincitore");
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante per ritornare alla lobby.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onBackToLobby(ActionEvent event) {
        try {
            LobbyViewController ctrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
            ctrl.setConnection(connection);
            ctrl.setUsername(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Inizializza i componenti grafici del controller dei risultati.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto radice.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
