/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gruppo05.gtwserver.sourcemanager.api;

import gruppo05.gtwserver.model.Source;
import gruppo05.gtwserver.model.Question;
import java.util.function.Consumer;

/**
 * @brief Interfaccia principale per la gestione delle fonti e la generazione di domande.
 * @invariant
 * Non ci sono invarianti di stato esplicite in quanto si tratta di un'interfaccia,
 * ma le classi che la implementano devono garantire che la chiamata al metodo
 * shutdown rilasci in modo consistente i thread e le risorse allocate.
 */
public interface SourceManager extends AutoCloseable {

/**
 * @brief Aggiunge in modo asincrono una fonte al sistema.
 * @param[in] source Il componente sorgente da registrare nel sistema.
 * @param[in] onSuccess Callback da eseguire in caso di successo della registrazione.
 * @param[in] onFailure Callback da eseguire in caso di errore, a cui viene passata l'eccezione riscontrata.
 * @pre 
 * Il parametro source non deve essere nullo.
 * I parametri onSuccess e onFailure non devono essere nulli.
 * @post 
 * La sorgente viene presa in carico dal sistema per l'analisi e l'immagazzinamento dei dati.
 */
void addSource(Source source, Runnable onSuccess, Consumer<Exception> onFailure);

/**
 * @brief Rimuove in modo asincrono una fonte dal sistema.
 * @param[in] source Il componente sorgente da rimuovere.
 * @param[in] onSuccess Callback da eseguire in caso di successo della rimozione.
 * @param[in] onFailure Callback da eseguire in caso di errore, a cui viene passata l'eccezione riscontrata.
 * @pre 
 * Il parametro source non deve essere nullo.
 * I parametri onSuccess e onFailure non devono essere nulli.
 * @post 
 * La sorgente e i relativi dati associati (come le frequenze delle parole) vengono rimossi dal sistema.
 */
void removeSource(Source source, Runnable onSuccess, Consumer<Exception> onFailure);

/**
 * @brief Genera in modo asincrono una domanda a partire da una fonte e una configurazione preimpostata.
 * @param[in] source La sorgente da cui estrarre i dati per la generazione della domanda.
 * @param[in] presetName Il nome del preset di configurazione da applicare per i criteri di generazione.
 * @param[in] onSuccess Callback da eseguire in caso di successo, a cui viene passata la domanda generata.
 * @param[in] onFailure Callback da eseguire in caso di errore (es. PresetNotFoundException), a cui viene passata l'eccezione.
 * @pre 
 * Il parametro source non deve essere nullo.
 * Il parametro presetName non deve essere nullo o vuoto.
 * I parametri onSuccess e onFailure non devono essere nulli.
 * @post 
 * Viene avviato il processo di estrazione e cifratura del testo basato sui vincoli del preset scelto.
 */
void generateQuestion(Source source, String presetName, Consumer<Question> onSuccess, Consumer<Exception> onFailure);

/**
 * @brief Avvia la procedura di spegnimento del manager, interrompendo l'accettazione di nuovi task.
 * @pre 
 * Il manager non deve essere già stato spento definitivamente.
 * @post 
 * L'esecutore interno rifiuta nuovi task, ma completa quelli già sottomessi.
 */
void shutdown();
}