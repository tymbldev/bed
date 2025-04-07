package com.tymbl.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Country information")
public class CountryDTO {

    @Schema(description = "Unique identifier of the country", example = "1")
    private Long id;

    @Schema(description = "Name of the country", example = "United States")
    private String name;

    @Schema(description = "Two-letter country code (ISO 3166-1 alpha-2)", example = "US")
    private String code;

    @Schema(description = "Country phone code (without +)", example = "1")
    private String phoneCode;
} 