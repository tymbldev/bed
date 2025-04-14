-- SQL script to populate the companies table with major tech companies for interview preparation
-- These INSERT statements will add companies with their basic information

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE companies;
ALTER TABLE companies AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Insert technology companies
INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Google', 'A multinational technology company that specializes in Internet-related services and products.', 'https://www.google.com', 'https://logo.clearbit.com/google.com'),
('Amazon', 'An American multinational technology company focusing on e-commerce, cloud computing, digital streaming, and artificial intelligence.', 'https://www.amazon.com', 'https://logo.clearbit.com/amazon.com'),
('Microsoft', 'An American multinational technology corporation that produces computer software, consumer electronics, personal computers, and related services.', 'https://www.microsoft.com', 'https://logo.clearbit.com/microsoft.com'),
('Apple', 'An American multinational technology company that specializes in consumer electronics, software and online services.', 'https://www.apple.com', 'https://logo.clearbit.com/apple.com'),
('Facebook', 'An American social media and technology company', 'https://www.facebook.com', 'https://logo.clearbit.com/facebook.com'),
('Netflix', 'An American subscription streaming service and production company.', 'https://www.netflix.com', 'https://logo.clearbit.com/netflix.com'),
('Twitter', 'An American microblogging and social networking service.', 'https://www.twitter.com', 'https://logo.clearbit.com/twitter.com'),
('LinkedIn', 'A business and employment-focused social media platform.', 'https://www.linkedin.com', 'https://logo.clearbit.com/linkedin.com'),
('Salesforce', 'An American cloud-based software company that provides customer relationship management service.', 'https://www.salesforce.com', 'https://logo.clearbit.com/salesforce.com'),
('Oracle', 'An American multinational computer technology corporation.', 'https://www.oracle.com', 'https://logo.clearbit.com/oracle.com'),
('IBM', 'An American multinational technology company', 'https://www.ibm.com', 'https://logo.clearbit.com/ibm.com'),
('Adobe', 'An American multinational computer software company.', 'https://www.adobe.com', 'https://logo.clearbit.com/adobe.com');


INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Tata Consultancy Services', 'A leading global IT services, consulting, and business solutions organization.', 'https://www.tcs.com', 'https://logo.clearbit.com/tcs.com'),
('Infosys', 'A global leader in technology services and consulting.', 'https://www.infosys.com', 'https://logo.clearbit.com/infosys.com'),
('HCLTech', 'A global technology company helping enterprises reimagine their businesses.', 'https://www.hcltech.com', 'https://logo.clearbit.com/hcltech.com'),
('Wipro', 'A global information technology, consulting and business process services company.', 'https://www.wipro.com', 'https://logo.clearbit.com/wipro.com'),
('Tech Mahindra', 'A leading provider of digital transformation, consulting and business reengineering services.', 'https://www.techmahindra.com', 'https://logo.clearbit.com/techmahindra.com'),
('LTIMindtree', 'A global technology consulting and digital solutions company formed by the merger of L&T Infotech and Mindtree.', 'https://www.ltimindtree.com', 'https://logo.clearbit.com/ltimindtree.com'),
('Persistent Systems', 'A global company delivering digital engineering and enterprise modernization.', 'https://www.persistent.com', 'https://logo.clearbit.com/persistent.com'),
('Oracle Financial Services Software', 'A subsidiary of Oracle Corporation providing IT solutions to the financial services industry.', 'https://www.oracle.com/industries/financial-services/', 'https://logo.clearbit.com/oracle.com'),
('Coforge', 'A global digital services and solutions provider.', 'https://www.coforge.com', 'https://logo.clearbit.com/coforge.com'),
('Mphasis', 'An IT services company providing cloud and cognitive services.', 'https://www.mphasis.com', 'https://logo.clearbit.com/mphasis.com'),
('Birlasoft', 'An enterprise digital and IT services company part of the CK Birla Group.', 'https://www.birlasoft.com', 'https://logo.clearbit.com/birlasoft.com'),
('Hexaware Technologies', 'A global IT, BPO and consulting company.', 'https://hexaware.com', 'https://logo.clearbit.com/hexaware.com'),
('Zensar Technologies', 'A digital solutions and technology services company.', 'https://www.zensar.com', 'https://logo.clearbit.com/zensar.com'),
('Sonata Software', 'A global IT services company delivering innovative solutions.', 'https://www.sonata-software.com', 'https://logo.clearbit.com/sonata-software.com'),
('Cyient', 'Provides engineering, manufacturing, data analytics, and networks & operations solutions.', 'https://www.cyient.com', 'https://logo.clearbit.com/cyient.com'),
('L&T Technology Services', 'A global leader in Engineering and R&D services.', 'https://www.ltts.com', 'https://logo.clearbit.com/ltts.com'),
('Tata Elxsi', 'A design and technology services company for product engineering and solutions.', 'https://www.tataelxsi.com', 'https://logo.clearbit.com/tataelxsi.com'),
('NIIT Technologies', 'Now Coforge, an IT solutions provider focused on digital services.', 'https://www.coforge.com', 'https://logo.clearbit.com/coforge.com'),
('Mindtree', 'A global technology consulting and services company.', 'https://www.mindtree.com', 'https://logo.clearbit.com/mindtree.com'),
('KPIT Technologies', 'A global technology company specializing in IT consulting and product engineering.', 'https://www.kpit.com', 'https://logo.clearbit.com/kpit.com');


INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Swiggy', 'India\'s largest online food ordering and delivery platform.', 'https://www.swiggy.com', 'https://logo.clearbit.com/swiggy.com'),
('Zomato', 'A restaurant aggregator and food delivery company.', 'https://www.zomato.com', 'https://logo.clearbit.com/zomato.com'),
('Ola', 'A ride-sharing company offering transportation services.', 'https://www.olacabs.com', 'https://logo.clearbit.com/olacabs.com'),
('Paytm', 'A digital payment and financial services company.', 'https://www.paytm.com', 'https://logo.clearbit.com/paytm.com'),
('Byju\'s', 'An educational technology and online tutoring firm.', 'https://www.byjus.com', 'https://logo.clearbit.com/byjus.com'),
('Unacademy', 'An online learning platform for competitive exams.', 'https://www.unacademy.com', 'https://logo.clearbit.com/unacademy.com'),
('Razorpay', 'A payment solutions company for businesses.', 'https://www.razorpay.com', 'https://logo.clearbit.com/razorpay.com'),
('Cred', 'A platform that rewards users for paying credit card bills.', 'https://www.cred.club', 'https://logo.clearbit.com/cred.club'),
('Meesho', 'A social commerce platform enabling small businesses.', 'https://www.meesho.com', 'https://logo.clearbit.com/meesho.com'),
('PharmEasy', 'An online pharmacy and medical platform.', 'https://www.pharmeasy.in', 'https://logo.clearbit.com/pharmeasy.in'),
('Delhivery', 'A logistics and supply chain services company.', 'https://www.delhivery.com', 'https://logo.clearbit.com/delhivery.com'),
('Urban Company', 'A platform for home services and repairs.', 'https://www.urbancompany.com', 'https://logo.clearbit.com/urbancompany.com'),
('Groww', 'An investment platform for mutual funds and stocks.', 'https://www.groww.in', 'https://logo.clearbit.com/groww.in'),
('Nykaa', 'An e-commerce platform for beauty and wellness products.', 'https://www.nykaa.com', 'https://logo.clearbit.com/nykaa.com'),
('Udaan', 'A B2B e-commerce platform for small and medium businesses.', 'https://www.udaan.com', 'https://logo.clearbit.com/udaan.com'),
('Dream11', 'A fantasy sports platform for cricket and other sports.', 'https://www.dream11.com', 'https://logo.clearbit.com/dream11.com'),
('Licious', 'An online meat and seafood ordering platform.', 'https://www.licious.in', 'https://logo.clearbit.com/licious.in'),
('BigBasket', 'An online grocery delivery service.', 'https://www.bigbasket.com', 'https://logo.clearbit.com/bigbasket.com'),
('PolicyBazaar', 'An insurance aggregator and financial technology company.', 'https://www.policybazaar.com', 'https://logo.clearbit.com/policybazaar.com'),
('ShareChat', 'A social networking service supporting Indian languages.', 'https://www.sharechat.com', 'https://logo.clearbit.com/sharechat.com');

INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Zepto', 'A quick commerce company delivering groceries and essentials across 10 cities in India.', 'https://www.zeptonow.com', 'https://logo.clearbit.com/zeptonow.com'),
('Fi', 'A financial app offering savings accounts, mutual fund investments, and personal loans.', 'https://fi.money', 'https://logo.clearbit.com/fi.money'),
('Sprinto', 'Automates information security compliances and privacy laws for SaaS companies.', 'https://sprinto.com', 'https://logo.clearbit.com/sprinto.com'),
('Supersourcing', 'A B2B AI-enabled platform for hiring remote engineers and providing pre-vetted developers.', 'https://supersourcing.com', 'https://logo.clearbit.com/supersourcing.com'),
('GrowthSchool', 'Partners with instructors to create cohort-based courses on personal and professional growth topics.', 'https://growthschool.io', 'https://logo.clearbit.com/growthschool.io'),
('Jar', 'An automated gold savings app that allows users to save spare change and invest in digital gold.', 'https://www.myjar.app', 'https://logo.clearbit.com/myjar.app'),
('Shyft', 'A wellness and life management platform offering health, yoga, nutrition, and dermatology programs.', 'https://www.betheshyft.com', 'https://logo.clearbit.com/betheshyft.com'),
('Teachnook', 'An e-learning platform offering curated programs to help students upskill in various topics.', 'https://teachnook.com', 'https://logo.clearbit.com/teachnook.com'),
('StockGro', 'A platform aiming to educate young investors about trading and investment.', 'https://stockgro.club', 'https://logo.clearbit.com/stockgro.club'),
('BharatPe', 'A fintech startup helping small and medium-sized businesses through its digital payment platform.', 'https://bharatpe.com', 'https://logo.clearbit.com/bharatpe.com'),
('Innoviti Solutions', 'India\'s leading payment solutions company in the fintech sector.', 'https://www.innoviti.com', 'https://logo.clearbit.com/innoviti.com'),
('Rupeek', 'Provides online gold loans at your doorstep.', 'https://www.rupeek.com', 'https://logo.clearbit.com/rupeek.com'),
('Cuemath', 'An educational platform focused on making kids great at math.', 'https://www.cuemath.com', 'https://logo.clearbit.com/cuemath.com'),
('Flipkart', 'An Indian e-commerce company offering a wide range of products.', 'https://www.flipkart.com', 'https://logo.clearbit.com/flipkart.com'),
('Boat', 'A lifestyle brand dealing in fashionable consumer electronics.', 'https://www.boat-lifestyle.com', 'https://logo.clearbit.com/boat-lifestyle.com'),
('Skyroot Aerospace', 'An Indian aerospace manufacturer and commercial launch service provider.', 'https://www.skyroot.in', 'https://logo.clearbit.com/skyroot.in'),
('InMobi', 'A global provider of cloud-based intelligent mobile platforms for enterprise marketers.', 'https://www.inmobi.com', 'https://logo.clearbit.com/inmobi.com'),
('Practo', 'A health tech company that connects patients with healthcare providers.', 'https://www.practo.com', 'https://logo.clearbit.com/practo.com'),
('PhonePe', 'A digital payments and financial services company.', 'https://www.phonepe.com', 'https://logo.clearbit.com/phonepe.com'),
('Apna', 'A professional networking platform for blue and grey-collar workers.', 'https://apna.co', 'https://logo.clearbit.com/apna.co'),
('CARS24', 'An online platform for buying and selling used cars.', 'https://www.cars24.com', 'https://logo.clearbit.com/cars24.com'),
('Skillmatics', 'Creates innovative educational products and games for children.', 'https://www.skillmaticsworld.com', 'https://logo.clearbit.com/skillmaticsworld.com'),
('Rapido', 'A bike taxi platform offering transportation services.', 'https://www.rapido.bike', 'https://logo.clearbit.com/rapido.bike'),
('KreditBee', 'Provides instant personal loans to young professionals.', 'https://www.kreditbee.in', 'https://logo.clearbit.com/kreditbee.in'),
('MobiKwik', 'A digital wallet and online payment system.', 'https://www.mobikwik.com', 'https://logo.clearbit.com/mobikwik.com'),
('Xpressbees', 'A logistics company providing express delivery services.', 'https://www.xpressbees.com', 'https://logo.clearbit.com/xpressbees.com'),
('Dunzo', 'An on-demand delivery service for groceries and essentials.', 'https://www.dunzo.com', 'https://logo.clearbit.com/dunzo.com');


INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('HDFC Bank', 'One of India\'s leading private sector banks offering a wide range of financial products and services.', 'https://www.hdfcbank.com', 'https://logo.clearbit.com/hdfcbank.com'),
('ICICI Bank', 'A major private sector bank in India providing banking and financial services.', 'https://www.icicibank.com', 'https://logo.clearbit.com/icicibank.com'),
('State Bank of India', 'India\'s largest public sector bank offering comprehensive banking services.', 'https://www.sbi.co.in', 'https://logo.clearbit.com/sbi.co.in'),
('Axis Bank', 'A prominent private sector bank in India offering a range of financial services.', 'https://www.axisbank.com', 'https://logo.clearbit.com/axisbank.com'),
('Kotak Mahindra Bank', 'A leading private sector bank in India providing banking and financial solutions.', 'https://www.kotak.com', 'https://logo.clearbit.com/kotak.com'),
('Bajaj Finance', 'A non-banking financial company offering consumer finance, SME finance, and commercial lending.', 'https://www.bajajfinserv.in', 'https://logo.clearbit.com/bajajfinserv.in'),
('Aditya Birla Capital', 'A financial services company offering a range of solutions including insurance and asset management.', 'https://www.adityabirlacapital.com', 'https://logo.clearbit.com/adityabirlacapital.com'),
('L&T Finance Holdings', 'A financial services company providing infrastructure finance, rural finance, and housing finance.', 'https://www.ltfs.com', 'https://logo.clearbit.com/ltfs.com'),
('Mahindra Finance', 'A non-banking financial company focusing on rural and semi-urban markets.', 'https://www.mahindrafinance.com', 'https://logo.clearbit.com/mahindrafinance.com'),
('Muthoot Finance', 'India\'s largest gold financing company in terms of loan portfolio.', 'https://www.muthootfinance.com', 'https://logo.clearbit.com/muthootfinance.com'),
('Manappuram Finance', 'A non-banking financial company offering gold loans and other financial services.', 'https://www.manappuram.com', 'https://logo.clearbit.com/manappuram.com'),
('Shriram Finance', 'A leading NBFC providing commercial vehicle finance, retail finance, and enterprise finance.', 'https://www.shriramfinance.in', 'https://logo.clearbit.com/shriramfinance.in'),
('Indiabulls Housing Finance', 'A housing finance company offering home loans and loan against property.', 'https://www.indiabullshomeloans.com', 'https://logo.clearbit.com/indiabullshomeloans.com'),
('PNB Housing Finance', 'A subsidiary of Punjab National Bank offering housing finance solutions.', 'https://www.pnbhousing.com', 'https://logo.clearbit.com/pnbhousing.com'),
('LIC Housing Finance', 'A subsidiary of Life Insurance Corporation of India providing housing finance.', 'https://www.lichousing.com', 'https://logo.clearbit.com/lichousing.com'),
('Can Fin Homes', 'A housing finance company promoted by Canara Bank.', 'https://www.canfinhomes.com', 'https://logo.clearbit.com/canfinhomes.com'),
('Sundaram Finance', 'A non-banking financial company offering vehicle finance and other financial services.', 'https://www.sundaramfinance.in', 'https://logo.clearbit.com/sundaramfinance.in'),
('Cholamandalam Investment and Finance Company', 'A financial services company offering vehicle finance, home loans, and SME loans.', 'https://www.cholamandalam.com', 'https://logo.clearbit.com/cholamandalam.com'),
('IDFC FIRST Bank', 'A private sector bank offering a range of financial products and services.', 'https://www.idfcfirstbank.com', 'https://logo.clearbit.com/idfcfirstbank.com'),
('Yes Bank', 'A private sector bank in India providing banking and financial services.', 'https://www.yesbank.in', 'https://logo.clearbit.com/yesbank.in');

