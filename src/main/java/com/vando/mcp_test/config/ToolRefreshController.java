package com.vando.mcp_test.config;

import com.vando.mcp_test.mcp.DynamicToolManager;
import com.vando.mcp_test.mcp.DynamicToolManager.RefreshResult;
import com.vando.mcp_test.mcp.SqlValidator;
import com.vando.mcp_test.service.TableRegistryService;
import com.vando.mcp_test.service.ToolRegistryService;
import com.vando.mcp_test.service.ToolRegistryService.ToolParameter;
import com.vando.mcp_test.service.DataQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ToolRefreshController {

    private final DynamicToolManager dynamicToolManager;
    private final ToolRegistryService toolRegistry;
    private final TableRegistryService tableRegistry;
    private final DataQueryService dataQuery;

    public ToolRefreshController(DynamicToolManager dynamicToolManager,
                                 ToolRegistryService toolRegistry,
                                 TableRegistryService tableRegistry,
                                 DataQueryService dataQuery) {
        this.dynamicToolManager = dynamicToolManager;
        this.toolRegistry = toolRegistry;
        this.tableRegistry = tableRegistry;
        this.dataQuery = dataQuery;
    }

    // ---- Dashboard ----

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("tools", toolRegistry.listAllTools().size());
        stats.put("activeTools", toolRegistry.loadActiveTools().size());
        stats.put("tables", tableRegistry.listTables().size());
        return ResponseEntity.ok(stats);
    }

    // ---- Tools CRUD ----

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, Object>>> listTools() {
        List<Map<String, Object>> tools = toolRegistry.listAllTools();
        for (Map<String, Object> tool : tools) {
            String name = (String) tool.get("TOOL_NAME");
            tool.put("parameters", toolRegistry.loadParameters(name));
        }
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/tools/{name}")
    public ResponseEntity<?> getTool(@PathVariable String name) {
        Map<String, Object> tool = toolRegistry.getTool(name);
        if (tool == null) {
            return ResponseEntity.notFound().build();
        }
        tool.put("parameters", toolRegistry.loadParameters(name));
        return ResponseEntity.ok(tool);
    }

    @PostMapping("/tools")
    public ResponseEntity<?> createTool(@RequestBody ToolRequest request) {
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tool name is required"));
        }
        if (request.sqlQuery == null || request.sqlQuery.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL query is required"));
        }

        try {
            SqlValidator.validate(request.sqlQuery);
        } catch (SqlValidator.SqlValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL validation failed: " + e.getMessage()));
        }

        if (toolRegistry.getTool(request.name) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tool '" + request.name + "' already exists"));
        }

        List<ToolParameter> params = request.parameters != null
                ? request.parameters.stream()
                    .map(p -> new ToolParameter(p.name, p.type != null ? p.type : "string", p.description, p.required))
                    .toList()
                : List.of();

        toolRegistry.createTool(request.name, request.description, request.sqlQuery,
                request.active, params);

        return ResponseEntity.ok(Map.of("message", "Tool '" + request.name + "' created"));
    }

    @PutMapping("/tools/{name}")
    public ResponseEntity<?> updateTool(@PathVariable String name, @RequestBody ToolRequest request) {
        if (toolRegistry.getTool(name) == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            SqlValidator.validate(request.sqlQuery);
        } catch (SqlValidator.SqlValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "SQL validation failed: " + e.getMessage()));
        }

        List<ToolParameter> params = request.parameters != null
                ? request.parameters.stream()
                    .map(p -> new ToolParameter(p.name, p.type != null ? p.type : "string", p.description, p.required))
                    .toList()
                : List.of();

        toolRegistry.updateTool(name, request.description, request.sqlQuery,
                request.active, params);

        return ResponseEntity.ok(Map.of("message", "Tool '" + name + "' updated"));
    }

    @DeleteMapping("/tools/{name}")
    public ResponseEntity<?> deleteTool(@PathVariable String name) {
        if (toolRegistry.getTool(name) == null) {
            return ResponseEntity.notFound().build();
        }
        toolRegistry.deleteTool(name);
        return ResponseEntity.ok(Map.of("message", "Tool '" + name + "' deleted"));
    }

    @PostMapping("/tools/{name}/toggle")
    public ResponseEntity<?> toggleTool(@PathVariable String name) {
        Map<String, Object> tool = toolRegistry.getTool(name);
        if (tool == null) {
            return ResponseEntity.notFound().build();
        }
        boolean currentActive = (Boolean) tool.get("ACTIVE");
        toolRegistry.setToolActive(name, !currentActive);
        return ResponseEntity.ok(Map.of("active", !currentActive));
    }

    @PostMapping("/tools/refresh")
    public ResponseEntity<RefreshResult> refreshTools() {
        RefreshResult result = dynamicToolManager.refreshTools();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/tools/validate-sql")
    public ResponseEntity<?> validateSql(@RequestBody Map<String, String> body) {
        String sql = body.get("sql");
        if (sql == null || sql.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "SQL is empty"));
        }
        try {
            SqlValidator.validate(sql);
            return ResponseEntity.ok(Map.of("valid", true));
        } catch (SqlValidator.SqlValidationException e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    // ---- Tables ----

    @GetMapping("/tables")
    public ResponseEntity<List<Map<String, Object>>> listTables() {
        return ResponseEntity.ok(tableRegistry.listTables());
    }

    @GetMapping("/tables/{name}/schema")
    public ResponseEntity<?> tableSchema(@PathVariable String name) {
        List<Map<String, Object>> columns = tableRegistry.describeTable(name);
        return ResponseEntity.ok(columns);
    }

    @GetMapping("/tables/{name}/data")
    public ResponseEntity<?> tableData(@PathVariable String name,
                                        @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rowCount", dataQuery.countRows(name));
        result.put("data", dataQuery.getTableData(name, limit));
        return ResponseEntity.ok(result);
    }

    // ---- Request DTOs ----

    public static class ToolRequest {
        public String name;
        public String description;
        public String sqlQuery;
        public boolean active = true;
        public List<ParamRequest> parameters;
    }

    public static class ParamRequest {
        public String name;
        public String type;
        public String description;
        public boolean required;
    }
}
