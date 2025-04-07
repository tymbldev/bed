-- Insert cities for India
-- Note: country_id = 81 for India (based on countries.sql)

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE cities;
ALTER TABLE cities AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert cities with country_id reference
INSERT INTO cities (name, country_id, zip_code) VALUES
-- Andhra Pradesh cities
('Visakhapatnam', 81, '530001'),
('Vijayawada', 81, '520001'),
('Guntur', 81, '522001'),
('Nellore', 81, '524001'),
('Tirupati', 81, '517501'),
('Kurnool', 81, '518001'),
('Kadapa', 81, '516001'),
('Anantapur', 81, '515001'),
('Kakinada', 81, '533001'),

-- Major cities from other states
('Mumbai', 81, '400001'),
('Delhi', 81, '110001'),
('Bangalore', 81, '560001'),
('Hyderabad', 81, '500001'),
('Chennai', 81, '600001'),
('Kolkata', 81, '700001'),
('Ahmedabad', 81, '380001'),
('Pune', 81, '411001'),
('Jaipur', 81, '302001'),
('Lucknow', 81, '226001'),
('Kanpur', 81, '208001'),
('Nagpur', 81, '440001'),
('Indore', 81, '452001'),
('Thane', 81, '400601'),
('Bhopal', 81, '462001'),
('Patna', 81, '800001'),
('Gurgaon', 81, '122001'),
('Noida', 81, '201301'),
('Chandigarh', 81, '160001'),
('Coimbatore', 81, '641001'),
('Guwahati', 81, '781001'),
('Bhubaneswar', 81, '751001'),
('Dehradun', 81, '248001'),
('Mysore', 81, '570001'),
('Trivandrum', 81, '695001'),
('Kochi', 81, '682001'),
('Goa', 81, '403001'),
('Shimla', 81, '171001'),
('Gangtok', 81, '737101'),
('Port Blair', 81, '744101'); 