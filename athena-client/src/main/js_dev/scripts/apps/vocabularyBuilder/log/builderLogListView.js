/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function(Log, AthenaApp, Backbone, Marionette, $, _){
    Log.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-message-log",

        onShow: function(){
            var self = this;
            var table = this.$el.find('#build_log_table').DataTable({
                "ajax":{
                    "url": "../athena-client/getLogForVocabulary",
                    "type": "GET",
                    "dataSrc": "",
                    "data":{
                        "vocabularyId": self.options.vocabularyId
                    }
                },
                "searching": false,
                "ordering": false,
                "stateSave": true,
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
                "pagingType": "simple",
                "createdRow": function(row, data, dataIndex){
                    if((data.opNumber == 999) || (data.opNumber == 0)){
                        $(row).addClass('success');
                    } else {
                        $(row).addClass('info');
                    }
                }
            });
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