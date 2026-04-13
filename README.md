# MCP Database Explorer Server

Ein MCP (Model Context Protocol) Server auf Basis von Spring Boot und Spring AI,
der dynamischen Zugriff auf Datenbanktabellen ermöglicht. Tabellen können zur
Laufzeit registriert, abgefragt und verwaltet werden.

## Voraussetzungen

- **Java 21** (oder höher)
- **Maven** (wird über den mitgelieferten Maven Wrapper bereitgestellt)

## Projekt bauen

```bash
# Kompilieren
./mvnw compile

# Tests ausführen
./mvnw test

# JAR-Datei erstellen
./mvnw package

# JAR-Datei erstellen (Tests überspringen)
./mvnw package -DskipTests
```

Unter Windows `mvnw.cmd` statt `./mvnw` verwenden.

## Server starten

```bash
# Über Maven
./mvnw spring-boot:run

# Oder direkt als JAR
java -jar target/mcp_test-0.0.1-SNAPSHOT.jar
```

Der Server startet auf **Port 8080**.

## Endpunkte

| Endpunkt | Beschreibung |
|---|---|
| `http://localhost:8080/mcp` | MCP Server (Streamable-HTTP Protokoll) |
| `http://localhost:8080/h2-console` | H2 Datenbank-Konsole (Entwicklung) |

### H2-Konsole Zugangsdaten

- **JDBC URL**: `jdbc:h2:mem:mcpdb`
- **User**: `sa`
- **Passwort**: _(leer)_

## MCP Client-Konfiguration

### VS Code (`.vscode/mcp.json`)

```json
{
  "servers": {
    "db-explorer": {
      "type": "http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### MCP Inspector (zum Testen)

```bash
npx @modelcontextprotocol/inspector
```

Dann als Transport "Streamable HTTP" wählen und `http://localhost:8080/mcp` eingeben.

## MCP-Funktionen

### Tools (5)

| Tool | Beschreibung |
|---|---|
| `list-tables` | Alle registrierten Tabellen mit Beschreibung auflisten |
| `describe-table` | Spaltendefinitionen einer Tabelle anzeigen (Name, Typ, Nullable, Primary Key) |
| `query-table` | Daten abfragen mit optionalem Filter, Sortierung und Limit |
| `count-rows` | Anzahl der Zeilen einer Tabelle zählen |
| `register-table` | Neue Tabelle dynamisch registrieren und in der Datenbank erstellen |

### Resources (3)

| Resource URI | Beschreibung |
|---|---|
| `db://tables` | Liste aller registrierten Tabellen als JSON |
| `db://table/{name}/schema` | Schema einer bestimmten Tabelle als JSON |
| `db://table/{name}/data` | Daten einer Tabelle als JSON (max. 100 Zeilen) |

### Prompts (2)

| Prompt | Beschreibung |
|---|---|
| `data-analysis` | Generiert einen Analyse-Prompt mit Schema und Beispieldaten einer Tabelle |
| `query-helper` | Generiert einen Prompt mit allen verfügbaren Tabellen und Schemata als Abfrage-Hilfe |

## Beispieldaten

Der Server startet mit zwei vordefinierten Tabellen:

- **CUSTOMERS** — Kundenstammdaten (ID, Name, Email, City) mit 5 Beispielkunden
- **ORDERS** — Bestellungen (ID, Customer_ID, Product, Amount, Order_Date) mit 7 Beispielbestellungen

## Später auf DB2 (IBM i) umstellen

1. DB2-Treiber in `pom.xml` hinzufügen:
   ```xml
   <dependency>
       <groupId>com.ibm.db2</groupId>
       <artifactId>jcc</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. `application.properties` anpassen:
   ```properties
   spring.datasource.url=jdbc:db2://HOST:PORT/DBNAME
   spring.datasource.driver-class-name=com.ibm.db2.jcc.DB2Driver
   spring.datasource.username=BENUTZER
   spring.datasource.password=PASSWORT
   spring.sql.init.mode=never
   spring.h2.console.enabled=false
   ```

3. Tabellen über das `register-table`-Tool oder direkt auf dem IBM i anlegen und in der Registry registrieren.
