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

-- Additional Major Indian Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Surat', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Vadodara', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rajkot', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhavnagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jamnagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gandhinagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Anand', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bharuch', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Valsad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mehsana', (SELECT id FROM countries WHERE code = 'IN'));

-- Maharashtra Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nashik', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Aurangabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Solapur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kolhapur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Amravati', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nanded', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sangli', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jalgaon', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Akola', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Latur', (SELECT id FROM countries WHERE code = 'IN'));

-- Tamil Nadu Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Salem', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tiruchirappalli', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Madurai', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Vellore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Erode', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tiruppur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dindigul', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Thoothukkudi', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tirunelveli', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kanchipuram', (SELECT id FROM countries WHERE code = 'IN'));

-- Karnataka Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mangalore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hubli', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Belgaum', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gulbarga', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Davanagere', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bellary', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bijapur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Shimoga', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tumkur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Raichur', (SELECT id FROM countries WHERE code = 'IN'));

-- Telangana Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Warangal', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Karimnagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nizamabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Khammam', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Adilabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nalgonda', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Siddipet', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Suryapet', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jagtial', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Mancherial', (SELECT id FROM countries WHERE code = 'IN'));

-- Uttar Pradesh Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Varanasi', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Agra', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Prayagraj', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bareilly', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gorakhpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Aligarh', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Moradabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Saharanpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jhansi', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ghaziabad', (SELECT id FROM countries WHERE code = 'IN'));

-- Madhya Pradesh Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jabalpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gwalior', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ujjain', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dewas', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Satna', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ratlam', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rewa', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Murwara', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Singrauli', (SELECT id FROM countries WHERE code = 'IN'));

-- Rajasthan Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jodhpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kota', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bikaner', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ajmer', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Udaipur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhilwara', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Alwar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sri Ganganagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sikar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Pali', (SELECT id FROM countries WHERE code = 'IN'));

-- Bihar Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gaya', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhagalpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Muzaffarpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Purnia', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Darbhanga', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bihar Sharif', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Arrah', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Begusarai', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Katihar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Munger', (SELECT id FROM countries WHERE code = 'IN'));

-- West Bengal Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Howrah', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Durgapur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Asansol', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Siliguri', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kharagpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bardhaman', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Malda', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Baharampur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Habra', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Krishnanagar', (SELECT id FROM countries WHERE code = 'IN'));

-- Kerala Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kozhikode', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Thrissur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kollam', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Alappuzha', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Palakkad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kottayam', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kannur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Pathanamthitta', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Idukki', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Wayanad', (SELECT id FROM countries WHERE code = 'IN'));

-- Punjab Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ludhiana', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Amritsar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jalandhar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Patiala', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bathinda', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Pathankot', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hoshiarpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Moga', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Firozpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sangrur', (SELECT id FROM countries WHERE code = 'IN'));

-- Haryana Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Faridabad', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gurugram', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Panipat', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Ambala', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Yamunanagar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rohtak', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hisar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Karnal', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sonipat', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Panchkula', (SELECT id FROM countries WHERE code = 'IN'));

-- Odisha Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cuttack', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Rourkela', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Berhampur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sambalpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Puri', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Balasore', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bhadrak', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Baripada', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jharsuguda', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bargarh', (SELECT id FROM countries WHERE code = 'IN'));

-- Assam Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Silchar', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dibrugarh', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Jorhat', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tinsukia', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tezpur', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nagaon', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bongaigaon', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Goalpara', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Barpeta', (SELECT id FROM countries WHERE code = 'IN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sivasagar', (SELECT id FROM countries WHERE code = 'IN'));

-- Additional Major International Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Frankfurt', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hamburg', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Stuttgart', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Düsseldorf', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Leipzig', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dortmund', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Essen', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bremen', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Dresden', (SELECT id FROM countries WHERE code = 'DE'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hannover', (SELECT id FROM countries WHERE code = 'DE'));

-- Additional UK Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Manchester', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Birmingham', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Leeds', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Liverpool', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sheffield', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Edinburgh', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Bristol', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Glasgow', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cardiff', (SELECT id FROM countries WHERE code = 'GB'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Belfast', (SELECT id FROM countries WHERE code = 'GB'));

-- Additional US Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nashville', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Charlotte', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Orlando', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Las Vegas', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('New Orleans', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kansas City', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cleveland', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Cincinnati', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Pittsburgh', (SELECT id FROM countries WHERE code = 'US'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Indianapolis', (SELECT id FROM countries WHERE code = 'US'));

-- Additional Canadian Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Edmonton', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Winnipeg', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Quebec City', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hamilton', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Kitchener', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('London', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Victoria', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Halifax', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Saskatoon', (SELECT id FROM countries WHERE code = 'CA'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Regina', (SELECT id FROM countries WHERE code = 'CA'));

-- Additional Australian Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Brisbane', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Perth', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Adelaide', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gold Coast', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Newcastle', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Canberra', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sunshine Coast', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Wollongong', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hobart', (SELECT id FROM countries WHERE code = 'AU'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Geelong', (SELECT id FROM countries WHERE code = 'AU'));

-- Additional Asian Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Osaka', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Yokohama', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nagoya', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Sapporo', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Fukuoka', (SELECT id FROM countries WHERE code = 'JP'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Busan', (SELECT id FROM countries WHERE code = 'KR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Incheon', (SELECT id FROM countries WHERE code = 'KR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Daegu', (SELECT id FROM countries WHERE code = 'KR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Daejeon', (SELECT id FROM countries WHERE code = 'KR'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Gwangju', (SELECT id FROM countries WHERE code = 'KR'));

-- Additional Chinese Cities
INSERT IGNORE INTO cities (name, country_id) VALUES ('Guangzhou', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Shenzhen', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Tianjin', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Chongqing', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Chengdu', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Hangzhou', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Wuhan', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Xi\'an', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Nanjing', (SELECT id FROM countries WHERE code = 'CN'));
INSERT IGNORE INTO cities (name, country_id) VALUES ('Qingdao', (SELECT id FROM countries WHERE code = 'CN')); 