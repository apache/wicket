// ** I18N
Calendar._DN = new Array
("Søndag",
 "Mandag",
 "Tirsdag",
 "Onsdag",
 "Torsdag",
 "Fredag",
 "Lørdag",
 "Søndag");

Calendar._SDN_len = 2;

Calendar._MN = new Array
("Januar",
 "Februar",
 "Marts",
 "April",
 "Maj",
 "Juni",
 "Juli",
 "August",
 "September",
 "Oktober",
 "November",
 "December");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Info";

Calendar._TT["ABOUT"] =
"DHTML Dato/Tid vælger\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" +
"Find seneste version her: http://www.dynarch.com/projects/calendar/\n" +
"Distribueret under GNU LGPL.  Se http://gnu.org/licenses/lgpl.html for detaljer." +
"\n\n" +
"Valg af dato:\n" +
"- Anvend \xab \xbb knapperne for at vælge år\n" +
"- Anvend " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " knapperne for at vælge måned\n" +
"- Hold museknappen nede på de nævnte knapper for hurtigere udvælgelse.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Valg af tid:\n" +
"- Klik på tidsangivelsen for at gøre den større\n" +
"- eller Skift-klik for at gøre den mindre\n" +
"- eller hold museknappen nede og træk for hurtigere udvælgelse.";

Calendar._TT["PREV_YEAR"] = "Forrige år (hold for menu)";
Calendar._TT["PREV_MONTH"] = "Forrige måned (hold for menu)";
Calendar._TT["GO_TODAY"] = "Gå til idag";
Calendar._TT["NEXT_MONTH"] = "Næste måned (hold for menu)";
Calendar._TT["NEXT_YEAR"] = "Næste år (hold for menu)";
Calendar._TT["SEL_DATE"] = "Vælg dato";
Calendar._TT["DRAG_TO_MOVE"] = "Træk for at flytte";
Calendar._TT["PART_TODAY"] = " (idag)";

Calendar._TT["DAY_FIRST"] = "Vis %s først";

Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Luk";
Calendar._TT["TODAY"] = "(idag)";
Calendar._TT["TIME_PART"] = "(Skift-)Klik eller træk for at ændre værdi";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d-%m-%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %e %b %Y";

Calendar._TT["WK"] = "uge";
Calendar._TT["TIME"] = "Tid:";