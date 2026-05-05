package com.keVend.backend.service;

import com.keVend.backend.dto.ParkingResponse;
import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Favorite;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    /** FR-15: cap on favorites per driver. */
    private static final int MAX_FAVORITES = 5;

    private final FavoriteRepository favoriteRepository;
    private final ParkingService parkingService;
    private final I18n i18n;

    @Transactional
    public Favorite add(User driver, Long parkingId) {
        if (favoriteRepository.findByDriverIdAndParkingId(driver.getId(), parkingId).isPresent()) {
            throw i18n.conflict("error.favorite.duplicate");
        }
        if (favoriteRepository.countByDriverId(driver.getId()) >= MAX_FAVORITES) {
            throw i18n.conflict("error.favorite.limit_reached", MAX_FAVORITES);
        }
        Favorite f = new Favorite();
        f.setDriver(driver);
        f.setParking(parkingService.loadOrThrow(parkingId));
        return favoriteRepository.save(f);
    }

    @Transactional
    public void remove(User driver, Long parkingId) {
        Favorite f = favoriteRepository.findByDriverIdAndParkingId(driver.getId(), parkingId)
                .orElseThrow(() -> i18n.notFound("error.favorite.not_found"));
        favoriteRepository.delete(f);
    }

    public List<ParkingResponse> list(User driver) {
        return favoriteRepository.findByDriverIdOrderByCreatedAtAsc(driver.getId()).stream()
                .map(f -> ParkingResponse.from(f.getParking()))
                .toList();
    }
}
