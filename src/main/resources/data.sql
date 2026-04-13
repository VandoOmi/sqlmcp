-- Register example tables in the registry
INSERT INTO TABLE_REGISTRY (TABLE_NAME, DESCRIPTION) VALUES
    ('CUSTOMERS', 'Customer master data with contact information'),
    ('ORDERS', 'Customer orders with product and amount details');

-- Column definitions for CUSTOMERS
INSERT INTO COLUMN_REGISTRY (TABLE_NAME, COLUMN_NAME, DATA_TYPE, DESCRIPTION, NULLABLE, PRIMARY_KEY, ORDINAL) VALUES
    ('CUSTOMERS', 'ID',    'INT',          'Unique customer identifier', FALSE, TRUE,  1),
    ('CUSTOMERS', 'NAME',  'VARCHAR(200)', 'Full name of the customer',  FALSE, FALSE, 2),
    ('CUSTOMERS', 'EMAIL', 'VARCHAR(200)', 'Email address',              TRUE,  FALSE, 3),
    ('CUSTOMERS', 'CITY',  'VARCHAR(100)', 'City of residence',          TRUE,  FALSE, 4);

-- Column definitions for ORDERS
INSERT INTO COLUMN_REGISTRY (TABLE_NAME, COLUMN_NAME, DATA_TYPE, DESCRIPTION, NULLABLE, PRIMARY_KEY, ORDINAL) VALUES
    ('ORDERS', 'ID',          'INT',            'Unique order identifier',    FALSE, TRUE,  1),
    ('ORDERS', 'CUSTOMER_ID', 'INT',            'Reference to CUSTOMERS.ID',  FALSE, FALSE, 2),
    ('ORDERS', 'PRODUCT',     'VARCHAR(200)',   'Product name',               FALSE, FALSE, 3),
    ('ORDERS', 'AMOUNT',      'DECIMAL(10,2)',  'Order amount in EUR',        FALSE, FALSE, 4),
    ('ORDERS', 'ORDER_DATE',  'DATE',           'Date the order was placed',  TRUE,  FALSE, 5);

-- =====================================================
-- Dynamic MCP Tool Definitions
-- =====================================================

-- Tool: list-tables (no parameters)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('list-tables', 'List all registered database tables with their descriptions',
     'SELECT TABLE_NAME, DESCRIPTION, CREATED_AT FROM TABLE_REGISTRY ORDER BY TABLE_NAME');

-- Tool: describe-table (1 parameter)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('describe-table', 'Show column definitions for a specific table including name, data type, nullable, and primary key information',
     'SELECT COLUMN_NAME, DATA_TYPE, DESCRIPTION, NULLABLE, PRIMARY_KEY FROM COLUMN_REGISTRY WHERE TABLE_NAME = :tableName ORDER BY ORDINAL');

INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) VALUES
    ('describe-table', 'tableName', 'string', 'Name of the table to describe', TRUE, 1);

-- Tool: query-customers (no parameters, returns all customers)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('query-customers', 'Retrieve all customers',
     'SELECT * FROM CUSTOMERS ORDER BY ID');

-- Tool: customers-by-city (1 parameter)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('customers-by-city', 'Find customers in a specific city',
     'SELECT * FROM CUSTOMERS WHERE CITY = :city ORDER BY NAME');

INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) VALUES
    ('customers-by-city', 'city', 'string', 'City to filter by', TRUE, 1);

-- Tool: orders-by-customer (1 parameter)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('orders-by-customer', 'Find orders for a customer by name (supports LIKE patterns)',
     'SELECT o.ID, o.PRODUCT, o.AMOUNT, o.ORDER_DATE, c.NAME AS CUSTOMER_NAME FROM ORDERS o JOIN CUSTOMERS c ON o.CUSTOMER_ID = c.ID WHERE c.NAME LIKE :customerName ORDER BY o.ORDER_DATE DESC');

INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) VALUES
    ('orders-by-customer', 'customerName', 'string', 'Customer name or LIKE pattern (e.g. %Muster%)', TRUE, 1);

-- Tool: high-value-orders (1 parameter)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('high-value-orders', 'Find orders above a given amount',
     'SELECT o.ID, c.NAME AS CUSTOMER_NAME, o.PRODUCT, o.AMOUNT, o.ORDER_DATE FROM ORDERS o JOIN CUSTOMERS c ON o.CUSTOMER_ID = c.ID WHERE o.AMOUNT > :minAmount ORDER BY o.AMOUNT DESC');

INSERT INTO TOOL_PARAMETER (TOOL_NAME, PARAM_NAME, PARAM_TYPE, DESCRIPTION, REQUIRED, ORDINAL) VALUES
    ('high-value-orders', 'minAmount', 'number', 'Minimum order amount', TRUE, 1);

-- Tool: count-rows (1 parameter — uses dynamic table name via registry lookup)
INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('count-customers', 'Count the number of customers',
     'SELECT COUNT(*) AS ROW_COUNT FROM CUSTOMERS');

INSERT INTO TOOL_REGISTRY (TOOL_NAME, DESCRIPTION, SQL_QUERY) VALUES
    ('count-orders', 'Count the number of orders',
     'SELECT COUNT(*) AS ROW_COUNT FROM ORDERS');

-- Example customers
INSERT INTO CUSTOMERS (NAME, EMAIL, CITY) VALUES
    ('Max Mustermann', 'max@example.com', 'Berlin'),
    ('Erika Muster', 'erika@example.com', 'München'),
    ('Hans Schmidt', 'hans@example.com', 'Hamburg'),
    ('Anna Weber', 'anna@example.com', 'Köln'),
    ('Peter Fischer', 'peter@example.com', 'Frankfurt');

-- Example orders
INSERT INTO ORDERS (CUSTOMER_ID, PRODUCT, AMOUNT, ORDER_DATE) VALUES
    (1, 'Laptop',      1299.99, '2026-03-15'),
    (1, 'Maus',          29.99, '2026-03-15'),
    (2, 'Monitor',      449.00, '2026-03-20'),
    (3, 'Tastatur',      89.50, '2026-04-01'),
    (4, 'Webcam',        79.99, '2026-04-05'),
    (4, 'Headset',      129.00, '2026-04-05'),
    (5, 'USB-Hub',       34.99, '2026-04-10');
