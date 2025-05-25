package com.example.travelweb.service;

import com.example.travelweb.entity.Destination;
import com.example.travelweb.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DestinationService {
    
    @Autowired
    private DestinationRepository destinationRepository;
    
    // Lấy tất cả destinations
    public List<Destination> getAllDestinations() {
        return destinationRepository.findAll();
    }
    
    // Lấy các destinations active
    public List<Destination> getActiveDestinations() {
        return destinationRepository.findByStatusTrue();
    }
    
    // Lấy destination theo ID
    public Optional<Destination> getDestinationById(Long id) {
        return destinationRepository.findById(id);
    }
    
    // Lấy destination theo tên
    public Optional<Destination> getDestinationByName(String name) {
        return destinationRepository.findByDestinationName(name);
    }
    
    // Tạo destination mới
    public Destination createDestination(Destination destination) {
        // Kiểm tra tên destination đã tồn tại
        if (destinationRepository.existsByDestinationName(destination.getDestinationName())) {
            throw new RuntimeException("Tên điểm đến đã tồn tại: " + destination.getDestinationName());
        }
        
        return destinationRepository.save(destination);
    }
    
    // Cập nhật destination
    public Destination updateDestination(Long id, Destination destinationDetails) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến với ID: " + id));
        
        // Kiểm tra tên destination (nếu thay đổi)
        if (!destination.getDestinationName().equals(destinationDetails.getDestinationName()) && 
            destinationRepository.existsByDestinationName(destinationDetails.getDestinationName())) {
            throw new RuntimeException("Tên điểm đến đã tồn tại: " + destinationDetails.getDestinationName());
        }
        
        // Cập nhật thông tin
        destination.setDestinationName(destinationDetails.getDestinationName());
        destination.setCountry(destinationDetails.getCountry());
        destination.setCity(destinationDetails.getCity());
        destination.setDescription(destinationDetails.getDescription());
        destination.setImageUrl(destinationDetails.getImageUrl());
        destination.setLatitude(destinationDetails.getLatitude());
        destination.setLongitude(destinationDetails.getLongitude());
        destination.setStatus(destinationDetails.getStatus());
        
        return destinationRepository.save(destination);
    }
    
    // Thay đổi status destination
    public Destination changeDestinationStatus(Long id, Boolean status) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến với ID: " + id));
        
        destination.setStatus(status);
        return destinationRepository.save(destination);
    }
    
    // Xóa destination
    public void deleteDestination(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến với ID: " + id));
        
        // Kiểm tra xem destination có tour nào không
        if (!destination.getTours().isEmpty()) {
            throw new RuntimeException("Không thể xóa điểm đến vì còn tour thuộc điểm đến này");
        }
        
        destinationRepository.deleteById(id);
    }
    
    // Lấy destinations theo quốc gia
    public List<Destination> getDestinationsByCountry(String country) {
        return destinationRepository.findByCountryAndStatusTrue(country);
    }
    
    // Lấy destinations theo thành phố
    public List<Destination> getDestinationsByCity(String city) {
        return destinationRepository.findByCityAndStatusTrue(city);
    }
    
    // Tìm kiếm destination theo từ khóa
    public List<Destination> searchDestinations(String keyword) {
        return destinationRepository.findByKeywordAndStatusTrue(keyword);
    }
    
    // Tìm kiếm destination theo tên
    public List<Destination> searchDestinationsByName(String name) {
        return destinationRepository.findByDestinationNameContainingAndStatusTrue(name);
    }
    
    // Lấy danh sách quốc gia
    public List<String> getAllCountries() {
        return destinationRepository.findDistinctCountries();
    }
    
    // Lấy danh sách thành phố theo quốc gia
    public List<String> getCitiesByCountry(String country) {
        return destinationRepository.findCitiesByCountry(country);
    }
    
    // Lấy các destinations có tour
    public List<Destination> getDestinationsWithTours() {
        return destinationRepository.findDestinationsWithActiveTours();
    }
    
    // Kiểm tra tên destination có tồn tại
    public boolean existsByName(String name) {
        return destinationRepository.existsByDestinationName(name);
    }
}
