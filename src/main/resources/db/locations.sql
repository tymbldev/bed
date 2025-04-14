-- SQL script to populate the locations table with a comprehensive list of locations

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE locations;
ALTER TABLE locations AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- United States
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('New York', 'NY', 'USA', '10001', 'New York, NY 10001, USA', 1, 1),
('San Francisco', 'CA', 'USA', '94105', 'San Francisco, CA 94105, USA', 2, 1),
('Los Angeles', 'CA', 'USA', '90001', 'Los Angeles, CA 90001, USA', 3, 1),
('Chicago', 'IL', 'USA', '60601', 'Chicago, IL 60601, USA', 4, 1),
('Boston', 'MA', 'USA', '02108', 'Boston, MA 02108, USA', 5, 1),
('Seattle', 'WA', 'USA', '98101', 'Seattle, WA 98101, USA', 6, 1),
('Austin', 'TX', 'USA', '78701', 'Austin, TX 78701, USA', 7, 1),
('Denver', 'CO', 'USA', '80202', 'Denver, CO 80202, USA', 8, 1),
('Washington', 'DC', 'USA', '20001', 'Washington, DC 20001, USA', 9, 1),
('Portland', 'OR', 'USA', '97201', 'Portland, OR 97201, USA', 10, 1),
('Atlanta', 'GA', 'USA', '30301', 'Atlanta, GA 30301, USA', 11, 1),
('Dallas', 'TX', 'USA', '75201', 'Dallas, TX 75201, USA', 12, 1),
('Houston', 'TX', 'USA', '77001', 'Houston, TX 77001, USA', 13, 1),
('Miami', 'FL', 'USA', '33101', 'Miami, FL 33101, USA', 14, 1),
('Philadelphia', 'PA', 'USA', '19101', 'Philadelphia, PA 19101, USA', 15, 1),
('Phoenix', 'AZ', 'USA', '85001', 'Phoenix, AZ 85001, USA', 16, 1),
('San Diego', 'CA', 'USA', '92101', 'San Diego, CA 92101, USA', 17, 1),
('San Jose', 'CA', 'USA', '95101', 'San Jose, CA 95101, USA', 18, 1),
('Minneapolis', 'MN', 'USA', '55401', 'Minneapolis, MN 55401, USA', 19, 1),
('Detroit', 'MI', 'USA', '48201', 'Detroit, MI 48201, USA', 20, 1);

-- Canada
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('Toronto', 'Ontario', 'Canada', 'M5V', 'Toronto, Ontario M5V, Canada', 21, 2),
('Vancouver', 'British Columbia', 'Canada', 'V6B', 'Vancouver, British Columbia V6B, Canada', 22, 2),
('Montreal', 'Quebec', 'Canada', 'H2Y', 'Montreal, Quebec H2Y, Canada', 23, 2),
('Calgary', 'Alberta', 'Canada', 'T2P', 'Calgary, Alberta T2P, Canada', 24, 2),
('Ottawa', 'Ontario', 'Canada', 'K1P', 'Ottawa, Ontario K1P, Canada', 25, 2);

-- Europe
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('London', 'England', 'UK', 'EC1A', 'London, England EC1A, UK', 26, 3),
('Paris', 'Île-de-France', 'France', '75001', 'Paris, Île-de-France 75001, France', 27, 4),
('Berlin', 'Berlin', 'Germany', '10115', 'Berlin, Berlin 10115, Germany', 28, 5),
('Amsterdam', 'North Holland', 'Netherlands', '1012', 'Amsterdam, North Holland 1012, Netherlands', 29, 6),
('Madrid', 'Madrid', 'Spain', '28001', 'Madrid, Madrid, 28001, Spain', 30, 7),
('Barcelona', 'Catalonia', 'Spain', '08001', 'Barcelona, Catalonia 08001, Spain', 31, 7),
('Rome', 'Lazio', 'Italy', '00100', 'Rome, Lazio 00100, Italy', 32, 8),
('Milan', 'Lombardy', 'Italy', '20121', 'Milan, Lombardy 20121, Italy', 33, 8),
('Dublin', 'Leinster', 'Ireland', 'D01', 'Dublin, Leinster D01, Ireland', 34, 9),
('Stockholm', 'Stockholm', 'Sweden', '111 44', 'Stockholm, Stockholm 111 44, Sweden', 35, 10),
('Copenhagen', 'Capital Region', 'Denmark', '1050', 'Copenhagen, Capital Region 1050, Denmark', 36, 11),
('Zurich', 'Zurich', 'Switzerland', '8001', 'Zurich, Zurich 8001, Switzerland', 37, 12),
('Munich', 'Bavaria', 'Germany', '80331', 'Munich, Bavaria 80331, Germany', 38, 5),
('Brussels', 'Brussels-Capital', 'Belgium', '1000', 'Brussels, Brussels-Capital 1000, Belgium', 39, 13),
('Vienna', 'Vienna', 'Austria', '1010', 'Vienna, Vienna 1010, Austria', 40, 14),
('Lisbon', 'Lisbon', 'Portugal', '1000-001', 'Lisbon, Lisbon 1000-001, Portugal', 41, 15),
('Athens', 'Attica', 'Greece', '10431', 'Athens, Attica 10431, Greece', 42, 16),
('Warsaw', 'Masovian', 'Poland', '00-001', 'Warsaw, Masovian 00-001, Poland', 43, 17),
('Prague', 'Prague', 'Czech Republic', '110 00', 'Prague, Prague 110 00, Czech Republic', 44, 18),
('Budapest', 'Budapest', 'Hungary', '1011', 'Budapest, Budapest 1011, Hungary', 45, 19);

