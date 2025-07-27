package com.tymbl.jobs.service;

import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompanyCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyCleanupService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    /**
     * Process all unprocessed companies for cleanup
     */
    @Transactional
    public Map<String, Object> processAllCompanies() {
        logger.info("Starting company cleanup process for all unprocessed companies");
        
        List<Company> unprocessedCompanies = companyRepository.findByCleanupProcessedFalse();
        logger.info("Found {} unprocessed companies for cleanup", unprocessedCompanies.size());
        
        int processedCount = 0;
        int removedCount = 0;
        int junkMarkedCount = 0;
        int renamedCount = 0;
        int noActionCount = 0;
        int errorCount = 0;
        
        List<Map<String, Object>> companyResults = new ArrayList<>();
        
        for (Company company : unprocessedCompanies) {
            try {
                Map<String, Object> result = processSingleCompany(company);
                
                // Add company info to result
                result.put("companyId", company.getId());
                result.put("originalName", company.getName());
                companyResults.add(result);
                
                // Count based on action
                String action = (String) result.get("action");
                if ("removed".equals(action)) {
                    removedCount++;
                } else if ("junk_marked".equals(action)) {
                    junkMarkedCount++;
                } else if ("renamed".equals(action)) {
                    renamedCount++;
                } else if ("no_action".equals(action)) {
                    noActionCount++;
                }
                
                processedCount++;
                
                logger.info("Processed company: {} - Action: {}", company.getName(), action);
                
            } catch (Exception e) {
                errorCount++;
                logger.error("Error processing company {}: {}", company.getName(), e.getMessage());
                
                // Mark as processed to avoid infinite retries
                company.setCleanupProcessed(true);
                company.setCleanupProcessedAt(LocalDateTime.now());
                companyRepository.save(company);
                
                // Add error result
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("companyId", company.getId());
                errorResult.put("originalName", company.getName());
                errorResult.put("action", "error");
                errorResult.put("error", e.getMessage());
                companyResults.add(errorResult);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProcessed", processedCount);
        summary.put("removed", removedCount);
        summary.put("junkMarked", junkMarkedCount);
        summary.put("renamed", renamedCount);
        summary.put("noAction", noActionCount);
        summary.put("errors", errorCount);
        summary.put("companyResults", companyResults);
        summary.put("message", "Company cleanup process completed. " + 
                   removedCount + " product entries removed, " + 
                   junkMarkedCount + " marked as junk. Review junk-marked companies before deletion.");
        
        logger.info("Company cleanup completed - Processed: {}, Removed: {}, Junk Marked: {}, Renamed: {}, No Action: {}, Errors: {}", 
                   processedCount, removedCount, junkMarkedCount, renamedCount, noActionCount, errorCount);
        
        return summary;
    }

    /**
     * Process a single company for cleanup
     */
    @Transactional
    public Map<String, Object> processSingleCompany(Company company) {
        logger.info("Processing company for cleanup: {}", company.getName());
        
        String prompt = buildCleanupPrompt(company.getName());
        String aiResponse = callGeminiAPI(prompt);
        
        Map<String, Object> result = parseCleanupResponse(aiResponse, company.getName());
        
        if (result.containsKey("delete")) {
            String parentCompanyName = (String) result.get("parentCompany");
            String reason = (String) result.get("reason");
            Boolean parentCompanyExists = (Boolean) result.get("parentCompanyExists");
            Boolean shouldRemove = (Boolean) result.get("shouldRemove");
            
            if (parentCompanyName != null && Boolean.TRUE.equals(parentCompanyExists) && Boolean.TRUE.equals(shouldRemove)) {
                // Product/service entry and parent company exists - REMOVE the product entry
                logger.info("Removing product entry '{}' - parent company '{}' already exists in database", 
                           company.getName(), parentCompanyName);
                
                companyRepository.delete(company);
                
                result.put("removed", true);
                result.put("action", "removed");
                result.put("reason", "Product/service entry removed - parent company already exists: " + parentCompanyName);
                result.put("parentCompany", parentCompanyName);
                
            } else {
                // Mark as junk (parent company doesn't exist or it's a junk entry)
                logger.info("Marking company {} as junk - Parent: {}, Reason: {}", 
                           company.getName(), parentCompanyName, reason);
                
                company.setIsJunk(true);
                company.setJunkReason(reason);
                company.setParentCompanyName(parentCompanyName);
                company.setCleanupProcessed(true);
                company.setCleanupProcessedAt(LocalDateTime.now());
                companyRepository.save(company);
                
                result.put("junkMarked", true);
                result.put("action", "junk_marked");
                result.put("reason", "Marked as junk - " + reason);
            }
        } else if (result.containsKey("rename")) {
            String newName = (String) result.get("newName");
            logger.info("Renaming company {} to {}", company.getName(), newName);
            company.setName(newName);
            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);
            result.put("renamed", true);
            result.put("action", "renamed");
        } else {
            // No action needed, mark as processed
            logger.info("No action needed for company: {}", company.getName());
            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);
            result.put("noAction", true);
            result.put("action", "no_action");
        }
        
        return result;
    }

    /**
     * Process a specific company by name
     */
    @Transactional
    public Map<String, Object> processCompanyByName(String companyName) {
        logger.info("Processing specific company for cleanup: {}", companyName);
        
        Optional<Company> companyOpt = companyRepository.findByNameIgnoreCase(companyName);
        if (!companyOpt.isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Company not found: " + companyName);
            return error;
        }
        
        Company company = companyOpt.get();
        if (company.getCleanupProcessed()) {
            Map<String, Object> info = new HashMap<>();
            info.put("info", "Company already processed for cleanup");
            info.put("processedAt", company.getCleanupProcessedAt());
            return info;
        }
        
        return processSingleCompany(company);
    }

    /**
     * Reset cleanup processed flag for all companies
     */
    @Transactional
    public Map<String, Object> resetCleanupProcessedFlag() {
        logger.info("Resetting cleanup processed flag for all companies");
        
        List<Company> allCompanies = companyRepository.findAll();
        for (Company company : allCompanies) {
            company.setCleanupProcessed(false);
            company.setCleanupProcessedAt(null);
        }
        companyRepository.saveAll(allCompanies);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Cleanup processed flag reset for all companies");
        result.put("totalCompanies", allCompanies.size());
        
        logger.info("Reset cleanup processed flag for {} companies", allCompanies.size());
        
        return result;
    }

    /**
     * Get all junk-marked companies for manual review
     */
    public Map<String, Object> getJunkMarkedCompanies() {
        logger.info("Retrieving all junk-marked companies for review");
        
        List<Company> junkCompanies = companyRepository.findByIsJunkTrue();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalJunkCompanies", junkCompanies.size());
        result.put("junkCompanies", junkCompanies.stream().map(company -> {
            Map<String, Object> companyData = new HashMap<>();
            companyData.put("id", company.getId());
            companyData.put("name", company.getName());
            companyData.put("junkReason", company.getJunkReason());
            companyData.put("parentCompanyName", company.getParentCompanyName());
            companyData.put("cleanupProcessedAt", company.getCleanupProcessedAt());
            return companyData;
        }).collect(java.util.stream.Collectors.toList()));
        result.put("message", "Junk-marked companies retrieved for review");
        
        logger.info("Retrieved {} junk-marked companies for review", junkCompanies.size());
        
        return result;
    }

    /**
     * Clear junk flag for a specific company (undo junk marking)
     */
    @Transactional
    public Map<String, Object> clearJunkFlag(Long companyId) {
        logger.info("Clearing junk flag for company ID: {}", companyId);
        
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (!companyOpt.isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Company not found with ID: " + companyId);
            return error;
        }
        
        Company company = companyOpt.get();
        company.setIsJunk(false);
        company.setJunkReason(null);
        company.setParentCompanyName(null);
        companyRepository.save(company);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("companyId", companyId);
        result.put("companyName", company.getName());
        result.put("message", "Junk flag cleared for company: " + company.getName());
        
        logger.info("Junk flag cleared for company: {}", company.getName());
        
        return result;
    }

    /**
     * Build the prompt for company cleanup analysis
     */
    private String buildCleanupPrompt(String companyName) {
        return String.format(
            "Analyze the company name \"%s\" and determine if it's:\n" +
            "1. A PRODUCT/SERVICE name that should be mapped to its parent company\n" +
            "2. A junk/incorrect entry that should be deleted\n" +
            "3. A valid company name that should remain unchanged\n" +
            "\n" +
            "IMPORTANT: Focus on identifying products, services, platforms, or tools that belong to larger companies.\n" +
            "\n" +
            "Examples of PRODUCTS/SERVICES that should be mapped to parent companies:\n" +
            "- \"AWS\" → Parent: \"Amazon Web Services\" or \"Amazon\"\n" +
            "- \"Google Cloud\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Microsoft Azure\" → Parent: \"Microsoft\"\n" +
            "- \"GitHub\" → Parent: \"Microsoft\"\n" +
            "- \"LinkedIn\" → Parent: \"Microsoft\"\n" +
            "- \"Instagram\" → Parent: \"Meta\" or \"Facebook\"\n" +
            "- \"WhatsApp\" → Parent: \"Meta\" or \"Facebook\"\n" +
            "- \"YouTube\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Chrome\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Android\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"iOS\" → Parent: \"Apple\"\n" +
            "- \"Safari\" → Parent: \"Apple\"\n" +
            "- \"Xcode\" → Parent: \"Apple\"\n" +
            "- \"Visual Studio\" → Parent: \"Microsoft\"\n" +
            "- \"Office 365\" → Parent: \"Microsoft\"\n" +
            "- \"Teams\" → Parent: \"Microsoft\"\n" +
            "- \"Slack\" → Parent: \"Salesforce\"\n" +
            "- \"Tableau\" → Parent: \"Salesforce\"\n" +
            "- \"MongoDB Atlas\" → Parent: \"MongoDB\"\n" +
            "- \"React\" → Parent: \"Meta\" or \"Facebook\"\n" +
            "- \"Angular\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Vue.js\" → Parent: \"Evan You\" (individual creator)\n" +
            "- \"Docker\" → Parent: \"Docker Inc.\"\n" +
            "- \"Kubernetes\" → Parent: \"Cloud Native Computing Foundation\"\n" +
            "- \"Jenkins\" → Parent: \"Jenkins Project\"\n" +
            "- \"Jira\" → Parent: \"Atlassian\"\n" +
            "- \"Confluence\" → Parent: \"Atlassian\"\n" +
            "- \"Bitbucket\" → Parent: \"Atlassian\"\n" +
            "- \"Trello\" → Parent: \"Atlassian\"\n" +
            "- \"Figma\" → Parent: \"Adobe\" (acquired)\n" +
            "- \"Sketch\" → Parent: \"Bohemian Coding\"\n" +
            "- \"Notion\" → Parent: \"Notion Labs\"\n" +
            "- \"Zoom\" → Parent: \"Zoom Video Communications\"\n" +
            "- \"Discord\" → Parent: \"Discord Inc.\"\n" +
            "- \"Twitch\" → Parent: \"Amazon\"\n" +
            "- \"Spotify\" → Parent: \"Spotify Technology\"\n" +
            "- \"Uber Eats\" → Parent: \"Uber\"\n" +
            "- \"DoorDash\" → Parent: \"DoorDash Inc.\"\n" +
            "- \"Zomato\" → Parent: \"Zomato Limited\"\n" +
            "- \"Swiggy\" → Parent: \"Swiggy\"\n" +
            "- \"PayPal\" → Parent: \"PayPal Holdings\"\n" +
            "- \"Stripe\" → Parent: \"Stripe Inc.\"\n" +
            "- \"Shopify\" → Parent: \"Shopify Inc.\"\n" +
            "- \"WooCommerce\" → Parent: \"Automattic\"\n" +
            "- \"Magento\" → Parent: \"Adobe\"\n" +
            "- \"Salesforce CRM\" → Parent: \"Salesforce\"\n" +
            "- \"HubSpot\" → Parent: \"HubSpot Inc.\"\n" +
            "- \"Mailchimp\" → Parent: \"Intuit\"\n" +
            "- \"Canva\" → Parent: \"Canva Pty Ltd\"\n" +
            "- \"Adobe Photoshop\" → Parent: \"Adobe\"\n" +
            "- \"Adobe Illustrator\" → Parent: \"Adobe\"\n" +
            "- \"Adobe Premiere\" → Parent: \"Adobe\"\n" +
            "- \"Final Cut Pro\" → Parent: \"Apple\"\n" +
            "- \"Logic Pro\" → Parent: \"Apple\"\n" +
            "- \"Android Studio\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"VS Code\" → Parent: \"Microsoft\"\n" +
            "- \"IntelliJ IDEA\" → Parent: \"JetBrains\"\n" +
            "- \"PyCharm\" → Parent: \"JetBrains\"\n" +
            "- \"WebStorm\" → Parent: \"JetBrains\"\n" +
            "- \"Eclipse\" → Parent: \"Eclipse Foundation\"\n" +
            "- \"NetBeans\" → Parent: \"Apache Software Foundation\"\n" +
            "- \"Sublime Text\" → Parent: \"Sublime HQ\"\n" +
            "- \"Atom\" → Parent: \"GitHub\" (now Microsoft)\n" +
            "- \"Brackets\" → Parent: \"Adobe\"\n" +
            "- \"Postman\" → Parent: \"Postman Inc.\"\n" +
            "- \"Insomnia\" → Parent: \"Kong Inc.\"\n" +
            "- \"Swagger\" → Parent: \"SmartBear Software\"\n" +
            "- \"JUnit\" → Parent: \"JUnit Team\"\n" +
            "- \"TestNG\" → Parent: \"Cedric Beust\"\n" +
            "- \"Selenium\" → Parent: \"Selenium Project\"\n" +
            "- \"Cypress\" → Parent: \"Cypress.io\"\n" +
            "- \"Playwright\" → Parent: \"Microsoft\"\n" +
            "- \"Puppeteer\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Jest\" → Parent: \"Meta\" or \"Facebook\"\n" +
            "- \"Mocha\" → Parent: \"Mocha Team\"\n" +
            "- \"Chai\" → Parent: \"Chai Team\"\n" +
            "- \"Lodash\" → Parent: \"Lodash Team\"\n" +
            "- \"Moment.js\" → Parent: \"Moment.js Team\"\n" +
            "- \"Axios\" → Parent: \"Axios Team\"\n" +
            "- \"Express.js\" → Parent: \"Express.js Team\"\n" +
            "- \"Next.js\" → Parent: \"Vercel\"\n" +
            "- \"Nuxt.js\" → Parent: \"Nuxt.js Team\"\n" +
            "- \"Gatsby\" → Parent: \"Netlify\"\n" +
            "- \"Webpack\" → Parent: \"Webpack Team\"\n" +
            "- \"Babel\" → Parent: \"Babel Team\"\n" +
            "- \"ESLint\" → Parent: \"ESLint Team\"\n" +
            "- \"Prettier\" → Parent: \"Prettier Team\"\n" +
            "- \"TypeScript\" → Parent: \"Microsoft\"\n" +
            "- \"Node.js\" → Parent: \"Node.js Foundation\"\n" +
            "- \"Deno\" → Parent: \"Deno Team\"\n" +
            "- \"Bun\" → Parent: \"Bun Team\"\n" +
            "- \"Rust\" → Parent: \"Rust Team\"\n" +
            "- \"Go\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"Kotlin\" → Parent: \"JetBrains\"\n" +
            "- \"Swift\" → Parent: \"Apple\"\n" +
            "- \"Flutter\" → Parent: \"Google\" or \"Alphabet\"\n" +
            "- \"React Native\" → Parent: \"Meta\" or \"Facebook\"\n" +
            "- \"Xamarin\" → Parent: \"Microsoft\"\n" +
            "- \"Ionic\" → Parent: \"Ionic Team\"\n" +
            "- \"Cordova\" → Parent: \"Apache Software Foundation\"\n" +
            "- \"Electron\" → Parent: \"GitHub\" (now Microsoft)\n" +
            "- \"Tauri\" → Parent: \"Tauri Team\"\n" +
            "- \"Unity\" → Parent: \"Unity Technologies\"\n" +
            "- \"Unreal Engine\" → Parent: \"Epic Games\"\n" +
            "- \"Godot\" → Parent: \"Godot Team\"\n" +
            "- \"Blender\" → Parent: \"Blender Foundation\"\n" +
            "- \"Maya\" → Parent: \"Autodesk\"\n" +
            "- \"3ds Max\" → Parent: \"Autodesk\"\n" +
            "- \"AutoCAD\" → Parent: \"Autodesk\"\n" +
            "- \"Fusion 360\" → Parent: \"Autodesk\"\n" +
            "- \"SolidWorks\" → Parent: \"Dassault Systèmes\"\n" +
            "- \"CATIA\" → Parent: \"Dassault Systèmes\"\n" +
            "- \"Creo\" → Parent: \"PTC\"\n" +
            "- \"Inventor\" → Parent: \"Autodesk\"\n" +
            "- \"SketchUp\" → Parent: \"Trimble\"\n" +
            "- \"Revit\" → Parent: \"Autodesk\"\n" +
            "- \"ArchiCAD\" → Parent: \"Graphisoft\"\n" +
            "- \"Vectorworks\" → Parent: \"Vectorworks Inc.\"\n" +
            "- \"Cinema 4D\" → Parent: \"Maxon\"\n" +
            "- \"Houdini\" → Parent: \"SideFX\"\n" +
            "- \"Nuke\" → Parent: \"Foundry\"\n" +
            "- \"DaVinci Resolve\" → Parent: \"Blackmagic Design\"\n" +
            "- \"Premiere Pro\" → Parent: \"Adobe\"\n" +
            "- \"After Effects\" → Parent: \"Adobe\"\n" +
            "- \"InDesign\" → Parent: \"Adobe\"\n" +
            "- \"Lightroom\" → Parent: \"Adobe\"\n" +
            "- \"Bridge\" → Parent: \"Adobe\"\n" +
            "- \"Acrobat\" → Parent: \"Adobe\"\n" +
            "- \"Dreamweaver\" → Parent: \"Adobe\"\n" +
            "- \"Flash\" → Parent: \"Adobe\" (discontinued)\n" +
            "- \"Fireworks\" → Parent: \"Adobe\" (discontinued)\n" +
            "- \"FreeHand\" → Parent: \"Adobe\" (discontinued)\n" +
            "- \"Director\" → Parent: \"Adobe\" (discontinued)\n" +
            "- \"Authorware\" → Parent: \"Adobe\" (discontinued)\n" +
            "- \"ColdFusion\" → Parent: \"Adobe\"\n" +
            "- \"RoboHelp\" → Parent: \"Adobe\"\n" +
            "- \"FrameMaker\" → Parent: \"Adobe\"\n" +
            "- \"Captivate\" → Parent: \"Adobe\"\n" +
            "- \"Presenter\" → Parent: \"Adobe\"\n" +
            "- \"Connect\" → Parent: \"Adobe\"\n" +
            "- \"Audition\" → Parent: \"Adobe\"\n" +
            "- \"Media Encoder\" → Parent: \"Adobe\"\n" +
            "- \"Character Animator\" → Parent: \"Adobe\"\n" +
            "- \"Dimension\" → Parent: \"Adobe\"\n" +
            "- \"Substance\" → Parent: \"Adobe\"\n" +
            "- \"Aero\" → Parent: \"Adobe\"\n" +
            "- \"Rush\" → Parent: \"Adobe\"\n" +
            "- \"Spark\" → Parent: \"Adobe\"\n" +
            "- \"Behance\" → Parent: \"Adobe\"\n" +
            "- \"Creative Cloud\" → Parent: \"Adobe\"\n" +
            "- \"Document Cloud\" → Parent: \"Adobe\"\n" +
            "- \"Experience Cloud\" → Parent: \"Adobe\"\n" +
            "- \"Analytics\" → Parent: \"Adobe\"\n" +
            "- \"Target\" → Parent: \"Adobe\"\n" +
            "- \"Audience Manager\" → Parent: \"Adobe\"\n" +
            "- \"Campaign\" → Parent: \"Adobe\"\n" +
            "- \"Experience Manager\" → Parent: \"Adobe\"\n" +
            "- \"Commerce\" → Parent: \"Adobe\"\n" +
            "- \"Workfront\" → Parent: \"Adobe\"\n" +
            "- \"Marketo\" → Parent: \"Adobe\"\n" +
            "- \"Adobe XD\" → Parent: \"Adobe\"\n" +
            "- \"InVision\" → Parent: \"InVision\"\n" +
            "- \"Marvel\" → Parent: \"Marvel\"\n" +
            "- \"Framer\" → Parent: \"Framer\"\n" +
            "- \"Principle\" → Parent: \"Principle\"\n" +
            "- \"Protopie\" → Parent: \"Protopie\"\n" +
            "- \"Axure\" → Parent: \"Axure Software Solutions\"\n" +
            "- \"Balsamiq\" → Parent: \"Balsamiq Studios\"\n" +
            "- \"Lucidchart\" → Parent: \"Lucid Software\"\n" +
            "- \"Draw.io\" → Parent: \"JGraph Ltd\"\n" +
            "- \"Visio\" → Parent: \"Microsoft\"\n" +
            "- \"OmniGraffle\" → Parent: \"The Omni Group\"\n" +
            "\n" +
            "Examples of VALID COMPANIES (no action needed):\n" +
            "- \"Netflix\" → Valid company, keep as is\n" +
            "- \"Spotify Technology\" → Valid company, keep as is\n" +
            "- \"Uber\" → Valid company, keep as is\n" +
            "- \"Airbnb\" → Valid company, keep as is\n" +
            "- \"Stripe Inc.\" → Valid company, keep as is\n" +
            "- \"Shopify Inc.\" → Valid company, keep as is\n" +
            "- \"Salesforce\" → Valid company, keep as is\n" +
            "- \"HubSpot Inc.\" → Valid company, keep as is\n" +
            "- \"Intuit\" → Valid company, keep as is\n" +
            "- \"Canva Pty Ltd\" → Valid company, keep as is\n" +
            "- \"Adobe\" → Valid company, keep as is\n" +
            "- \"Microsoft\" → Valid company, keep as is\n" +
            "- \"Apple\" → Valid company, keep as is\n" +
            "- \"Google\" → Valid company, keep as is\n" +
            "- \"Alphabet\" → Valid company, keep as is\n" +
            "- \"Meta\" → Valid company, keep as is\n" +
            "- \"Facebook\" → Valid company, keep as is\n" +
            "- \"Amazon\" → Valid company, keep as is\n" +
            "- \"Amazon Web Services\" → Valid company, keep as is\n" +
            "- \"JetBrains\" → Valid company, keep as is\n" +
            "- \"Eclipse Foundation\" → Valid company, keep as is\n" +
            "- \"Apache Software Foundation\" → Valid company, keep as is\n" +
            "- \"Sublime HQ\" → Valid company, keep as is\n" +
            "- \"Postman Inc.\" → Valid company, keep as is\n" +
            "- \"Kong Inc.\" → Valid company, keep as is\n" +
            "- \"SmartBear Software\" → Valid company, keep as is\n" +
            "- \"JUnit Team\" → Valid company, keep as is\n" +
            "- \"Selenium Project\" → Valid company, keep as is\n" +
            "- \"Cypress.io\" → Valid company, keep as is\n" +
            "- \"Mocha Team\" → Valid company, keep as is\n" +
            "- \"Chai Team\" → Valid company, keep as is\n" +
            "- \"Lodash Team\" → Valid company, keep as is\n" +
            "- \"Moment.js Team\" → Valid company, keep as is\n" +
            "- \"Axios Team\" → Valid company, keep as is\n" +
            "- \"Express.js Team\" → Valid company, keep as is\n" +
            "- \"Vercel\" → Valid company, keep as is\n" +
            "- \"Nuxt.js Team\" → Valid company, keep as is\n" +
            "- \"Netlify\" → Valid company, keep as is\n" +
            "- \"Webpack Team\" → Valid company, keep as is\n" +
            "- \"Babel Team\" → Valid company, keep as is\n" +
            "- \"ESLint Team\" → Valid company, keep as is\n" +
            "- \"Prettier Team\" → Valid company, keep as is\n" +
            "- \"Node.js Foundation\" → Valid company, keep as is\n" +
            "- \"Deno Team\" → Valid company, keep as is\n" +
            "- \"Bun Team\" → Valid company, keep as is\n" +
            "- \"Rust Team\" → Valid company, keep as is\n" +
            "- \"Tauri Team\" → Valid company, keep as is\n" +
            "- \"Unity Technologies\" → Valid company, keep as is\n" +
            "- \"Epic Games\" → Valid company, keep as is\n" +
            "- \"Godot Team\" → Valid company, keep as is\n" +
            "- \"Blender Foundation\" → Valid company, keep as is\n" +
            "- \"Autodesk\" → Valid company, keep as is\n" +
            "- \"Dassault Systèmes\" → Valid company, keep as is\n" +
            "- \"PTC\" → Valid company, keep as is\n" +
            "- \"Trimble\" → Valid company, keep as is\n" +
            "- \"Graphisoft\" → Valid company, keep as is\n" +
            "- \"Vectorworks Inc.\" → Valid company, keep as is\n" +
            "- \"Maxon\" → Valid company, keep as is\n" +
            "- \"SideFX\" → Valid company, keep as is\n" +
            "- \"Foundry\" → Valid company, keep as is\n" +
            "- \"Blackmagic Design\" → Valid company, keep as is\n" +
            "- \"Bohemian Coding\" → Valid company, keep as is\n" +
            "- \"InVision\" → Valid company, keep as is\n" +
            "- \"Marvel\" → Valid company, keep as is\n" +
            "- \"Framer\" → Valid company, keep as is\n" +
            "- \"Principle\" → Valid company, keep as is\n" +
            "- \"Protopie\" → Valid company, keep as is\n" +
            "- \"Axure Software Solutions\" → Valid company, keep as is\n" +
            "- \"Balsamiq Studios\" → Valid company, keep as is\n" +
            "- \"Lucid Software\" → Valid company, keep as is\n" +
            "- \"JGraph Ltd\" → Valid company, keep as is\n" +
            "- \"The Omni Group\" → Valid company, keep as is\n" +
            "\n" +
            "Examples of JUNK entries (should be deleted):\n" +
            "- \"Random Junk Name\" → Should be deleted\n" +
            "- \"Test Company\" → Should be deleted\n" +
            "- \"Sample Corp\" → Should be deleted\n" +
            "- \"Demo Inc\" → Should be deleted\n" +
            "- \"Example Ltd\" → Should be deleted\n" +
            "- \"Fake Company\" → Should be deleted\n" +
            "- \"Dummy Corp\" → Should be deleted\n" +
            "- \"Mock Business\" → Should be deleted\n" +
            "- \"Placeholder Inc\" → Should be deleted\n" +
            "- \"Temporary Corp\" → Should be deleted\n" +
            "\n" +
            "Respond in JSON format:\n" +
            "{\n" +
            "    \"action\": \"delete|rename|keep\",\n" +
            "    \"reason\": \"detailed explanation\",\n" +
            "    \"parentCompany\": \"parent company name if this is a product/service\",\n" +
            "    \"newName\": \"new name if rename action\"\n" +
            "}\n" +
            "\n" +
            "IMPORTANT RULES:\n" +
            "1. If it's a product/service, always provide the parentCompany name\n" +
            "2. Use action \"delete\" for products/services that should be mapped to parent companies\n" +
            "3. Use action \"delete\" for junk entries\n" +
            "4. Use action \"keep\" only for valid standalone companies\n" +
            "5. Use action \"rename\" only if the current name is incorrect but it's still a valid company\n" +
            "6. Be very thorough in identifying products vs companies\n" +
            "7. When in doubt, treat it as a product and provide the most likely parent company", 
            companyName);
    }

    /**
     * Call Gemini API for company cleanup analysis
     */
    private String callGeminiAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);
            content.put("parts", parts);
            
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(content);
            requestBody.put("contents", contents);
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 1);
            generationConfig.put("maxOutputTokens", 500);
            requestBody.put("generationConfig", generationConfig);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts2 = (List<Map<String, Object>>) content2.get("parts");
                    if (parts2 != null && !parts2.isEmpty()) {
                        return (String) parts2.get(0).get("text");
                    }
                }
            }
            
            throw new RuntimeException("Invalid response from Gemini API");
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage());
        }
    }

    /**
     * Parse the AI response for cleanup actions
     */
    private Map<String, Object> parseCleanupResponse(String response, String originalName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract JSON from response
            Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}");
            Matcher matcher = jsonPattern.matcher(response);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                
                // Extract action
                if (jsonStr.contains("\"action\":\"delete\"")) {
                    result.put("delete", true);
                    // Extract parent company name
                    Pattern parentPattern = Pattern.compile("\"parentCompany\":\"([^\"]+)\"");
                    Matcher parentMatcher = parentPattern.matcher(jsonStr);
                    if (parentMatcher.find()) {
                        String parentCompany = parentMatcher.group(1);
                        result.put("parentCompany", parentCompany);
                        
                        // Check if parent company exists in database
                        boolean parentExists = checkIfParentCompanyExists(parentCompany);
                        result.put("parentCompanyExists", parentExists);
                        
                        if (parentExists) {
                            result.put("shouldRemove", true);
                            result.put("reason", "Product/service entry - parent company already exists in database");
                        } else {
                            result.put("shouldRemove", false);
                            result.put("reason", "Product/service entry - parent company not found, will be marked as junk");
                        }
                    }
                } else if (jsonStr.contains("\"action\":\"rename\"")) {
                    result.put("rename", true);
                    // Extract new name
                    Pattern newNamePattern = Pattern.compile("\"newName\":\"([^\"]+)\"");
                    Matcher newNameMatcher = newNamePattern.matcher(jsonStr);
                    if (newNameMatcher.find()) {
                        result.put("newName", newNameMatcher.group(1));
                    }
                } else {
                    result.put("keep", true);
                }
                
                // Extract reason
                Pattern reasonPattern = Pattern.compile("\"reason\":\"([^\"]+)\"");
                Matcher reasonMatcher = reasonPattern.matcher(jsonStr);
                if (reasonMatcher.find()) {
                    String aiReason = reasonMatcher.group(1);
                    // Use AI reason if we don't have a custom reason
                    if (!result.containsKey("reason")) {
                        result.put("reason", aiReason);
                    }
                }
            } else {
                // Fallback parsing
                if (response.toLowerCase().contains("delete")) {
                    result.put("delete", true);
                    result.put("reason", "Parsed from AI response - marked for deletion");
                } else if (response.toLowerCase().contains("rename")) {
                    result.put("rename", true);
                    result.put("reason", "Parsed from AI response - marked for rename");
                } else {
                    result.put("keep", true);
                    result.put("reason", "Parsed from AI response - no action needed");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            result.put("error", "Failed to parse AI response");
        }
        
        return result;
    }

    /**
     * Check if parent company exists in database
     */
    private boolean checkIfParentCompanyExists(String parentCompanyName) {
        try {
            // Check for exact match first
            Optional<Company> exactMatch = companyRepository.findByNameIgnoreCase(parentCompanyName);
            if (exactMatch.isPresent()) {
                return true;
            }
            
            // Check for partial matches (case insensitive)
            List<Company> allCompanies = companyRepository.findAll();
            for (Company company : allCompanies) {
                if (company.getName() != null && 
                    (company.getName().toLowerCase().contains(parentCompanyName.toLowerCase()) ||
                     parentCompanyName.toLowerCase().contains(company.getName().toLowerCase()))) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error checking if parent company exists: {}", e.getMessage());
            return false;
        }
    }
} 