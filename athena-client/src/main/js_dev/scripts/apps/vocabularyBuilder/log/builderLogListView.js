/**
 * Created by GMalikov on 05.05.2015.
 */
AthenaApp.module("VocabularyBuilder.Log", function(Log, AthenaApp, Backbone, Marionette, $, _){
    Log.Show = Marionette.ItemView.extend({
        tagName: "tr",
        template: "#vocab-message-log",

        onRender: function(){
            if(this.model.get('status') === 0){
                this.$el.addClass("danger");
            } else if(this.model.get('status') === 2){
                this.$el.addClass("success");
            } else {
                this.$el.addClass("info");
            }

        }
    });

    Log.List = Marionette.CompositeView.extend({
        tagName: "table",
        className: "table table-condensed",
        template: "#message-log-list",
        childView: Log.Show,
        itemViewContainer: "tbody",

        templateHelpers: function(){
            var vocabulary = this.options.vocabulary;
            return{
                vocabulary: function(){
                    if(vocabulary === undefined){
                        return "";
                    } else {
                        return vocabulary;
                    }
                }
            }
        }
    });
});