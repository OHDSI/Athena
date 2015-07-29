/**
 * Created by GMalikov on 09.07.2015.
 */
AthenaApp.module("Browser.Vocabularies", function (Vocabularies, AthenaApp, Backbone, Marionette, $, _) {
    Vocabularies.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-vocabularies",
        onShow: function () {
            var self = this;
            var table = this.$el.find("#vocabularies-table").jqGrid({
                url: '../athena-client/getVocabulariesForBrowser',
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
//                width: $('#vocabulariesList').width(),
                width: 617,
                height: 500,
                colModel: [
                    {label: '', name: 'id', index: 'id', key: true, hidden: true},
                    {label: 'Vocabulary', name: 'name', index: 'name', width: 130, key: false, sortable: true, search: false},
                    {label: 'Full name', name: 'fullName', index: 'fullName', width: 455, key: false, sortable: false, search: true}
                ],
                sortname: 'id',
                viewrecords: true,
                sortorder: 'asc',
                rowNum: 20,
                shrinkToFit: false,
                pager: '#vocabularies-pager',
                beforeSelectRow: function (rowid) {
                    var expanded = jQuery("td.sgexpanded", "#vocabularies-table")[0];
                    if (expanded) {
                        setTimeout(function () {
                            $(expanded).trigger("click");
                        }, 100);
                    }
                    if ($(this).jqGrid("getGridParam", "selrow") === rowid) {
                        $(this).jqGrid("resetSelection");
                        self.trigger("browser:vocabulary:deselected");
                        return false;
                    } else {
                        self.trigger("browser:vocabulary:selected", $(this).jqGrid('getCell', rowid, 'name'), true);
                        return true;
                    }
                },
                subGrid: true,
                subGridBeforeExpand: function (divid, rowid) {
                    var expanded = jQuery("td.sgexpanded", "#vocabularies-table")[0];
                    if (expanded) {
                        setTimeout(function () {
                            $(expanded).trigger("click");
                        }, 100);
                    }
                    if ($(this).jqGrid("getGridParam", "selrow") !== rowid) {
                        $(this).jqGrid('setSelection', rowid, true);
                        self.trigger("browser:vocabulary:selected", $(this).jqGrid('getCell', rowid, 'name'));
                    }
                },
                subGridRowExpanded: function (subgrid_id, row_id) {
                    var subgrid_table_id;
                    var pager_id;
                    var vocabularyId = $("#vocabularies-table").jqGrid('getCell', row_id, 'name');
                    subgrid_table_id = subgrid_id.replace(/ /g, "_") + "t";
                    pager_id = "p_" + subgrid_table_id;
                    $("#" + subgrid_id).html("<table id='" + subgrid_table_id + "' class='scroll'></table><div id='" + pager_id + "' class='scroll'></div>");
                    $("#" + subgrid_table_id).jqGrid({
                        url: '../athena-client/getDomainsForBrowser',
                        datatype: 'json',
                        mtype: 'GET',
                        loadonce: true,
                        rowNum: 10,
                        height: '100%',
                        colNames: ['Domain', 'Concepts count'],
                        colModel: [
                            {name: 'domain', index: 'domain', width: 130, key: true},
                            {name: 'conceptCount', index: 'conceptCount', width: 150}
                        ],
                        sortName: 'domain',
                        sortorder: 'asc',
                        postData: {
                            vocabularyId: vocabularyId
                        },
                        pager: pager_id,
                        beforeSelectRow: function (rowid) {
                            if ($(this).jqGrid("getGridParam", "selrow") === rowid) {
                                $(this).jqGrid("resetSelection");
                                self.trigger("browser:domain:deselected");
                                return false;
                            } else {
                                self.trigger("browser:domain:selected", $(this).jqGrid('getCell', rowid, 'domain'));
                                return true;
                            }
                        }
                    });

                },
                subGridRowColapsed: function (subgrid_id, row_id) {
                    self.trigger("browser:domain:deselected");
                }
            });
            $('#vocabularies-table').jqGrid('filterToolbar', {searchOnEnter: true, searchOperators: false, search: true});
        }
    });
});