package org.odhsi.athena.dao;

import org.odhsi.athena.entity.SecPermission;

import java.util.List;

/**
 * Created by GMalikov on 21.09.2015.
 */
public interface SecPermissionDAO {

    public SecPermission getById(Long id);

    public List<SecPermission> getUserPermissions(Long userId);
}
