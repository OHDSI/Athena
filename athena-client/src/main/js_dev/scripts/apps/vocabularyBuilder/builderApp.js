/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder", function(VocabularyBuilder, AthenaApp, Backbone, Marionette, $, _){
    VocabularyBuilder.Router = Marionette.AppRouter.extend({
        appRoutes:{
            "statusList" : "listStatus"
        }
    });

    var API = {
        listStatus: function(){
            VocabularyBuilder.List.Controller.listVocabularyStatuses();
            AthenaApp.execute("set:active:menu", "statusList");
        }
    };

    AthenaApp.on("builder:listStatus", function(){
        AthenaApp.navigate("statusList");
        API.listStatus();
    });

    AthenaApp.addInitializer(function () {
        new VocabularyBuilder.Router({
            controller: API
        });
    });
});
