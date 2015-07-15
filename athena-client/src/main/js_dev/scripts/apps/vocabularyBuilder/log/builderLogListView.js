/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function(Log, AthenaApp, Backbone, Marionette, $, _){
    Log.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-message-log",
        searchFilter: "All",
        ui:{
            "filterAll" : "#filter_all_logs",
            "filterErrorsOnly" : "#filter_errors_only_logs",
            "filterBuildOnly" : "#filter_build_only_logs"
        },
        events: {
            "click @ui.filterAll" : "setFilterAll",
            "click @ui.filterErrorsOnly" : "setFilterErrorsOnly",
            "click @ui.filterBuildOnly" : "setFilterBuildOnly"
        },
        setFilterAll: function(){
            this.searchFilter = "All";
            $('#build_log_table').dataTable().api().ajax.reload(null, false);
        },
        setFilterErrorsOnly: function(){
            this.searchFilter = "Errors";
            $('#build_log_table').dataTable().api().ajax.reload(null, false);
        },
        setFilterBuildOnly: function(){
            this.searchFilter = "Successful";
            $('#build_log_table').dataTable().api().ajax.reload(null, false);
        },
        onShow: function(){
            var self = this;
            var table = this.$el.find('#build_log_table').DataTable({
                "ajax":{
                    "url": "../athena-client/getLogForVocabulary",
                    "type": "GET",
                    "dataSrc": "",
                    "data":{
                        "vocabularyId": self.options.vocabularyId,
                        filter: function(){ return self.searchFilter}
                    }
                },
                "searching": false,
                "ordering": false,
                "columnDefs":[
                    {
                        "targets": "vocab_log_id",
                        "data": "id",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab_log_opStart",
                        "data": "opStart",
                        "searchable": false,
                        "sortable": false,
                        "visible": true
                    },
                    {
                        "targets": "vocab_log_opNumber",
                        "data": "opNumber",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab_log_opDescription",
                        "data": "opDescription",
                        "searchable": false,
                        "sortable": false,
                        "visible": true
                    },
                    {
                        "targets": "vocab_log_opEnd",
                        "data": "opEnd",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab_log_opStatus",
                        "data": "opStatus",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab_log_opDetail",
                        "data": "opDetail",
                        "searchable": false,
                        "visible": false
                    }
                ],
                "paging": false,
                "scrollY": "500px",
                "scrollCollapse": true,
                stateSave: false,
                "createdRow": function(row, data, dataIndex){
                    if(data.opStatus == 1){
                        $(row).addClass('success');
                    } else if(data.opStatus == 0 || data.opStatus == -1) {
                        $(row).addClass('info');
                    }else if(data.opStatus == 2){
                        $(row).addClass('warning');
                    } else if(data.opStatus == 3){
                        $(row).addClass('danger');
                    }
                }
            });

//            setInterval(function(){
//                table.ajax.reload(null, false);
//            }, 5000);
        }
    });

//    Log.List = Marionette.CompositeView.extend({
//        tagName: "table",
//        className: "table table-condensed",
//        template: "#message-log-list",
//        childView: Log.Show,
//        itemViewContainer: "tbody",
//
//        templateHelpers: function(){
//            var vocabulary = this.options.vocabulary;
//            return{
//                vocabulary: function(){
//                    if(vocabulary === undefined){
//                        return "";
//                    } else {
//                        return vocabulary;
//                    }
//                }
//            }
//        }
//    });
});