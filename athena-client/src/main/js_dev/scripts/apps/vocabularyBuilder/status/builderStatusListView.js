/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.Status", function (Status, AthenaApp, Backbone, Marionette, $, _) {
    Status.Show = Marionette.ItemView.extend({
        tagName: "div",
        template: "#vocab-list",

        onShow: function () {
            this.$el.find('#status_table').DataTable({
                "ajax": {
                    "url": "../athena-client/getVocabularyStatuses",
                    "type": "GET",
                    "dataSrc": ""
                },
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
                        "defaultContent": "<button type='button' class='btn btn-xs'><span class='glyphicon glyphicon-eye-open'></span>View log</button>",
                        "sortable": false
                    }
                ],
                "pagingType": "simple"
            });
        }
    });
});