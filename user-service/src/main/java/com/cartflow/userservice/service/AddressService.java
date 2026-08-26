package com.cartflow.userservice.service;

import com.cartflow.userservice.dto.AddressRequest;
import com.cartflow.userservice.dto.AddressResponse;
import com.cartflow.userservice.entity.Address;
import com.cartflow.userservice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressResponse> getAll(Long userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse add(Long userId, AddressRequest request) {
        Address address = Address.builder()
                .userId(userId)
                .name(request.getName())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() != null ? request.getCountry() : "India")
                .build();

        // first address is auto-default
        if (addressRepository.findByUserId(userId).isEmpty()) {
            address.setDefault(true);
        }

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
        Address address = findOwned(userId, addressId);

        address.setName(request.getName());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = findOwned(userId, addressId);
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        findOwned(userId, addressId);
        addressRepository.clearDefaultForUser(userId);

        Address address = findOwned(userId, addressId);
        address.setDefault(true);
        return AddressResponse.from(addressRepository.save(address));
    }

    private Address findOwned(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));
    }
}
