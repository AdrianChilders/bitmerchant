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
                if (data=="Sync Complete") {
                    clearInterval(intervalID);
                }
            },
            error: function(request, status, error) {

                // toastr.error(request.responseText);
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
                $(divId).css({"width": pct});
                if (data==1.0) {
                  $(divId).removeClass('active progress-bar-striped');
                    clearInterval(intervalID);
                }
            },
            error: function(request, status, error) {

                // toastr.error(request.responseText);
            }
        });
    }, 300); // 1000 milliseconds = 1 second.
}
