package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.networking.NetworkMessage;
import gruppo05.gtwshared.utility.Difficulty;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller JavaFX per la gestione della schermata di gioco.
 */
public class GameViewController implements Initializable {
    @FXML
    private Label lblOpponent;
    @FXML
    private TextArea txaCipheredText;
    @FXML
    private TextField txfAnswer;
    @FXML
    private Button btnSubmit;
    @FXML
    private Label lblTimer;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblDifficulty;

    private ClientConnection connection;
    private String username;
    private long startTime;
    private Timeline timeline;
    private boolean gameActive = false;

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
     * @brief Inizializza lo stato del gioco con i dati della sfida corrente.
     * @param[in] dto Dati di callback contenenti le informazioni sulla sfida.
     */
    public void initGame(CallbackDTO dto) {
        txaCipheredText.setText(dto.getCipheredText());
        lblOpponent.setText("Avversario: " + dto.getOpponentUsername());
        lblStatus.setText("In gioco");
        
        txfAnswer.clear();
        txfAnswer.setDisable(false);
        btnSubmit.setDisable(true); // Disabilitato fino a quando viene inserito del testo
        
        Difficulty diff = dto.getDifficulty();
        if (diff != null && lblDifficulty != null) {
            lblDifficulty.setText(diff.name());
            String color;
            switch (diff) {
                case EASY: color = "#27ae60"; break;
                case HARD: color = "#c0392b"; break;
                default: color = "#e67e22"; break;
            }
            lblDifficulty.setStyle(lblDifficulty.getStyle() + "; -fx-background-color: " + color + ";");
        }

        this.gameActive = true;
        this.startTime = System.currentTimeMillis();
        
        startCountdown(dto.getTimer());
    }

    /**
     * @brief Aggiorna l'interfaccia segnalando che l'avversario ha risposto.
     */
    public void showOpponentAnswered() {
        if (gameActive) {
            lblStatus.setText("L'avversario ha risposto!");
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante di invio risposta.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onSubmit(ActionEvent event) {
        // NON fermare il timer qui, in modo da consentire nuovi tentativi se la parola inviata è errata.
        // In questo modo, il giocatore può ritentare l'inserimento di una nuova parola.
        
        gameActive = false;
        btnSubmit.setDisable(true);
        txfAnswer.setDisable(true);
        lblStatus.setText("Risposta inviata. In attesa del risultato...");

        long endTime = System.currentTimeMillis();
        int responseTime = (int) (endTime - startTime);

        try {
            if (connection != null) {
                connection.send(new NetworkMessage.AnswerSubmission(txfAnswer.getText(), responseTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Gestisce un tentativo errato pulendo l'interfaccia e permettendo all'utente di riprovare con un nuovo inserimento.
     * @details Consente un inserimento rapido di una nuova risposta, in modo tale da non far perdere tempo all'utente
     *          nel cancellare l'ultima risposta inviata come tentativo errato.
     */
    public void showWrongAnswer() {
        if (timeline != null) {
            gameActive = true;
            lblStatus.setText("Tentativo errato, riprova!");
            txfAnswer.clear();
            txfAnswer.setDisable(false);
            btnSubmit.setDisable(true);
            txfAnswer.requestFocus();
        }
    }

    /**
     * @brief Avvia il timer per il countdown del turno corrente.
     * @param[in] seconds Numero di secondi per la durata del turno.
     */
    private void startCountdown(int seconds) {
        if (timeline != null) {
            timeline.stop();
        }
        final int[] timeRemaining = {seconds};
        lblTimer.setText(String.valueOf(timeRemaining[0]));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeRemaining[0]--;
            lblTimer.setText(String.valueOf(timeRemaining[0]));
            if (timeRemaining[0] <= 0) {
                timeline.stop();
                gameActive = false;
                btnSubmit.setDisable(true);
                txfAnswer.setDisable(true);
                lblStatus.setText("Tempo scaduto!");
                
                try {
                    if (connection != null) {
                        connection.send(new NetworkMessage.AnswerSubmission("", seconds * 1000));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * @brief Inizializza i componenti grafici del controller del gioco.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto radice.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txfAnswer.textProperty().addListener((observable, oldValue, newValue) -> {
            if (gameActive) {
                btnSubmit.setDisable(newValue.trim().isEmpty());
            }
        });
    }
}
