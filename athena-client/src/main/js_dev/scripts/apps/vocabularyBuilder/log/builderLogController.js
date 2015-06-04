/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function(Log, AthenaApp, Backbone, Marionette, $, _){
    Log.Controller = {
        showVocabularyLog: function(region, vocabularyId){
//            var logMessages = AthenaApp.request("logMessages:entities");

            var logMessagesView = new AthenaApp.VocabularyBuilder.Log.Show({
                vocabularyId: vocabularyId
            });

            region.show(logMessagesView);
        }
    }
});