package com.tymbl.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    Server devServer = new Server()
        .url("http://localhost:8080")
        .description("Development server");

    Contact contact = new Contact()
        .name("Tymbl")
        .email("support@tymbl.com")
        .url("https://tymbl.com");

    Info info = new Info()
        .title("Job Referral API")
        .version("1.0.0")
        .contact(contact)
        .description(
            "API documentation for the Job Referral Application. This API provides endpoints for user registration, authentication, job posting, and management.")
        .license(new License().name("Apache 2.0").url("http://springdoc.org"));

    // Define security scheme
    SecurityScheme securityScheme = new SecurityScheme()
        .name("Bearer Authentication")
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("Enter JWT token in the format: Bearer {token}");

    SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

    // Example for registration request
    Example registerExample = new Example()
        .summary("Sample registration request")
        .description("Example of a user registration request")
        .value("{\n" +
            "  \"email\": \"user@example.com\",\n" +
            "  \"password\": \"Password123\",\n" +
            "  \"firstName\": \"John\",\n" +
            "  \"lastName\": \"Doe\",\n" +
            "  \"role\": \"USER\",\n" +
            "  \"phoneNumber\": \"+1234567890\",\n" +
            "  \"company\": \"Example Corp\",\n" +
            "  \"position\": \"Software Engineer\",\n" +
            "  \"department\": \"Engineering\",\n" +
            "  \"location\": \"San Francisco, CA\",\n" +
            "  \"yearsOfExperience\": 5,\n" +
            "  \"skills\": [\"Java\", \"Spring Boot\", \"Microservices\"]\n" +
            "}");

    // Example for login request
    Example loginExample = new Example()
        .summary("Sample login request")
        .description("Example of a user login request")
        .value("{\n" +
            "  \"email\": \"user@example.com\",\n" +
            "  \"password\": \"Password123\"\n" +
            "}");

    // Example for job post request
    Example jobPostExample = new Example()
        .summary("Sample job post request")
        .description("Example of a job posting request")
        .value("{\n" +
            "  \"title\": \"Senior Java Developer\",\n" +
            "  \"description\": \"We are looking for an experienced Java Developer...\",\n" +
            "  \"company\": \"Example Corp\",\n" +
            "  \"department\": \"Engineering\",\n" +
            "  \"location\": \"San Francisco, CA\",\n" +
            "  \"jobType\": \"FULL_TIME\",\n" +
            "  \"experienceLevel\": \"SENIOR\",\n" +
            "  \"minExperience\": 5,\n" +
            "  \"maxExperience\": 10,\n" +
            "  \"minSalary\": \"120000\",\n" +
            "  \"maxSalary\": \"160000\",\n" +
            "  \"requiredSkills\": [\"Java\", \"Spring Boot\", \"Microservices\"],\n" +
            "  \"qualifications\": [\"Bachelor's degree in CS or related field\"],\n" +
            "  \"responsibilities\": [\"Develop backend services\", \"Mentor junior developers\"],\n"
            +
            "  \"workplaceType\": \"HYBRID\",\n" +
            "  \"remoteAllowed\": true,\n" +
            "  \"applicationDeadline\": \"2023-12-31T23:59:59\",\n" +
            "  \"numberOfOpenings\": 2\n" +
            "}");

    Map<String, Example> examples = new HashMap<>();
    examples.put("register", registerExample);
    examples.put("login", loginExample);
    examples.put("jobPost", jobPostExample);

    return new OpenAPI()
        .info(info)
        .servers(Arrays.asList(devServer))
        .components(new Components()
            .securitySchemes(Collections.singletonMap("bearerAuth", securityScheme))
            .examples(examples))
        .addSecurityItem(securityRequirement)
        .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
            .name("Authentication")
            .description("Authentication operations including login and token validation"))
        .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
            .name("Registration")
            .description("User registration endpoints"))
        .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
            .name("Jobs")
            .description("Job posting and management operations"))
        .addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
            .name("Dropdowns")
            .description("Operations for dropdown data: departments, locations, and designations"));
  }

  private String getCurlForRegister() {
    return "curl -X POST http://localhost:8080/api/v1/registration \\\n" +
        "  -H 'Content-Type: application/json' \\\n" +
        "  -d '{\n" +
        "  \"email\": \"user@example.com\",\n" +
        "  \"password\": \"Password123\",\n" +
        "  \"firstName\": \"John\",\n" +
        "  \"lastName\": \"Doe\",\n" +
        "  \"role\": \"USER\",\n" +
        "  \"phoneNumber\": \"+1234567890\",\n" +
        "  \"company\": \"Example Corp\",\n" +
        "  \"position\": \"Software Engineer\"\n" +
        "}'";
  }

  private String getCurlForLogin() {
    return "curl -X POST http://localhost:8080/api/v1/auth/login \\\n" +
        "  -H 'Content-Type: application/json' \\\n" +
        "  -d '{\n" +
        "  \"email\": \"user@example.com\",\n" +
        "  \"password\": \"Password123\"\n" +
        "}'";
  }

  private String getCurlForJobPost() {
    return "curl -X POST http://localhost:8080/api/v1/jobs \\\n" +
        "  -H 'Content-Type: application/json' \\\n" +
        "  -H 'Authorization: Bearer YOUR_JWT_TOKEN' \\\n" +
        "  -d '{\n" +
        "  \"title\": \"Senior Java Developer\",\n" +
        "  \"description\": \"We are looking for an experienced Java Developer...\",\n" +
        "  \"company\": \"Example Corp\",\n" +
        "  \"location\": \"San Francisco, CA\",\n" +
        "  \"jobType\": \"FULL_TIME\",\n" +
        "  \"experienceLevel\": \"SENIOR\",\n" +
        "  \"minExperience\": 5,\n" +
        "  \"workplaceType\": \"HYBRID\",\n" +
        "  \"applicationDeadline\": \"2023-12-31T23:59:59\",\n" +
        "  \"numberOfOpenings\": 2\n" +
        "}'";
  }
}
