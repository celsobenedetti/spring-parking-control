package com.api.parkingcontrol.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingSpotDto {

    @NotBlank
    private String parkingSpotNumber;

    @NotBlank
    @Size(min = 7, max = 7)
    private String licensePlateCar;

    @NotBlank
    private String carBrand;

    @NotBlank
    private String carModel;

    @NotBlank
    private String carColor;

    @NotBlank
    private String responsibleName;

    @NotBlank
    private String block;
}
