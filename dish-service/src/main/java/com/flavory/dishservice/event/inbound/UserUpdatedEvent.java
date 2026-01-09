package com.flavory.dishservice.event.inbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String auth0Id;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String role;
    private String status;

    private String eventId;
}