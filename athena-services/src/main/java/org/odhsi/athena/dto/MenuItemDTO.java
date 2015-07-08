package org.odhsi.athena.dto;

/**
 * Created by GMalikov on 03.07.2015.
 */
public class MenuItemDTO {
    String id;
    String name;
    String navigationTrigger;

    public MenuItemDTO(String id, String name, String navigationTrigger){
        this.id = id;
        this.name = name;
        this.navigationTrigger = navigationTrigger;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNavigationTrigger() {
        return navigationTrigger;
    }

    public void setNavigationTrigger(String navigationTrigger) {
        this.navigationTrigger = navigationTrigger;
    }
}
