/**
 * Created by GMalikov on 21.07.2015.
 */
AthenaApp.module("Browser.Relations", function (Relations, AthenaApp, Backbone, Marionette, $, _) {
    Relations.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-relations",
        onShow: function () {
            var self = this;
            var cellatrWordWrap = function (rowId, tv, rawObject, cm, rdata) {
                return 'style="white-space: normal;'
            };
            var table = this.$el.find("#relations-table").jqGrid({
                url: '../athena-client/getConceptRelationsForBrowser',
                datatype: 'json',
                mtype: 'GET',
                jsonReader: {
                    root: function (obj) {
                        return obj.data
                    },
                    page: function (obj) {
                        return obj.page
                    },
                    records: function (obj) {
                        return obj.records
                    },
                    total: function (obj) {
                        return obj.totalPages
                    }
                },
                width: $("#relationsList").width(),
                height: 315,
                colModel: [
                    {label: 'Relation name', name: 'relationName', index: 'relationName', width: 144, search: false, sortable: true, cellattr: cellatrWordWrap},
                    {label: 'Related concept', name: 'conceptName', index: 'conceptName', width: 455, search: true, sortable: false, cellattr: cellatrWordWrap}
                ],
                sortname: 'relationName',
                viewrecords: true,
                sortorder: 'asc',
                rowNum: 10,
                shrinkToFit: false,
                pager: "#relations-pager",
                postData: {
                    conceptId: function () {
                        return AthenaApp.Browser.getCurrentConcept();
                    }
                }
            });

            $(table).jqGrid('filterToolbar', {searchOnEnter: true, searchOperators: false, search: true});

            AthenaApp.Browser.on("browser:concept:changed", function () {
                $(table).trigger("reloadGrid");
            });
        }
    });
});