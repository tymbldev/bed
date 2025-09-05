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
@Schema(description = "Company information for dropdown selection")
public class CompanyDropdownDTO {

  @Schema(description = "Unique identifier of the company", example = "1")
  private Long id;

  @Schema(description = "Name of the company", example = "Google")
  private String name;
}
