/**
 * Created by GMalikov on 09.07.2015.
 */
AthenaApp.module("Browser.Vocabularies", function (Vocabularies, AthenaApp, Backbone, Marionette, $, _) {
    Vocabularies.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-vocabularies",
        onShow: function () {
            var self = this;
            var table = this.$el.find("#vocabularies-table").DataTable({
                serverSide: true,
                pagingType: 'simple',
                bLengthChange: false,
                ajax:{
                    url: '../athena-client/getVocabulariesForBrowser',
                    type: "GET",
                    data:{
                        filterOptions: "some value"
                    }
                },
                columnDefs:[
                    {
                        "targets": "vocab-short-name",
                        "data": "shortName",
                        "searchable": true,
                        "visible": true
                    },
                    {
                        "targets": "vocab-full-name",
                        "data": "fullName",
                        "searchable": false,
                        "visible": true
                    }
                ]
            });
        }
    });
});