/**
 * Created by GMalikov on 21.07.2015.
 */
AthenaApp.module("Browser.Relations", function (Relations, AthenaApp, Backbone, Marionette, $, _) {
    Relations.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-relations",
        onShow: function () {
            var self = this;
            var table = this.$el.find("#relations-table").DataTable({
                serverSide: true,
                pagingType: 'simple',
                bLengthChange: false,
                ajax: {
                    url: '../athena-client/getConceptRelationsForBrowser',
                    type: "GET",
                    data: {
                        conceptId: function(){return AthenaApp.Browser.getCurrentConcept();}
                    }
                },
                columnDefs: [
                    {
                        "targets": "relation-name",
                        "data": "relationName",
                        "searchable": true,
                        "visible": true
                    },
                    {
                        "targets": "related-concept-name",
                        "data": "conceptName",
                        "searchable": true,
                        "visible": true
                    }
                ]
            });

            $('#relations-table tbody').on('click', 'tr', function () {
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