package org.lab.dev.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Logan Nguyen
 * CartDto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private Long id;
    private CustomerDto customer;
    private String status;
}
