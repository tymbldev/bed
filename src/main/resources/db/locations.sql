-- SQL script to populate the locations table with a comprehensive list of locations

-- Clear existing data
TRUNCATE TABLE locations RESTART IDENTITY CASCADE;

-- United States
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('New York', 'NY', 'USA', '10001', 'New York, NY 10001, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('San Francisco', 'CA', 'USA', '94105', 'San Francisco, CA 94105, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Los Angeles', 'CA', 'USA', '90001', 'Los Angeles, CA 90001, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Chicago', 'IL', 'USA', '60601', 'Chicago, IL 60601, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Boston', 'MA', 'USA', '02108', 'Boston, MA 02108, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Seattle', 'WA', 'USA', '98101', 'Seattle, WA 98101, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Austin', 'TX', 'USA', '78701', 'Austin, TX 78701, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Denver', 'CO', 'USA', '80202', 'Denver, CO 80202, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Washington', 'DC', 'USA', '20001', 'Washington, DC 20001, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Portland', 'OR', 'USA', '97201', 'Portland, OR 97201, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Atlanta', 'GA', 'USA', '30301', 'Atlanta, GA 30301, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Dallas', 'TX', 'USA', '75201', 'Dallas, TX 75201, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Houston', 'TX', 'USA', '77001', 'Houston, TX 77001, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Miami', 'FL', 'USA', '33101', 'Miami, FL 33101, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Philadelphia', 'PA', 'USA', '19101', 'Philadelphia, PA 19101, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Phoenix', 'AZ', 'USA', '85001', 'Phoenix, AZ 85001, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('San Diego', 'CA', 'USA', '92101', 'San Diego, CA 92101, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('San Jose', 'CA', 'USA', '95101', 'San Jose, CA 95101, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Minneapolis', 'MN', 'USA', '55401', 'Minneapolis, MN 55401, USA');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Detroit', 'MI', 'USA', '48201', 'Detroit, MI 48201, USA');

-- Canada
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Toronto', 'Ontario', 'Canada', 'M5V', 'Toronto, Ontario M5V, Canada');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Vancouver', 'British Columbia', 'Canada', 'V6B', 'Vancouver, British Columbia V6B, Canada');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Montreal', 'Quebec', 'Canada', 'H2Y', 'Montreal, Quebec H2Y, Canada');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Calgary', 'Alberta', 'Canada', 'T2P', 'Calgary, Alberta T2P, Canada');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Ottawa', 'Ontario', 'Canada', 'K1P', 'Ottawa, Ontario K1P, Canada');

-- Europe
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('London', 'England', 'UK', 'EC1A', 'London, England EC1A, UK');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Paris', 'Île-de-France', 'France', '75001', 'Paris, Île-de-France 75001, France');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Berlin', 'Berlin', 'Germany', '10115', 'Berlin, Berlin 10115, Germany');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Amsterdam', 'North Holland', 'Netherlands', '1012', 'Amsterdam, North Holland 1012, Netherlands');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Madrid', 'Madrid', 'Spain', '28001', 'Madrid, Madrid, 28001, Spain');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Barcelona', 'Catalonia', 'Spain', '08001', 'Barcelona, Catalonia 08001, Spain');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Rome', 'Lazio', 'Italy', '00100', 'Rome, Lazio 00100, Italy');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Milan', 'Lombardy', 'Italy', '20121', 'Milan, Lombardy 20121, Italy');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Dublin', 'Leinster', 'Ireland', 'D01', 'Dublin, Leinster D01, Ireland');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Stockholm', 'Stockholm', 'Sweden', '111 44', 'Stockholm, Stockholm 111 44, Sweden');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Copenhagen', 'Capital Region', 'Denmark', '1050', 'Copenhagen, Capital Region 1050, Denmark');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Zurich', 'Zurich', 'Switzerland', '8001', 'Zurich, Zurich 8001, Switzerland');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Munich', 'Bavaria', 'Germany', '80331', 'Munich, Bavaria 80331, Germany');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Brussels', 'Brussels-Capital', 'Belgium', '1000', 'Brussels, Brussels-Capital 1000, Belgium');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Vienna', 'Vienna', 'Austria', '1010', 'Vienna, Vienna 1010, Austria');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Lisbon', 'Lisbon', 'Portugal', '1000-001', 'Lisbon, Lisbon 1000-001, Portugal');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Athens', 'Attica', 'Greece', '10431', 'Athens, Attica 10431, Greece');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Warsaw', 'Masovian', 'Poland', '00-001', 'Warsaw, Masovian 00-001, Poland');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Prague', 'Prague', 'Czech Republic', '110 00', 'Prague, Prague 110 00, Czech Republic');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Budapest', 'Budapest', 'Hungary', '1011', 'Budapest, Budapest 1011, Hungary');

