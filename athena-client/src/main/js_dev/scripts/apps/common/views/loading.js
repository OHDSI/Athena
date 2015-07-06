/**
 * Created by GMalikov on 01.07.2015.
 */
AthenaApp.module("Common.Views", function (Views, AthenaApp, Backbone, Marionette, $, _) {
    Views.Loading = Marionette.ItemView.extend({
        template: "#loading-view",

        onShow: function () {
            var cl = new CanvasLoader('spinner');
            cl.setShape('spiral'); // default is 'oval'
            cl.show(); // Hidden by default
        }
    });
});