package org.odhsi.athena.services;

import org.odhsi.athena.dto.MenuItemDTO;
import org.odhsi.athena.entity.SecPermission;
import org.odhsi.athena.entity.SecRole;
import org.odhsi.athena.entity.SecUser;

import java.util.List;

/**
 * Created by GMalikov on 03.07.2015.
 */
public interface SecurityService {

    public List<MenuItemDTO> getMenuItems();

    public SecUser getUserByUsername(String username);

    public List<SecRole> getUserRoles(Long userId);

    public List<SecPermission> getUserPermissions(Long userId);
}
