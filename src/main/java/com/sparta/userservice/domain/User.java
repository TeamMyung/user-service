package com.sparta.userservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static com.sparta.userservice.domain.UserStatus.APPROVE;
import static com.sparta.userservice.domain.UserStatus.REJECT;

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

    public void approve() {
        status = APPROVE;
    }

    public void reject() {
        status = REJECT;
    }

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

    private Boolean isDeliveryManager = false;

    public void assignAsDeliveryManager() {
        isDeliveryManager = true;
    }
}
