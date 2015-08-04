/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function (Log, AthenaApp, Backbone, Marionette, $, _) {
    Log.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-message-log",
        searchFilter: "All",
        events: {
            "click #filter_all_logs": function(){ this.changeFilter("All")},
            "click #filter_errors_only_logs": function(){ this.changeFilter("Errors")},
            "click #filter_build_only_logs": function(){ this.changeFilter("Successful")}
        },

        changeFilter: function(filterStatus){
            this.searchFilter = filterStatus;
            $("#build_log_table").setGridParam({datatype:'json', page:1}).trigger("reloadGrid");
        },
        onShow: function () {
            var self = this;
            var cellatrWordWrap = function (rowId, tv, rawObject, cm, rdata) {
                return 'style="white-space: normal;'
            };
            var table = this.$el.find('#build_log_table').jqGrid({
                url: '../athena-client/getLogForVocabulary',
                datatype: 'json',
                mtype: 'GET',
                loadonce: true,
                width: 625,
                height: 500,
                rowNum: 21,
                colModel: [
                    {label: '', name: 'id', index: 'id', hidden: true, key: true},
                    {label: 'Date', name: 'opStart', index: 'opStart', width: 130, sortable: false},
                    {label: 'Description', name: 'opDescription', index: 'opDescription', width: 450, sortable: false, cellattr: cellatrWordWrap},
                    {label: '', name: 'opStatus', index: 'opStatus', hidden: true}
                ],
                sortName: 'opStart',
                sortorder: 'asc',
                shrinkToFit: true,
                pager: '#build_log_pager',
                postData: {
                    vocabularyId: self.options.vocabularyId,
                    filter: function () {
                        return self.searchFilter
                    }
                },
                rowattr: function (rowData, currentObj, rowId) {
                    if (rowData.opStatus == 1) {
                        return {"class": "success"};
                    } else if (rowData.opStatus == 0 || rowData.opStatus == -1) {
                        return {"class": "info"};
                    } else if (rowData.opStatus == 2) {
                        return {"class": "warning"};
                    } else if (rowData.opStatus == 3) {
                        return {"class": "danger"};
                    }
                }
            });

//            setInterval(function(){
//                var currentPage = $(table).getGridParam('page');
//                $(table).setGridParam({datatype:'json', page:currentPage}).trigger("reloadGrid");
//            }, 5000);
        }
    });
});