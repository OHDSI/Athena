/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function(Log, AthenaApp, Backbone, Marionette, $, _){
    Log.Controller = {
        showVocabularyLog: function(region, model){
            var logMessages = AthenaApp.request("logMessages:entities");

            var logMessagesView = new AthenaApp.VocabularyBuilder.Log.List({
                collection: logMessages,
                vocabulary: model.get('name')
            });

            region.show(logMessagesView);
        }
    }
});