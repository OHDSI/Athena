package org.odhsi.athena.services;

import org.odhsi.athena.dto.MenuItemDTO;

import java.util.List;

/**
 * Created by GMalikov on 03.07.2015.
 */
public interface SecurityService {

    public List<MenuItemDTO> getMenuItems();
}
