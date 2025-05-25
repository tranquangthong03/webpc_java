package com.example.travelweb.repository;

import com.example.travelweb.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {
    
    // Tìm destination theo tên
    Optional<Destination> findByDestinationName(String destinationName);
    
    // Tìm các destination active
    List<Destination> findByStatusTrue();
    
    // Tìm destination theo quốc gia
    List<Destination> findByCountryAndStatusTrue(String country);
    
    // Tìm destination theo thành phố
    List<Destination> findByCityAndStatusTrue(String city);
    
    // Tìm destination theo tên (tìm kiếm gần đúng)
    @Query("SELECT d FROM Destination d WHERE d.destinationName LIKE %:name% AND d.status = true")
    List<Destination> findByDestinationNameContainingAndStatusTrue(@Param("name") String name);
    
    // Tìm destination theo quốc gia hoặc thành phố
    @Query("SELECT d FROM Destination d WHERE (d.country LIKE %:keyword% OR d.city LIKE %:keyword% OR d.destinationName LIKE %:keyword%) AND d.status = true")
    List<Destination> findByKeywordAndStatusTrue(@Param("keyword") String keyword);
    
    // Lấy danh sách quốc gia duy nhất
    @Query("SELECT DISTINCT d.country FROM Destination d WHERE d.status = true ORDER BY d.country")
    List<String> findDistinctCountries();
    
    // Lấy danh sách thành phố theo quốc gia
    @Query("SELECT DISTINCT d.city FROM Destination d WHERE d.country = :country AND d.status = true ORDER BY d.city")
    List<String> findCitiesByCountry(@Param("country") String country);
    
    // Tìm các destination có tour
    @Query("SELECT DISTINCT d FROM Destination d JOIN d.tours t WHERE d.status = true AND t.status = 'ACTIVE'")
    List<Destination> findDestinationsWithActiveTours();
    
    // Kiểm tra tên destination đã tồn tại
    boolean existsByDestinationName(String destinationName);
}
