/**
 * Created by GMalikov on 08.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Info", function (Info, AthenaApp, Backbone, Marionette, $, _) {
    Info.Controller = {
        showVocabularyInfo: function (region, vocabularyId) {
            var loadingView = new AthenaApp.Common.Views.Loading();
            region.show(loadingView);
            var fetchingInfo = AthenaApp.request("vocabInfo:getById", vocabularyId);

            $.when(fetchingInfo).done(function (info) {
                var vocabularyInfoView = new AthenaApp.VocabularyBuilder.Info.Show({
                    model: info
                });
                region.show(vocabularyInfoView);
            });
        }
    }
});