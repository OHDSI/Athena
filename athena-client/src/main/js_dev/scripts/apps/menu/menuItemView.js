/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    MainMenu.Item = Marionette.ItemView.extend({
        tagName: "li",
        template: "#menu-item",
        model: AthenaApp.Entities.MenuItem
    });
});