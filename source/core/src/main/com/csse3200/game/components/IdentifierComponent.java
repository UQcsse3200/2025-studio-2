package com.csse3200.game.components;

/**
 * Component that stores a string identifier for an entity.
 */
public class IdentifierComponent extends Component {
    private final String id;

    /**
     * Creates a new IdentifierComponent with the given ID.
     * @param id the identifier string
     */
    public IdentifierComponent(String id) {
        this.id = id;
    }

    /**
     * @return the identifier string
     */
    public String getId() {
        return id;
    }
}
