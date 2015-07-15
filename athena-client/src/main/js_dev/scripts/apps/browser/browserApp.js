/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser", function(Browser, AthenaApp, Backbone, Marionette, $, _){
    Browser.Router = Marionette.AppRouter.extend({
        appRoutes:{
            "browser" : "browser"
        }
    });

    var API = {
        vocabularyBrowser: function(){
            Browser.Main.Controller.showMainLayout();
            AthenaApp.execute("set:active:menu", "browser");
        }
    };

    AthenaApp.on("browser:list", function(){
        AthenaApp.navigate("browser");
        API.vocabularyBrowser();
    });
    AthenaApp.addInitializer(function(){
        new Browser.Router({
            controller: API
        });
    });
});