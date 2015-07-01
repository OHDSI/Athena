/**
 * Created by GMalikov on 26.06.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.VocabInfo = Backbone.Model.extend({
        urlRoot: "../athena-client/getVocabularyInfo",
        idAttribute: "id"
//        defaults:{
//            "recordsCount" : 0,
//            "domainsCount" : 0,
//            "conceptsCount": 0,
//            "relationsCount" : 0,
//            "sourceName" : "",
//            "lastUpdated" : ""
//        }
    });

    var API = {
        getVocabularyInfo: function(vocabularyId){
            var vocabInfo = new Entities.VocabInfo();
            var defer = $.Deferred();
            vocabInfo.fetch({
                data: $.param({id: vocabularyId}),
                success: function(data){
                    defer.resolve(data);
                },
                failure: function(data, error){
                    alert(error.message);
                }
            });

            return defer.promise();
        }
    };

    AthenaApp.reqres.setHandler("vocabInfo:getById", function(vocabularyId){
        return API.getVocabularyInfo(vocabularyId);
    });
});