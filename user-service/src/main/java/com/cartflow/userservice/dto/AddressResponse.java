package com.cartflow.userservice.dto;

import com.cartflow.userservice.entity.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {

    private Long id;
    private String name;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private boolean isDefault;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .name(address.getName())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }
}
