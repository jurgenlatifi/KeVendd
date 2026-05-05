package com.keVend.backend.repository;

import com.keVend.backend.model.ParkingSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSensorRepository extends JpaRepository<ParkingSensor, Long> {

    Optional<ParkingSensor> findByApiKeyHash(String apiKeyHash);

    List<ParkingSensor> findByParkingId(Long parkingId);
}
