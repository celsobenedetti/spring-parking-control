package com.api.parkingcontrol.controllers;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findOneById(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);
        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }
        return ResponseEntity.ok(parkingSpotOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> createParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        var message = parkingSpotService.checkSpotRegistered(parkingSpotDto);
        if (message != "") {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOne(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);

        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }

        parkingSpotService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Parking spot delete successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOne(@PathVariable(value = "id") UUID id,
            @RequestBody ParkingSpotDto parkingSpotDto) {

        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);
        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(parkingSpotService.updateOne(parkingSpotOptional.get(), parkingSpotDto));
    }

}
