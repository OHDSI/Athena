/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    var API = {
        showMenu: function(){
            MainMenu.Controller.showMenu();
        }
    };

    MainMenu.on("start", function(){
        API.showMenu();
    });

    AthenaApp.commands.setHandler("set:active:menu", function(menuId){
        MainMenu.Controller.setActiveMenu(menuId);
    });
});