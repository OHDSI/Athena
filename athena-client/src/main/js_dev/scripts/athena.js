/**
 * Created by GMalikov on 27.04.2015.
 */
var AthenaApp = new Backbone.Marionette.Application();

AthenaApp.addRegions({
    mainRegion: "#mainRegion",
    logRegion: "#logRegion"
});

AthenaApp.navigate = function(route, options){
    options || (options = {});
    Backbone.history.navigate(route, options);
};

AthenaApp.getCurrentRoute = function(){
    return Backbone.history.fragment;
};

AthenaApp.on('start', function(){
    console.log("Athena has started!");
    if(Backbone.history){
        Backbone.history.start();

        if(this.getCurrentRoute() === ""){
            AthenaApp.trigger("builder:listStatus");
        }
    }

});





