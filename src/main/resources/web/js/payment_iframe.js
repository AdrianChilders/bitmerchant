var currentOrder;
$(document).ready(function() {



  // Get the order number
  // FINALLY FUCKING FOUND IT, TOOK FOR GODDAMN EVER
  // var orderNum = window.frameElement.getAttribute("order");

  console.log('button num = ' + buttonNum);

  var buttonNum = window.frames["name"];

  $.when(setupSuggestedAmounts(buttonNum)).done(function(e) {
    console.log(e);
    var priceChoices = e['button']['price_select'];
    if (priceChoices) {

      var customPriceString = $('input[name=chosen_price]:checked').val();
      console.log('cust price = ' + customPriceString);
      simplePost('api/buttons/' + buttonNum + '/create_order/' + customPriceString,
        null, false, successCreatedButton, true, true);

      // Create a new order each time a selection is made
      $('input[name="chosen_price"]').on('change', function() {
        var customPriceString = $('input[name=chosen_price]:checked').val();
        console.log('cust price = ' + customPriceString);
        simplePost('api/buttons/' + buttonNum + '/create_order/' + customPriceString,
          null, false, successCreatedButton, true, true);
      });

      // for the custom price stuff
    } else {
      simplePost('api/buttons/' + buttonNum + '/create_order', null, false, successCreatedButton, true, true);
    }
    var variablePrice = e['button']['variable_price'];
    if (variablePrice) {
      var wto;
      $('#custom_price').bootstrapValidator({
        message: 'This value is not valid',
        excluded: [':disabled']
      }).on('success.field.bv', function(event) {

        clearTimeout(wto);

        console.log('cust price = ' + customPriceString);
        wto = setTimeout(function() {
          var customPriceString = $('#custom_price_string').val();
          simplePost('api/buttons/' + buttonNum + '/create_order/' + customPriceString,
            null, false, successCreatedButton, true, true);
        }, 1000);
      });
    }
  });



  // First, create the order for the button
  // if a variable price, create new orders for each selection





});

function setupSuggestedAmounts(buttonNum) {

  var url = 'api/buttons/' + buttonNum;
  return getJson(url, false, true).done(function(result) {
    var b = result;
    var priceSelect = b['button']['price_select'];


    if (priceSelect) {
      $('#suggested_prices').removeClass('hide');
      // For the suggested prices
      for (var i = 1; i <= 5; i++) {
        var priceNumStr = "price_" + i;
        var inputId = '#' + priceNumStr;
        var cPrice = b['button'][priceNumStr];
        var nativeCurrencyIso = b['button']['native_currency_iso'];

        console.log('cprice = ' + cPrice);

        // unhide and set the price
        if (cPrice != null) {
          // Check the first one
          var html;
          if (i == 1) {
            html = '<input type="radio" name="chosen_price" value="' + cPrice + '" checked="checked">' + formatMoney(cPrice) + ' ' + nativeCurrencyIso;
          } else {
            html = '<input type="radio" name="chosen_price" value="' + cPrice + '">' + formatMoney(cPrice) + ' ' + nativeCurrencyIso;

          }
          console.log(inputId);
          $(inputId).removeClass('hide');


          $(inputId).html(html);
        }
      }
    }

    var customPrice = b['button']['variable_price'];
    if (customPrice) {
      $('#custom_price').removeClass('hide');
    }
  });
}

function successCreatedButton(data) {
  console.log('created order = ' + data);
  console.log(data);

  var orderNum = data['order']['id'];
  var url = 'api/orders/' + orderNum;
  currentOrder = orderNum;
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
    $(".loading-spinner").addClass('hide');
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
    $('.total_native').html(total_native);


    var native_currency_iso = j['order']['native_currency_iso'];
    $('.native_currency_iso').html(native_currency_iso);

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

        if (status != 'new' || timeLeft == '00:00' || currentOrder != orderNum) {
          if (status == 'completed') {
            $('.foggy').foggy();

            $('#payment_status').addClass('btn-success');
            $('#payment_status').text('Payment Completed');
            $('#count_down').addClass('hide');


          } else if (timeLeft == '00:00') {
            $('#payment_status').addClass('btn-danger');
            $('#payment_status').text('Order Expired');
          } else if (currentOrder != orderNum) {
            clearInterval(intervalID);
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
