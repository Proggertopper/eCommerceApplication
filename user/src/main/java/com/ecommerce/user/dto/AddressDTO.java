package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    private String state;

    @NotBlank
    private String country;

    private String zipcode;
}
