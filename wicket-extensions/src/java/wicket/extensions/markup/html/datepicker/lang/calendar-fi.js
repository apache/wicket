// ** I18N

// Calendar FI language (Finnish, Suomi)
// Author: Jarno K?yhk?, <gambler@phnet.fi>
// Encoding: UTF-8
// Distributed under the same terms as the calendar itself.

// full day names
Calendar._DN = new Array
("Sunnuntai",
 "Maanantai",
 "Tiistai",
 "Keskiviikko",
 "Torstai",
 "Perjantai",
 "Lauantai",
 "Sunnuntai");

// short day names
Calendar._SDN = new Array
("Su",
 "Ma",
 "Ti",
 "Ke",
 "To",
 "Pe",
 "La",
 "Su");

// full month names
Calendar._MN = new Array
("Tammikuu",
 "Helmikuu",
 "Maaliskuu",
 "Huhtikuu",
 "Toukokuu",
 "Kes?kuu",
 "Hein?kuu",
 "Elokuu",
 "Syyskuu",
 "Lokakuu",
 "Marraskuu",
 "Joulukuu");

// short month names
Calendar._SMN = new Array
("Tam",
 "Hel",
 "Maa",
 "Huh",
 "Tou",
 "Kes",
 "Hei",
 "Elo",
 "Syy",
 "Lok",
 "Mar",
 "Jou");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Tietoja kalenterista";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"Uusin versio osoitteessa: http://www.dynarch.com/projects/calendar/\n" +
"Julkaistu GNU LGPL lisenssin alaisuudessa. Lis?tietoja osoitteessa http://gnu.org/licenses/lgpl.html" +
"\n\n" +
"P?iv?m??r? valinta:\n" +
"- K?yt? \xab, \xbb painikkeita valitaksesi vuosi\n" +
"- K?yt? " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " painikkeita valitaksesi kuukausi\n" +
"- Pit?m?ll? hiiren painiketta mink? tahansa yll? olevan painikkeen kohdalla, saat n?kyviin valikon nopeampaan siirtymiseen.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Ajan valinta:\n" +
"- Klikkaa kellonajan numeroita lis?t?ksesi aikaa\n" +
"- tai pit?m?ll? Shift-n?pp?int? pohjassa saat aikaa taaksep?in\n" +
"- tai klikkaa ja pid? hiiren painike pohjassa sek? liikuta hiirt? muuttaaksesi aikaa nopeasti eteen- ja taaksep?in.";

Calendar._TT["PREV_YEAR"] = "Edell. vuosi (paina hetki, n?et valikon)";
Calendar._TT["PREV_MONTH"] = "Edell. kuukausi (paina hetki, n?et valikon)";
Calendar._TT["GO_TODAY"] = "Siirry t?h?n p?iv??n";
Calendar._TT["NEXT_MONTH"] = "Seur. kuukausi (paina hetki, n?et valikon)";
Calendar._TT["NEXT_YEAR"] = "Seur. vuosi (paina hetki, n?et valikon)";
Calendar._TT["SEL_DATE"] = "Valitse p?iv?m??r?";
Calendar._TT["DRAG_TO_MOVE"] = "Siirr? kalenterin paikkaa";
Calendar._TT["PART_TODAY"] = " (t?n??n)";
Calendar._TT["MON_FIRST"] = "N?yt? maanantai ensimm?isen?";
Calendar._TT["SUN_FIRST"] = "N?yt? sunnuntai ensimm?isen?";
Calendar._TT["CLOSE"] = "Sulje";
Calendar._TT["TODAY"] = "T?n??n";
Calendar._TT["TIME_PART"] = "(Shift-) Klikkaa tai liikuta muuttaaksesi aikaa";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Display %s first";

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
