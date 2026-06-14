#!/bin/bash

# Risolve il percorso assoluto della cartella principale del progetto
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=== Preparazione della cartella eseguibili per la consegna ==="

echo "FASE 1) Pulizia dei file esistenti..."
rm -f "$PROJECT_ROOT/eseguibili/server.jar"
rm -f "$PROJECT_ROOT/eseguibili/client.jar"
rm -f "$PROJECT_ROOT/eseguibili/ServerDB" # Assicura che la consegna parta da zero, rimuovendo DB residui di test (da simula_revisore.sh)
rm -f "$PROJECT_ROOT/eseguibili/properties/server.properties"
rm -f "$PROJECT_ROOT/eseguibili/properties/client.properties"

# Per assicurarsi dell'esistenza delle cartelle, in opportuna gerarchia
mkdir -p "$PROJECT_ROOT/eseguibili/properties"

mkdir -p "$PROJECT_ROOT/eseguibili/documents"
mkdir -p "$PROJECT_ROOT/eseguibili/data"

echo "FASE 2) Compilazione e produzione jar del progetto (mvn clean package)..."
cd "$PROJECT_ROOT"
# Forza l'uso di Java 8 per Maven se necessario
if /usr/libexec/java_home -v 1.8 >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
fi

# Verifica presenza di Maven
if ! command -v mvn >/dev/null 2>&1; then
    echo "Errore: Maven (mvn) non trovato nel sistema."
    echo "Installa Maven (es. brew install maven) prima di procedere."
    exit 1
fi

mvn clean package -DskipTests
# mvn clean install

echo "FASE 3) Spostamento JAR e Copia file properties..."
# Copia i file .jar e li rinomina come richiesto
mv "$PROJECT_ROOT/GTWServer/target/GTWServer-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/eseguibili/server.jar"
mv "$PROJECT_ROOT/GTWClient/target/GTWClient-1.0-SNAPSHOT.jar" "$PROJECT_ROOT/eseguibili/client.jar"

# Copia i file properties
cp "$PROJECT_ROOT/GTWServer/server.properties" "$PROJECT_ROOT/eseguibili/properties/"
cp "$PROJECT_ROOT/GTWClient/client.properties" "$PROJECT_ROOT/eseguibili/properties/"

# Copia i file di testo di default nella cartella data
cp "$PROJECT_ROOT/GTWServer/src/main/resources/example_text_1.txt" "$PROJECT_ROOT/eseguibili/data/"
cp "$PROJECT_ROOT/GTWServer/src/main/resources/example_text_2.txt" "$PROJECT_ROOT/eseguibili/data/"

echo "=== Archivio per la consegna pronto ==="
