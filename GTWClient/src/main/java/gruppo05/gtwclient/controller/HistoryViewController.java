package gruppo05.gtwclient.controller;

import gruppo05.gtwclient.networking.ClientConnection;
import gruppo05.gtwshared.dto.CallbackDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller JavaFX per la gestione della schermata dello storico delle partite.
 */
public class HistoryViewController implements Initializable {
    @FXML
    private Label lblWon;
    @FXML
    private Label lblPlayed;
    @FXML
    private Label lblAvgTime;
    @FXML
    private TableView<CallbackDTO.MatchRecord> tblHistory;
    @FXML
    private TableColumn<CallbackDTO.MatchRecord, String> colOpponent;
    @FXML
    private TableColumn<CallbackDTO.MatchRecord, String> colResult;
    @FXML
    private TableColumn<CallbackDTO.MatchRecord, String> colDate;
    @FXML
    private TableColumn<CallbackDTO.MatchRecord, Integer> colTime;
    @FXML
    private Button btnBack;

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
     * @brief Imposta lo username dell'utente in sessione.
     * @param[in] username Nome utente corrente.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @brief Mostra lo storico delle partite dell'utente e le relative statistiche.
     * @param[in] dto Dati di callback contenenti le statistiche e lo storico.
     */
    public void showHistory(CallbackDTO dto) {
        lblWon.setText(String.valueOf(dto.getTotalMatchesWon()));
        lblPlayed.setText(String.valueOf(dto.getTotalMatchesPlayed()));
        lblAvgTime.setText(String.format("%.1f ms", dto.getAvgResponseTime()));

        if (dto.getMatchHistory() != null) {
            ObservableList<CallbackDTO.MatchRecord> historyList = FXCollections.observableArrayList(dto.getMatchHistory());
            tblHistory.setItems(historyList);
        }
    }

    /**
     * @brief Gestore dell'evento click sul pulsante per ritornare alla lobby.
     * @param[in] event Evento di azione scatenato dal click.
     */
    @FXML
    void onBack(ActionEvent event) {
        try {
            LobbyViewController ctrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
            ctrl.setConnection(connection);
            ctrl.setUsername(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Inizializza i componenti grafici e la tabella della cronologia.
     * @param[in] location Percorso relativo per la risoluzione dell'oggetto radice.
     * @param[in] resources Risorse localizzate.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colOpponent.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("responseTime"));

        colDate.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDate();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(date.format(formatter));
            }
            return new SimpleStringProperty("");
        });
    }
}
