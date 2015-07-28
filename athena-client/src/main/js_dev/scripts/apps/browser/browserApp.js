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
        browser: function(){
            Browser.Main.Controller.showMainLayout();
            AthenaApp.execute("set:active:menu", "browser");
        }
    };

    AthenaApp.on("browser:list", function(){
        AthenaApp.navigate("browser");
        API.browser();
    });
    AthenaApp.addInitializer(function(){
        new Browser.Router({
            controller: API
        });
    });

    Browser.currentVocabulary = null;
    Browser.currentDomain = null;
    Browser.currentConcept = null;

    Browser.setCurrentVocabulary = function(vocabulary){
        Browser.currentVocabulary = vocabulary;
        Browser.setCurrentDomain(null);
        Browser.trigger("browser:vocabulary:changed");
        console.log("Current vocabulary is: " + vocabulary);
    };

    Browser.getCurrentVocabulary = function(){
        return Browser.currentVocabulary;
    };

    Browser.setCurrentDomain = function(domain){
        Browser.currentDomain = domain;
        Browser.setCurrentConcept(null);
        Browser.trigger("browser:domain:changed");
        console.log("Current domain is: " + domain);
    };

    Browser.getCurrentDomain = function(){
        return Browser.currentDomain;
    };

    Browser.setCurrentConcept = function(concept){
        Browser.currentConcept = concept;
        Browser.trigger("browser:concept:changed");
    };

    Browser.getCurrentConcept = function(){
        if(Browser.currentConcept == null){
            return -1;
        } else {
            return Browser.currentConcept;
        }
    };
});