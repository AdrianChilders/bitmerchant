var transactionsTemplate = null;

$(document).ready(function() {


  setupSendForm();
  fillStatusText('status_text', '#status_text');

  fillProgressBar('status_progress', '#progress_bar');

  fillSimpleText('balance', '.balance');

  qrCodeAndReceiveAddress();

  // have to only call this once :(
  transactionsTemplate = $('#transactions_template').html();

  fetchReceivedTransactions('newest_received_tx', transactionsTemplate);

  transactionsTable(transactionsTemplate);



});

function qrCodeAndReceiveAddress() {

  getJson('receive_address').done(function(result) {
    var btcText = "bitcoin:" + result;
    $('#qrcode').html('');
    $('#qrcode').qrcode({
      "width": 100,
      "height": 100,
      "color": "#3a3",
      "text": btcText
    });
    $('#receive_address').html(result);

  });
}

function transactionsTable(templateHTML) {

  pageNumbers['#transactions_table'] = 1;
  setupPagedTable('get_transactions', templateHTML, '#transactions', '#transactions_table');

}


function sendStatus() {

  fillSendMoneyStatusText('send_status', '#send_status');
  fillSimpleText('balance', '.balance');
  transactionsTable(transactionsTemplate);
}

function setupSendForm() {

  getJson('wallet_is_encrypted').done(function(result) {
    var isEncrypted = (result == 'true');

    if (isEncrypted) {
      $('#sendBtn').attr("data-target", "#sendEncryptedModal");
    } else {
      $('#sendBtn').attr("data-target", "#sendModal");
    }


  });

  $('#sendMoneyEncryptedForm').bootstrapValidator({
      message: 'This value is not valid',
      excluded: [':disabled'],
      submitButtons: 'button[type="submit"]'
    })
    .on('success.form.bv', function(event) {
      event.preventDefault();
      standardFormPost('send_money_encrypted', "#sendMoneyEncryptedForm",
        "#sendEncryptedModal", false, sendStatus);
    });

  $('#sendMoneyForm').bootstrapValidator({
      message: 'This value is not valid',
      excluded: [':disabled'],
      submitButtons: 'button[type="submit"]'
    })
    .on('success.form.bv', function(event) {
      event.preventDefault();
      standardFormPost('send_money', "#sendMoneyForm",
        "#sendModal", false, sendStatus);
    });








  getJson('balance').done(function(result) {
    $('.othermain-col').removeClass('hide');
    var fundsNum = result.replace(/[^0-9\.]+/g, "");
    var usersFunds = parseFloat(fundsNum);

    $('[name="funds"]').text(result);
    $('[name="sendAmount"]').attr('placeholder', 'Current funds : ' + result);
    $('[name="sendAmount"]').bind('keyup', function(f) {
      var sendAmount = parseFloat($(this).val());

      var fundsLeft = usersFunds - sendAmount;
      if (!isNaN(fundsLeft)) {

        $('[name="fundsLeft"]').text('$' + fundsLeft);

        if (fundsLeft < 0) {

          $('[name="placeSendBtn"]').prop('disabled', true);
          $('[name="fundsLeft"]').addClass("text-danger");
          $('[name="fundsLeft"]').removeClass("text-success");

        } else {
          $('[name="placeSendBtn"]').prop('disabled', false);
          $('[name="fundsLeft"]').addClass("text-success");
          $('[name="fundsLeft"]').removeClass("text-danger");
        }
      }

    });
  });

  // $("#placeSendBtn").click(function(event) {
  //     standardFormPost('user_send', '#sendForm', '#sendModal', true);
  //     event.preventDefault();
  // });


}

function fetchReceivedTransactions(url, templateHTML) {
  var url = sparkService + url // the script where you handle the form input.
  var lastReceivedHash = getCookie("newestReceivedTransaction");
  var intervalID = setInterval(function() {
    $.ajax({
      type: "GET",
      url: url,
      xhrFields: {
        withCredentials: true
      },
      // data: seriesData, 
      success: function(data, status, xhr) {

        xhr.getResponseHeader('Set-Cookie');
        var tx = JSON.parse(data);

        var nextReceivedHash = tx['hash'];
        var amount = tx['amount'];

        // console.log(tx);
        // console.log('next: ' + nextReceivedHash);
        // console.log('last: ' + lastReceivedHash);

        if (nextReceivedHash != lastReceivedHash) {
          if (nextReceivedHash != 'none yet') {
            var message = 'You were sent ' + amount;
            toastr.success(message);
          }





          qrCodeAndReceiveAddress();
          fillSimpleText('balance', '.balance');
          transactionsTable(templateHTML);

          // Now set the vars to be the same
          // lastReceivedHash = nextReceivedHash;
          lastReceivedHash = getCookie("newestReceivedTransaction");
        }

      },
      error: function(request, status, error) {

        // toastr.error(request.responseText);
        clearInterval(intervalID);
      }
    });

    // console.log(getCookies());
  }, 60000); // 1000 milliseconds = 1 second.
}
