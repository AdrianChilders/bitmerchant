$(document).ready(function() {


  // Get the order number
  // FINALLY FUCKING FOUND IT, TOOK FOR GODDAMN EVER
  // var orderNum = window.frameElement.getAttribute("order");

  console.log('button num = ' + buttonNum);

  var buttonNum = window.frames["name"];

  // First, create the order for the button
  // if a variable price, create new orders for each selection

  simplePost('api/buttons/' + buttonNum + '/create_order', null, false, successCreatedButton, true, true);



});

function successCreatedButton(data) {
  console.log('created order = ' + data);
  console.log(data);

  var orderNum = data['order']['id'];
  var url = 'api/orders/' + orderNum;

  console.log('orderNum = ' + orderNum);
  setupFields(orderNum, url);
  checkStatus(orderNum, url);

}

function qrCode(btcText) {

  $('#qrcode').html('');
  $('#qrcode').qrcode({
    // "render": "div",
    //  "width": 30,
    //  "height": 30,
    size: 100,
    "color": "#3a3",
    "text": btcText
  });

}

function setupFields(orderNum, url) {
  getJson(url, false, true).done(function(result) {
    $(".container-fluid").removeClass('hide');
    console.log(result);
    var j = result;

    // TODO If the expire time is '00:00', just ignore everything and say that the order is expired
    var expire_time = j['order']['expire_time'];
    countdown('#count_down', expire_time);


    var button_name = j['order']['button_name'];
    $('#button_name').html(button_name);

    var button_description = j['order']['button_description'];
    $('#button_description').html(button_description);

    var type = j['order']['type'];
    if (type == 'buy_now') {
      type = 'A Purchase';
    } else {
      type = 'A Donation';
    }
    $('#type').html(type);

    var total_native = j['order']['total_native'];
    total_native = formatMoney(total_native);
    $('#total_native').html(total_native);


    var native_currency_iso = j['order']['native_currency_iso'];
    $('#native_currency_iso').html(native_currency_iso);

    if (native_currency_iso != 'BTC') {
      var total_satoshis = j['order']['total_satoshis'];

      var mBTC = formatMoney(parseFloat(total_satoshis) * 1E-5);
      console.log(mBTC);
      $('#total_btc').html(mBTC);
    } else {
      $('#is_already_btc').addClass('hide');
    }



    var payment_request_url = j['order']['payment_request_url'];
    $('.qr_link').attr('href', payment_request_url);


    qrCode(payment_request_url);


    console.log(button_name);
  });
}

function checkStatus(orderNum, url) {
  var url = externalSpark + url // the script where you handle the form input.
  var intervalID = setInterval(function() {
    $.ajax({
      type: "GET",
      url: url,
      xhrFields: {
        withCredentials: true
      },
      // data: seriesData, 
      success: function(data, status, xhr) {
        console.log(data);

        var timeLeft = $('#count_down').text();
        var status = data['order']['status'];
        console.log(timeLeft);

        if (status != 'new' || timeLeft == '00:00') {
          if (status == 'completed') {
            $('#payment_status').addClass('btn-success');
            $('#payment_status').text('Payment Completed');
            $('#count_down').addClass('hide');
          } else if (timeLeft == '00:00') {
            $('#payment_status').addClass('btn-danger');
            $('#payment_status').text('Order Expired');
          } else {
            $('#payment_status').addClass('btn-warning');
            $('#payment_status').text(status);
          }



          clearInterval(intervalID);

        }
      },
      error: function(request, status, error) {

        // toastr.error(request.responseText);
        clearInterval(intervalID);
      }
    });
  }, 2000); // 1000 milliseconds = 1 second.
}
