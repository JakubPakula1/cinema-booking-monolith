package io.github.jakubpakula1.cinema.service;

import groovy.util.logging.Slf4j;
import io.github.jakubpakula1.cinema.repository.TemporaryReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@lombok.extern.slf4j.Slf4j
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupService {
    private final TemporaryReservationRepository temporaryReservationRepository;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Starting cleanup of expired temporary reservations.");
        temporaryReservationRepository.deleteByExpiresAtBefore(java.time.LocalDateTime.now());
        log.info("Cleanup of expired temporary reservations completed at {}", LocalDateTime.now());
    }
}
