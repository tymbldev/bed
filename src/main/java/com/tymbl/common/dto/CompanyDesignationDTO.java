package com.tymbl.common.dto;

import com.tymbl.interview.dto.SkillDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDesignationDTO {
    private Long id;
    private String name;
    private Long companyId;
    private String companyName;
    private List<SkillDTO> skills;
} 