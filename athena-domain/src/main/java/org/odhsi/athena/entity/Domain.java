package org.odhsi.athena.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by GMalikov on 25.03.2015.
 */

@Entity
@Table(name = "DOMAIN")
public class Domain {

    private String id;
    private String name;


    @Id
    @Column(name = "DOMAIN_ID", nullable = false, length = 20)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "DOMAIN_NAME", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
