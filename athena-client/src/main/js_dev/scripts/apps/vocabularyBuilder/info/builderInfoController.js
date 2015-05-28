/**
 * Created by GMalikov on 08.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Info", function(Info, AthenaApp, Backbone, Marionette, $, _){
    Info.Controller = {
        showVocabularyInfo: function(region, model){
            var vocabularyInfoView = new AthenaApp.VocabularyBuilder.Info.Show({
                model: model
            });

            region.show(vocabularyInfoView);
        }
    }
});