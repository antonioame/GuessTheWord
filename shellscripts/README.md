# Shell Scripts - GuessTheWord

Questa cartella contiene script Bash di utilità per automatizzare la compilazione, il test, l'esecuzione e la preparazione della consegna del progetto.

Tutti gli script garantiscono l'utilizzo corretto di **Java 8 (JDK 1.8)**.

---

## Script di Sviluppo

### `run.sh`
È lo script principale utilizzato durante la fase di sviluppo. Esegue operazioni tramite Maven dalla root del progetto.
**Utilizzo:**
- `./run.sh build` : Compila e installa tutti i moduli del progetto.
- `./run.sh test` : Esegue la suite di test.
- `./run.sh server` : Avvia un'istanza del Server nel terminale corrente.
- `./run.sh client` : Avvia un'istanza del Client nel terminale corrente.
- `./run.sh start` (o semplicemente `./run.sh`) : Avvia l'infrastruttura completa per i test manuali (1 Server in background, 1 Client in background e 1 Client in primo piano).

---

## Script per la Consegna del Progetto e Simulazione del Comportamento del Revisore

### `crea_consegna.sh`
Prepara in modo automatico la cartella `eseguibili/` per la consegna finale del progetto.
**Cosa fa:**
1. Pulisce la cartella `eseguibili/` da file precedenti o file di database residui.
2. Compila il progetto (`mvn clean package -DskipTests`).
3. Sposta e rinomina gli artefatti compilati in `server.jar` e `client.jar` nella cartella `eseguibili/`.
4. Ricrea la gerarchia corretta copiando i file `.properties` e i file di dati (`.txt`) necessari per il funzionamento standalone in `eseguibili/` (la cartella nella quale si posizionerà il revisore).

### `simula_revisore.sh`
Simula il comportamento di test dell'applicazione da parte del revisore esterno, agendo direttamente dalla cartella `eseguibili/` consegnata dal team.
**Cosa fa:**
- Avvia il Server e un primo Client in background, catturandone i log nella cartella `run_logs/`.
- Avvia un secondo Client nel terminale corrente, dove lo script viene eseguito.
- Alla chiusura (tramite CTRL+C o terminazione del Client in primo piano), arresta in modo pulito tutti i processi rimasti in background.

### `run_server.sh`
Script minimale per avviare manualmente `server.jar` (istanza dell'app del Server) posizionandosi correttamente nella cartella `eseguibili/`.

### `run_client.sh`
Script minimale per avviare manualmente `client.jar` (istanza dell'app del Client) posizionandosi correttamente nella cartella `eseguibili/`.
