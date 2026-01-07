package com.fulfilment.application.monolith.products;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

  @GET
  public List<Product> get() {
    LOGGER.info("Listing all products");
    return productRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    LOGGER.infof("Getting product by ID: %d", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Product not found with ID: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    LOGGER.info("Creating a new product");
    if (product.id != null) {
      LOGGER.warn("Attempted to create product with pre-set ID");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    productRepository.persist(product);
    LOGGER.infof("Product persisted with ID: %d", product.id);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    LOGGER.infof("Updating product with ID: %d", id);
    if (product.name == null) {
      LOGGER.warnf("Product Name not set in update request for ID: %d", id);
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Product not found for update with ID: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);
    LOGGER.infof("Product updated with ID: %d", id);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infof("Deleting product with ID: %d", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Product not found for deletion with ID: %d", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    productRepository.delete(entity);
    LOGGER.infof("Product deleted with ID: %d", id);
    return Response.status(204).build();
  }
}
