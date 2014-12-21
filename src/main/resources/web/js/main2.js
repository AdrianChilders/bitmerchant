$(document).ready(function() {

    getJson('hello').done(function(result) {

        console.log("result = " + result);
    });

    fillSimpleText('status_text', '#test1');

});
