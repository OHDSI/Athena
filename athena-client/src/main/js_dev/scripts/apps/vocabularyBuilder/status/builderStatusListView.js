/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.Status", function (Status, AthenaApp, Backbone, Marionette, $, _) {
    Status.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-list",

        onShow: function () {
            var self = this;
            var table = this.$el.find('#status_table').DataTable({
                "ajax": {
                    "url": "../athena-client/getVocabularyStatuses",
                    "type": "GET",
                    "dataSrc": ""
                },
                "stateSave": true,
                "columnDefs": [
                    {
                        "targets": "vocab-list-id",
                        "data": "id",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab-list-name",
                        "data": "name",
                        "searchable": true,
                        "visible": true
                    },
                    {
                        "targets": "vocab-list-status",
                        "data": "status",
                        "searchable": true,
                        "visible": false
                    },
                    {
                        "targets": "vocab-list-statusName",
                        "data": "statusName",
                        "searchable": false,
                        "visible": true
                    },
                    {
                        "targets": "vocab-list-opNumber",
                        "data": "opNumber",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab-list-description",
                        "data": "description",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": "vocab-list-detail",
                        "data": "detail",
                        "searchable": false,
                        "visible": false
                    },
                    {
                        "targets": -1,
                        "data": null,
                        "defaultContent": "<button type='button' class='btn btn-xs showLog'>" +
                            "<span class='glyphicon glyphicon-eye-open'></span>View log" +
                            "</button> &nbsp;" +
                            "<button type='button' class='btn btn-xs build'>" +
                            "<span class='glyphicon glyphicon-cog'></span>Build" +
                            "</button>",
                        "sortable": false
                    }
                ],
                "pagingType": "simple",
                "createdRow": function(row, data, dataIndex){
                    if(data.status == 0){
                        $(row).addClass('info');
                    } else if(data.status == 1){
                        $(row).addClass('success');
                    } else if(data.status == 2){
                        $(row).addClass('warning');
                    } else if(data.status == 3){
                        $(row).addClass('danger');
                    }
                }
            });

            setInterval(function(){
                table.ajax.reload(null, false);
            }, 5000);
            $('#status_table tbody').on('click', '.showLog', function(){
                var data = table.row($(this).parents('tr')[0]).data();
                self.trigger("showLog", data.id);
            });

            $('#status_table tbody').on('click', '.build', function(){
                var data = table.row($(this).parents('tr')[0]).data();
                if(data.status === "1" || data.status === "2" || data.status === "3"){
                    self.trigger("buildVocabulary", data.id);
                }
            });
        }
    });
});