-- Asia & Pacific
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('Tokyo', 'Tokyo', 'Japan', '100-0004', 'Tokyo, Tokyo 100-0004, Japan', 46, 20),
('Singapore', 'Central Region', 'Singapore', '018956', 'Singapore, Central Region 018956, Singapore', 47, 21),
('Hong Kong', 'Hong Kong Island', 'Hong Kong', '', 'Hong Kong, Hong Kong Island, Hong Kong', 48, 22),
('Seoul', 'Seoul', 'South Korea', '04524', 'Seoul, Seoul 04524, South Korea', 49, 23),
('Sydney', 'NSW', 'Australia', '2000', 'Sydney, NSW 2000, Australia', 50, 24),
('Melbourne', 'Victoria', 'Australia', '3000', 'Melbourne, Victoria 3000, Australia', 51, 24),
('Shanghai', 'Shanghai', 'China', '200000', 'Shanghai, Shanghai 200000, China', 52, 25),
('Beijing', 'Beijing', 'China', '100000', 'Beijing, Beijing 100000, China', 53, 25),
('Shenzhen', 'Guangdong', 'China', '518000', 'Shenzhen, Guangdong 518000, China', 54, 25),
('Auckland', 'Auckland', 'New Zealand', '1010', 'Auckland, Auckland 1010, New Zealand', 55, 26),
('Bangkok', 'Bangkok', 'Thailand', '10200', 'Bangkok, Bangkok 10200, Thailand', 56, 27),
('Jakarta', 'Jakarta', 'Indonesia', '10110', 'Jakarta, Jakarta 10110, Indonesia', 57, 28),
('Manila', 'Metro Manila', 'Philippines', '1000', 'Manila, Metro Manila 1000, Philippines', 58, 29),
('Ho Chi Minh City', 'Ho Chi Minh', 'Vietnam', '700000', 'Ho Chi Minh City, Ho Chi Minh 700000, Vietnam', 59, 30);

