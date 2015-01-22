$(function () {
    setIFrameSize();
    $(window).resize(function () {
        setIFrameSize();
    });
});

function setIFrameSize() {
    var parentDivWidth = $("#myframe").parent().width();
    var parentDivHeight = $("#myframe").parent().height();
    $("#myframe")[0].setAttribute("width", parentDivWidth);
    $("#myframe")[0].setAttribute("height", parentDivHeight);
}