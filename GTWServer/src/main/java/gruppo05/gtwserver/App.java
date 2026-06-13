package gruppo05.gtwserver;

import gruppo05.gtwserver.controller.ServerLoginManager;
import gruppo05.gtwserver.controller.ServerSignupManager;
import gruppo05.gtwserver.db.DatabaseManager;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.db.ConcreteSourceDAO;
import gruppo05.gtwserver.db.ConcreteWordDAO;
import gruppo05.gtwserver.controller.AdminDashboardViewController;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwserver.networking.ServerConnection;
import gruppo05.gtwserver.networking.ServerConnectionCreator;
import gruppo05.gtwserver.config.SourceManagerInitializer; 
import gruppo05.gtwserver.sourcemanager.api.SourceManager;
import gruppo05.gtwshared.controller.LoginViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private ServerConnection connection;
    private ServerConnectionCreator connectionCreator;
    private SourceManager globalSourceManager;
    
    @Override
    public void start(Stage stage) throws IOException {     
        // Inizializza lo stage per la navigazione delle schermate lato server
        SceneNavigator.init(stage);
        
        // Inizializza il database SQLite (crea schema e tabelle se il file del database non esiste)
        DatabaseManager.initDB();
        
        // ---------------------------------------------------------
        // 1. INIZIALIZZAZIONE DEL CORE (SOURCE MANAGER)
        // ---------------------------------------------------------
        SourceDAO sourceDao = new ConcreteSourceDAO();
        WordDAO wordDao = new ConcreteWordDAO();
        
        // Inizializza il Singleton una sola volta all'avvio
        SourceManagerInitializer.init(sourceDao, wordDao);
        
        // Recupera l'istanza configurata (cast a BasicSourceManager se il connectionCreator richiede l'implementazione concreta)
        globalSourceManager = SourceManagerInitializer.getInstance();
        
        // ---------------------------------------------------------
        // 2. AVVIO DEL SERVER DI RETE
        // ---------------------------------------------------------
        // Crea la connessione di rete e avvia il server in ascolto
        connectionCreator = new ServerConnectionCreator(globalSourceManager);
        connection = connectionCreator.createConnection();

        // ---------------------------------------------------------
        // 3. CARICAMENTO UI
        // ---------------------------------------------------------
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gruppo05/gtwshared/controller/LoginView.fxml"));
        Parent root = loader.load();
        LoginViewController ctrl = loader.getController();
        
        // Istanzia il gestore dell'autenticazione per l'amministratore del server
        ServerLoginManager loginMgr = new ServerLoginManager();
        loginMgr.setOnSuccessCallback(() -> {
            try {
                AdminDashboardViewController adminCtrl = SceneNavigator.navigateAndGetController("/gruppo05/gtwserver/controller/AdminDashboardView.fxml");
                
                Stage dashboardStage = SceneNavigator.getStage();
                if (dashboardStage != null) {
                    dashboardStage.setMinWidth(650);
                    dashboardStage.setMinHeight(500);
                }
                
                adminCtrl.setConnection(connection, connectionCreator);
                
                // Passa semplicemente l'istanza già pronta al controller!
                adminCtrl.setSourceManager(SourceManagerInitializer.getInstance());
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        ctrl.setLoginManager(loginMgr);
        
        ServerSignupManager signupMgr = new ServerSignupManager();
        signupMgr.setOnSuccessCallback(() -> {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/gruppo05/gtwshared/controller/LoginView.fxml"));
            try {
                Parent loginRoot = loginLoader.load();
                LoginViewController loginCtrl = loginLoader.getController();
                loginCtrl.setLoginManager(loginMgr);
                loginCtrl.setSignupManager(signupMgr);   
                
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        ctrl.setSignupManager(signupMgr);
        
        stage.setScene(new Scene(root));
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