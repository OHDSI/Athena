/**
 * Created by GMalikov on 06.07.2015.
 */
AthenaApp.module("MainMenu", function(MainMenu, AthenaApp, Backbone, Marionette, $, _){
    MainMenu.Item = Marionette.ItemView.extend({
        tagName: "li",
        template: "#menu-item",
        model: AthenaApp.Entities.MenuItem,

        events:{
            "click a": "navigate"
        },

        navigate: function(e){
            e.preventDefault();
            this.trigger("navigate", this.model);
        },

        onRender: function(){
            if(this.model.selected){
                this.$el.addClass("active");
            }
        }
    });
});