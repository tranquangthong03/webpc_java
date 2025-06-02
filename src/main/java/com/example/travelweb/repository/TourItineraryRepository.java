package com.example.travelweb.repository;

import com.example.travelweb.entity.TourItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourItineraryRepository extends JpaRepository<TourItinerary, Long> {
    
    // Tìm itineraries theo tour
    List<TourItinerary> findByTourTourIdOrderByDayNumberAsc(Long tourId);
    
    // Xóa semua itinerary berdasarkan tour ID
    @Modifying
    @Query(value = "DELETE FROM TourItineraries WHERE tour_id = :tourId", nativeQuery = true)
    void deleteAllByTourId(@Param("tourId") Long tourId);
} 