package com.api.parkingcontrol.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parking_spot")
@Getter
@Setter
public class ParkingSpotModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10, name = "parking_spot_number")
    private String parkingSpotNumber;

    @Column(nullable = false, unique = true, length = 7, name = "license_plate_car")
    private String licensePlateCar;

    @Column(nullable = false, length = 70, name = "car_brand")
    private String carBrand;

    @Column(nullable = false, length = 70, name = "car_model")
    private String carModel;

    @Column(nullable = false, length = 70, name = "parking_spot_number")
    private String carColor;

    @Column(nullable = false, name = "registration_datec")
    private LocalDateTime registrationDate;

    @Column(nullable = false, length = 130, name = "responsible_name")
    private String responsibleName;

    @Column(nullable = false, unique = true, length = 30)
    private String block;
}
