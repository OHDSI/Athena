/**
 * Created by GMalikov on 29.04.2015.
 */
AthenaApp.module("Entities", function(Entities, AthenaApp, Backbone, Marionette, $, _){
    Entities.VocabStatus = Backbone.Model.extend({});

    Entities.VocabStatusCollection = Backbone.Collection.extend({
        model: Entities.VocabStatus
    });

    var vocabStatuses;

    var initializeStatuses = function(){
        vocabStatuses = new Entities.VocabStatusCollection([
            {id: 1, name: "SNOMED" , status: 0, statusName: "Ready",
                recordsTotal: 23456, domains: 16, concepts: 18345, relations: 9876, sourceName: "SNOMED", intersected: "ICD10, CPT4, LOINC", lastUpdated: "04.02.2015"},
            {id: 2, name: "ICD10" , status: 1, statusName: "Build in progress",
                recordsTotal: 11357, domains: 5, concepts: 9765, relations: 4563, sourceName: "ICD10", intersected: "SNOMED, CPT4, LOINC, MedRA", lastUpdated: "11.03.2015"},
            {id: 3, name: "ICD9" , status: 2, statusName: "Build failed",
                recordsTotal: 9327, domains: 7, concepts: 11900, relations: 2789, sourceName: "ICD9", intersected: "DRG, NDFRT", lastUpdated: "16.01.2015"},
            {id: 4, name: "CPT4" , status: 0, statusName: "Ready",
                recordsTotal: 25674, domains: 10, concepts: 33098, relations: 8762, sourceName: "CPT4", intersected: "SNOMED, ICD10", lastUpdated: "01.05.2015"},
            {id: 5, name: "HCPCS" , status: 0, statusName: "Ready",
                recordsTotal: 45023, domains: 9, concepts: 20009, relations: 3675, sourceName: "HCPCS", intersected: "DRG, NDFRT", lastUpdated: "31.09.2014"},
            {id: 6, name: "LOINC" , status: 1, statusName: "Build in progress",
                recordsTotal: 78265, domains: 14, concepts: 32456, relations: 5677, sourceName: "LOINC", intersected: "SNOMED, ICD10", lastUpdated: "22.02.2015"},
            {id: 7, name: "NDFRT" , status: 3, statusName: "Unavailable",
                recordsTotal: 36876, domains: 5, concepts: 12565, relations: 1987, sourceName: "NDFRT", intersected: "ICD9, HCPCS", lastUpdated: "18.04.2015"},
            {id: 8, name: "RxNorm" , status: 0, statusName: "Ready",
                recordsTotal: 43876, domains: 11, concepts: 65432, relations: 34521, sourceName: "RxNorm", intersected: "DRG, MedRA", lastUpdated: "21.01.2015"},
            {id: 9, name: "DRG" , status: 0, statusName: "Ready",
                recordsTotal: 27123, domains: 8, concepts: 18987, relations: 21456, sourceName: "DRG", intersected: "RxNorm, HCPCS", lastUpdated: "18.03.2015"},
            {id: 10, name: "MedDRA" , status: 1, statusName: "Build in progress",
                recordsTotal: 78123, domains: 18, concepts: 238987, relations: 78787, sourceName: "MedRA", intersected: "RxNorm, ICD10", lastUpdated: "19.04.2015"}
        ]);
    };

    var API ={
        getVocabStatuses: function(){
            if(vocabStatuses === undefined){
                initializeStatuses();
            }
            return vocabStatuses;
        }
    };

    AthenaApp.reqres.setHandler("vocabStatus:entities", function(){
        return API.getVocabStatuses();
    });
});