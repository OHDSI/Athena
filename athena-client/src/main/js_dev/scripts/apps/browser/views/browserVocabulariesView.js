/**
 * Created by GMalikov on 09.07.2015.
 */
AthenaApp.module("Browser.Vocabularies", function (Vocabularies, AthenaApp, Backbone, Marionette, $, _) {
    Vocabularies.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-vocabularies",
        onShow: function () {
            var self = this;
            var table = this.$el.find("#vocabularies-table").DataTable({
                scrollX: true,
                serverSide: true,
                pagingType: 'simple',
                bLengthChange: false,
                ajax:{
                    url: '../athena-client/getVocabulariesForBrowser',
                    type: "GET",
                    data:{
                        filterOptions: "some value"
                    }
                },
                columnDefs:[
                    {
                        "targets": "vocab-short-name",
                        "data": "shortName",
                        "searchable": true,
                        "visible": true
                    },
                    {
                        "targets": "vocab-full-name",
                        "data": "fullName",
                        "searchable": false,
                        "visible": true
                    }
                ]
            });

            $('#vocabularies-table tbody').on('click', 'tr', function(){
                var data;
                if($(this).hasClass('info')){
                    $(this).removeClass('info');
                    self.trigger("browser:vocabulary:deselected");
                } else {
                    table.$('tr.info').removeClass('info');
                    $(this).addClass('info');
                    data = table.row($(this)).data();
                    self.trigger("browser:vocabulary:selected", data.shortName);
                }
            });

        }
    });
});