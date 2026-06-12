package gruppo05.gtwserver;

import gruppo05.gtwserver.controller.ServerLoginManager;
import gruppo05.gtwserver.controller.ServerSignupManager;
import gruppo05.gtwserver.db.DatabaseManager;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.db.ConcreteSourceDAO;
import gruppo05.gtwserver.db.ConcreteWordDAO;
import gruppo05.gtwserver.controller.AdminDashboardViewController;
import gruppo05.gtwserver.controller.SceneNavigator;
import gruppo05.gtwserver.networking.ServerConnection;
import gruppo05.gtwserver.networking.ServerConnectionCreator;
import gruppo05.gtwserver.sourcemanager.api.BasicSourceManager;
import gruppo05.gtwserver.sourcemanager.api.config.PresetConfig;
import gruppo05.gtwserver.sourcemanager.api.config.SourceManagerConfig;
import gruppo05.gtwserver.sourcemanager.internal.io.IOManager;
import gruppo05.gtwshared.controller.LoginViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private ServerConnection connection;
    private ServerConnectionCreator connectionCreator;
    
    @Override
    public void start(Stage stage) throws IOException {     
        // Inizializza lo stage per la navigazione delle schermate lato server
        SceneNavigator.init(stage);
        
        // Inizializza il database SQLite (crea schema e tabelle se il file del database non esiste)
        DatabaseManager.initDB();
        
        // Crea la connessione di rete e avvia il server in ascolto
        connectionCreator = new ServerConnectionCreator();
        connection = connectionCreator.createConnection();

        // Carica la schermata di Login condivisa definita in GTWShared
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gruppo05/gtwshared/controller/LoginView.fxml"));
        
        Parent root = loader.load();
        LoginViewController ctrl = (LoginViewController) loader.getController();
        
        // Istanzia il gestore dell'autenticazione per l'amministratore del server
        ServerLoginManager loginMgr = new ServerLoginManager();
        loginMgr.setOnSuccessCallback(() -> {
            try {
                // In caso di successo dell'autenticazione, naviga alla Dashboard dell'Admin del server
                AdminDashboardViewController adminCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwserver/controller/AdminDashboardView.fxml");
                
                // Imposta la grandezza minima per la finestra del pannello di controllo dell'Amministratore (Lato Server)
                Stage dashboardStage = SceneNavigator.getStage();
                if (dashboardStage != null) {
                    dashboardStage.setMinWidth(650);
                    dashboardStage.setMinHeight(500);
                }
                
                adminCtrl.setConnection(connection, connectionCreator);
                
                // Istanzia i DAO per la gestione dei sorgenti e delle parole estratte
                SourceDAO sourceDao = new ConcreteSourceDAO();
                WordDAO wordDao = new ConcreteWordDAO();
                
                // Carica l'elenco delle stop-words (parole da escludere durante l'analisi del testo).
                // Il builder di SourceManagerConfig richiede obbligatoriamente un set di stop-words (che di default è vuoto).
                // Si tenta la lettura dinamica dal file risorsa 'stopwords-it.txt' tramite IOManager;
                // se si verifica un errore, si utilizza un set fisso come fallback di sicurezza.
                Set<String> stopWords;
                try {
                    IOManager ioManager = new IOManager(sourceDao, wordDao);
                    stopWords = ioManager.readDefaultStopWords();
                } catch (Exception e) {
                    stopWords = new HashSet<>();
                    stopWords.add("il");
                    stopWords.add("la");
                    stopWords.add("di");
                    stopWords.add("a");
                    stopWords.add("da");
                    stopWords.add("in");
                    stopWords.add("con");
                    stopWords.add("su");
                    stopWords.add("per");
                    stopWords.add("tra");
                    stopWords.add("fra");
                }
                
                // Configura il preset predefinito per la generazione delle sfide/domande (istanziandolo)
                PresetConfig defaultPreset = 
                    new PresetConfig.Builder()
                        .withNumberOfPeriods(3)
                        .withMaximumWordFrequency(5)
                        .withMaximumSimilarWordInQuestionText(2)
                        .withShiftingOffset(1)
                        .build();
                        
                // Istanzia l'oggetto di configurazione per il gestore dei sorgenti
                SourceManagerConfig managerConfig = 
                    new SourceManagerConfig.Builder(
                        sourceDao,
                        wordDao,
                        String::equalsIgnoreCase,       // TODO Criterio per determinare la similarità testuale
                        (freq1, freq2) -> freq1 < freq2 // Criterio per il fallback delle parole basato sulla frequenza
                    )
                    .withCustomStopWords(stopWords)
                    .addPreset("DEFAULT", defaultPreset)
                    .build();
                
                // Istanzia il core del BasicSourceManager e lo inietta nel controller della dashboard
                BasicSourceManager sourceManager = new BasicSourceManager(managerConfig);
                    
                adminCtrl.setSourceManager(sourceManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        ctrl.setLoginManager(loginMgr);
        ctrl.setSignupManager(new ServerSignupManager());
        
        stage.setScene(new Scene(root));
        // Assicura lo spegnimento pulito del server all'atto della chiusura della finestra JavaFX
        stage.setOnCloseRequest(event -> {
            if (connection != null) {
                connection.stopServer();
            }
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
