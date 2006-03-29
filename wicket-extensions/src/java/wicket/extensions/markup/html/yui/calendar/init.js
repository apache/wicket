var ${javaScriptId};
function init${javaScriptId}() {
	${javaScriptId} = new YAHOO.widget.Calendar("${javaScriptId}","${elementId}");
	${javaScriptId}.Options.NAV_ARROW_LEFT = "${navigationArrowLeft}";
	${javaScriptId}.Options.NAV_ARROW_RIGHT = "${navigationArrowRight}";
	${javaScriptId}.render();
}
