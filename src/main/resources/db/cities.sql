-- SQL script to populate the cities table with a comprehensive list

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE cities;
ALTER TABLE cities AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- United States
INSERT IGNORE INTO cities (name, country_id) VALUES ('New York', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('San Francisco', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Los Angeles', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Chicago', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Boston', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Seattle', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Austin', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Denver', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Washington', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Portland', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Atlanta', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dallas', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Houston', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Miami', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Philadelphia', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Phoenix', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('San Diego', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('San Jose', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Minneapolis', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Detroit', (SELECT id FROM countries WHERE code = 'US'));

-- Canada
INSERT IGNORE INTO cities (name, country_id) VALUES ('Toronto', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Vancouver', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Montreal', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Calgary', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ottawa', (SELECT id FROM countries WHERE code = 'CA'));

-- Europe
INSERT IGNORE INTO cities (name, country_id) VALUES ('London', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Paris', (SELECT id FROM countries WHERE code = 'FR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Berlin', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Amsterdam', (SELECT id FROM countries WHERE code = 'NL'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Madrid', (SELECT id FROM countries WHERE code = 'ES'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Barcelona', (SELECT id FROM countries WHERE code = 'ES'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rome', (SELECT id FROM countries WHERE code = 'IT'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Milan', (SELECT id FROM countries WHERE code = 'IT'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dublin', (SELECT id FROM countries WHERE code = 'IE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Stockholm', (SELECT id FROM countries WHERE code = 'SE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Copenhagen', (SELECT id FROM countries WHERE code = 'DK'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Zurich', (SELECT id FROM countries WHERE code = 'CH'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Munich', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Brussels', (SELECT id FROM countries WHERE code = 'BE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Vienna', (SELECT id FROM countries WHERE code = 'AT'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Lisbon', (SELECT id FROM countries WHERE code = 'PT'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Athens', (SELECT id FROM countries WHERE code = 'GR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Warsaw', (SELECT id FROM countries WHERE code = 'PL'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Prague', (SELECT id FROM countries WHERE code = 'CZ'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Budapest', (SELECT id FROM countries WHERE code = 'HU'));

-- Asia & Pacific
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tokyo', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Singapore', (SELECT id FROM countries WHERE code = 'SG'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hong Kong', (SELECT id FROM countries WHERE code = 'HK'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Seoul', (SELECT id FROM countries WHERE code = 'KR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sydney', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Melbourne', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Shanghai', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Beijing', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Shenzhen', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Auckland', (SELECT id FROM countries WHERE code = 'NZ'));

-- India
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bangalore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mumbai', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('New Delhi', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hyderabad', (SELECT id FROM countries WHERE code = 'IN'));
-- Additional Indian Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Visakhapatnam', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Vijayawada', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Guntur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nellore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tirupati', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kurnool', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kadapa', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Anantapur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kakinada', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Chennai', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kolkata', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ahmedabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Pune', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jaipur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Lucknow', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kanpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nagpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Indore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Thane', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhopal', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Patna', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gurgaon', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Noida', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Chandigarh', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Coimbatore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Guwahati', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhubaneswar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dehradun', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mysore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Trivandrum', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kochi', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Goa', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Shimla', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gangtok', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Port Blair', (SELECT id FROM countries WHERE code = 'IN'));

-- Rest of Asia & Pacific
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bangkok', (SELECT id FROM countries WHERE code = 'TH'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jakarta', (SELECT id FROM countries WHERE code = 'ID'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Manila', (SELECT id FROM countries WHERE code = 'PH'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ho Chi Minh City', (SELECT id FROM countries WHERE code = 'VN'));

-- Middle East & Africa
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dubai', (SELECT id FROM countries WHERE code = 'AE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tel Aviv', (SELECT id FROM countries WHERE code = 'IL'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cape Town', (SELECT id FROM countries WHERE code = 'ZA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Johannesburg', (SELECT id FROM countries WHERE code = 'ZA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nairobi', (SELECT id FROM countries WHERE code = 'KE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Lagos', (SELECT id FROM countries WHERE code = 'NG'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cairo', (SELECT id FROM countries WHERE code = 'EG'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Riyadh', (SELECT id FROM countries WHERE code = 'SA'));

-- Latin America
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mexico City', (SELECT id FROM countries WHERE code = 'MX'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('São Paulo', (SELECT id FROM countries WHERE code = 'BR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Buenos Aires', (SELECT id FROM countries WHERE code = 'AR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rio de Janeiro', (SELECT id FROM countries WHERE code = 'BR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bogotá', (SELECT id FROM countries WHERE code = 'CO'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Santiago', (SELECT id FROM countries WHERE code = 'CL'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Lima', (SELECT id FROM countries WHERE code = 'PE'));

-- Remote
INSERT IGNORE INTO cities (name, country_id) VALUES ('Remote', (SELECT id FROM countries WHERE code = 'RM')); 