INSERT IGNORE INTO companies (name, description, website, logo_url) VALUES
('Hindustan Unilever Limited', 'India\'s largest FMCG company offering a wide range of products in personal care, home care, and food & beverages.', 'https://www.hul.co.in', 'https://logo.clearbit.com/hul.co.in'),
('ITC Limited', 'A diversified conglomerate with significant presence in FMCG, hotels, paperboards, and agribusiness.', 'https://www.itcportal.com', 'https://logo.clearbit.com/itcportal.com'),
('Nestlé India', 'A leading nutrition, health, and wellness company with popular brands like Maggi, Nescafé, and KitKat.', 'https://www.nestle.in', 'https://logo.clearbit.com/nestle.in'),
('Britannia Industries', 'A major food company known for its biscuits, dairy products, and bakery items.', 'https://www.britannia.co.in', 'https://logo.clearbit.com/britannia.co.in'),
('Dabur India', 'A leading Ayurvedic and natural health care company with products in health supplements, personal care, and food.', 'https://www.dabur.com', 'https://logo.clearbit.com/dabur.com'),
('Marico Limited', 'A consumer goods company specializing in beauty and wellness products, including Parachute and Saffola.', 'https://www.marico.com', 'https://logo.clearbit.com/marico.com'),
('Godrej Consumer Products', 'A major player in the FMCG sector with products in personal care, hair care, and home care.', 'https://www.godrejcp.com', 'https://logo.clearbit.com/godrejcp.com'),
('Colgate-Palmolive India', 'A leading oral care company known for its toothpaste, toothbrushes, and personal care products.', 'https://www.colgatepalmolive.co.in', 'https://logo.clearbit.com/colgatepalmolive.co.in'),
('Emami Limited', 'A diversified FMCG company with brands in personal care, health care, and beauty products.', 'https://www.emamiltd.in', 'https://logo.clearbit.com/emamiltd.in'),
('Patanjali Ayurved', 'An Indian consumer goods company offering Ayurvedic products in personal care, food, and health care.', 'https://www.patanjaliayurved.net', 'https://logo.clearbit.com/patanjaliayurved.net'),
('Amul (GCMMF)', 'India\'s largest dairy cooperative known for its milk, butter, cheese, and other dairy products.', 'https://www.amul.com', 'https://logo.clearbit.com/amul.com'),
('Parle Products', 'A leading food company famous for its biscuits, confectionery, and snacks.', 'https://www.parleproducts.com', 'https://logo.clearbit.com/parleproducts.com'),
('Reckitt Benckiser India', 'A global consumer health and hygiene company with brands like Dettol, Lysol, and Harpic.', 'https://www.rb.com/in', 'https://logo.clearbit.com/rb.com'),
('Procter & Gamble India', 'A multinational consumer goods corporation with products in personal health, hygiene, and home care.', 'https://www.pgcareers.com/india', 'https://logo.clearbit.com/pgcareers.com'),
('PepsiCo India', 'A global food and beverage leader with brands like Lay\'s, Kurkure, and Tropicana.', 'https://www.pepsicoindia.co.in', 'https://logo.clearbit.com/pepsicoindia.co.in'),
('Coca-Cola India', 'A leading beverage company offering a range of soft drinks, juices, and bottled water.', 'https://www.coca-colaindia.com', 'https://logo.clearbit.com/coca-colaindia.com'),
('Tata Consumer Products', 'A consumer products company with a strong presence in beverages and food products.', 'https://www.tataconsumer.com', 'https://logo.clearbit.com/tataconsumer.com'),
('Adani Wilmar', 'A leading food company offering edible oils, rice, and other staple foods under the Fortune brand.', 'https://www.adaniwilmar.com', 'https://logo.clearbit.com/adaniwilmar.com'),
('Haldiram\'s', 'A major Indian sweets and snacks manufacturer with a wide range of traditional products.', 'https://www.haldirams.com', 'https://logo.clearbit.com/haldirams.com'),
('Cavinkare', 'An Indian conglomerate with interests in personal care, dairy, and food products.', 'https://www.cavinkare.com', 'https://logo.clearbit.com/cavinkare.com');
