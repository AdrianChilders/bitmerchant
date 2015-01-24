var currenciesTemplate = null;

$(document).ready(function() {

  // remove the ssl warning if necessary
  getJson('ssl').done(function(isSSL) {
    console.log(isSSL);
    if (isSSL) {
      console.log('hiding ssl warning');
      $('#ssl_warning').addClass('hide');
    }
  });

  // Fill the currency options
  currenciesTemplate = $('#currencies_template').html();

  $.when(fillMustacheFromJson('api/currencies', currenciesTemplate, "#currencies"),
      getJson('merchant_info'))
    .done(function(v1, v2) {
      var merchant_info = JSON.parse(v2[0]);

      // select the correct currency id and merchant name
      var native_currency = merchant_info['native_currency_iso'];
      $('#currency_select option[value=' + native_currency + ']').prop('selected', true);

      var merchantName = merchant_info['name'];
      // console.log(merchantName);
      if (merchantName != null) {
        $('#merchant_name').val(merchantName);
      }

      $('#currency_select').on('change', function() {
        var formData = $('#currency_form').serializeArray();
        console.log(formData);
        standardFormPost('save_merchant_info', "#currency_form",
          null, false, null, true, false);
      });
    });


  var wto;
  $('#currency_form').bootstrapValidator({
      message: 'This value is not valid',
      excluded: [':disabled'],
      submitButtons: 'button[type="submit"]'
    })
    .on('success.field.bv', function(event) {
      event.preventDefault();

      clearTimeout(wto);


      wto = setTimeout(function() {
        var formData = $('#currency_form').serializeArray();
        console.log(formData);

        standardFormPost('save_merchant_info', "#currency_form",
          null, false, null, true, false);
      }, 1000);


    });



});
