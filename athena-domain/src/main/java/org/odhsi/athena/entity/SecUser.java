package org.odhsi.athena.entity;

import java.util.Set;

/**
 * Created by GMalikov on 18.09.2015.
 */
public class SecUser {
    private Long id;
    private String userName;
    private String password;
    private String salt;
    private String name;
    private Set<SecRole> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SecRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SecRole> roles) {
        this.roles = roles;
    }
}
