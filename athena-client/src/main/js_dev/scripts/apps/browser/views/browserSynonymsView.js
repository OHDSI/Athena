/**
 * Created by GMalikov on 22.07.2015.
 */
AthenaApp.module("Browser.Synonyms", function(Synonyms, AthenaApp, Backbone, Marionette, $, _){
    Synonyms.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-synonyms",
        onShow: function(){
            var self = this;
            var table = this.$el.find("#synonyms-table").DataTable({
                serverSide: true,
                pagingType: 'simple',
                bLenghtChange: false,
                ajax:{
                    url: '../athena-client/getSynonymsForBrowser',
                    type: 'GET',
                    data:{
                        conceptId: function(){return AthenaApp.Browser.getCurrentConcept();}
                    }
                },
                columnDefs:[
                    {
                        targets: "synonym-name",
                        data: "name",
                        searchable: true,
                        visible: true
                    },
                    {
                        targets: "synonym-language",
                        data: "language",
                        searchable: false,
                        visible: true
                    }
                ]
            });

            $('#synonyms-table tbody').on('click', 'tr', function () {
                if ($(this).hasClass('info')) {
                    $(this).removeClass('info');
                } else {
                    table.$('tr.info').removeClass('info');
                    $(this).addClass('info');
                }
            });

            AthenaApp.Browser.on("browser:concept:changed", function(){
                table.ajax.reload(null,false);
            });
        }
    });
});