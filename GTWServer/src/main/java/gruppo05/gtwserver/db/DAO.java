package gruppo05.gtwserver.db;

import java.util.List;

/**
 * @author francesco-vecchione
 * @brief Interfaccia generica radice per il pattern Data Access Object (DAO)
 * che definisce le operazioni CRUD.
 * @param <T> Il tipo di entità del modello gestito dal DAO.
 */
public interface DAO<T> {
    
    /**
     * @brief Recupera tutte le istanze dell'entità memorizzate nel database.
     * @return Una lista contenente tutti gli oggetti di tipo T trovati nel sistema.
     * Se non sono presenti record, restituisce una lista vuota.
     * @post
     * Il valore restituito non è null.
     */
    List<T> selectAll();
    
    /**
     * @brief Inserisce una nuova istanza dell'entità all'interno del database.
     * @param[in] model L'oggetto di tipo T da persistere.
     * @pre
     * Il parametro model non deve essere null.
     * @post
     * Lo stato dell'oggetto model viene memorizzato in modo persistente nel database.
     */
    void insert(T model);
    
    /**
     * @brief Inserisce una lista di istanze dell'entità all'interno del database.
     * @param[in] modelList La lista di oggetti di tipo T da persistere.
     * @pre
     * Il parametro modelList non deve essere null.
     * @post
     * Tutte le entità valide contenute in modelList vengono memorizzate nel database.
     */
    void insertAll(List<T> modelList);
    
    /**
     * @brief Aggiorna i dati di un'istanza esistente all'interno del database.
     * @param[in] model L'oggetto di tipo T contenente i nuovi dati da aggiornare.
     * @pre
     * Il parametro model non deve essere null.
     * @post
     * I dati del record corrispondente nel database vengono allineati allo stato dell'oggetto model.
     */
    void update(T model);
}
