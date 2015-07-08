package org.odhsi.athena.services.impl;

import org.odhsi.athena.dto.MenuItemDTO;
import org.odhsi.athena.services.SecurityService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GMalikov on 03.07.2015.
 */
public class SecurityServiceImpl implements SecurityService {

    @Override
    public List<MenuItemDTO> getMenuItems() {
        List<MenuItemDTO> result = new ArrayList<>();
        result.add(new MenuItemDTO("statusList", "Build vocabularies", "builder:listStatus"));
        result.add(new MenuItemDTO("browser", "Browse vocabularies", "browser:listVocabularies"));
        return result;
    }
}
