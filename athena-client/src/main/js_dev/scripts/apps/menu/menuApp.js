/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    var API = {
        showMenu: function(){
            MainMenu.Controller.showMenu();
        }
    };

    AthenaApp.on("showMenu", function(){
        API.showMenu();
    });
});