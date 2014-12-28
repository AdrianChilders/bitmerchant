$(document).ready(function() {

    getJson('hello').done(function(result) {

        console.log("result = " + result);
    });

   	setupDate();

    fillStatusText('status_text', '#status_text');

    fillProgressBar('status_progress', '#progress_bar');

    fillSimpleText('balance', '#balance');

    fillSimpleText('recieve_address', '#recieve_address');

    fillSimpleText('wallet_words', '#wallet_words');

   
});

function setupDate() {
	 $('.datepicker').pickadate({
        selectYears: true,
        selectMonths: true,
        format: 'yyyy-mm-dd',
      	formatSubmit: 'yyyy-mm-dd'
        
    });


    getJson('wallet_creation_date').done(function(result) {
        var picker = $("#wallet_creation_date").pickadate('picker');
        picker.set('select', result, { format: 'yyyy-mm-dd' });
    });
}
