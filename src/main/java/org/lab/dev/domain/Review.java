package org.lab.dev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Review
 * author : Nebress
 * Copy : Logan Nguyen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = false)
@Entity
@Table(name = "reviews")
public class Review extends AbstractEntity{

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "rating", nullable = false)
    private String rating;
}
