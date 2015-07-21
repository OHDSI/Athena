/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser.Main", function(Main, AthenaApp, Backbone, Marionette, $, _){
    Main.Controller = {
        showMainLayout: function(){
            var browserLayout = new AthenaApp.Browser.Layout.Main();

            var vocabulariesView = new AthenaApp.Browser.Vocabularies.View();

            var domainsView = new AthenaApp.Browser.Domains.View();

            var conceptsView = new AthenaApp.Browser.Concepts.View();

            vocabulariesView.on("browser:vocabulary:selected", function(vocabularyId){
                AthenaApp.Browser.setCurrentVocabulary(vocabularyId);
            });

            vocabulariesView.on("browser:vocabulary:deselected", function(){
                AthenaApp.Browser.setCurrentVocabulary(null);
            });

            domainsView.on("browser:domain:selected", function(domainId){
                AthenaApp.Browser.setCurrentDomain(domainId);
            });

            domainsView.on("browser:domain:deselected", function(){
                AthenaApp.Browser.setCurrentDomain(null);
            });

            browserLayout.on("show", function(){
                browserLayout.vocabulariesRegion.show(vocabulariesView);
                browserLayout.domainsRegion.show(domainsView);
                browserLayout.conceptsRegion.show(conceptsView);
            });

            AthenaApp.mainRegion.show(browserLayout);
        }
    }
});