/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.model.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.odysseusinc.athena.model.athena.License;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class AthenaUser implements UserDetails {
    @Id
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    private Long id;
    @Column(name = "login", nullable = false)
    private String username;
    @Transient
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "origin", nullable = false)
    private String origin;
    @Transient
    private Collection<? extends GrantedAuthority> authorities;
    @Transient
    private Boolean accountNonExpired = true;
    @Transient
    private Boolean accountNonLocked = true;
    @Transient
    private Boolean credentialsNonExpired = true;
    @Transient
    private Boolean enabled = true;
    @Column(name = "firstname")
    private String firstName;
    @Column(name = "middlename")
    private String middleName;
    @Column(name = "lastname")
    private String lastName;
    @Column
    private String organization;

    @ManyToMany(targetEntity = AthenaRole.class, fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<AthenaRole> roles;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "user", targetEntity = License.class)
    private List<License> licenses;

    public AthenaUser() {

        super();
    }

    public AthenaUser(Long id, String username, String origin, String password, String email,
                      Collection<? extends GrantedAuthority> authorities) {

        this.setId(id);
        this.setUsername(username);
        this.setOrigin(origin);
        this.setPassword(password);
        this.setEmail(email);
        this.setAuthorities(authorities);
    }

    public AthenaUser(Long id) {

        this.setId(id);
    }

    public List<License> getLicenses() {

        return licenses;
    }

    public void setLicenses(List<License> licenses) {

        this.licenses = licenses;
    }

    public Long getId() {

        return this.id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getUsername() {

        return this.username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    @JsonIgnore
    public String getPassword() {

        return this.password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getOrigin() {

        return origin;
    }

    public void setOrigin(String origin) {

        this.origin = origin;
    }

    public String getEmail() {

        return this.email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return this.authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {

        this.authorities = authorities;
    }

    @JsonIgnore
    public Boolean getAccountNonExpired() {

        return this.accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {

        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonExpired() {

        return this.getAccountNonExpired();
    }

    @JsonIgnore
    public Boolean getAccountNonLocked() {

        return this.accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {

        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isAccountNonLocked() {

        return this.getAccountNonLocked();
    }

    @JsonIgnore
    public Boolean getCredentialsNonExpired() {

        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {

        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return this.getCredentialsNonExpired();
    }

    @JsonIgnore
    public Boolean getEnabled() {

        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {

        return this.getEnabled();
    }

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }

    public String getMiddleName() {

        return middleName;
    }

    public void setMiddleName(String middleName) {

        this.middleName = middleName;
    }

    public String getLastName() {

        return lastName;
    }

    public void setLastName(String lastName) {

        this.lastName = lastName;
    }

    public List<AthenaRole> getRoles() {

        return roles;
    }

    public void setRoles(List<AthenaRole> roles) {

        this.roles = roles;
    }

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    @Override
    public String toString() {

        return "AthenaUser{"
                + "username='"
                + username
                + '\''
                + ", email='"
                + email
                + '\''
                + ", organization='"
                + organization
                + '\''
                + ", origin='" + origin
                + '\''
                + '}';
    }
}
