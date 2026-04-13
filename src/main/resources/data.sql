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
