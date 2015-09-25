package org.odhsi.athena.services.impl;

import org.odhsi.athena.dao.SecPermissionDAO;
import org.odhsi.athena.dao.SecRoleDAO;
import org.odhsi.athena.dao.SecUserDAO;
import org.odhsi.athena.dto.MenuItemDTO;
import org.odhsi.athena.entity.SecPermission;
import org.odhsi.athena.entity.SecRole;
import org.odhsi.athena.entity.SecUser;
import org.odhsi.athena.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 03.07.2015.
 */
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SecUserDAO secUserDAO;

    @Autowired
    private SecRoleDAO secRoleDAO;

    @Autowired
    private SecPermissionDAO secPermissionDAO;

    @Override
    public List<MenuItemDTO> getMenuItems() {
        List<MenuItemDTO> result = new ArrayList<>();
        result.add(new MenuItemDTO("statusList", "Build vocabularies", "builder:listStatus"));
        result.add(new MenuItemDTO("browser", "Browse vocabularies", "browser:list"));
        return result;
    }

    @Override
    public SecUser getUserByUsername(String username) {
        return secUserDAO.getByUsername(username);

    }

    @Override
    public List<SecRole> getUserRoles(Long userId) {
        return secRoleDAO.getUserRoles(userId);
    }

    @Override
    public List<SecPermission> getUserPermissions(Long userId) {
        return secPermissionDAO.getUserPermissions(userId);
    }
}
