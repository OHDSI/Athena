/**
 * Created by GMalikov on 20.07.2015.
 */
AthenaApp.module("Browser.Concepts", function(Concepts, AthenaApp, Backbone, Marionette, $, _){
    Concepts.View = Marionette.ItemView.extend({
        tagName: "div",
        template: "#browser-concepts",
        onShow: function(){
            var self = this;
            var table = this.$el.find("#concepts-table").DataTable({
                serverSide: true,
                pagingType: 'simple',
                bLengthChange: false,
                ajax:{
                    url: '../athena-client/getConceptsForBrowser',
                    type: 'GET',
                    data:{
                        vocabularyId: function(){return AthenaApp.Browser.getCurrentVocabulary();},
                        domainId: function(){return AthenaApp.Browser.getCurrentDomain();}
                    }
                },
                columnDefs:[
                    {
                        targets: "concept-id",
                        data: "id",
                        searchable: false,
                        visible: false
                    },
                    {
                        targets: "concept-name",
                        data: "name",
                        searchable: true,
                        visible: true
                    }
                ]
            });

            $('#concepts-table tbody').on('click', 'tr', function(){
                var data;
                if($(this).hasClass('info')){
                    $(this).removeClass('info');
                    self.trigger("browser:concept:deselected");
                } else {
                    table.$('tr.info').removeClass('info');
                    $(this).addClass('info');
                    data = table.row($(this)).data();
                    self.trigger("browser:concept:selected", data.id);
                }
            });

            AthenaApp.Browser.on("browser:domain:changed", function(){
                table.ajax.reload(null,false);
            });
        }
    });
});