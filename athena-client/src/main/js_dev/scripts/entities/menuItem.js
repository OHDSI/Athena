/**
 * Created by GMalikov on 02.07.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.MenuItem = Backbone.Model.extend({});

    Entities.MenuCollection = Backbone.Collection.extend({
        url: "../athena-client/getMenuItems",
        model: Entities.MenuItem
    });

    var API ={
        getMenuItems: function(){
            var menuItems = new Entities.MenuCollection();
            var defer = $.Deferred();
            menuItems.fetch({
                success: function(data){
                    defer.resolve(data);
                }
            });
            return defer.promise();
        }
    };
    AthenaApp.reqres.setHandler("menuItems", function(){
        return API.getMenuItems();
    });
});