-- Asia & Pacific
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Tokyo', 'Tokyo', 'Japan', '100-0004', 'Tokyo, Tokyo 100-0004, Japan');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Singapore', 'Central Region', 'Singapore', '018956', 'Singapore, Central Region 018956, Singapore');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Hong Kong', 'Hong Kong Island', 'Hong Kong', '', 'Hong Kong, Hong Kong Island, Hong Kong');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Seoul', 'Seoul', 'South Korea', '04524', 'Seoul, Seoul 04524, South Korea');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Sydney', 'NSW', 'Australia', '2000', 'Sydney, NSW 2000, Australia');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Melbourne', 'Victoria', 'Australia', '3000', 'Melbourne, Victoria 3000, Australia');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Shanghai', 'Shanghai', 'China', '200000', 'Shanghai, Shanghai 200000, China');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Beijing', 'Beijing', 'China', '100000', 'Beijing, Beijing 100000, China');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Shenzhen', 'Guangdong', 'China', '518000', 'Shenzhen, Guangdong 518000, China');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Auckland', 'Auckland', 'New Zealand', '1010', 'Auckland, Auckland 1010, New Zealand');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Bangalore', 'Karnataka', 'India', '560001', 'Bangalore, Karnataka 560001, India');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Mumbai', 'Maharashtra', 'India', '400001', 'Mumbai, Maharashtra 400001, India');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('New Delhi', 'Delhi', 'India', '110001', 'New Delhi, Delhi 110001, India');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Hyderabad', 'Telangana', 'India', '500001', 'Hyderabad, Telangana 500001, India');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Bangkok', 'Bangkok', 'Thailand', '10200', 'Bangkok, Bangkok 10200, Thailand');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Jakarta', 'Jakarta', 'Indonesia', '10110', 'Jakarta, Jakarta 10110, Indonesia');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Manila', 'Metro Manila', 'Philippines', '1000', 'Manila, Metro Manila 1000, Philippines');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Ho Chi Minh City', 'Ho Chi Minh', 'Vietnam', '700000', 'Ho Chi Minh City, Ho Chi Minh 700000, Vietnam');

-- Middle East & Africa
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Dubai', 'Dubai', 'UAE', '', 'Dubai, Dubai, UAE');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Tel Aviv', 'Tel Aviv', 'Israel', '6701101', 'Tel Aviv, Tel Aviv 6701101, Israel');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Cape Town', 'Western Cape', 'South Africa', '8001', 'Cape Town, Western Cape 8001, South Africa');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Johannesburg', 'Gauteng', 'South Africa', '2000', 'Johannesburg, Gauteng 2000, South Africa');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Nairobi', 'Nairobi', 'Kenya', '00100', 'Nairobi, Nairobi 00100, Kenya');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Lagos', 'Lagos', 'Nigeria', '100001', 'Lagos, Lagos 100001, Nigeria');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Cairo', 'Cairo', 'Egypt', '11511', 'Cairo, Cairo 11511, Egypt');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Riyadh', 'Riyadh', 'Saudi Arabia', '12214', 'Riyadh, Riyadh 12214, Saudi Arabia');

-- Latin America
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Mexico City', 'CDMX', 'Mexico', '06000', 'Mexico City, CDMX 06000, Mexico');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('São Paulo', 'São Paulo', 'Brazil', '01310-100', 'São Paulo, São Paulo 01310-100, Brazil');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Buenos Aires', 'Buenos Aires', 'Argentina', 'C1001', 'Buenos Aires, Buenos Aires C1001, Argentina');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Rio de Janeiro', 'Rio de Janeiro', 'Brazil', '20021-130', 'Rio de Janeiro, Rio de Janeiro 20021-130, Brazil');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Bogotá', 'Bogotá', 'Colombia', '110111', 'Bogotá, Bogotá 110111, Colombia');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Santiago', 'Santiago', 'Chile', '8320000', 'Santiago, Santiago 8320000, Chile');
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Lima', 'Lima', 'Peru', '15001', 'Lima, Lima 15001, Peru');

-- Remote
INSERT INTO locations (city, state, country, zip_code, display_name) VALUES ('Remote', '', 'Remote', '', 'Remote'); 