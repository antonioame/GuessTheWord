package gruppo05.gtwclient.networking;

import java.io.IOException;
import java.io.Serializable;
import javafx.application.Platform;
import gruppo05.gtwshared.networking.NetworkConfiguration;
import gruppo05.gtwshared.networking.NetworkConnectionCreator;
import gruppo05.gtwshared.dto.CallbackDTO;
import gruppo05.gtwshared.networking.NetworkMessage;
import javafx.scene.control.Alert;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwclient.controller.LobbyViewController;
import gruppo05.gtwclient.controller.WaitingViewController;
import gruppo05.gtwclient.controller.GameViewController;
import gruppo05.gtwclient.controller.ResultViewController;
import gruppo05.gtwclient.controller.HistoryViewController;
import gruppo05.gtwclient.controller.ClientLoginManager;
import gruppo05.gtwclient.controller.ClientSignupManager;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.controller.SignupViewController;

/**
 * @class ClientConnectionCreator
 * @brief Factory e Controller di rete di altissimo livello per il lato Client.
 * * @details Questa classe estende {@link NetworkConnectionCreator} per gestire l'instaurazione 
 * della connessione TCP con il server. Agisce come mediatore tra il layer di rete e l'interfaccia 
 * grafica, ricevendo i messaggi, convertendoli in DTO e aggiornando la UI in modo thread-safe.
 * * @author chiara
 * @version 1.0
 */
public class ClientConnectionCreator extends NetworkConnectionCreator {

    /** Riferimento alla connessione attiva verso il server. */
    private ClientConnection connection;

    /** Controller per la gestione della logica di gioco. */
    private GameViewController gameViewController;
    
    /** Controller per la visualizzazione dello storico partite. */
    private HistoryViewController historyViewController;
    
    /** Controller per la visualizzazione dei risultati finali. */
    private ResultViewController resultViewController;
    
    private Runnable onServerDisconnect = null;

    /**
     * Imposta il controller della vista di gioco.
     * @param gameViewController Il controller della GameView.
     */
    public void setGameViewController(GameViewController gameViewController) {
        this.gameViewController = gameViewController;
    }

    /**
     * Imposta il controller della vista dello storico.
     * @param historyViewController Il controller della HistoryView.
     */
    public void setHistoryViewController(HistoryViewController historyViewController) {
        this.historyViewController = historyViewController;
    }

    /**
     * Imposta il controller della vista dei risultati.
     * @param resultViewController Il controller della ResultView.
     */
    public void setResultViewController(ResultViewController resultViewController) {
        this.resultViewController = resultViewController;
    }

    public void setOnServerDisconnect(Runnable callback) {
        this.onServerDisconnect = callback;
    }

    /**
     * @brief Inizializza la connessione verso il server leggendo i parametri di configurazione.
     * * @details Legge il file "client.properties", configura il socket e definisce le callback
     * per gestire i messaggi in entrata e le interruzioni di linea.
     * * @return L'istanza configurata di ClientConnection.
     */
    @Override
    public ClientConnection createConnection() {
        // Carica IP e porta dal file di configurazione
        NetworkConfiguration config = this.readConfiguration("client.properties");
        
        // Istanzia la connessione definendo le lambda per gestire ricezione e disconnessione
        this.connection = new ClientConnection(
                config.getIp(),
                config.getPort(),
                this::handleMessage,
                (index) -> handleServerDisconnect()
        );
        
        this.connection.connect();
        return this.connection;
    }

