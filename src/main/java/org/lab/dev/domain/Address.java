package org.lab.dev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {
    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @NotNull
    @Column(name = "city")
    private String city;

    @NotNull
    @Size(max = 10)
    @Column(name = "postcode", length = 10, nullable = false)
    private String postcode;

    @NotNull
    @Size(max = 3)
    @Column(name = "country")
    private String country;

}
