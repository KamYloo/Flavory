package com.flavory.userservice.dto.request;

import com.flavory.userservice.validation.ValidPhone;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+$",
            message = "The name can only contain letters")
    private String firstName;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\-]+$",
            message = "The surname can only contain letters and a dash")
    private String lastName;

    @ValidPhone
    private String phoneNumber;

    @Size(max = 1000)
    private String cookDescription;

    @Size(max = 500)
    @Pattern(regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
            message = "Invalid image URL format\"")
    private String profileImageUrl;
}