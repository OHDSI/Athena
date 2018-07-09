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

package com.odysseusinc.athena.api.v1.controller.dto;

import com.odysseusinc.athena.model.security.AthenaRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;

public class AthenaUserDTO extends BaseAthenaUserWithEmailDTO {

    private String username;
    private String password;
    private String origin;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean accountNonExpired = true;
    private Boolean accountNonLocked = true;
    private Boolean credentialsNonExpired = true;
    private Boolean enabled = true;

    private List<AthenaRole> roles;

    public AthenaUserDTO(BaseAthenaUserWithEmailDTO other) {

        super(other);
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
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

    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {

        this.authorities = authorities;
    }

    public Boolean getAccountNonExpired() {

        return accountNonExpired;
    }

    public void setAccountNonExpired(Boolean accountNonExpired) {

        this.accountNonExpired = accountNonExpired;
    }

    public Boolean getAccountNonLocked() {

        return accountNonLocked;
    }

    public void setAccountNonLocked(Boolean accountNonLocked) {

        this.accountNonLocked = accountNonLocked;
    }

    public Boolean getCredentialsNonExpired() {

        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {

        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Boolean getEnabled() {

        return enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    public List<AthenaRole> getRoles() {

        return roles;
    }

    public void setRoles(List<AthenaRole> roles) {

        this.roles = roles;
    }
}
