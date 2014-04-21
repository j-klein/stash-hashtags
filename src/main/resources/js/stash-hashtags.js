$(document).ready(function() {

  // Use AUI classes.
  $.fn.dataTableExt.oStdClasses.sPageButton = "aui-button";
  $.fn.dataTableExt.oStdClasses.sPageButtonActive = "aui-button aui-button-selected";
  $.fn.dataTableExt.oStdClasses.sPageButtonStaticDisabled = "aui-button aui-button-disabled";
  $.fn.dataTableExt.oStdClasses.sPageFirst  = "aui-button";
  $.fn.dataTableExt.oStdClasses.sPageLast = "aui-button";
  $.fn.dataTableExt.oStdClasses.sPagePrevious = "aui-button";
  $.fn.dataTableExt.oStdClasses.sPageNext = "aui-button";

  // Disable alternating row colors.
  $.fn.dataTableExt.oStdClasses.sStripeOdd = "";
  $.fn.dataTableExt.oStdClasses.sStripeEven = "";

  // Setup datatable.
  var table = $("#hashtag_table").DataTable( {
        "aaSorting" : [[0, "asc"]],
        "aoColumnDefs" : [{"bSortable" : false, "aTargets" : [1]}],
        "sDom" : '<"wrapper"flipt>',
        "sPaginationType" : "full_numbers"
      });

  $("#hashtag_table_paginate").addClass("aui-buttons");
});
