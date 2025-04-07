-- SQL script to populate the skills table with a comprehensive list of technical and soft skills

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE skills;
ALTER TABLE skills AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert technical skills: Programming Languages
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Java', true, 500),
('Python', true, 450),
('JavaScript', true, 400),
('TypeScript', true, 350),
('C#', true, 300),
('C++', true, 250),
('PHP', true, 200),
('Go', true, 180),
('Ruby', true, 170),
('Swift', true, 160),
('Kotlin', true, 150),
('Rust', true, 140),
('Scala', true, 130),
('Perl', true, 120),
('Shell Scripting', true, 110),
('R', true, 100),
('Dart', true, 90),
('MATLAB', true, 80),
('Groovy', true, 70),
('Objective-C', true, 60),
('Assembly', true, 50),
('Haskell', true, 40),
('Elixir', true, 30),
('Clojure', true, 20),
('F#', true, 10);

-- Insert technical skills: Frontend Technologies
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('React', true, 380),
('Angular', true, 350),
('Vue.js', true, 320),
('Next.js', true, 300),
('HTML5', true, 290),
('CSS3', true, 280),
('SASS/SCSS', true, 260),
('Less', true, 240),
('Bootstrap', true, 230),
('Tailwind CSS', true, 220),
('Material UI', true, 210),
('jQuery', true, 200),
('Redux', true, 190),
('MobX', true, 180),
('Gatsby', true, 170),
('Svelte', true, 160),
('Ember.js', true, 150),
('Backbone.js', true, 140),
('Webpack', true, 130),
('Rollup', true, 120),
('Parcel', true, 110),
('Web Components', true, 100),
('PWA', true, 90),
('WebGL', true, 80),
('D3.js', true, 70);

-- Insert technical skills: Backend Technologies
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Node.js', true, 370),
('Express.js', true, 350),
('Spring Framework', true, 330),
('Spring Boot', true, 310),
('.NET Core', true, 290),
('Django', true, 270),
('Flask', true, 250),
('Laravel', true, 230),
('Ruby on Rails', true, 210),
('ASP.NET', true, 190),
('Hibernate', true, 170),
('JPA', true, 150),
('NestJS', true, 130),
('FastAPI', true, 110),
('Phoenix', true, 90),
('Play Framework', true, 70),
('Ktor', true, 50),
('Micronaut', true, 30),
('Quarkus', true, 20),
('Spring WebFlux', true, 10);

-- Insert technical skills: Databases
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('MySQL', true, 350),
('PostgreSQL', true, 330),
('MongoDB', true, 310),
('SQLite', true, 290),
('SQL Server', true, 270),
('Oracle', true, 250),
('Redis', true, 230),
('Elasticsearch', true, 210),
('DynamoDB', true, 190),
('Cassandra', true, 170),
('Neo4j', true, 150),
('Firestore', true, 130),
('CouchDB', true, 110),
('MariaDB', true, 90),
('Couchbase', true, 70),
('InfluxDB', true, 50),
('HBase', true, 30),
('ArangoDB', true, 20),
('RavenDB', true, 10);

-- Insert technical skills: DevOps & Cloud
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('AWS', true, 400),
('Docker', true, 380),
('Kubernetes', true, 360),
('Azure', true, 340),
('Google Cloud Platform', true, 320),
('Jenkins', true, 300),
('GitLab CI/CD', true, 280),
('GitHub Actions', true, 260),
('Terraform', true, 240),
('Ansible', true, 220),
('Puppet', true, 200),
('Chef', true, 180),
('Prometheus', true, 160),
('Grafana', true, 140),
('ELK Stack', true, 120),
('Helm', true, 100),
('Istio', true, 80),
('Vault', true, 60),
('CircleCI', true, 40),
('Travis CI', true, 20);

-- Insert technical skills: Mobile Development
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Android Development', true, 300),
('iOS Development', true, 280),
('React Native', true, 260),
('Flutter', true, 240),
('Xamarin', true, 220),
('Ionic', true, 200),
('SwiftUI', true, 180),
('Jetpack Compose', true, 160),
('Cordova', true, 140),
('NativeScript', true, 120);

-- Insert technical skills: Data Science & Machine Learning
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Machine Learning', true, 280),
('Deep Learning', true, 260),
('TensorFlow', true, 240),
('PyTorch', true, 220),
('Scikit-learn', true, 200),
('Natural Language Processing', true, 180),
('Computer Vision', true, 160),
('Data Analytics', true, 140),
('Big Data', true, 120),
('Apache Spark', true, 100),
('Hadoop', true, 80),
('Neural Networks', true, 60),
('Reinforcement Learning', true, 40),
('Data Visualization', true, 20);

-- Insert technical skills: Version Control & Tools
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Git', true, 500),
('GitHub', true, 480),
('GitLab', true, 460),
('Bitbucket', true, 440),
('JIRA', true, 420),
('Confluence', true, 400),
('Trello', true, 380),
('Slack', true, 360),
('Microsoft Teams', true, 340),
('VS Code', true, 320),
('IntelliJ IDEA', true, 300),
('Eclipse', true, 280),
('Postman', true, 260),
('Swagger', true, 240),
('Figma', true, 220);

-- Insert technical skills: Security
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Cybersecurity', true, 200),
('Penetration Testing', true, 180),
('Network Security', true, 160),
('OAuth 2.0', true, 140),
('OpenID Connect', true, 120),
('JWT', true, 100),
('HTTPS/TLS', true, 80),
('Application Security', true, 60),
('OWASP', true, 40),
('Vulnerability Assessment', true, 20);

-- Insert technical skills: Testing
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Unit Testing', true, 250),
('Integration Testing', true, 230),
('End-to-End Testing', true, 210),
('Test Automation', true, 190),
('Selenium', true, 170),
('Jest', true, 150),
('Mocha', true, 130),
('Jasmine', true, 110),
('TestNG', true, 90),
('JUnit', true, 70),
('NUnit', true, 50),
('Cypress', true, 30),
('Playwright', true, 20),
('Appium', true, 10);

-- Insert soft skills
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Communication', true, 500),
('Team Collaboration', true, 480),
('Problem Solving', true, 460),
('Leadership', true, 440),
('Time Management', true, 420),
('Critical Thinking', true, 400),
('Adaptability', true, 380),
('Conflict Resolution', true, 360),
('Creativity', true, 340),
('Emotional Intelligence', true, 320),
('Presentation Skills', true, 300),
('Negotiation', true, 280),
('Decision Making', true, 260),
('Project Management', true, 240),
('Mentoring', true, 220);

-- Insert architecture & methodology skills
INSERT IGNORE INTO skills (name, enabled, usage_count) VALUES
('Microservices', true, 200),
('Serverless', true, 190),
('Event-Driven Architecture', true, 180),
('Domain-Driven Design', true, 170),
('RESTful API Design', true, 160),
('GraphQL', true, 150),
('gRPC', true, 140),
('CQRS', true, 130),
('Service Mesh', true, 120),
('API Gateway', true, 110),
('Monolithic Architecture', true, 100),
('Hexagonal Architecture', true, 90),
('Clean Architecture', true, 80),
('SOLID Principles', true, 70),
('Design Patterns', true, 60),
('System Design', true, 50),
('Distributed Systems', true, 40),
('Scalability', true, 30),
('High Availability', true, 20),
('Performance Optimization', true, 10); 