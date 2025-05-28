-- Inicialización de monedas
-- Este script se ejecutará automáticamente al iniciar la aplicación si spring.jpa.hibernate.ddl-auto=create o create-drop

-- Verificar si ya existen monedas
INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'USD', 'Dólar estadounidense', '$', 1.0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'USD');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'EUR', 'Euro', '€', 0.85, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'EUR');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'GBP', 'Libra esterlina', '£', 0.75, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'GBP');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'JPY', 'Yen japonés', '¥', 110.0, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'JPY');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'CAD', 'Dólar canadiense', 'C$', 1.25, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'CAD');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'AUD', 'Dólar australiano', 'A$', 1.35, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'AUD');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'CHF', 'Franco suizo', 'Fr', 0.92, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'CHF');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'CNY', 'Yuan chino', '¥', 6.45, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'CNY');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'MXN', 'Peso mexicano', '$', 20.0, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'MXN');

INSERT INTO currencies (code, name, symbol, exchange_rate, is_base_currency, created_at, updated_at)
SELECT 'BRL', 'Real brasileño', 'R$', 5.25, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM currencies WHERE code = 'BRL');
