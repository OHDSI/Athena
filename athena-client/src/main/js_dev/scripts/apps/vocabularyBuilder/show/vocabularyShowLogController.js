/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Show", function(Show, AthenaApp, Backbone, Marionette, $, _){
    Show.Controller = {
        showVocabularyLog: function(region, model){
            var logMessages = AthenaApp.request("logMessages:entities");

            var logMessagesView = new AthenaApp.VocabularyBuilder.Show.LogCollection({
                collection: logMessages,
                vocabulary: model.get('name')
            });

            region.show(logMessagesView);
        }
    }
});