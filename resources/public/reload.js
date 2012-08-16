 $(document).ready(function() {
   var refreshId = setInterval(function() {
       $("#scoreboard").load('showscoretable');
   }, 10000);
   $.ajaxSetup({ cache: false });
});
