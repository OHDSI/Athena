/**
 * Created by GMalikov on 08.07.2015.
 */
AthenaApp.module("Browser.Layout", function(Layout, AthenaApp, Backbone, Marionette, $, _){
    Layout.Main = Backbone.Marionette.LayoutView.extend({
        template: "#vocab-browser-layout",
        regions:{
            vocabulariesRegion: "#vocabulariesList",
            domainsRegion: "#domainsList",
            conceptsRegion: "#conceptsList",
            relationsRegion: "#relationsList",
            synonymsRegion: "#synonymsList"
        }
    });
});