/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function (MainMenu, AthenaApp, Backbone, Marionette, $, _) {
    MainMenu.Controller = {
        showMenu: function () {

            var fetchingMenuItems = AthenaApp.request("menuItems");
            $.when(fetchingMenuItems).done(function (menuItems) {
                console.log("Showing the composite view");
                var menuMainView = new MainMenu.Main({
                    collection: menuItems
                });

                menuMainView.on("childview:navigate", function (childView, model) {
                    AthenaApp.trigger(model.get("navigationTrigger"));
                });
                AthenaApp.menuRegion.show(menuMainView);
            });
        },
        setActiveMenu: function (menuId) {
            if (AthenaApp.menuRegion.currentView) {
                var menuItems = AthenaApp.menuRegion.currentView.collection;
                var menuToSelect = menuItems.find(function (menuItem) {
                    return menuItem.get("id") === menuId;
                });
                menuToSelect.select();
                AthenaApp.menuRegion.currentView.collection.reset(menuItems.models);
            }
        }
    }
});