-- SQL script to populate the currencies table with major world currencies

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE currencies;
ALTER TABLE currencies AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Major World Currencies
INSERT IGNORE INTO currencies (code, name, symbol, exchange_rate, is_active, created_at, updated_at) VALUES
('USD', 'US Dollar', '$', 1.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EUR', 'Euro', '€', 0.92, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('GBP', 'British Pound', '£', 0.79, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('INR', 'Indian Rupee', '₹', 83.12, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('JPY', 'Japanese Yen', '¥', 151.45, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AUD', 'Australian Dollar', 'A$', 1.52, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CAD', 'Canadian Dollar', 'C$', 1.35, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SGD', 'Singapore Dollar', 'S$', 1.35, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CHF', 'Swiss Franc', 'Fr', 0.90, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CNY', 'Chinese Yuan', '¥', 7.24, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('AED', 'UAE Dirham', 'د.إ', 3.67, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SAR', 'Saudi Riyal', '﷼', 3.75, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HKD', 'Hong Kong Dollar', 'HK$', 7.83, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('NZD', 'New Zealand Dollar', 'NZ$', 1.66, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SEK', 'Swedish Krona', 'kr', 10.58, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('NOK', 'Norwegian Krone', 'kr', 10.72, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DKK', 'Danish Krone', 'kr', 6.88, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('ZAR', 'South African Rand', 'R', 18.89, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BRL', 'Brazilian Real', 'R$', 5.01, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MXN', 'Mexican Peso', '$', 16.67, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 