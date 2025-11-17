package org.lab.dev.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.lab.dev.service.CategoryService;
import org.lab.dev.web.dto.CategoryDto;
import org.lab.dev.web.dto.ProductDto;

import java.util.List;

@Path("/categories")
@Tag(name = "category", description = "All the category methods")
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    public List<CategoryDto> findAll() {
        return this.categoryService.findAll();
    }

    @GET
    @Path("/{id}")
    public CategoryDto findById(@PathParam("id") Long id) {
        return this.categoryService.findById(id);
    }

    @GET
    @Path("/{id}/products")
    public List<ProductDto> findProductsByCategoryId(@PathParam("id") Long id) {
        return this.categoryService.findProductsByCategoryId(id);
    }

    @RolesAllowed("admin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CategoryDto create(CategoryDto categoryDto) {
        return this.categoryService.create(categoryDto);
    }

    @RolesAllowed("admin")
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.categoryService.delete(id);
    }
}
