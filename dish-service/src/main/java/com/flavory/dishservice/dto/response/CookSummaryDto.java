package com.flavory.dishservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CookSummaryDto {
    private String fullName;
    private String profileImageUrl;
    private String role;
    private String status;
}