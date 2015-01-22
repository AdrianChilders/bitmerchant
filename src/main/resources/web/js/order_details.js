var orderTemplate = null;

$(document).ready(function() {

  orderTemplate = $('#orders_template').html();

  var orderNum = getUrlParameter('order');
  console.log('o = ' + orderNum);

  ordersTable(orderTemplate, orderNum);

});


function ordersTable(templateHTML, orderNum) {

  fillMustacheFromJson('api/orders/' + orderNum, templateHTML, "#orders");
}
