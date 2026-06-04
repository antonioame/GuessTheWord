| ----------------------------------------------------------------------- |
| ISTRUZIONI PER LA COMPILAZIONE E L'ESECUZIONE DEL PROGETTO GUESSTHEWORD |
| ----------------------------------------------------------------------- |
Il progetto è composto da tre moduli:
- GTWShared
- GTWClient
- GTWServer

Per compilare tutti i progetti in una volta sola dovremmo aprire il cmd (o terminale), andare nella root del progetto (ovvero quella in cui sono visibili le tre cartelle prima citate con i nomi dei moduli) e scrivere il comando:
- mvn clean compile
Il comando elimina la compilazione precedente ed esegue una nuova compilazione e, se svolto nella root directory, compilerà in un colpo solo tutti e tre i progetti.

Alternativamente, si possono compilare i singoli progetti all'interno delle directory dei singoli progetti con lo stesso comando.

Alternativamente, per compilare i progetti singolarmente, si può sempre usare l'opzione di compilazione da netbeans.

Per eseguire poi il singolo modulo (eseguibile) basta entrare nelle directory GTWClient o GTWServer e scrivere il comando:
- mvn exec:java
Questo, anche in assenza del file jar (creato unicamente eseguendo "mvn clean package"), permette di eseguire il codice appena compilato.

Alternativamente, si può eseguire da netbeans come qualsiasi altro progetto, impostando come main class la class App.


| ------------------- |
| QUESTIONS & ANSWERS | 
| ------------------- |
1: Posso lanciare i comandi "mvn clean", "mvn compile", "mvn package" ed "mvn exec:java" direttamente dalle sottodirectory GTWServer e GTWClient?
La risposta è sì. Si consiglia però di lanciare i comandi di compilazione (dunque "mvn clean", "mvn compile", "mvn package" e le loro combinazioni "mvn clean compile" ed "mvn clean package") a partire dalla root directory, mentre il comando di esecuzione ("mvn exec:java") DEVE essere lanciato nelle sottodirectory in quanto altrimenti maven lancia un errore.

2: Cosa succede se eseguo il comando "mvn exec:java" dopo aver eseguito nella root "mvn clean"?
Maven ritornerà un errore. Questo perché il progetto non è stato compilato. C'è bisogno di prima di compilare il progetto (dalla root o dal modulo GTWClient e GTWServer) eseguendo il comando "mvn compile" e poi spostarsi in una delle sottodirectory (meno che GTWShared per ovvie ragioni) e lanciare il comando "mvn exec:java".

3: E' meglio "mvn clean compile" o "mvn clean package"?
In realtà, eseguire "mvn clean package" dalla root directory ogni volta sarebbe meglio in quanto questo comando:
- compila le classi				:	lavoro di "mvn compile" 
- compila i test e li esegue			:	lavoro di "mvn test"
- genera i jar (i fat jar nel nostro cas)
Per questo motivo va bene lo stesso eseguire "mvn clean package" ma solitamente è basta "mvn clean compile".


| ----------------------- |
| PROBLEMI NOTI E RISOLTI |
| ----------------------- |
1: Maven non trova le librerie di javafx.
Verificare con "mvn -v" la versione di maven. Ciò che dovrebbe comparire dovrebbe essere analogo a:
----------------------------------------------------------------------------------------------------
Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
Maven home: C:\Program Files\Apache\apache-maven-3.9.11
Java version: 1.8.0_202, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_202\jre
Default locale: it_IT, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
----------------------------------------------------------------------------------------------------
Se vedete una versione di java diversa dalla 1.8, allora dovete obbligare maven ad utilizzare la versione corretta di java. Per fare questo bisogna impostare nelle variabili d'ambiente JAVA_HOME = "path\to\jdk-1.8". 

Se ancora non funziona è possibile che abbiate nella variabile di ambiente globale Path la directory "C:\Program Files\Common Files\Oracle\Java\javapath" che va a recuperare sempre la versione più recente di java. Per ovviare al problema, aggiungete una nuova variabile d'ambiente a Path che sarà il path alla cartella bin del jdk 1.8 (ad esempio a me è: "C:\Program Files\Java\jdk1.8.0_202\bin"). Una volta inserita questa variabile d'ambiente, spostarla in alto, al di sopra di javapath, in modo che sia prioritaria rispetto a quella. Maven a questo punto non dovrebbe avere difficoltà a trovare la versione giusta di java.

Nel caso estremo in cui neanche questo dovesse funzionare, controllate che la versione dell'sdk 1.8 che avete contenga i file eseguibili e le librerie di javafx. Se non ce li ha, installateli a parte ed aggiungeteli manualmente alla directory, oppure scaricate un'altra versione dell'sdk 1.8.

2: Il file jar non si apre con il doppio click (Windows)
Probabilmente, è un problema di associazione con il file jar. I file jar di questo progetto saranno compilati usando sdk 1.8, che, come sapete, include (sempre in base alla versione) javafx. Le versioni successive invece non includono javafx. Per questo motivo, nel caso in cui non riuscite ad aprire il file con il doppio tocco su windows è probabile che sia perché Windows sta cercando di aprirlo usando un eseguibile di un jdk successivo alla 8. 

Per provare questo, andate da terminale, posizionatevi nella cartella del file fat jar e digitate il comando:
- javaw -jar file.jar
Supponendo che abbiate impostato come variabile d'ambiente JAVA_HOME il path per jdk 1.8, nel caso in cui il comando dovesse essere eseguito e vi porta ad aprire l'applicazione significa che, come detto prima, è un problema di associazione.

Per provarlo, aprite il terminale e scrivete i seguenti comandi:
- assoc .jar
- ftype jarfile
Al secondo comando, se jarfile è uguale ad un path che porta ad un jdk superiore all'1.8 allora significa che l'ipotesi è confermata e Windows sta cercando di aprire il file con una versione di javaw superiore a 1.8.

La soluzione a questo problema sarebbe impostare un nuovo valore per jarfile, tuttavia vi consiglio o di eseguire l'applicazione attraverso "mvn exec:java" direttamente oppure usare il comando "java -jar file.jar" per evitare problemi poi di inconsistenza.