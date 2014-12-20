var sparkService = "http://localhost:4567/"

function getJson(shortUrl) {
  var url = sparkService + shortUrl // the script where you handle the form input.
  return $.ajax({
    type: "GET",
    url: url,
    xhrFields: {
      withCredentials: true
    },
    // data: seriesData, 
    success: function(data, status, xhr) {
      // console.log(data);
      // var jsonObj = JSON.parse(data);
      // JSON.useDateParser();
      // var jsonObj = jQuery.parseJSON(data);
      // JSON.useDateParser();
      // var jsonObj = JSON.parse(data);
      // $('[data-spy="scroll"]').each(function() {
      //     $(this).scrollspy('refresh');
      // });
    },
    error: function(request, status, error) {

      toastr.error(request.responseText);
    }
  });
}