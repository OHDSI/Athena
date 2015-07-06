/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    MainMenu.Main = Marionette.CompositeView.extend({
        tagName: "nav",
        className: "navbar navbar-inverse",
        template: "#main-menu",
        childView: MainMenu.Item,
        childViewContainer: "#menuContainer"
    });
});