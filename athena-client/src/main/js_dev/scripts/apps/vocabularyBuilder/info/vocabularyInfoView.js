/**
 * Created by GMalikov on 08.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Info", function(Info, AthenaApp, Backbone, Marionette, $, _){
    Info.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocabulary-info-template",
        className: "panel panel-primary"
    });
});