package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.Review;
import org.lab.dev.repository.ProductRepository;
import org.lab.dev.repository.ReviewRepository;
import org.lab.dev.web.dto.ReviewDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class ReviewService {

    @Inject
    ReviewRepository reviewRepository;
    @Inject
    ProductRepository productRepository;

    public static ReviewDto mapToDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getTitle(),
                review.getDescription(),
                review.getRating()
        );
    }

    public List<ReviewDto> findByProduct_Id(Long id) {
        log.debug("Request to get all Reviews");
        return this.reviewRepository.findByProduct_Id(id)
                .stream()
                .map(ReviewService::mapToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto findById(Long id) {
        log.debug("Request to get Review : {}", id);
        return this.reviewRepository.findById(id)
                .map(ReviewService::mapToDto)
                .orElse(null);
    }

    public ReviewDto create(ReviewDto reviewDto, Long productId) {
        log.debug("Request to create Review : {} ofr the Product {}", reviewDto, productId);

        var product = this.productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product with ID:" + productId + " was not found !"));

        var savedReview = this.reviewRepository.saveAndFlush(
                new Review(
                        reviewDto.getTitle(),
                        reviewDto.getDescription(),
                        reviewDto.getRating()
                )
        );

        product.getReviews().add(savedReview);
        this.productRepository.saveAndFlush(product);

        return mapToDto(savedReview);
    }

    public void delete(Long reviewId) {
        log.debug("Request to delete Review : {}", reviewId);

        var review = this.reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalStateException("Product with ID:" + reviewId + " was not found !"));

        var product = this.productRepository.findByReviews_Id(reviewId);

        product.getReviews().remove(review);

        this.productRepository.saveAndFlush(product);
        this.reviewRepository.delete(review);
    }
}