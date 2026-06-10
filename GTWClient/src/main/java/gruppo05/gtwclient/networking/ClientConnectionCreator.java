package gruppo05.gtwclient.networking;

import java.io.IOException;
import java.io.Serializable;
import javafx.application.Platform;
import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;
import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.networking.NetworkMessage;
import javafx.scene.control.Alert;
import gruppo05.gtwclient.controller.SceneNavigator;
import gruppo05.gtwclient.controller.LobbyViewController;
import gruppo05.gtwclient.controller.WaitingViewController;
import gruppo05.gtwclient.controller.GameViewController;
import gruppo05.gtwclient.controller.ResultViewController;
import gruppo05.gtwclient.controller.HistoryViewController;
import gruppo05.gtwclient.controller.ClientLoginManager;
import gruppo05.gtwclient.controller.ClientSignupManager;
import gruppo05.gtwshared.controller.LoginViewController;

/**
 * @class ClientConnectionCreator
 * @brief Factory e Controller di rete di altissimo livello per il lato Client.
 * @details Questa classe si occupa di instaurare la connessione TCP verso il server e funge 
 * da "traduttore" tra gli eventi di rete e l'interfaccia grafica. Riceve i pacchetti, li 
 * converte in DTO sicuri e demanda l'aggiornamento della UI al thread di JavaFX tramite 
 * {@link Platform#runLater}.
 * 
 * @author chiara
 * @version 2.0
 */
public class ClientConnectionCreator extends NetworkConnectionCreator {

    /**
     * @brief Riferimento alla connessione client attiva.
     */
    private ClientConnection connection;

    private GameViewController gameViewController;
    private HistoryViewController historyViewController;
    private ResultViewController resultViewController;

    public void setGameViewController(GameViewController gameViewController) {
        this.gameViewController = gameViewController;
    }

    public void setHistoryViewController(HistoryViewController historyViewController) {
        this.historyViewController = historyViewController;
    }

    public void setResultViewController(ResultViewController resultViewController) {
        this.resultViewController = resultViewController;
    }

    /**
     * @brief Inizializza la connessione verso il server leggendo i parametri locali.
     * @details Legge il file "client.properties" per recuperare IP e porta del server, 
     * istanzia il socket e registra le callback per i messaggi in arrivo e le disconnessioni.
     * @return L'istanza configurata e pronta all'uso di ClientConnection.
     */
    @Override
    public ClientConnection createConnection() {
        NetworkConfiguration config = this.readConfiguration("client.properties");
        
        this.connection = new ClientConnection(
                config.getIp(),     // Idirizzo IP del server
                config.getPort(),  // Porta del server
                this::handleMessage,   // onReceive
                (index) -> System.out.println("Disconnessione dal server o server offline.")  // onDisconnect
        );
        
        this.connection.connect();
        return this.connection;
    }

