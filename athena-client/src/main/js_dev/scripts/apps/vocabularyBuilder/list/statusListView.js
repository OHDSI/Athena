/**
 * Created by GMalikov on 30.04.2015.
 */
AthenaApp.module("VocabularyBuilder.List", function(List, AthenaApp, Backbone, Marionette, $,_){
    List.VocabStatus = Marionette.ItemView.extend({
        tagName: "tr",
        template: "#vocab-list-item",

        events: {
            "click button": "highlightName",
            "click button.js-build": "deleteClicked",
            "click button.js-log": "showClicked"
        },

        highlightName: function(){
            this.$el.addClass("warning");
            this.triggerMethod('highlight:vocabulary');
        },
        deleteClicked: function(e){
            e.stopPropagation();
            this.trigger("vocabStatus:build", this.model);
        },
        showClicked: function(){
            this.trigger("vocabStatus:show", this.model);
        }
    });

    List.VocabStatusCollection = Marionette.CompositeView.extend({
//        tagName: "table",
//        className: "table table-hover table-bordered",
        template: "#vocab-list",
        childView: List.VocabStatus,
        childViewContainer: "#statusListTableBody",
        onRender: function(){
            console.log("Collection view is rendered!");
        },
        childEvents:{
            'highlight:vocabulary': function(childView){
                console.log('Got the childView event!' + childView);
                this.children.each(function(view){
                    if(view.cid !== childView.cid){
                        view.$el.removeClass("warning");
                    }
                });
            }
        }
    });
});