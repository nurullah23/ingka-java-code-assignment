package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final Logger LOGGER = Logger.getLogger(LocationGateway.class);

  private static final List<Location> locations = new ArrayList<>();

  static {
    locations.add(new Location("ZWOLLE-001", 1, 40));
    locations.add(new Location("ZWOLLE-002", 2, 50));
    locations.add(new Location("AMSTERDAM-001", 5, 100));
    locations.add(new Location("AMSTERDAM-002", 3, 75));
    locations.add(new Location("TILBURG-001", 1, 40));
    locations.add(new Location("HELMOND-001", 1, 45));
    locations.add(new Location("EINDHOVEN-001", 2, 70));
    locations.add(new Location("VETSBY-001", 1, 90));
  }

  @Override
  public Location resolveByIdentifier(String identifier) {
    LOGGER.debugf("Resolving location for identifier: %s", identifier);
    Optional<Location> loc = locations.stream()
        .filter(location -> location.identification.equals(identifier))
        .findFirst();
    
    if (loc.isPresent()) {
        return loc.get();
    }
    else {
        // TODO: SHould I throw error in this case?
        LOGGER.warnf("Location NOT found for identifier: %s", identifier);
        return null;
    }
  }
}
