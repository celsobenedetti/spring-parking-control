package com.api.parkingcontrol.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.repositories.ParkingSpotRepository;

@Service
public class ParkingSpotService {
    final ParkingSpotRepository parkingSpotRepository;

    ParkingSpotService(ParkingSpotRepository parkingSpotRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @Transactional
    public ParkingSpotModel save(ParkingSpotDto parkingSpotDto) {
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return parkingSpotRepository.save(parkingSpotModel);
    }

    public String checkSpotRegistered(ParkingSpotDto parkingSpotDto) {
        System.out.println(parkingSpotDto.getParkingSpotNumber());
        if (parkingSpotRepository.findByLicensePlateCar(parkingSpotDto.getLicensePlateCar()) != null)
            return "The car is already parked";
        if (parkingSpotRepository.findByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber()) != null)
            return "The spot is already taken";

        return "";
    }

    public Page<ParkingSpotModel> findAll(Pageable pageable) {
        return parkingSpotRepository.findAll(pageable);
    }

    public Optional<ParkingSpotModel> findOneById(UUID id) {
        return parkingSpotRepository.findById(id);
    }

    @Transactional
    public void delete(UUID id) {
        parkingSpotRepository.deleteById(id);
    }

    @Transactional
    public ParkingSpotModel updateOne(ParkingSpotModel parkingSpotModel,
            ParkingSpotDto parkingSpotDto) {
        parkingSpotModel.setBlock(parkingSpotDto.getBlock());
        parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
        parkingSpotModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
        parkingSpotModel.setCarBrand(parkingSpotDto.getCarBrand());
        parkingSpotModel.setCarColor(parkingSpotDto.getCarColor());
        parkingSpotModel.setCarModel(parkingSpotDto.getCarModel());
        parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());

        return parkingSpotRepository.save(parkingSpotModel);
    }

}
