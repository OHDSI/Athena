/**
 * Created by GMalikov on 20.07.2015.
 */
AthenaApp.module("Browser.Concepts", function (Concepts, AthenaApp, Backbone, Marionette, $, _) {
    Concepts.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-concepts",
        onShow: function () {
            var self = this;
            var table = this.$el.find("#concepts-table").jqGrid({
                url: '../athena-client/getConceptsForBrowser',
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
//                width: $("#conceptsList").width(),
                width: 617,
                height: 500,
                colModel: [
                    {label: '', name: 'id', index: 'id', key: true, hidden: true},
                    {label: 'Concept', name: 'name', index: 'name', width: 613, key: false, sortable: true, search: true,
                        cellattr: function (rowId, tv, rawObject, cm, rdata) {
                            return 'style="white-space: normal;'
                        }
                    }
                ],
                sortname: 'name',
                viewrecords: true,
                sortorder: 'asc',
                rowNum: 20,
                shrinkToFit: false,
                pager: "#concepts-pager",
                postData: {
                    vocabularyId: function () {
                        return AthenaApp.Browser.getCurrentVocabulary();
                    },
                    domainId: function () {
                        return AthenaApp.Browser.getCurrentDomain();
                    }

                }
            });

            $('#concepts-table').jqGrid('filterToolbar', {searchOnEnter: true, searchOperators: false, search: true});

            AthenaApp.Browser.on("browser:domain:changed", function () {
                $("#concepts-table").trigger("reloadGrid");
            });
        }
    });
});