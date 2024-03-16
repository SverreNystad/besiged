package com.softwarearchitecture.ecs;

import java.util.UUID;

/**
 * Represents an entity within an Entity-Component-System (ECS) architecture.
 * Each entity is uniquely identified by a UUID and can have various components
 * attached to it.
 * 
 * The Entity class provides methods for adding components to the entity.
 * Components are managed by the
 * {@link ECSManager}, which ensures that each component type is associated with
 * its corresponding
 * entity efficiently.
 */
public class Entity {
    /** Unique identifier for the entity. */
    private UUID id;

    /** Reference to the ECSManager, used for component management. */
    private ECSManager ecs;

    /**
     * Constructs a new Entity with a unique UUID and initializes its connection to
     * the ECSManager.
     * The ECSManager instance is retrieved through its singleton instance, ensuring
     * that all entities
     * interact with the same ECS management system.
     */
    public Entity() {
        this.id = UUID.randomUUID();
        this.ecs = ECSManager.getInstance();
    }

    /**
     * Returns the unique identifier (UUID) of the entity.
     * 
     * @return The UUID of this entity.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Adds a component of a specific type to this entity. If the ECSManager has a
     * ComponentManager
     * for the specified component type, the component is added to this entity
     * through that manager.
     * 
     * @param componentType The class type of the component to add.
     * @param component     The component instance to add to this entity.
     * @param <T>           The type of the component.
     */
    public <T> void addComponent(Class<T> componentType, T component) {
        ComponentManager<T> manager = ecs.getComponentManager(componentType);
        if (manager != null) {
            manager.addComponent(this, component);
        }
    }
}