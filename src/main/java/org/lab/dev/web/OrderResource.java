package org.lab.dev.web;

import io.quarkus.security.Authenticated;
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
import org.lab.dev.service.OrderService;
import org.lab.dev.web.dto.OrderDto;

import java.util.List;

@Authenticated
@Path("/orders")
@Tag(name = "order", description = "All the order methods")
public class OrderResource {

    @Inject
    OrderService orderService;

    @RolesAllowed("admin")
    @GET
    public List<OrderDto> findAll() {
        return this.orderService.findAll();
    }

    @GET
    @Path("/customer/{id}")
    public List<OrderDto> findAllByUser(@PathParam("id") Long id) {
        return this.orderService.findAllByUser(id);
    }

    @GET
    @Path("/{id}")
    public OrderDto findById(@PathParam("id") Long id) {
        return this.orderService.findById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDto create(OrderDto orderDto) {
        return this.orderService.create(orderDto);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.orderService.delete(id);
    }

    @GET
    @Path("/exists/{id}")
    public boolean existsById(@PathParam("id") Long id) {
        return this.orderService.existsById(id);
    }
}
