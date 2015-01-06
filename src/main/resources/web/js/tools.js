var sparkService = "http://localhost:4567/"
var pageNumbers = {};


function getJson(shortUrl, noToast) {

  noToast = (typeof noToast === "undefined") ? false : noToast;

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
      if (!noToast) {
        toastr.error(request.responseText);
      }
    }
  });
}

function fillSimpleText(url, divId) {
  var url = sparkService + url // the script where you handle the form input.
  $.ajax({
    type: "GET",
    url: url,
    xhrFields: {
      withCredentials: true
    },
    // data: seriesData, 
    success: function(data, status, xhr) {
      // console.log(data);
      $(divId).html(data);


    },
    error: function(request, status, error) {

      toastr.error(request.responseText);
    }
  });
}

function fillStatusText(url, divId) {
  var url = sparkService + url // the script where you handle the form input.
  var intervalID = setInterval(function() {
    $.ajax({
      type: "GET",
      url: url,
      xhrFields: {
        withCredentials: true
      },
      // data: seriesData, 
      success: function(data, status, xhr) {
        $(divId).html(data);
        if (data == "Sync Complete") {
          clearInterval(intervalID);
          $('.sync-incomplete').fadeOut(1500);
          $('.wallet-btn').removeClass('disabled');
          fillSimpleText('balance', '#balance');
        }
      },
      error: function(request, status, error) {

        // toastr.error(request.responseText);
        clearInterval(intervalID);
      }
    });
  }, 300); // 1000 milliseconds = 1 second.
}

function fillSendMoneyStatusText(url, divId) {
  var url = sparkService + url // the script where you handle the form input.
  var intervalID = setInterval(function() {
    $.ajax({
      type: "GET",
      url: url,
      xhrFields: {
        withCredentials: true
      },
      // data: seriesData, 
      success: function(data, status, xhr) {
        $(divId).html(data);
        if (data == "Success") {
          clearInterval(intervalID);
          $('.send-incomplete').fadeOut(1500);
          fillSimpleText('balance', '#balance');
        }
      },
      error: function(request, status, error) {

        // toastr.error(request.responseText);
        clearInterval(intervalID);
      }
    });
  }, 300); // 1000 milliseconds = 1 second.
}

function fillProgressBar(url, divId) {
  var url = sparkService + url // the script where you handle the form input.
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
        var pct = (data * 100.0) + "%";
        console.log(pct);
        $(divId).css({
          "width": pct
        });
        if (data == 1.0) {
          $(divId).removeClass('active progress-bar-striped');
          clearInterval(intervalID);

        }
      },
      error: function(request, status, error) {

        // toastr.error(request.responseText);
        clearInterval(intervalID);
      }
    });
  }, 300); // 1000 milliseconds = 1 second.
}

function standardFormPost(shortUrl, formId, modalId, reload, successFunctions) {
  // !!!!!!They must have names unfortunately
  // An optional arg
  modalId = (typeof modalId === "undefined") ? "defaultValue" : modalId;

  reload = (typeof reload === "undefined") ? false : reload;



  // serializes the form's elements.
  var formData = $(formId).serializeArray();
  // console.log(formData);

  var btn = $(".btn", formId);

  // Loading
  btn.button('loading');

  var url = sparkService + shortUrl; // the script where you handle the form input.
  // console.log(url);
  $.ajax({
    type: "POST",
    url: url,
    xhrFields: {
      withCredentials: true
    },
    data: formData,
    success: function(data, status, xhr) {

      // console.log('posted the data');
      xhr.getResponseHeader('Set-Cookie');
      // document.cookie="authenticated_session_id=" + data + 
      // "; expires=" + expireTimeString(60*60); // 1 hour (field is in seconds)
      // Hide the modal, reset the form, show successful

      // $(formId)[0].reset();
      $(modalId).modal('hide');
      // console.log(modalId);
      toastr.success(data);
      if (successFunctions != null) {
        successFunctions();
      }
      if (reload) {
        // refresh the page, too much info has now changed
        window.setTimeout(function() {
          location.reload();
        }, 3000);
      }

      btn.button('reset');

      // console.log(document.cookie);
      return data;

    },
    error: function(request, status, error) {
      toastr.error(request.responseText);
      btn.button('reset');
    }
  });

  event.preventDefault();
  return false;



  // event.preventDefault();


}

function setupPagedTable(shortUrl, templateHtml, divId, tableId) {
    var pageNum = pageNumbers[tableId];

    var nextId = divId + "_pager_next";
    var prevId = divId + "_pager_prev";
    // console.log(nextId);
    // TODO get page numbers here
    // fillTableFromMustache(shortUrl + pageNum,
        fillTableFromMustache(shortUrl,
        templateHtml, divId, tableId);

    $(nextId).click(function(e) {
        pageNum++;
        $(prevId).removeClass('disabled');

        fillTableFromMustache(shortUrl + pageNum,
            templateHtml, divId, tableId);

    });
    $(prevId).click(function(e) {
        if (pageNum > 1) {
            pageNum--;

            fillTableFromMustache(shortUrl + pageNum,
                templateHtml, divId, tableId);
        }
        if (pageNum == 1) {
            $(this).addClass('disabled');
            return;
        }


    });
}

function fillTableFromMustache(url, templateHtml, divId, tableId) {
    //         $.tablesorter.addParser({ 
    //           id: 'my_date_column', 
    //           is: function(s) { 
    //       // return false so this parser is not auto detected 
    //       return false; 
    //     }, 
    //     format: function(s) { 
    //       console.log(s);
    //       var timeInMillis = new Date.parse(s);

    //       // var date = new Date(parseInt(s));
    //       return date;         
    //     }, 
    //   // set type, either numeric or text 
    //   type: 'numeric' 
    // });

    var url = sparkService + url // the script where you handle the form input.
    $.ajax({
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
            var jsonObj = JSON.parseWithDate(data);


            Mustache.parse(templateHtml); // optional, speeds up future uses
            var rendered = Mustache.render(templateHtml, jsonObj);
            $(divId).html(rendered);
            $(tableId).tablesorter({
                debug: false
                // textExtraction: extractData
                //     headers: { 
                //   0: {       // Change this to your column position
                //     sorter:'my_date_column' 
                //   } 
                // }
            });
            // console.log(jsonObj);
            // console.log(templateHtml);
            // console.log(rendered);


        },
        error: function(request, status, error) {

            toastr.error(request.responseText);
        }
    });

}