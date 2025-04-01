package com.tymbl.registration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LinkedInProfile {
    private String id;
    
    @JsonProperty("localizedFirstName")
    private String firstName;
    
    @JsonProperty("localizedLastName")
    private String lastName;
    
    @JsonProperty("email-address")
    private String email;
    
    @JsonProperty("profilePicture")
    private ProfilePicture profilePicture;
    
    @Data
    public static class ProfilePicture {
        @JsonProperty("displayImage~")
        private DisplayImage displayImage;
    }
    
    @Data
    public static class DisplayImage {
        private String[] elements;
    }
} 