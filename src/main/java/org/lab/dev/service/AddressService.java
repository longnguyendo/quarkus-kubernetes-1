package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.Address;
import org.lab.dev.web.dto.AddressDto;

@Slf4j
@ApplicationScoped
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressService {

    public static Address createFromDto(AddressDto addressDto) {
        return new Address(
                addressDto.getAddress1(),
                addressDto.getAddress2(),
                addressDto.getCity(),
                addressDto.getPostcode(),
                addressDto.getCountry()
        );
    }

    public static AddressDto mapToDto(Address address) {
        return new AddressDto(
                address.getAddress1(),
                address.getAddress2(),
                address.getCity(),
                address.getPostcode(),
                address.getCountry()
        );
    }
}
