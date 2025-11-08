package org.lab.dev.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address Dto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String address1;
    private String address2;
    private String city;
    private String postcode;
    @Size(min = 1, max = 3)
    private String country;
}
