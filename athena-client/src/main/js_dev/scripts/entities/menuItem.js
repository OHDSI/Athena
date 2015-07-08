/**
 * Created by GMalikov on 02.07.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.MenuItem = Backbone.Model.extend({
        initialize: function(){
            var selectable = new Backbone.Picky.Selectable(this);
            _.extend(this, selectable);
        }
    });

    Entities.MenuCollection = Backbone.Collection.extend({
        url: "../athena-client/getMenuItems",
        model: Entities.MenuItem,

        initialize: function(){
            var singleSelect = new Backbone.Picky.SingleSelect(this);
            _.extend(this, singleSelect);
        }
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