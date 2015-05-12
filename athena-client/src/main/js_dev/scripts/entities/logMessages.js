/**
 * Created by GMalikov on 07.05.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.LogMessage = Backbone.Model.extend({});

    Entities.LogMessageCollection = Backbone.Collection.extend({
        model: Entities.LogMessage
    });

    var logMessages;

    var initializeMessages = function(){
        logMessages = new Entities.LogMessageCollection([
            {date: "07.05.2015 12:53:16" ,status: 1, text: "Build process started."},
            {date: "07.05.2015 13:00:23", status: 1, text: "Processing CONCEPT_RELATIONS"},
            {date: "07.05.2015 13:07:17", status: 0, text: "Unable to read CONCEPT_RELATIONS table."},
            {date: "07.05.2015 13:08:00", status: 0, text: "Build process failed."},
            {date: "07.05.2015 13:20:55", status: 1, text: "Build process started."},
            {date: "07.05.2015 12:21:09", status: 1, text: "Processed 123456 records during the build process."},
            {date: "07.05.2015 12:22:10", status: 2, text: "Build process successfully finished. Vocabulary is ready for download."},
            {date: "08.05.2015 12:22:10", status: 1, text: "Build process started."},
            {date: "07.05.2015 12:25:54", status: 1, text: "Processing DOMAIN table. 1023 records added."},
            {date: "07.05.2015 12:30:25", status: 1, text: "Processing DRUG_STRENGTH table."},
            {date: "07.05.2015 12:33:25", status: 0, text: "Error. VALID_END is NULL in record DRUG_CONCEPT_ID 700500. Skipping record."},
            {date: "07.05.2015 12:40:25", status: 1, text: "Processing DOMAIN table. 3564187 records added."},
            {date: "07.05.2015 12:40:25", status: 2, text: "Build process successfully finished. Vocabulary is ready for download."}
        ]);
    };

    var API = {
        getLogMessages: function(){
            if(logMessages === undefined){
                initializeMessages();
            }
            return logMessages;
        }
    };

    AthenaApp.reqres.setHandler("logMessages:entities", function(){
        return API.getLogMessages();
    });
});