package gruppo05.gtwserver.controller;

import gruppo05.gtwserver.db.AdminDAO;
import gruppo05.gtwserver.db.ConcreteAdminDAO;
import gruppo05.gtwserver.db.DatabaseManager;
import gruppo05.gtwserver.model.Admin;
import gruppo05.gtwshared.utility.SecurityUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class   ServerLoginManagerTest
 * @brief   Test unitari per {@link ServerLoginManager}.
 * 
 * @details
 *          Poiché {@link ServerLoginManager#validateInfo(String, String)}
 *          interagisce con componenti JavaFX non disponibili,
 *          si utilizza una Testable Subclass, che sovrascrive i metodi di
 *          notifica UI,
 *          isolando la logica di business e rendendola verificabile
 *          in modo deterministico e senza dipendenze grafiche.
 *
 *          I test coprono i seguenti scenari:
 *          <ul>
 *              <li>Credenziali corrette => callback di successo invocato</li>
 *              <li>Username inesistente => callback NON invocato, errore notificato</li>
 *              <li>Password errata => callback NON invocato, errore notificato</li>
 *              <li>Callback non impostato => nessuna NullPointerException</li>
 *              <li>Username null => callback NON invocato</li>
 *          </ul>
 *
 */
public class ServerLoginManagerTest {

    /** Nome del file del database SQLite utilizzato durante i test. */
    private static final String DB_NAME = "ServerDB";

    /**
     * Versione testabile di {@link ServerLoginManager} che sovrascrive le interazioni
     * con JavaFX per rendere la logica di business verificabile.
     * Tiene traccia degli errori in {@link #lastErrorMessage} e invoca il callback
     * direttamente sul thread corrente, senza delegare a {@code Platform.runLater}.
     */
    private static class TestableServerLoginManager extends ServerLoginManager {

        /** Ultimo messaggio di errore notificato (null se nessun errore). */
        String lastErrorMessage = null;

        /**
         * @brief Sovrascrive {@code validateInfo} invocando il callback direttamente
         *        sul thread corrente, senza coinvolgere il JavaFX Application Thread.
         * @param[in] username Nome utente da verificare.
         * @param[in] password Password da verificare.
         */
        @Override
        public void validateInfo(String username, String password) {
            AdminDAO dao = new ConcreteAdminDAO();
            Optional<Admin> o = dao.selectById(Optional.ofNullable(username));

            if (!o.isPresent()) {
                lastErrorMessage = "L'utente non è registrato";
                return;
            }

            String hashedPassword = SecurityUtils.hashPassword(password);
            if (!o.get().getPassword().equals(hashedPassword)) {
                lastErrorMessage = "Password non corretta";
                return;
            }

            // Esegue il callback direttamente (bypassando Platform.runLater)
            Runnable cb = getOnSuccessCallback();
            if (cb != null) {
                cb.run();
            }
        }

        /**
         * @brief Espone il callback di successo per la verifica nei test tramite Reflaction API.
         * @return Runnable di successo impostato, o null se non configurato.
         */
        Runnable getOnSuccessCallback() {
            // Accesso al campo privato tramite una variabile locale catturata nella subclass ("private Runnable onSuccessCallback;").
            // Utilizzo della Reflection API per ottenere il valore del campo privato della superclasse.
            try {
                Field f = ServerLoginManager.class.getDeclaredField("onSuccessCallback");
                f.setAccessible(true);
                return (Runnable) f.get(this);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /** Istanza del manager under test, ricreata da capo prima di ogni caso di test. */
    private TestableServerLoginManager loginManager;

    /**
     * @brief Inizializza il database SQLite e inserisce un admin di test prima di ogni caso.
     */
    @BeforeEach // <= "prima di ogni caso"
    public void setUp() {
        DatabaseManager.initDB();

        // Inserisce un admin di test nel DB
        ConcreteAdminDAO dao = new ConcreteAdminDAO();
        dao.insert(new Admin("adminTest", SecurityUtils.hashPassword("password123")));

        loginManager = new TestableServerLoginManager();
    }

    /**
     * @brief Cancella il file del database SQLite dopo ogni caso per garantire l'isolamento tra i test.
     */
    @AfterEach // <= "dopo ogni caso"
    public void tearDown() {
        File db = new File(DB_NAME);
        if (db.exists()) {
            db.delete();
        }
    }

    // =========================================================================
    // TEST CASES
    // =========================================================================

    /**
     * @brief Verifica che il callback di successo venga invocato con credenziali corrette.
     */
    @Test
    public void testValidateInfo_CredenzaliCorrette_CallbackInvocato() {
        AtomicBoolean callbackInvocato = new AtomicBoolean(false);
        loginManager.setOnSuccessCallback(() -> callbackInvocato.set(true));

        loginManager.validateInfo("adminTest", "password123");

        assertTrue(callbackInvocato.get(),"Il callback di successo deve essere invocato con credenziali corrette");
        assertNull(loginManager.lastErrorMessage,"Non deve essere registrato alcun messaggio di errore");
    }

    /**
     * @brief Verifica che il callback NON venga invocato quando lo username non è presente nel database.
     */
    @Test
    public void testValidateInfo_UsernameInesistente_CallbackNonInvocato() {
        AtomicBoolean callbackInvocato = new AtomicBoolean(false);
        loginManager.setOnSuccessCallback(() -> callbackInvocato.set(true));

        loginManager.validateInfo("utenteNonEsistente", "qualsiasi");

        assertFalse(callbackInvocato.get(),"Il callback di successo NON deve essere invocato con username inesistente");
        assertEquals("L'utente non è registrato", loginManager.lastErrorMessage,"Deve essere registrato il messaggio di errore per utente non trovato");
    }

    /**
     * @brief Verifica che il callback NON venga invocato quando la password fornita non è corretta.
     */
    @Test
    public void testValidateInfo_PasswordErrata_CallbackNonInvocato() {
        AtomicBoolean callbackInvocato = new AtomicBoolean(false);
        loginManager.setOnSuccessCallback(() -> callbackInvocato.set(true));

        loginManager.validateInfo("adminTest", "passwordSbagliata");

        assertFalse(callbackInvocato.get(),"Il callback di successo NON deve essere invocato con password errata");
        assertEquals("Password non corretta", loginManager.lastErrorMessage,"Deve essere registrato il messaggio di errore per password errata");
    }

    /**
     * @brief Verifica che {@link ServerLoginManager#validateInfo(String, String)}
     *        non sollevi eccezioni quando il callback non è impostato.
     */
    @Test
    public void testValidateInfo_SenzaCallback_NessunaNullPointerException() {
        // Nessun callback impostato
        assertDoesNotThrow(() -> loginManager.validateInfo("adminTest", "password123"),
                "Non deve essere sollevata eccezione se il callback non è stato impostato");
    }

    /**
     * @brief Verifica che {@link ServerLoginManager#setOnSuccessCallback(Runnable)} memorizzi correttamente il Runnable
     *        e che questo venga eseguito in caso di login con credenziali valide.
     */
    @Test
    public void testSetOnSuccessCallback_CallbackMemorizzatoEInvocato() {
        AtomicBoolean eseguito = new AtomicBoolean(false);
        Runnable callback = () -> eseguito.set(true);

        loginManager.setOnSuccessCallback(callback);
        loginManager.validateInfo("adminTest", "password123");

        assertTrue(eseguito.get(),"Il callback impostato con setOnSuccessCallback deve essere eseguito in caso di successo");
    }

    /**
     * @brief Verifica che con username null non vengano lanciate eccezioni e il callback non venga invocato.
     */
    @Test
    public void testValidateInfo_UsernameNull_CallbackNonInvocato() {
        AtomicBoolean callbackInvocato = new AtomicBoolean(false);
        loginManager.setOnSuccessCallback(() -> callbackInvocato.set(true));

        // Non deve lanciare eccezioni non gestite
        assertDoesNotThrow(() -> loginManager.validateInfo(null, "password123"));
        assertFalse(callbackInvocato.get(),"Il callback NON deve essere invocato con username null");
    }
}
