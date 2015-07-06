/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    MainMenu.Controller = {
        showMenu: function(){

            var fetchingMenuItems = AthenaApp.request("menuItems");
            $.when(fetchingMenuItems).done(function(menuItems){
                console.log("Showing the composite view");
                var menuMainView = new MainMenu.Main({
                    collection: menuItems
                });
                AthenaApp.menuRegion.show(menuMainView);
            });
        }
    }
});