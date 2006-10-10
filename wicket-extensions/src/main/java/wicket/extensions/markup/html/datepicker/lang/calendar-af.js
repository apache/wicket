// ** I18N Afrikaans
Calendar._DN = new Array
("Sondag",
 "Maandag",
 "Dinsdag",
 "Woensdag",
 "Donderdag",
 "Vrydag",
 "Saterdag",
 "Sondag");
Calendar._MN = new Array
("Januarie",
 "Februarie",
 "Maart",
 "April",
 "Mei",
 "Junie",
 "Julie",
 "Augustus",
 "September",
 "Oktober",
 "November",
 "Desember");
 
// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// tooltips
Calendar._TT = {};
Calendar._TT["TOGGLE"] = "Verander eerste dag van die week";
Calendar._TT["PREV_YEAR"] = "Vorige jaar (hou vir keuselys)";
Calendar._TT["PREV_MONTH"] = "Vorige maand (hou vir keuselys)";
Calendar._TT["GO_TODAY"] = "Gaan na vandag";
Calendar._TT["NEXT_MONTH"] = "Volgende maand (hou vir keuselys)";
Calendar._TT["NEXT_YEAR"] = "Volgende jaar (hou vir keuselys)";
Calendar._TT["SEL_DATE"] = "Kies datum";
Calendar._TT["DRAG_TO_MOVE"] = "Kliek en skuif";
Calendar._TT["PART_TODAY"] = " (vandag)";
Calendar._TT["MON_FIRST"] = "Vertoon Maandag eerste";
Calendar._TT["SUN_FIRST"] = "Vertoon Sondag eerste";
Calendar._TT["CLOSE"] = "Maak toe";
Calendar._TT["TODAY"] = "Vandag";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Vertoon %s eerste";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["TIME_PART"] = "(Shift-)Click or drag to change value";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "Tijd:";