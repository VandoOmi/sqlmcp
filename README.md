# MCP Database Explorer Server

Ein MCP (Model Context Protocol) Server auf Basis von Spring Boot und Spring AI,
der dynamischen Zugriff auf Datenbanktabellen ermöglicht. MCP-Tools werden als
SQL-Query-Templates in der Datenbank gespeichert und können zur Laufzeit
hinzugefügt, geändert oder entfernt werden.

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
| `POST http://localhost:8080/api/tools/refresh` | Tools aus der Datenbank neu laden |
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

### Tools (dynamisch)

Tools werden aus der Tabelle `TOOL_REGISTRY` geladen. Jedes Tool führt ein vordefiniertes
SQL-SELECT-Statement aus. Parameter werden über `TOOL_PARAMETER` definiert.

**Mitgelieferte Seed-Tools:**

| Tool | Beschreibung |
|---|---|
| `list-tables` | Alle registrierten Tabellen mit Beschreibung auflisten |
| `describe-table` | Spaltendefinitionen einer Tabelle anzeigen |
| `query-customers` | Alle Kunden abrufen |
| `customers-by-city` | Kunden nach Stadt filtern |
| `orders-by-customer` | Bestellungen eines Kunden suchen (LIKE-Muster) |
| `high-value-orders` | Bestellungen über einem Mindestbetrag |
| `count-customers` | Anzahl der Kunden |
| `count-orders` | Anzahl der Bestellungen |

**Neues Tool zur Laufzeit hinzufügen:**

```sql
-- In der H2-Konsole oder per SQL-Client:
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('my-tool', 'Beschreibung des Tools', 'SELECT * FROM CUSTOMERS WHERE CITY = :city');

INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) VALUES
    ('my-tool', 'city', 'string', 'Stadt zum Filtern', TRUE, 1);
```

Danach `POST http://localhost:8080/api/tools/refresh` aufrufen — das Tool ist sofort im MCP Client verfügbar.

**Sicherheit:** Nur `SELECT`-Queries erlaubt. DDL/DML-Keywords und gefährliche H2-Funktionen werden blockiert. Parameter werden per `NamedParameterJdbcTemplate` gebunden (kein SQL-Injection). Queries haben ein Timeout von 10 Sekunden und ein automatisches LIMIT von 1000 Zeilen.

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

3. Tabellen direkt auf dem IBM i anlegen, in der Registry registrieren und passende Tools in `TOOL_REGISTRY` definieren.