-- India
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('Visakhapatnam', 'Andhra Pradesh', 'India', '530001', 'Visakhapatnam, Andhra Pradesh 530001, India', 60, 31),
('Vijayawada', 'Andhra Pradesh', 'India', '520001', 'Vijayawada, Andhra Pradesh 520001, India', 61, 31),
('Guntur', 'Andhra Pradesh', 'India', '522001', 'Guntur, Andhra Pradesh 522001, India', 62, 31),
('Nellore', 'Andhra Pradesh', 'India', '524001', 'Nellore, Andhra Pradesh 524001, India', 63, 31),
('Tirupati', 'Andhra Pradesh', 'India', '517501', 'Tirupati, Andhra Pradesh 517501, India', 64, 31),
('Kurnool', 'Andhra Pradesh', 'India', '518001', 'Kurnool, Andhra Pradesh 518001, India', 65, 31),
('Kadapa', 'Andhra Pradesh', 'India', '516001', 'Kadapa, Andhra Pradesh 516001, India', 66, 31),
('Anantapur', 'Andhra Pradesh', 'India', '515001', 'Anantapur, Andhra Pradesh 515001, India', 67, 31),
('Kakinada', 'Andhra Pradesh', 'India', '533001', 'Kakinsada, Andhra Pradesh 533001, India', 68, 31),
('Mumbai', 'Maharashtra', 'India', '400001', 'Mumbai, Maharashtra 400001, India', 69, 31),
('Delhi', 'Delhi', 'India', '110001', 'Delhi, Delhi 110001, India', 70, 31),
('Bangalore', 'Karnataka', 'India', '560001', 'Bangalore, Karnataka 560001, India', 71, 31),
('Hyderabad', 'Telangana', 'India', '500001', 'Hyderabad, Telangana 500001, India', 72, 31),
('Chennai', 'Tamil Nadu', 'India', '600001', 'Chennai, Tamil Nadu 600001, India', 73, 31),
('Kolkata', 'West Bengal', 'India', '700001', 'Kolkata, West Bengal 700001, India', 74, 31),
('Ahmedabad', 'Gujarat', 'India', '380001', 'Ahmedabad, Gujarat 380001, India', 75, 31),
('Pune', 'Maharashtra', 'India', '411001', 'Pune, Maharashtra 411001, India', 76, 31),
('Jaipur', 'Rajasthan', 'India', '302001', 'Jaipur, Rajasthan 302001, India', 77, 31),
('Lucknow', 'Uttar Pradesh', 'India', '226001', 'Lucknow, Uttar Pradesh 226001, India', 78, 31),
('Kanpur', 'Uttar Pradesh', 'India', '208001', 'Kanpur, Uttar Pradesh 208001, India', 79, 31),
('Nagpur', 'Maharashtra', 'India', '440001', 'Nagpur, Maharashtra 440001, India', 80, 31),
('Indore', 'Madhya Pradesh', 'India', '452001', 'Indore, Madhya Pradesh 452001, India', 81, 31),
('Thane', 'Maharashtra', 'India', '400601', 'Thane, Maharashtra 400601, India', 82, 31),
('Bhopal', 'Madhya Pradesh', 'India', '462001', 'Bhopal, Madhya Pradesh 462001, India', 83, 31),
('Patna', 'Bihar', 'India', '800001', 'Patna, Bihar 800001, India', 84, 31),
('Gurgaon', 'Haryana', 'India', '122001', 'Gurgaon, Haryana 122001, India', 85, 31),
('Noida', 'Uttar Pradesh', 'India', '201301', 'Noida, Uttar Pradesh 201301, India', 86, 31),
('Chandigarh', 'Chandigarh', 'India', '160001', 'Chandigarh, Chandigarh 160001, India', 87, 31),
('Coimbatore', 'Tamil Nadu', 'India', '641001', 'Coimbatore, Tamil Nadu 641001, India', 88, 31),
('Guwahati', 'Assam', 'India', '781001', 'Guwahati, Assam 781001, India', 89, 31);

-- Middle East & Africa
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('Dubai', 'Dubai', 'UAE', '', 'Dubai, Dubai, UAE', 90, 32),
('Tel Aviv', 'Tel Aviv', 'Israel', '6701101', 'Tel Aviv, Tel Aviv 6701101, Israel', 91, 33),
('Cape Town', 'Western Cape', 'South Africa', '8001', 'Cape Town, Western Cape 8001, South Africa', 92, 34),
('Johannesburg', 'Gauteng', 'South Africa', '2000', 'Johannesburg, Gauteng 2000, South Africa', 93, 34),
('Nairobi', 'Nairobi', 'Kenya', '00100', 'Nairobi, Nairobi 00100, Kenya', 94, 35),
('Lagos', 'Lagos', 'Nigeria', '100001', 'Lagos, Lagos 100001, Nigeria', 95, 36),
('Cairo', 'Cairo', 'Egypt', '11511', 'Cairo, Cairo 11511, Egypt', 96, 37),
('Riyadh', 'Riyadh', 'Saudi Arabia', '12214', 'Riyadh, Riyadh 12214, Saudi Arabia', 97, 38);

-- Latin America
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, city_id, country_id) VALUES 
('Mexico City', 'CDMX', 'Mexico', '06000', 'Mexico City, CDMX 06000, Mexico', 98, 39),
('São Paulo', 'São Paulo', 'Brazil', '01310-100', 'São Paulo, São Paulo 01310-100, Brazil', 99, 40),
('Buenos Aires', 'Buenos Aires', 'Argentina', 'C1001', 'Buenos Aires, Buenos Aires C1001, Argentina', 100, 41),
('Rio de Janeiro', 'Rio de Janeiro', 'Brazil', '20021-130', 'Rio de Janeiro, Rio de Janeiro 20021-130, Brazil', 101, 40),
('Bogotá', 'Bogotá', 'Colombia', '110111', 'Bogotá, Bogotá 110111, Colombia', 102, 42),
('Santiago', 'Santiago', 'Chile', '8320000', 'Santiago, Santiago 8320000, Chile', 103, 43),
('Lima', 'Lima', 'Peru', '15001', 'Lima, Lima 15001, Peru', 104, 44);

-- Remote
INSERT IGNORE INTO locations (city, state, country, zip_code, display_name, is_remote) VALUES 
('Remote', '', 'Remote', '', 'Remote', TRUE); 