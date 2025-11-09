package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.Category;
import org.lab.dev.repository.CategoryRepository;
import org.lab.dev.repository.ProductRepository;
import org.lab.dev.web.dto.CategoryDto;
import org.lab.dev.web.dto.ProductDto;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class CategorySevice {
    @Inject
    CategoryRepository categoryRepository;
    @Inject
    ProductRepository productRepository;

    public static CategoryDto mapToDto(Category category, Long productsCount) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                productsCount
        );
    }

    public List<CategoryDto> findAll() {
        log.debug("Request to get all categories");
        return this.categoryRepository.findAll()
                .stream()
                .map(category ->
                        mapToDto(category, productRepository.countAllCategoryId(category.getId())))
                .collect(Collectors.toList());
    }

    public CategoryDto findById(Long id) {
        log.debug("Request to get category by id: {}", id);
        return this.categoryRepository.findById(id)
                .map(category -> mapToDto(category, productRepository.countAllCategoryId(category.getId())))
                .orElse(null);
    }

    public CategoryDto create(CategoryDto categoryDto) {
        log.debug("Request to create a new category: {}", categoryDto);
        return mapToDto(this.categoryRepository
                .save(new Category(categoryDto.getName(), categoryDto.getDescription())), 0L);
    }

    public void delete(Long id) {
        log.debug("Request to delete category: {}", id);
        log.debug("Deleting all products with category: {}", id);
        this.productRepository.deleteAllByCategoryId(id);
        log.debug("Deleting category: {}", id);
        this.categoryRepository.deleteById(id);
    }

    public List<ProductDto> findProductsByCategoryId(Long id) {
        return this.productRepository.findAllByCategoryId(id)
                .stream()
                .map(ProductService::mapToDto)
                .collect(Collectors.toList());
    }
}
