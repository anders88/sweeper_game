"use strict";

$.urlParam = function(name){
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    return results[1] || 0;
}

$ (function() {

    var playerid = $.urlParam("id");

    var BoardRowModel = Backbone.Model.extend({});

    var BoardRowCollection = Backbone.Collection.extend({
        model: BoardRowModel,
        url: "/boarddebug?id=" + playerid
    });

    var BoardRowView = Backbone.View.extend({
        tagName: "tr",

        render: function() {
            var self = this;
            //console.log("A row " + this.model.get("row"));
            $.each(this.model.get("row"),function(index,value) {
                self.$el.append("<td>" + value + "</td>");
                //console.log("Cell: " + value);
            });
            //this.$el.html("<td>Linje</td>");
            return this;
        }
    });

    var board = new BoardRowCollection;

    var BoardTableView = Backbone.View.extend({
        initialize: function() {
            board.bind("reset",this.updateTable,this);
            board.fetch();
            /*window.setInterval(function() {
                scores.fetch();
            },5000);*/

        },

        updateTable: function() {
            var boardRow = $("#boardRow");
            boardRow.html(" ");
            board.each(function(row) {
                var rowView = new BoardRowView({model: row});
                boardRow.append(rowView.render().el);
            });
        }
    });

    var appView = new BoardTableView({el: $("#boardTable")});


    //$("#idhere").html($.urlParam("id"));

});