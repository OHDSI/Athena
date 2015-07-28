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
                width: $('#vocabulariesList').width(),
                height: 'auto',
                colModel: [
                    {label: '', name: 'id', index:'id', key: true, hidden: true},
                    {label: 'Vocabulary', name: 'name', index: 'name', width: 130, key: false, sortable: true, search: false},
                    {label: 'Full name', name: 'fullName', index: 'fullName', width: 500, key: false, sortable: false, search: true}
                ],
                sortname: 'id',
                viewrecords: true,
                sortorder: 'asc',
                rowNum: 10,
                shrinkToFit: false,
                pager: '#vocabularies-pager',
                beforeSelectRow: function(rowid){
                    if ($(this).jqGrid("getGridParam", "selrow") === rowid) {
                        $(this).jqGrid("resetSelection");
                        return false;
                    } else {
                        return true;
                    }
                },
                subGrid: true,
                subGridBeforeExpand: function(divid, rowid) {
                    var expanded = jQuery("td.sgexpanded", "#vocabularies-table")[0];
                    if(expanded) {
                        setTimeout(function(){
                            $(expanded).trigger("click");
                        }, 100);
                    }
                },
                subGridRowExpanded: function(subgrid_id, row_id){
                    var subgrid_table_id;
                    var pager_id;
                    var vocabularyId = $("#vocabularies-table").jqGrid('getCell', row_id,'name');
                    subgrid_table_id = subgrid_id.replace(/ /g, "_")+"t";
                    pager_id = "p_"+subgrid_table_id;
                    $("#"+subgrid_id).html("<table id='"+subgrid_table_id+"' class='scroll'></table><div id='"+ pager_id +"' class='scroll'></div>");
                    $("#"+subgrid_table_id).jqGrid({
                        url: '../athena-client/getDomainsForBrowser',
                        datatype: 'json',
                        mtype: 'GET',
                        loadonce: true,
//                        jsonReader: {
//                            root: function (obj) {
//                                return obj;
//                            },
//                            page: function (obj) {
//                                return 1;
//                            },
//                            records: function (obj) {
//                                return obj.length;
//                            },
//                            total: function (obj) {
//                                var result = (obj.length - obj.length%10)/10;
//                                if(obj.length%10 > 0){
//                                    result = result + 1;
//                                }
//                                return result;
//                            }
//                        },
                        rowNum: 10,
                        height: '100%',
                        colNames: ['Domain', 'Concepts count'],
                        colModel:[
                            {name: 'domain', index:'domain', width:130, key: true},
                            {name: 'conceptCount', index:'conceptCount', width:150}
                        ],
                        sortName: 'domain',
                        sortorder: 'asc',
                        postData:{
                            vocabularyId: vocabularyId
                        },
                        pager: pager_id
                    });

                }
            });
            $('#vocabularies-table').jqGrid('filterToolbar', {searchOnEnter: true, searchOperators: false, search: true});

//            $('#vocabularies-table tbody').on('click', 'tr', function(){
//                var data;
//                if($(this).hasClass('info')){
//                    $(this).removeClass('info');
//                    self.trigger("browser:vocabulary:deselected");
//                } else {
//                    table.$('tr.info').removeClass('info');
//                    $(this).addClass('info');
//                    data = table.row($(this)).data();
//                    self.trigger("browser:vocabulary:selected", data.shortName);
//                }
//            });

        }
    });
});