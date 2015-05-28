/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.Layout", function(Layout, AthenaApp, Backbone, Marionette, $, _){
    Layout.Main = Backbone.Marionette.LayoutView.extend({
        template: "#vocab-builder-layout",

        regions: {
            statusTableRegion: "#vocabularyStatusTable",
            logTableRegion: "#vocabularyLogTable",
            vocabularyInfoRegion: "#vocabularyInfo"
        }
    });
});