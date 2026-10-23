package com.kiran.flightbooking.repository;

import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.enums.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightCodeIgnoreCase(String flightCode);

    boolean existsByFlightCodeIgnoreCase(String flightCode);

    @Query("SELECT f FROM Flight f WHERE " +
           "(:source IS NULL OR LOWER(f.source) = LOWER(:source)) AND " +
           "(:destination IS NULL OR LOWER(f.destination) = LOWER(:destination)) AND " +
           "(:departureDateStart IS NULL OR f.departureTime >= :departureDateStart) AND " +
           "(:departureDateEnd IS NULL OR f.departureTime <= :departureDateEnd) AND " +
           "(:minFare IS NULL OR f.baseFare >= :minFare) AND " +
           "(:maxFare IS NULL OR f.baseFare <= :maxFare) AND " +
           "(:minSeats IS NULL OR f.availableSeats >= :minSeats) AND " +
           "(f.status = :status)")
    Page<Flight> searchFlights(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("departureDateStart") OffsetDateTime departureDateStart,
            @Param("departureDateEnd") OffsetDateTime departureDateEnd,
            @Param("minFare") BigDecimal minFare,
            @Param("maxFare") BigDecimal maxFare,
            @Param("minSeats") Integer minSeats,
            @Param("status") FlightStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Flight f SET f.availableSeats = f.availableSeats - :tickets, f.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE f.id = :flightId AND f.availableSeats >= :tickets AND f.status = 'SCHEDULED'")
    int reserveSeats(@Param("flightId") Long flightId, @Param("tickets") int tickets);

    @Modifying
    @Query("UPDATE Flight f SET f.availableSeats = f.availableSeats + :tickets, f.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE f.id = :flightId AND (f.availableSeats + :tickets) <= f.totalSeats")
    int releaseSeats(@Param("flightId") Long flightId, @Param("tickets") int tickets);
}
