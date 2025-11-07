package com.sparta.userservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "p_delivery_managers")
public class DeliveryManager {

    @Id
    private Long deliveryManagerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType type;

    private UUID hubId;
    public void assignHubId(UUID hubId) {
        this.hubId = hubId;
    }

    private Integer serialNumber;
}
