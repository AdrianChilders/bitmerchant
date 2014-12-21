$(document).ready(function() {

    getJson('hello').done(function(result) {

        console.log("result = " + result);
    });

    fillStatusText('status_text', '#status_text');

    fillProgressBar('status_progress','#progress_bar');

});
