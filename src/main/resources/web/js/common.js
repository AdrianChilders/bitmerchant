$(document).ready(function() {

  powerStuff();

});


function powerStuff() {

  $("#power_off").click(function(event) {
    simplePost('power_off', null, false, null, true, false);
    powerToast();
    event.preventDefault();
  });

  $("#restart").click(function(event) {
    simplePost('restart', null, false, null, true, false);
    restartToast();
    event.preventDefault();
  });
}

function powerToast() {
  toastr.success('Bitmerchant is powering off...');

  setTimeout(function() {

    open(location, '_self').close();

  }, 2000);

}

function restartToast() {
  toastr.success('Bitmerchant is restarting...');

  setTimeout(function() {

   open(location, '_self').close();

  }, 3000);

}
