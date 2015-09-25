package org.odhsi.athena.dao;

import org.odhsi.athena.entity.SecUser;

/**
 * Created by GMalikov on 18.09.2015.
 */
public interface SecUserDAO {
    public SecUser getById(Long id);

    public SecUser getUserWithRolesById(Long id);

    public SecUser getByUsername(String username);
}
