/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser.Main", function(Main, AthenaApp, Backbone, Marionette, $, _){
    Main.Controller = {
        showMainLayout: function(){
            var browserLayout = new AthenaApp.Browser.Layout.Main();

            var vocabulariesAndDomainsView = new AthenaApp.Browser.Vocabularies.View();

            var conceptsView = new AthenaApp.Browser.Concepts.View();

            var relationsView = new AthenaApp.Browser.Relations.View();

            var synonymsView = new AthenaApp.Browser.Synonyms.View();

            vocabulariesAndDomainsView.on("browser:vocabulary:selected", function(vocabularyId){
                AthenaApp.Browser.setCurrentVocabulary(vocabularyId);
            });

            vocabulariesAndDomainsView.on("browser:vocabulary:deselected", function(){
                AthenaApp.Browser.setCurrentVocabulary(null);
            });

            vocabulariesAndDomainsView.on("browser:domain:selected", function(domainId){
                AthenaApp.Browser.setCurrentDomain(domainId);
            });

            vocabulariesAndDomainsView.on("browser:domain:deselected", function(){
                AthenaApp.Browser.setCurrentDomain(null);
            });

            conceptsView.on("browser:concept:selected", function(conceptId){
                AthenaApp.Browser.setCurrentConcept(conceptId);
            });

            conceptsView.on("browser:concept:deselected", function(){
                AthenaApp.Browser.setCurrentConcept(null);
            });

            browserLayout.on("show", function(){
                browserLayout.vocabulariesRegion.show(vocabulariesAndDomainsView);
//                browserLayout.domainsRegion.show(domainsView);
                browserLayout.conceptsRegion.show(conceptsView);
//                browserLayout.relationsRegion.show(relationsView);
//                browserLayout.synonymsRegion.show(synonymsView);
            });

            AthenaApp.mainRegion.show(browserLayout);
        }
    }
});