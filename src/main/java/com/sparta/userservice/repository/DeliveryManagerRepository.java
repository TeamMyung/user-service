package com.sparta.userservice.repository;

import com.sparta.userservice.domain.deliverymanager.DeliveryManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryManagerRepository extends JpaRepository<DeliveryManager, Long> {
}
