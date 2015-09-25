package org.odhsi.athena.entity;

import java.util.Set;

/**
 * Created by GMalikov on 18.09.2015.
 */
public class SecRole {
    private Long id;
    private String name;
    private Set<SecAction> actions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SecAction> getActions() {
        return actions;
    }

    public void setActions(Set<SecAction> actions) {
        this.actions = actions;
    }
}
