/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser.Main", function(Main, AthenaApp, Backbone, Marionette, $, _){
    Main.Controller = {
        showMainLayout: function(){
            var browserLayout = new AthenaApp.Browser.Layout.Main();

            var browseVocabularies = new AthenaApp.Browser.Vocabularies.View();

            browserLayout.on("show", function(){
                browserLayout.vocabulariesRegion.show(browseVocabularies);
            });

            AthenaApp.mainRegion.show(browserLayout);
        }
    }
});