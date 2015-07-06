package org.odhsi.athena.controllers;

import org.odhsi.athena.dto.MenuItemDTO;
import org.odhsi.athena.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by GMalikov on 23.03.2015.
 */

@Controller
public class MainAppController {

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "getMenuItems", method = RequestMethod.GET)
    @ResponseBody
    public List<MenuItemDTO> getMenuItems(){
        return securityService.getMenuItems();
    }
}
