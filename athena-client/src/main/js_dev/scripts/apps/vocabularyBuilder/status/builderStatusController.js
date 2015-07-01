/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.List", function (List, AthenaApp, Backbone, Marionette, $, _) {
    List.Controller = {
        listVocabularyStatuses: function () {

            var vocabularyBuilderLayout = new AthenaApp.VocabularyBuilder.Layout.Main();

            var vocabularyStatusesView = new AthenaApp.VocabularyBuilder.Status.Show();

            vocabularyBuilderLayout.on("show", function () {
                vocabularyBuilderLayout.statusTableRegion.show(vocabularyStatusesView);
            });

            vocabularyStatusesView.on("buildVocabulary", function (vocabularyId) {
                AthenaApp.request("vocabStatus:buildVocabulary", vocabularyId);
            });

            vocabularyStatusesView.on("childview:vocabStatus:show", function (childView, model) {
                AthenaApp.VocabularyBuilder.Log.Controller.showVocabularyLog(vocabularyBuilderLayout.logTableRegion, model);
//                AthenaApp.VocabularyBuilder.Info.Controller.showVocabularyInfo(vocabularyBuilderLayout.vocabularyInfoRegion, model);
            });

            vocabularyStatusesView.on("showLog", function(vocabularyId){
                AthenaApp.VocabularyBuilder.Log.Controller.showVocabularyLog(vocabularyBuilderLayout.logTableRegion, vocabularyId);
                AthenaApp.VocabularyBuilder.Info.Controller.showVocabularyInfo(vocabularyBuilderLayout.vocabularyInfoRegion, vocabularyId);
            });

            AthenaApp.mainRegion.show(vocabularyBuilderLayout);

        }
    }
});
