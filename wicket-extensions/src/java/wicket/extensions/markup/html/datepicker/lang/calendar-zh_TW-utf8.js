// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mihai_bazon@yahoo.com>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("\u9031\u65e5",
 "\u9031\u4e00",
 "\u9031\u4e8c",
 "\u9031\u4e09",
 "\u9031\u56db",
 "\u9031\u4e94",
 "\u9031\u516d",
 "\u9031\u65e5");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("\u9031\u65e5",
 "\u9031\u4e00",
 "\u9031\u4e8c",
 "\u9031\u4e09",
 "\u9031\u56db",
 "\u9031\u4e94",
 "\u9031\u516d",
 "\u9031\u65e5");

// First day of the week. "0" means display Sunday first, "1" means display
// Monday first, etc.
Calendar._FD = 1;

// full month names
Calendar._MN = new Array
("\u4e00\u6708",
 "\u4e8c\u6708",
 "\u4e09\u6708",
 "\u56db\u6708",
 "\u4e94\u6708",
 "\u516d\u6708",
 "\u4e03\u6708",
 "\u516b\u6708",
 "\u4e5d\u6708",
 "\u5341\u6708",
 "\u5341\u4e00\u6708",
 "\u5341\u4e8c\u6708");

// short month names
Calendar._SMN = new Array
("1\u6708",
 "2\u6708",
 "3\u6708",
 "4\u6708",
 "5\u6708",
 "6\u6708",
 "7\u6708",
 "8\u6708",
 "9\u6708",
 "10\u6708",
 "11\u6708",
 "12\u6708");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "\u95dc\u65bc";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2005 / Author: Mihai Bazon\n" + // don't translate this this ;-)
"For latest version visit: http://www.dynarch.com/projects/calendar/\n" +
"Distributed under GNU LGPL.  See http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"\u65e5\u671f\u9078\u64c7\u65b9\u6cd5:\n" +
"- \u4f7f\u7528 \xab, \xbb \u6309\u9215\u4f86\u9078\u64c7\u5e74\u4efd\n" +
"- \u4f7f\u7528" + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " \u6309\u9215\u4f86\u9078\u64c7\u6708\u4efd\n" +
"- \u6309\u4f4f\u4e0a\u9762\u7684\u6309\u9215\u53ef\u4ee5\u52a0\u5feb\u9078\u53d6\u3002";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"\u6642\u9593\u9078\u64c7\u65b9\u6cd5:\n" +
"- \u9ede\u64ca\u4efb\u4f55\u7684\u6642\u9593\u90e8\u4efd\u53ef\u589e\u52a0\u5176\u503c\n" +
"- \u540c\u6642\u6309Shift\u9375\u518d\u9ede\u64ca\u53ef\u6e1b\u5c11\u5176\u503c\n" +
"- \u9ede\u64ca\u4e26\u62d6\u66f3\u53ef\u52a0\u5feb\u6539\u8b8a\u7684\u503c\u3002";

Calendar._TT["PREV_YEAR"] = "\u4e0a\u4e00\u5e74 (\u6309\u4f4f\u8df3\u51fa\u9078\u55ae)";
Calendar._TT["PREV_MONTH"] = "\u4e0a\u4e00\u500b\u6708 (\u6309\u4f4f\u8df3\u51fa\u9078\u55ae)";
Calendar._TT["GO_TODAY"] = "\u5230\u4eca\u5929";
Calendar._TT["NEXT_MONTH"] = "\u4e0b\u4e00\u500b\u6708 (\u6309\u4f4f\u8df3\u51fa\u9078\u55ae)";
Calendar._TT["NEXT_YEAR"] = "\u4e0b\u4e00\u5e74 (\u6309\u4f4f\u8df3\u51fa\u9078\u55ae)";
Calendar._TT["SEL_DATE"] = "\u9078\u64c7\u65e5\u671f";
Calendar._TT["DRAG_TO_MOVE"] = "\u62d6\u66f3\u6ed1\u9f20\u79fb\u52d5\u8996\u7a97";
Calendar._TT["PART_TODAY"] = " (\u4eca\u5929)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "\u5c07 %s \u986f\u793a\u5728\u524d";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "\u95dc\u9589";
Calendar._TT["TODAY"] = "\u4eca\u5929";
Calendar._TT["TIME_PART"] = "\u9ede\u64caor\u62d6\u66f3\u53ef\u6539\u8b8a\u6642\u9593(\u540c\u6642\u6309Shift\u70ba\u6e1b)";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "\u9031";
Calendar._TT["TIME"] = "\u6642\u9593:";
