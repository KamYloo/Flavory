package com.flavory.dishservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cook_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CookProfile {

    @Id
    @Column(name = "cook_id", nullable = false)
    private String cookId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, length = 20)
    private String status;

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}