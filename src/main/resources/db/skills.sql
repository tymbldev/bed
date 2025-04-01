-- Clear existing data
TRUNCATE TABLE skills RESTART IDENTITY CASCADE;

-- Insert initial skills
INSERT INTO skills (name, enabled, usage_count) VALUES
('Java', true, 100),
('Spring Boot', true, 95),
('Python', true, 90),
('JavaScript', true, 85),
('React', true, 80),
('Angular', true, 75),
('Node.js', true, 70),
('SQL', true, 65),
('AWS', true, 60),
('Docker', true, 55),
('Kubernetes', true, 50),
('Git', true, 45),
('REST APIs', true, 40),
('Microservices', true, 35),
('CI/CD', true, 30),
('Agile', true, 25),
('Machine Learning', true, 20),
('Data Science', true, 15),
('DevOps', true, 10),
('Cloud Computing', true, 5); 