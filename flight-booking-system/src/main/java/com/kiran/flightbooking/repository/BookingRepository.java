package com.kiran.flightbooking.repository;

import com.kiran.flightbooking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"user", "flight"})
    Optional<Booking> findByBookingReference(String bookingReference);

    @EntityGraph(attributePaths = {"user", "flight"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "flight"})
    Page<Booking> findByUserEmailIgnoreCase(String email, Pageable pageable);

    boolean existsByBookingReference(String bookingReference);
}
