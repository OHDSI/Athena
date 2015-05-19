/**
 * Created by GMalikov on 29.04.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.VocabStatus = Backbone.Model.extend({});

    Entities.VocabStatusCollection = Backbone.Collection.extend({
        url: "../athena-client/getVocabularyStatuses",
        model: Entities.VocabStatus
    });

    var API ={
        getVocabStatuses: function(){
            var vocabStatuses = new Entities.VocabStatusCollection();
            var defer = $.Deferred();
            vocabStatuses.fetch({
                success: function(data){
                    defer.resolve(data);
                }
            });
            return defer.promise();
        }
    };

    AthenaApp.reqres.setHandler("vocabStatus:entities", function(){
        return API.getVocabStatuses();
    });
});