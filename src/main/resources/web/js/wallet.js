$(document).ready(function() {


  setupSendForm();
  fillStatusText('status_text', '#status_text');

  fillProgressBar('status_progress', '#progress_bar');

  fillSimpleText('balance', '.balance');

  fillSimpleText('receive_address', '#receive_address');

  transactionsTable();

  qrCode();

});

function qrCode() {

  getJson('receive_address').done(function(result) {
    var btcText = "bitcoin:" + result;
    $('#qrcode').qrcode({
      "width": 100,
      "height": 100,
      "color": "#3a3",
      "text": btcText
    });

  });
}

function transactionsTable() {
  var template = $('#transactions_template').html();
  pageNumbers['#transactions_table'] = 1;
  setupPagedTable('get_transactions', template, '#transactions', '#transactions_table');

}

function sendStatus() {
  fillSendMoneyStatusText('send_status', '#send_status');
  fillSimpleText('balance', '.balance');
  transactionsTable();
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
