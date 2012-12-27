"use strict";

$.urlParam = function(name){
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    return results ? results[1] || 0 : 0;
}

$ (function() {

    var ScoreRowModel = Backbone.Model.extend({});

    var ScoreRowCollection = Backbone.Collection.extend({
        model: ScoreRowModel,
        url: "/scorejson?id=" + $.urlParam("id")
    });

    var ScoreRowView = Backbone.View.extend({
        tagName: "tr",

        template: _.template($("#row-template").html()),

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        }
    });

    var scores = new ScoreRowCollection;

    var ScoreTableView = Backbone.View.extend({
        initialize: function() {
            scores.bind("reset",this.updateTable,this);
            scores.fetch({cache: false});
            window.setInterval(function() {
                scores.fetch({cache: false});
            },5000);

        },

        updateTable: function() {
            var scoreRow = $("#scoreRow");
            scoreRow.html(" ");
            scores.each(function(row) {
                var rowView = new ScoreRowView({model: row});
                scoreRow.append(rowView.render().el);
            });
        }
    });

    var appView = new ScoreTableView({el: $("#scoreTable")});


});