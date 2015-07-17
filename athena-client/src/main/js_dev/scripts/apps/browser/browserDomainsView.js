/**
 * Created by GMalikov on 16.07.2015.
 */
AthenaApp.module("Browser.Domains", function(Domains, AthenaApp, Backbone, Marionette, $, _){
    Domains.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-domains",
        onShow: function(){
            var self = this;
            var table = this.$el.find("#domains-table").DataTable({
                pagingType: 'simple',
                bLengthChange: false,
                ajax:{
                    url: '../athena-client/getDomainsForBrowser',
                    type: "GET",
                    dataSrc: "",
                    data:{
                        vocabularyId: function(){return AthenaApp.Browser.getCurrentVocabulary();}
                    }
                },
                columnDefs:[
                    {
                        targets: "domain-id",
                        data: "id",
                        searchable: false,
                        visible: true
                    },
                    {
                        targets: "domain-concepts-count",
                        data: "conceptCount",
                        searchable: true,
                        visible: true
                    }
                ]
            });

            AthenaApp.Browser.on("browser:vocabulary:changed", function(){
                console.log("Current vocabulary: " + AthenaApp.Browser.getCurrentVocabulary());
                table.ajax.reload(null, false);
            });
        }
    });
});