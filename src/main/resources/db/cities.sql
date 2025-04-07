-- SQL script to populate the cities table with major cities worldwide
-- These INSERT statements will add cities with references to the countries table

-- Clear existing data


TRUNCATE TABLE cities;
ALTER TABLE cities AUTO_INCREMENT = 1;


-- We'll create city entries for major countries
-- Note: country_id values should be based on the sequence from countries.sql

-- United States (country_id = 197)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('New York', 197, '10001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Los Angeles', 197, '90001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Chicago', 197, '60601');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Houston', 197, '77001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Phoenix', 197, '85001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Philadelphia', 197, '19101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('San Antonio', 197, '78201');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('San Diego', 197, '92101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Dallas', 197, '75201');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('San Jose', 197, '95101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Austin', 197, '78701');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Jacksonville', 197, '32099');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Fort Worth', 197, '76101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Columbus', 197, '43085');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('San Francisco', 197, '94102');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Charlotte', 197, '28201');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Indianapolis', 197, '46201');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Seattle', 197, '98101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Denver', 197, '80201');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Washington DC', 197, '20001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Boston', 197, '02101');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Atlanta', 197, '30301');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Miami', 197, '33101');

-- Canada (country_id = 30)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Toronto', 30, 'M5A');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Montreal', 30, 'H2Y');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Vancouver', 30, 'V5K');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Calgary', 30, 'T2P');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Edmonton', 30, 'T5J');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Ottawa', 30, 'K1P');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Winnipeg', 30, 'R3C');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Quebec City', 30, 'G1R');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Hamilton', 30, 'L8P');

-- United Kingdom (country_id = 196)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('London', 196, 'EC1A');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Manchester', 196, 'M1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Birmingham', 196, 'B1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Glasgow', 196, 'G1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Liverpool', 196, 'L1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Bristol', 196, 'BS1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Edinburgh', 196, 'EH1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Leeds', 196, 'LS1');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Sheffield', 196, 'S1');

-- Australia (country_id = 8)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Sydney', 8, '2000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Melbourne', 8, '3000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Brisbane', 8, '4000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Perth', 8, '6000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Adelaide', 8, '5000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Gold Coast', 8, '4217');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Canberra', 8, '2600');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Newcastle', 8, '2300');

-- Germany (country_id = 66)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Berlin', 66, '10115');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Hamburg', 66, '20095');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Munich', 66, '80331');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Cologne', 66, '50667');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Frankfurt', 66, '60311');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Stuttgart', 66, '70173');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Düsseldorf', 66, '40213');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Leipzig', 66, '04109');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Dortmund', 66, '44137');

-- France (country_id = 62)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Paris', 62, '75001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Marseille', 62, '13001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Lyon', 62, '69001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Toulouse', 62, '31000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Nice', 62, '06000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Nantes', 62, '44000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Strasbourg', 62, '67000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Montpellier', 62, '34000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Bordeaux', 62, '33000');

-- India (country_id = 81)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Mumbai', 81, '400001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Delhi', 81, '110001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Bangalore', 81, '560001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Hyderabad', 81, '500001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Chennai', 81, '600001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Kolkata', 81, '700001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Ahmedabad', 81, '380001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Pune', 81, '411001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Jaipur', 81, '302001');

-- China (country_id = 35)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Beijing', 35, '100000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Shanghai', 35, '200000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Guangzhou', 35, '510000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Shenzhen', 35, '518000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Chengdu', 35, '610000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Tianjin', 35, '300000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Wuhan', 35, '430000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Xi\'an', 35, '710000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Hangzhou', 35, '310000');

-- Japan (country_id = 91)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Tokyo', 91, '100-0001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Osaka', 91, '530-0001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Yokohama', 91, '220-0000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Nagoya', 91, '450-0001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Sapporo', 91, '060-0000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Kobe', 91, '650-0000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Kyoto', 91, '600-0000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Fukuoka', 91, '810-0000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Kawasaki', 91, '210-0000');

-- Brazil (country_id = 23)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('São Paulo', 23, '01000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Rio de Janeiro', 23, '20000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Brasília', 23, '70000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Salvador', 23, '40000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Fortaleza', 23, '60000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Belo Horizonte', 23, '30000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Manaus', 23, '69000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Curitiba', 23, '80000-000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Recife', 23, '50000-000');

-- South Africa (country_id = 169)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Johannesburg', 169, '2000');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Cape Town', 169, '8001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Durban', 169, '4001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Pretoria', 169, '0001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Port Elizabeth', 169, '6001');
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Bloemfontein', 169, '9301');

-- Singapore (country_id = 165)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Singapore', 165, '018956');

-- Remote (country_id = 196)
INSERT IGNORE INTO cities (name, country_id, zip_code) VALUES ('Remote', 196, ''); 