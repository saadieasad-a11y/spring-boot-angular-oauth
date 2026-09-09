package com.oauth.controller;

import com.oauth.dto.LocationDTO;
import com.oauth.entity.Location;
import com.oauth.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "http://localhost:4200")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        List<LocationDTO> locationDTOs = locations.stream()
                .map(locationService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
        Location location = locationService.getLocationById(id);
        if (location != null) {
            return ResponseEntity.ok(locationService.convertToDTO(location));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@RequestBody LocationDTO locationDTO) {
        Location location = locationService.convertToEntity(locationDTO);
        Location savedLocation = locationService.saveLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.convertToDTO(savedLocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable Long id, @RequestBody LocationDTO locationDTO) {
        Location existingLocation = locationService.getLocationById(id);
        if (existingLocation == null) {
            return ResponseEntity.notFound().build();
        }
        Location location = locationService.convertToEntity(locationDTO);
        location.setId(id);
        Location updatedLocation = locationService.saveLocation(location);
        return ResponseEntity.ok(locationService.convertToDTO(updatedLocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
