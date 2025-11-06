package org.lab.dev.repository;

import org.lab.dev.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    Long countAllCategoryId(Long categoryId);

    @Query("Select p from Product p JOIN p.reviews r Where r.id = ?1")
    Product findProductByReviewId(Long reviewId);

    void deleteAllByCategoryId(Long id);

    List<Product> findAllByCategoryId(Long id);
}
