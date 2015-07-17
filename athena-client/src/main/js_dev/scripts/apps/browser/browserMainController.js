/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser.Main", function(Main, AthenaApp, Backbone, Marionette, $, _){
    Main.Controller = {
        showMainLayout: function(){
            var browserLayout = new AthenaApp.Browser.Layout.Main();

            var browseVocabularies = new AthenaApp.Browser.Vocabularies.View();

            var browseDomains = new AthenaApp.Browser.Domains.View();

            browseVocabularies.on("browser:vocabulary:selected", function(vocabularyId){
                AthenaApp.Browser.setCurrentVocabulary(vocabularyId);
                console.log("Current vocabulary selected: " + AthenaApp.Browser.currentVocabulary);
            });

            browseVocabularies.on("browser:vocabulary:deselected", function(){
                AthenaApp.Browser.setCurrentVocabulary(null);
                console.log("Current vocabulary deselected.");
            });

            browserLayout.on("show", function(){
                browserLayout.vocabulariesRegion.show(browseVocabularies);
                browserLayout.domainsRegion.show(browseDomains);
            });

            AthenaApp.mainRegion.show(browserLayout);
        }
    }
});