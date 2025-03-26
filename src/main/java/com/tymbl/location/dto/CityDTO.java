package com.tymbl.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "City information")
public class CityDTO {

    @Schema(description = "Unique identifier of the city", example = "1")
    private Long id;

    @Schema(description = "Name of the city", example = "New York")
    private String name;

    @Schema(description = "Zip/Postal code of the city", example = "10001")
    private String zipCode;

    @Schema(description = "ID of the country this city belongs to", example = "1")
    private Long countryId;

    @Schema(description = "Name of the country this city belongs to", example = "United States")
    private String countryName;
} 