    /**
     * @brief Gestisce la disconnessione improvvisa del server.
     * @details Viene invocata dal thread di rete (non Thread JavaFX).
     * Usa {@link Platform#runLater} per mostrare un Alert bloccante
     * sul thread UI e poi eseguire il callback configurato
     * (navigazione per tornare alla LoginView).
     */
    private void handleServerDisconnect() {
        System.out.println("[ClientConnection] Disconnessione dal server o server offline.");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "La connessione con il server è stata interrotta inaspettatamente.");
            alert.setHeaderText("Server disconnesso");
            alert.showAndWait();
            if (onServerDisconnect != null) {
                onServerDisconnect.run();
            }
        });
    }

    /**
     * @brief Dispatcher principale per la gestione dei messaggi ricevuti dal server.
     * * @details Esegue il parsing dei messaggi, li converte in {@link CallbackDTO} e 
     * utilizza {@link Platform#runLater} per delegare l'aggiornamento grafico al thread di JavaFX.
     * * @param[in] channelIndex Identificatore del canale di comunicazione.
     * @param[in] msg Oggetto serializzato ricevuto dal server.
     */
    private void handleMessage(Integer channelIndex, Serializable msg) {
        // Verifica che il messaggio sia del tipo atteso dal protocollo
        if (!(msg instanceof NetworkMessage)) return;
        
        // Deserializzazione del pacchetto in un DTO (Data Transfer Object)
        CallbackDTO dto = ((NetworkMessage) msg).toDTO();

        // Utilizzo del thread JavaFX per evitare eccezioni di modifica UI esterna
        Platform.runLater(() -> {
            
            // Instradamento del DTO in base al tipo di evento ricevuto
            switch (dto.getEventType()) {
                
                case LOGIN_RESPONSE:
                    if (dto.isSuccess()) {
                        // Navigazione alla Lobby dopo login riuscito
                        System.out.println("Login accettato.");
                        try {
                            LobbyViewController lobbyCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
                            lobbyCtrl.setConnection(connection);
                            lobbyCtrl.setUsername(connection.getUsername());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Notifica errore login tramite Alert
                        String errorMsg = (dto.getMessage() != null && !dto.getMessage().isEmpty()) 
                                ? dto.getMessage() 
                                : "L'utente non è registrato";
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg);
                        alert.showAndWait();
                        
                        // SBLOCCO IL PULSANTE DI LOGIN!
                        if (LoginViewController.instance != null) {
                            LoginViewController.instance.resetLoginButton();
                            
                        System.out.println("Login fallito: " + errorMsg);
                        }
                    }
                    break;
                
                case REGISTER_RESPONSE:
                    if (dto.isSuccess()) {
                        // Registrazione riuscita, ritorno alla LoginView
                        System.out.println("Registrazione accettata.");
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "La registrazione è andata a buon fine");
                        alert.showAndWait();
                        try {
                            LoginViewController loginCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwshared/controller/LoginView.fxml");
                            loginCtrl.setLoginManager(new ClientLoginManager(connection));
                            loginCtrl.setSignupManager(new ClientSignupManager(connection));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String errorMsg = (dto.getMessage() != null && !dto.getMessage().isEmpty()) 
                                ? dto.getMessage() 
                                : "La registrazione non è andata a buon fine";
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg);
                        alert.showAndWait();
                        
                        // SBLOCCO IL PULSANTE DI REGISTRAZIONE!
                        if (SignupViewController.instance != null) {
                            SignupViewController.instance.resetSignupButton();
                            
                        System.out.println("Registrazione fallita: " + errorMsg);
                        }
                    }
                    break;
                
                case PLAY_RESPONSE:
                    if (dto.getStatus() == CallbackDTO.Status.MATCH_FOUND) {
                        System.out.println("Avversario trovato! Preparazione partita.");
                    } else {
                        // Attesa in coda (UI di caricamento)
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
                    // Inizializzazione della partita con i dati del DTO
                    System.out.println("Inizio partita contro " + dto.getOpponentUsername());
                    if (gameViewController != null) {
                        gameViewController.initGame(dto);
                    }
                    break;
                
                case OPPONENT_ANSWERED:
                    // Feedback visivo per risposta avversario
                    System.out.println("L'avversario ha dato la sua risposta!");
                    if (gameViewController != null) {
                        gameViewController.showOpponentAnswered();
                    }
                    break;
                
                case GAME_RESULT:
                    // Visualizzazione esito finale e navigazione alla vista risultati
                    System.out.println("Partita terminata! Esito: " + dto.getGameResult());
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
                    // Caricamento storico partite nella UI
                    System.out.println("Ricevuto storico partite.");
                    try {
                        // Visualizzazione dello storico
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
                    // Gestione evento disconnessione avversario
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'avversario si è disconnesso!");
                    alert.setHeaderText("Partita Interrotta");
                    alert.showAndWait();

                    try {
                        // Ritorno alla pagina principale
                        LobbyViewController lobbyCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwclient/controller/LobbyView.fxml");
                        lobbyCtrl.setConnection(connection);
                        lobbyCtrl.setUsername(connection.getUsername());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                
                case TEXT_MESSAGE:
                    // Gestione notifiche testuali dal server
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "Messaggio dal Server: " + dto.getMessage());
                    infoAlert.setHeaderText("Notifica");
                    infoAlert.showAndWait(); 
                    System.out.println("Notifica dal Server: " + dto.getMessage());
                    break;
                
                default:
                    // Caso di default per messaggi non gestiti
                    System.out.println("Evento ignorato o non gestito: " + dto.getEventType());
            }
        });
    }
}