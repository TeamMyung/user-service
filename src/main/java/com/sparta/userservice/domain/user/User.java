package com.sparta.userservice.domain.user;

import com.sparta.userservice.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "p_users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    public void updatePassword(String tempPassword) {
        password = tempPassword;
    }

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(nullable = false, length = 100)
    private String slackAccountId;

    private UUID hubId;

    public void assignHubId(UUID hubId) {
        this.hubId = hubId;
    }

    private UUID vendorId;

    public void assignVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    private boolean isDeliveryManager = false;

    public void assignAsDeliveryManager() {
        isDeliveryManager = true;
    }
}
