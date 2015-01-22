$(document).ready(function() {

  // set up the css for the button
  // var buttonImageUrl = 'http://dabuttonfactory.com/b.png?t=Purchase&f=DejaVuSansCondensed&ts=24&tc=ffffff&c=5&bgt=unicolored&bgc=e37e0c&hp=20&vp=11';
  // $('.bitmerchant-button').html('<img src="' + buttonImageUrl + '" style="cursor: pointer;">'
    // cursor: pointer;
    // width: 200 px;
    // height: 100 px;
    // border: none;
  // );

  var buttonNum = $('.bitmerchant-button').attr("name");
  console.log('button num = ' + buttonNum);
  var iframe = $('<iframe frameborder="0" marginwidth="0" marginheight="0" name="' + buttonNum + '" allowfullscreen></iframe>');
  var dialog = $("<div></div>").append(iframe).appendTo("body").dialog({
    autoOpen: false,
    modal: true,
    resizable: false,
    width: "auto",
    height: "auto",
    position: {
      my: 'center',
      at: 'center',
      of: window
    },
    close: function() {
      iframe.attr("src", "");

    }
  });
  $(".bitmerchant-button").on("click", function(e) {
    e.preventDefault();
    var src = $(this).attr("href");
    var title = $(this).attr("data-title");
    var width = $(this).attr("data-width");
    var height = $(this).attr("data-height");
    console.log(height);
    iframe.attr({
      width: +width,
      height: +height,
      src: src
    });
    dialog.dialog('option', 'position', 'center');
    dialog.dialog("option", "title", title).dialog("open");
  });
});

// var dialog = $("#dialog").html($("<iframe />").attr("src", "payment_iframe.html")).dialog({
//   autoOpen: false,
//   modal: true,
//   show: "blind",
//   hide: "blind",
//   height: 300,
//   width: 350,
//   close: function() {
//     iframe.attr("src", "");
//   }

// });



$(".bitmerchant-button").click(function(e) {
  console.log('got here3');
  e.preventDefault();

  // Load the popup modal


});





//     function createIframe() {
//       var i = document.createElement("iframe");
//       i.src = "payment_button.html";
//       i.scrolling = "no";
//       i.frameborder = "0";
//       i.width = "100%";
//       i.height = "130%";
//       // document.body.insertBefore(i);
//       $('body').prepend(i);
//     };
