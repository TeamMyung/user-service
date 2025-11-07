package com.sparta.userservice.domain.deliverymanager;

import com.sparta.userservice.domain.user.User;
import jakarta.persistence.*;

import java.util.UUID;

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
    private DeliveryType type;

    private UUID hubId;
    private Integer serialNumber;
}
