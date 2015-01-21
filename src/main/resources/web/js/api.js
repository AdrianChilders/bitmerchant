$(document).ready(function() {
	scrollSpy();
	console.log("doin the scrollspy");
});

function scrollSpy() {
  $('body').scrollspy({
    target: '.bs-docs-sidebar',
    offset: 70
  });
  // $('#main').scrollspy('refresh');
  //    $('[data-spy="scroll"]').each(function () {
  //   var $spy = $(this).scrollspy('refresh')
  // });
}