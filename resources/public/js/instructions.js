"use strict";

$ (function() {
    var InstructionModel = Backbone.Model.extend({
        url: "/instructions.json",
        initialize: function () {
            this.set({host: window.location.origin})
        }
    });

    var InstructionsView = Backbone.View.extend({
        template: _.template($("#instructionsTemplate").html()),

        render: function() {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        }

    });

    var instructionModel = new InstructionModel;
    instructionModel.fetch({
        async:false,
        cache: false
    });

    var instructionsView = new InstructionsView({
        el: $("#instructionsPart"),
        model: instructionModel
    });

    instructionsView.render();


});