    /**
     * @brief Dispatcher centrale per tutti i messaggi ricevuti dal server.
     * @details Converte il NetworkMessage in CallbackDTO e, spostandosi sul thread di JavaFX, 
     * esegue gli switch di interfaccia o aggiorna i componenti visivi in base all'evento.
     * @param[in] channelIndex L'indice del canale (sempre 0 per il client).
     * @param[in] msg L'oggetto serializzato ricevuto dalla rete.
     */
    private void handleMessage(Integer channelIndex, Serializable msg) {
        // Scarta pacchetti non validi o non conformi al protocollo
        if (!(msg instanceof NetworkMessage)) return;
        
        // Converte il pacchetto in un DTO immutabile per una lettura sicura
        CallbackDTO dto = ((NetworkMessage) msg).toDTO();

        // Sposta l'esecuzione sul JavaFX Application Thread per evitare IllegalStateException
        Platform.runLater(() -> {
            
            // switch(interfaccia) - Logica di routing verso i Controller UI
            switch (dto.getEventType()) {
                
                case LOGIN_RESPONSE:
                    if (dto.isSuccess()) {
                        // SUCCESSO: L'utente è loggato.
                        // !!! SWITCH ALLA HOME
                        System.out.println("Login accettato.");
                        try {
                            LobbyViewController lobbyCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
                            lobbyCtrl.setConnection(connection);
                            lobbyCtrl.setUsername(connection.getUsername());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // FALLIMENTO: Credenziali errate.
                        // ALERT LOGIN FALLITO
                        Alert alert = new Alert(Alert.AlertType.ERROR, "L'utente non è registrato");
                        alert.showAndWait();
                    }
                    break;
                    
                case REGISTER_RESPONSE:
                    if (dto.isSuccess()) {
                        // SUCCESSO: Registrazione completata.
                        // !!! SWITCH LOGIN
                        System.out.println("Registrazione accettata.");
                        try {
                            LoginViewController loginCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwshared/controller/LoginView.fxml");
                            loginCtrl.setLoginManager(new ClientLoginManager(connection));
                            loginCtrl.setSignupManager(new ClientSignupManager(connection));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // FALLIMENTO: Username occupato o errore DB.
                        // ALERT REGISTRAZIONE FALLITA
                        Alert alert = new Alert(Alert.AlertType.ERROR, "La registrazione non è andata a buon fine");
                        alert.showAndWait();
                    }
                    break;
                    
                case PLAY_RESPONSE:
                    if (dto.getStatus() == CallbackDTO.Status.MATCH_FOUND) {
                        // MATCH TROVATO: Il server ha accoppiato i giocatori.
                        System.out.println("Avversario trovato! Preparazione pagina di gioco...");
                        // !!! SWITCH PAGINA GAME
                    } else {
                        // IN ATTESA: Il server ci ha messi in coda.
                        // !!! SWITCH ATTESA
                        System.out.println("In attesa di un avversario.");
                        try {
                            WaitingViewController waitingCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/WaitingView.fxml");
                            waitingCtrl.setConnection(connection);
                            waitingCtrl.setUsername(connection.getUsername());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    
                case GAME_START:
                    // INIZIO PARTITA: Abbiamo tutti i dati per giocare.
                    // !!! GAME CONTROLLER A CUI PASSARE
                    // 1. dto.getCipheredText() (la stringa con gli asterischi)
                    // 2. dto.getTimer() (per far partire il countdown grafico)
                    // 3. dto.getOpponentUsername() 
                    // 4. dto.getChallengeCode() 
                    System.out.println("Inizio partita contro " + dto.getOpponentUsername());
                    if (gameViewController != null) {
                        gameViewController.initGame(dto);
                    }
                    break;
                    
                case OPPONENT_ANSWERED:
                    // !!! NOTIFICA IN-GAME: L'avversario ha premuto "Invia".
                    // Dovrebbe mostrare qualcosa che faccia capire che l'avversario ha risposto
                    System.out.println("L'avversario ha sottomesso la sua risposta!");
                    if (gameViewController != null) {
                        gameViewController.showOpponentAnswered();
                    }
                    break;
                    
                case GAME_RESULT:
                    // FINE PARTITA: Il server decreta il risultato.
                    // !!! SWITCH RISULTATO, si deve passare:
                    // 1. dto.getGameResult() (WIN, LOSE, DRAW)
                    // 2. dto.getCorrectWord() (Per mostrare la soluzione reale se l'utente ha sbagliato)
                    // 3. dto.getWinnerUsername() (Per dichiarare chi ha vinto)
                    System.out.println("Partita terminata! Esito: " + dto.getGameResult() + ". Parola: " + dto.getCorrectWord());
                    try {
                        ResultViewController resultCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/ResultView.fxml");
                        resultCtrl.setConnection(connection);
                        resultCtrl.setUsername(connection.getUsername());
                        resultCtrl.showResult(dto);
                        this.resultViewController = resultCtrl;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case HISTORY_RESPONSE:
                    // DATI RICEVUTI: Popolamento statistiche.
                    // !!! SWITCH STORICO, si passano:
                    // 1. dto.getMatchHistory() (Lista da iniettare in una TableView)
                    // 2. dto.getTotalMatchesWon(), dto.getTotalMatchesPlayed() (Per aggiornare i contatori)
                    // 3. dto.getAvgResponseTime() (Per mostrare il grafico o label della media)
                    // 4. dto.getTotalPlayedTime() (Per mostrare il tempo totale giocato)
                    System.out.println("Ricevuto storico partite.");
                    try {
                        HistoryViewController historyCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/HistoryView.fxml");
                        historyCtrl.setConnection(connection);
                        historyCtrl.setUsername(connection.getUsername());
                        historyCtrl.showHistory(dto);
                        this.historyViewController = historyCtrl;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                    
                case OPPONENT_DISCONNECTED:
                    // DISCONNESSIONE ANOMALA: L'avversario ha chiuso il gioco durante il match.
                    // ALERT 
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'avversario si è disconnesso!");
                    alert.setHeaderText("Partita Interrotta");
                    alert.showAndWait(); // Attende che l'utente clicchi OK
                    // !!! SWITCH HOME
                    System.out.println("L'avversario si è disconnesso");
                    try {
                        LobbyViewController lobbyCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
                        lobbyCtrl.setConnection(connection);
                        lobbyCtrl.setUsername(connection.getUsername());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                    
                case TEXT_MESSAGE:
                    // NOTIFICA GENERICA: Messaggi di sistema dal server.
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "Messaggio dal Server: " + dto.getMessage());
                    infoAlert.setHeaderText("Notifica");
                    infoAlert.showAndWait(); 

                    System.out.println("Notifica dal Server: " + dto.getMessage());
                    break;
                    
                default:
                    // CATCH-ALL per pacchetti sconosciuti o implementazioni future.
                    System.out.println("Evento ignorato o non gestito: " + dto.getEventType());
            }
        });
    }
}
