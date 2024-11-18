package com.bytecoders.pharmaid.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utils for general helpers in service classes.
 */
@Slf4j
@Component
public class ServiceUtils {

  /**
   * Finds an entityId or throws ResponseStatusException if not found.
   *
   * @param <T>        Entity type
   * @param id         The ID of the entity
   * @param entityName The name of the entity (eg "user", "medication")
   * @param repository The repository for the entity
   * @return The entity if found
   */
  public <T> T findEntityById(
      String id, String entityName, JpaRepository<T, String> repository) {
    return repository.findById(id).orElseThrow(() -> {
      log.warn("Provided {}Id does not exist: {}", entityName, id);
      return new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format("Provided %sId does not exist: %s", entityName, id));
    });
  }
}