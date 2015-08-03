/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.Status", function (Status, AthenaApp, Backbone, Marionette, $, _) {
    Status.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-list",
        searchFilter: "All",
        events:{
            "click #filter_all_vocabularies" : function(){ this.changeFilter("All")},
            "click #filter_available_vocabularies" : function(){ this.changeFilter("Available")},
            "click #filter_unavailable_vocabularies" : function(){ this.changeFilter("Unavailable")},
            "click #filter_ready_vocabularies" : function(){ this.changeFilter("Ready")},
            "click #filter_failed_vocabularies" : function(){ this.changeFilter("Failed")}
        },

        changeFilter: function(filterStatus){
            this.searchFilter = filterStatus;
            $("#status_table").setGridParam({datatype:'json', page:1}).trigger("reloadGrid");
        },
        onShow: function () {
            var self = this;
            var table = this.$el.find("#status_table").jqGrid({
                url: '../athena-client/getVocabularyStatuses',
                datatype: 'json',
                mtype: 'GET',
                loadonce: true,
                rowNum: 21,
                width: 580,
                height: 500,
                colModel:[
                    {label: 'Name', name: 'id', index:'id', width: 130, key:true, sortable: true, search: true},
                    {label: '', name: 'status', index: 'status', hidden: true},
                    {label: 'Status', name: 'statusName', index:'statusName', width: 130, sortable: true, search: false},
                    {label: 'Controls', name: 'controls', width: 130, sortable: false, search: false, align: 'center', formatter:formatControls }
                ],
                sortName: 'id',
                sortorder: 'asc',
                shrinkToFit: true,
                postData:{
                    filter: function(){return self.searchFilter}
                },
                pager: '#status_table_pager',
                rowattr: function(rowData, currentObj, rowId){
                    if(rowData.status === "0"){
                        return {"class": "info"};
                    } else if(rowData.status === "1"){
                        return {"class": "success"};
                    } else if(rowData.status === "2"){
                        return {"class": "warning"};
                    } else if(rowData.status === "3"){
                        return {"class": "danger"};
                    }
                }
            });
            $('#status_table').jqGrid('filterToolbar', {searchOnEnter: true, searchOperators: false, search: true});

            function formatControls(cellValue, options, rowObject){
                var html = "<button type='button' id='info_" + rowObject.id + "' class='btn btn-xs showLog'>" +
                            "<span class='glyphicon glyphicon-eye-open'></span>View log" +
                            "</button> &nbsp;" +
                            "<button type='button' id='build_" + rowObject.id + "' class='btn btn-xs build'>" +
                            "<span class='glyphicon glyphicon-cog'></span>Build" +
                            "</button>";
                return html;
            }
//            setInterval(function(){
//                var currentPage = $(table).getGridParam('page');
//                $(table).setGridParam({datatype:'json', page:currentPage}).trigger("reloadGrid");
//            }, 5000);
            var status_tbody = $('#status_table tbody');
            status_tbody.on('click', '.showLog', function(){
                self.trigger("showLog", $(this).attr('id').split("_")[1]);
            });

            status_tbody.on('click', '.build', function(){
                var rowId = $(this).attr('id').split("_")[1];
                var currentStatus = $(table).jqGrid('getCell', rowId, 'status');
                if(currentStatus === "1" || currentStatus === "2" || currentStatus === "3"){
                    self.trigger("buildVocabulary", rowId);
                    var rowData = $(table).jqGrid('getRowData', rowId);
                    rowData.status = "0";
                    rowData.statusName = "Build in progress";
                    $(table).jqGrid('setRowData', rowId, rowData);
                    alert("Starting to build vocabulary: " + rowId);
                } else if(currentStatus === "4"){
                    alert(rowId + " vocabulary currently unavailable.");
                } else if(currentStatus === "0"){
                    alert(rowId + " vocabulary currently is being build.");
                }
            });
        }
    });
});