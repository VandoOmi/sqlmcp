# Projektstruktur und Datei-Dokumentation

```
mcp_test/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main/
    ├── java/com/vando/mcp_test/
    │   ├── McpTestApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java
    │   │   └── JacksonConfig.java
    │   ├── service/
    │   │   ├── TableRegistryService.java
    │   │   └── DataQueryService.java
    │   └── mcp/
    │       ├── DatabaseTools.java
    │       ├── DatabaseResources.java
    │       └── DatabasePrompts.java
    └── resources/
        ├── application.properties
        ├── schema.sql
        └── data.sql
```

---

## Build & Konfiguration

### `pom.xml`
Maven-Projektdefinition. Enthält alle Abhängigkeiten:
- **spring-ai-starter-mcp-server-webmvc** — MCP Server mit Streamable-HTTP Transport über Spring WebMVC
- **spring-boot-starter-security** — HTTP-Security für MCP-Endpunkte
- **spring-boot-starter-jdbc** — Datenbankzugriff über JdbcTemplate
- **h2** — Eingebettete In-Memory-Datenbank zum Testen

### `src/main/resources/application.properties`
Zentrale Konfigurationsdatei. Definiert:
- MCP-Server-Einstellungen (Name: `db-explorer`, Protokoll: `STREAMABLE`, Typ: `SYNC`)
- H2-Datenbank-Verbindung (`jdbc:h2:mem:mcpdb`)
- H2-Web-Konsole auf `/h2-console`

### `src/main/resources/schema.sql`
Wird beim Start automatisch ausgeführt. Erstellt die Datenbanktabellen:
- **TABLE_REGISTRY** — Metadaten-Tabelle: speichert Namen und Beschreibung registrierter Tabellen
- **COLUMN_REGISTRY** — Metadaten-Tabelle: speichert Spaltendefinitionen (Name, Datentyp, Beschreibung, Nullable, Primary Key) pro Tabelle
- **CUSTOMERS** — Beispieltabelle für Kundendaten
- **ORDERS** — Beispieltabelle für Bestellungen

### `src/main/resources/data.sql`
Wird nach `schema.sql` automatisch ausgeführt. Befüllt:
- Registry-Einträge für CUSTOMERS und ORDERS (mit Spaltenbeschreibungen)
- 5 Beispielkunden und 7 Beispielbestellungen

---

## Java-Klassen

### `McpTestApplication.java`
Spring Boot Hauptklasse. Startet die Anwendung mit `SpringApplication.run()`. Keine weitere Logik — die MCP-Server-Konfiguration erfolgt automatisch durch Spring AI Auto-Configuration.

### `config/SecurityConfig.java`
Spring Security Konfiguration. Gibt die MCP-Endpunkte (`/mcp/**`) und die H2-Konsole (`/h2-console/**`) frei und deaktiviert CSRF sowie Frame-Options (für die H2-Konsole nötig).

### `config/JacksonConfig.java`
Stellt eine `ObjectMapper`-Bean (Jackson 3.x) bereit, die von allen MCP-Klassen für JSON-Serialisierung verwendet wird.

### `service/TableRegistryService.java`
Verwaltet die Tabellen-Registry. Funktionen:
- `listTables()` — Alle registrierten Tabellen auflisten
- `describeTable(name)` — Spaltendefinitionen einer Tabelle aus der Registry laden
- `tableExists(name)` — Prüfen ob eine Tabelle registriert ist
- `registerTable(name, description, columns)` — Neue Tabelle registrieren: Einträge in TABLE_REGISTRY und COLUMN_REGISTRY erstellen, dann die Tabelle per dynamischem DDL (`CREATE TABLE`) in der Datenbank anlegen
- `removeTable(name)` — Tabelle aus DB und Registry löschen

Sicherheit: Alle Tabellen- und Spaltennamen werden gegen ein Whitelist-Pattern (`[A-Za-z][A-Za-z0-9_]`) validiert, um SQL-Injection zu verhindern.

### `service/DataQueryService.java`
Führt Datenabfragen auf registrierten Tabellen durch. Funktionen:
- `queryTable(name, filterColumn, filterOperator, filterValue, orderBy, limit)` — Dynamische SELECT-Query mit optionalem WHERE-Filter (parametrisiert), ORDER BY und LIMIT
- `countRows(name)` — `SELECT COUNT(*)` auf eine Tabelle
- `getTableData(name, limit)` — Alle Daten einer Tabelle mit Limit abrufen

Sicherheit: Nur registrierte Tabellen können abgefragt werden. Operatoren werden auf eine Whitelist beschränkt (`=`, `!=`, `>`, `<`, `>=`, `<=`, `LIKE`). Filterwerte werden als parametrisierte Query-Parameter übergeben.

### `mcp/DatabaseTools.java`
MCP-Tools — die Hauptschnittstelle für AI-Clients. Stellt 5 Tools bereit, die über `@McpTool`-Annotationen definiert sind:
- **list-tables** — Ruft `TableRegistryService.listTables()` auf und gibt das Ergebnis als formatiertes JSON zurück
- **describe-table** — Zeigt Spaltendefinitionen einer Tabelle
- **query-table** — Flexible Datenabfrage mit Filter, Sortierung und Limit
- **count-rows** — Zählt Zeilen einer Tabelle
- **register-table** — Nimmt einen Tabellennamen, Beschreibung und JSON-Array von Spaltendefinitionen entgegen, erstellt die Tabelle dynamisch

### `mcp/DatabaseResources.java`
MCP-Resources — stellt Daten als adressierbare URIs bereit, die AI-Clients lesen können:
- **db://tables** — Statische Resource: Liste aller Tabellen
- **db://table/{name}/schema** — Resource-Template: Schema einer bestimmten Tabelle
- **db://table/{name}/data** — Resource-Template: Daten einer Tabelle (max. 100 Zeilen)

### `mcp/DatabasePrompts.java`
MCP-Prompts — vorgefertigte Prompt-Templates für AI-Interaktionen:
- **data-analysis** — Generiert einen strukturierten Analyse-Prompt mit Tabellenschema, Zeilenanzahl und bis zu 10 Beispielzeilen. Fordert das AI-Modell auf, Zusammenfassung, Datenqualität, Muster und Query-Vorschläge zu liefern.
- **query-helper** — Generiert einen Kontext-Prompt mit allen verfügbaren Tabellen und deren Schemata, damit das AI-Modell beim Erstellen von Abfragen helfen kann.
