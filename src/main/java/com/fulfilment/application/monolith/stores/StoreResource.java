package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("stores")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;
  @Inject TransactionSynchronizationRegistry transactionSynchronizationRegistry;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    LOGGER.info("Listing all stores");
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    LOGGER.infof("Getting store by ID: %d", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store not found with ID: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    LOGGER.info("Creating a new store");
    if (store.id != null) {
      LOGGER.warn("Attempted to create store with pre-set ID");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    store.persist();
    LOGGER.infof("Store persisted with ID: %d", store.id);

    transactionSynchronizationRegistry.registerInterposedSynchronization(
        new Synchronization() {
          @Override
          public void beforeCompletion() {}

          @Override
          public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
              LOGGER.infof("Transaction committed, syncing store %d with legacy system", store.id);
              legacyStoreManagerGateway.createStoreOnLegacySystem(store);
            }
          }
        });

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    LOGGER.infof("Updating store with ID: %d", id);
    if (updatedStore.name == null) {
      LOGGER.warnf("Store Name not set in update request for ID: %d", id);
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for update with ID: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    transactionSynchronizationRegistry.registerInterposedSynchronization(
        new Synchronization() {
          @Override
          public void beforeCompletion() {}

          @Override
          public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
              LOGGER.infof("Transaction committed, syncing updated store %d with legacy system", id);
              legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
            }
          }
        });

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    LOGGER.infof("Patching store with ID: %d", id);
    if (updatedStore.name == null) {
      LOGGER.warnf("Store Name not set in patch request for ID: %d", id);
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store not found for patch with ID: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    transactionSynchronizationRegistry.registerInterposedSynchronization(
        new Synchronization() {
          @Override
          public void beforeCompletion() {}

          @Override
          public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
              LOGGER.infof("Transaction committed, syncing patched store %d with legacy system", id);
              legacyStoreManagerGateway.updateStoreOnLegacySystem(entity);
            }
          }
        });

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infof("Deleting store with ID: %d", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store not found for deletion with ID: %d", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    LOGGER.infof("Store deleted with ID: %d", id);
    return Response.status(204).build();
  }
}
