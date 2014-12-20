$(document).ready(function() {

    getJson('hello').done(function(result) {

        console.log("result = " + result);
    });

});
