package com.sparta.userservice.dto.response;

import com.sparta.userservice.domain.DeliveryManager;
import com.sparta.userservice.domain.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryManagerDto {

    private Long deliveryManagerId;
    private DeliveryType type;
    private UUID hubId;

    public static DeliveryManagerDto from(DeliveryManager deliveryManager) {
        return DeliveryManagerDto.builder()
                .deliveryManagerId(deliveryManager.getDeliveryManagerId())
                .type(deliveryManager.getType())
                .hubId(deliveryManager.getHubId())
                .build();
    }

    public static DeliveryManagerDto empty() {
        return new DeliveryManagerDto();
    }
}
