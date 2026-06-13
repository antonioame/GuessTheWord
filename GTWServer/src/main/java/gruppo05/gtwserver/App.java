package gruppo05.gtwserver;

import gruppo05.gtwserver.controller.ServerLoginManager;
import gruppo05.gtwserver.controller.ServerSignupManager;
import gruppo05.gtwserver.db.DatabaseManager;
import gruppo05.gtwserver.db.SourceDAO;
import gruppo05.gtwserver.db.WordDAO;
import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.db.ConcreteSourceDAO;
import gruppo05.gtwserver.db.ConcreteWordDAO;
import gruppo05.gtwserver.controller.AdminDashboardViewController;
import gruppo05.gtwshared.controller.SceneNavigator;
import gruppo05.gtwserver.networking.ServerConnection;
import gruppo05.gtwserver.networking.ServerConnectionCreator;
import gruppo05.gtwserver.config.SourceManagerInitializer; 
import gruppo05.gtwserver.sourcemanager.api.SourceManager;
import gruppo05.gtwshared.controller.LoginViewController;
import gruppo05.gtwshared.controller.SignupViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * JavaFX App
 */
public class App extends Application {

    private ServerConnection connection;
    private ServerConnectionCreator connectionCreator;
    private SourceManager globalSourceManager;
    
    @Override
    public void start(Stage stage) throws IOException {     
        // Inizializzazione navigazione e database
        SceneNavigator.init(stage);
        
        // Inizializza il database SQLite (crea schema e tabelle se il file del database non esiste)
        DatabaseManager.initDB();
        
        // Inizializzazione sorgenti e server di rete
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

        // Caricamento UI Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gruppo05/gtwshared/controller/LoginView.fxml"));
        Parent root = loader.load();
        LoginViewController ctrl = loader.getController();
        
        // --- FIX DEFINITIVO PER IL TASTO CONFERMA (LOGIN) ---
        // Svincoliamo il bottone e forziamo lo sblocco se l'admin corregge i dati dopo un errore
        ctrl.getTxfPswd().textProperty().addListener((obs, oldVal, newVal) -> {
            ctrl.getBtnConfirm().disableProperty().unbind();
            ctrl.getBtnConfirm().setDisable(false);
        });
        
        ctrl.getTxfUsername().textProperty().addListener((obs, oldVal, newVal) -> {
            ctrl.getBtnConfirm().disableProperty().unbind();
            ctrl.getBtnConfirm().setDisable(false);
        });
        // ----------------------------------------------------
        
        // Gestione Login Admin
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
        
        // Gestione Signup Admin
        ServerSignupManager signupMgr = new ServerSignupManager();
        signupMgr.setOnSuccessCallback(() -> {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/gruppo05/gtwshared/controller/LoginView.fxml"));
            try {
                Parent loginRoot = loginLoader.load();
                LoginViewController loginCtrl = loginLoader.getController();
                
                // --- APPLICAZIONE STESSO FIX ANCHE ALLA LOGIN DOPO SIGNUP ---
                loginCtrl.getTxfPswd().textProperty().addListener((obs, oldVal, newVal) -> {
                    loginCtrl.getBtnConfirm().disableProperty().unbind();
                    loginCtrl.getBtnConfirm().setDisable(false);
                });
                
                loginCtrl.setLoginManager(loginMgr);
                loginCtrl.setSignupManager(signupMgr);   
                
                stage.setScene(new Scene(loginRoot));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        ctrl.setSignupManager(signupMgr);
        
        // ---------------------------------------------------------
        // 3. AUTO-AGGIUNTA SORGENTI DI DEFAULT (NON BLOCCANTE E SICURA)
        // ---------------------------------------------------------
        Thread autoAddSourcesThread = new Thread(() -> {
            String[] defaultFiles = {"example_text_1.txt", "example_text_2.txt"};
            try {
                List<Source> existingSources = sourceDao.selectAll();
                
                for (String fileName : defaultFiles) {
                    try {
                        Path path = null;
                        Path[] possiblePaths = {
                            Paths.get("data", fileName),                                        // 1. Quando avviato da eseguibili/ (per simulare comportamento del revisore)
                            Paths.get("src", "main", "resources", fileName),            // 2. Quando avviato dall'IDE (in GTWServer/)
                            Paths.get("GTWServer", "src", "main", "resources", fileName)// 3. Quando avviato dalla root del progetto
                        };
                        
                        // Cerca il file sul filesystem
                        for (Path p : possiblePaths) {
                            if (Files.exists(p)) {
                                path = p.toAbsolutePath();
                                break;
                            }
                        }
                        
                        // Se non lo trova sul filesystem, prova con il resource URL (solo fuori dal JAR)
                        if (path == null) {
                            URL resourceUrl = App.class.getResource("/" + fileName);
                            if (resourceUrl != null && !resourceUrl.getProtocol().equals("jar")) {
                                path = Paths.get(resourceUrl.toURI());
                            }
                        }
                        
                        if (path != null && Files.exists(path)) {
                            final Path finalPath = path.toAbsolutePath();
                            boolean alreadyExists = existingSources.stream()
                                    .anyMatch(s -> s.getPath() != null && s.getPath().toAbsolutePath().equals(finalPath));
                                    
                            if (!alreadyExists) {
                                Source source = new Source(finalPath);
                                globalSourceManager.addSource(source, () -> {
                                    System.out.println("[INFO] Sorgente di default aggiunta con successo: " + fileName);
                                }, (e) -> {
                                    System.err.println("[WARN] Errore nell'aggiunta della sorgente di default " + fileName + ": " + e.getMessage());
                                });
                            } else {
                                System.out.println("[INFO] Sorgente di default già presente nel sistema: " + fileName);
                            }
                        } else {
                            System.out.println("[WARN] File di default non trovato (ignorato in modo sicuro): " + fileName);
                        }
                    } catch (Exception e) {
                        System.err.println("[WARN] Impossibile caricare il file di default " + fileName + " in modo sicuro: " + e.getMessage());
                    }
                }
            } catch (Exception mainException) {
                System.err.println("[WARN] Errore generale nell'auto-aggiunta delle sorgenti: " + mainException.getMessage());
            }
        });
        autoAddSourcesThread.setDaemon(true);
        autoAddSourcesThread.start();
        
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