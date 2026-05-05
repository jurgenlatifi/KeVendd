package com.keVend.backend.repository;

import com.keVend.backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByDriverIdOrderByCreatedAtAsc(Long driverId);

    long countByDriverId(Long driverId);

    Optional<Favorite> findByDriverIdAndParkingId(Long driverId, Long parkingId);
}
