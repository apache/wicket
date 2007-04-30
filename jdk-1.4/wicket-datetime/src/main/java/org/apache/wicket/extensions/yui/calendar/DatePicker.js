YAHOO.namespace("wicket");

// init the date picker
function init${widgetId}DpJs() {

 // create date picker instance
 YAHOO.wicket.${widgetId}DpJs = new YAHOO.widget.Calendar("${widgetId}DpJs","${widgetId}Dp", { ${calendarInit} });

 // inner function to show the calendar
 function showCalendar() {
    var dateValue = YAHOO.util.Dom.get("${widgetId}").value;

    if (dateValue) {
      dateValue = Wicket.DateTime.parseDate('${datePattern}', dateValue);
      YAHOO.wicket.${widgetId}DpJs.select(dateValue);
      var firstDate = YAHOO.wicket.${widgetId}DpJs.getSelectedDates()[0];
      YAHOO.wicket.${widgetId}DpJs.cfg.setProperty("pagedate", (firstDate.getMonth() + 1) + "/" + firstDate.getFullYear());
      YAHOO.wicket.${widgetId}DpJs.render();
    }

    YAHOO.wicket.${widgetId}DpJs.show();
  }

  // trigger popping up the date picker when the icon is clicked 
  YAHOO.util.Event.addListener("${widgetId}Icon", "click", showCalendar, YAHOO.wicket.${widgetId}DpJs, true);

  // inner function for handling calendar selects  
  function selectHandler(type, args, cal) {
    var selDateArray = args[0][0];
    var yr = selDateArray[0];
    var month = selDateArray[1];
    var dt = selDateArray[2];
    var val = '${datePattern}'.replace(/d+/, dt).replace(/M+/, month).replace(/y+/, yr);
    YAHOO.util.Dom.get("${widgetId}").value = val;

    // hide picker
    cal.hide();
  }

  // register the select handler function
  YAHOO.wicket.${widgetId}DpJs.selectEvent.subscribe(selectHandler, YAHOO.wicket.${widgetId}DpJs);
  
  // now that everything is set up, render the date picker
  YAHOO.wicket.${widgetId}DpJs.render();
}