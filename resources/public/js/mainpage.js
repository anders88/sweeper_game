"use strict";

$ (function() {

    var ScoreRowModel = Backbone.Model.extend({});

    var ScoreRowCollection = Backbone.Collection.extend({
        model: ScoreRowModel,
        url: "/scorejson"
    });

    var ScoreRowView = Backbone.View.extend({
        tagName: "li",

        template: _.template($("#row-template").html()),

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        }
    });

    var scores = new ScoreRowCollection;

    //scores.add([{point: "1"}, {point: "2"}]);

    var ScoreTableView = Backbone.View.extend({
        initialize: function() {
            scores.fetch({
                async: false
            });
            var scoreRow = $("#scoreRow");
            scores.each(function(row) {
                //scoreRow.append("<li>" + row.get("point") + "</li>");
                var rowView = new ScoreRowView({model: row});
                scoreRow.append(rowView.render().el);
            });
        }
    });

    var appView = new ScoreTableView({el: $("#scoreTable")});


});