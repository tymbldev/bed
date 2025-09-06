/**
 * Example demonstrating the new tokenization logic for designation matching
 * 
 * Input: "SENIOR, SOFTWARE ENGINEER"
 * 
 * Tokenization process:
 * 1. Split by delimiters: [\\s,\\-/&+()]+
 * 2. Filter empty tokens
 * 3. Convert to lowercase
 * 
 * Result: ["senior", "software", "engineer"]
 * 
 * Matching process:
 * - Get all designations from dropdown service
 * - Filter designations that contain at least one token
 * - Examples of matches:
 *   - "Senior Software Engineer" ✓ (contains "senior", "software", "engineer")
 *   - "Software Engineer" ✓ (contains "software", "engineer")
 *   - "Senior Developer" ✓ (contains "senior")
 *   - "Data Scientist" ✗ (contains none of the tokens)
 * 
 * Benefits:
 * 1. More accurate matching by considering all relevant designations
 * 2. Case-insensitive matching
 * 3. Handles various delimiters (spaces, commas, hyphens, etc.)
 * 4. Reduces GenAI processing load by pre-filtering candidates
 * 5. Better performance than processing all designations
 */

public class TokenizationExample {
    
    public static void main(String[] args) {
        // Example input
        String jobTitle = "SENIOR, SOFTWARE ENGINEER";
        
        // Simulate tokenization
        String[] tokens = jobTitle.trim()
            .split("[\\s,\\-/&+()]+");
        
        System.out.println("Input: " + jobTitle);
        System.out.println("Tokens: " + java.util.Arrays.toString(tokens));
        
        // Example designations that would match
        String[] exampleDesignations = {
            "Senior Software Engineer",
            "Software Engineer", 
            "Senior Developer",
            "Data Scientist",
            "Senior Data Engineer"
        };
        
        System.out.println("\nMatching results:");
        for (String designation : exampleDesignations) {
            boolean matches = containsAnyToken(designation, java.util.Arrays.asList(tokens));
            System.out.println(designation + " -> " + (matches ? "MATCH" : "NO MATCH"));
        }
    }
    
    private static boolean containsAnyToken(String designationName, java.util.List<String> tokens) {
        if (designationName == null || designationName.trim().isEmpty() || tokens.isEmpty()) {
            return false;
        }
        
        String lowerDesignationName = designationName.toLowerCase();
        
        return tokens.stream()
            .anyMatch(token -> lowerDesignationName.contains(token.toLowerCase()));
    }
}
