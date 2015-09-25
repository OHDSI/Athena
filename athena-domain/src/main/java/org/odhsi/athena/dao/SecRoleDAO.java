package org.odhsi.athena.dao;

import org.odhsi.athena.entity.SecRole;

import java.util.List;

/**
 * Created by GMalikov on 21.09.2015.
 */
public interface SecRoleDAO {

    public SecRole getById(Long id);

    public SecRole getRoleWithActionsById(Long id);

    public List<SecRole> getUserRoles(Long userId);
}
