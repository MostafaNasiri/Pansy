package io.github.mostafanasiri.pansy.app.domain.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entity, int entityId) {
        super(String.format("%s with id %s was not found", entity.getSimpleName(), entityId));
    }
}
