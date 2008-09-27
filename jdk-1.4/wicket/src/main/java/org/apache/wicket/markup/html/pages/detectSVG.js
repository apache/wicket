// Javascript-based SVG support detection
// From http://thomas.tanrei.ca/modern-script-based-svg-detection/
function detectSVG()
{
	var results = { support:null, plugin:null, builtin:null };
	var obj = null;
	if ( navigator && navigator.mimeTypes && navigator.mimeTypes.length )
	{
		for ( var mime in { "image/svg+xml":null, "image/svg":null, "image/svg-xml":null } )
		{
			if ( navigator.mimeTypes[ mime ] && ( obj = navigator.mimeTypes[ mime ].enabledPlugin ) && obj )
				results = { plugin:( obj = obj.name.toLowerCase()) && obj.indexOf( "adobe" ) >= 0 ? "Adobe" : ( obj.indexOf( "renesis" ) >= 0 ? "Renesis" : "Unknown" ) };
		}
	}
	else if ( ( obj = document.createElement( "object" )) && obj && typeof obj.setAttribute( "type", "image/svg+xml" ))
	{
		if ( typeof obj.USE_SVGZ == "string" )
			results = { plugin:"Adobe", IID:"Adobe.SVGCtl", pluginVersion:obj.window && obj.window._window_impl ? ( obj.window.evalScript ? 6 : 3 ) : 2 };
		else if ( typeof obj.ReadyState == "number" && obj.ReadyState == 0 )
			results = { plugin:"Renesis", IID:"RenesisX.RenesisCtrl.1", pluginVersion:">=1.0" };
		else if ( obj.window && obj.window.getSVGViewerVersion().indexOf( "enesis" ) > 0 )
			results = { plugin:"Renesis", IID:"RenesisX.RenesisCtrl.1", pluginVersion:"<1.0" };
	}
	results.IID = ( results.plugin == "Adobe" ? "Adobe.SVGCtl" : ( results.plugin == "Renesis" ? "renesisX.RenesisCtrl.1" : null ));

	// Does the browser support SVG natively? Gecko claims no support if a plugin is active, but still gives back an NSI interface. Safari 3 does not claim support but does - use devicePixelRatio
	var claimed = !!window.devicePixelRatio || ( typeof SVGAngle == "object" || ( document && document.implementation && document.implementation.hasFeature( "org.w3c.dom.svg", "1.0" )));
	var nsi = window.Components && window.Components.interfaces && !!Components.interfaces.nsIDOMGetSVGDocument;
	results.builtin = claimed ? ( !!window.opera ? "Opera" : ( nsi ? "Gecko" : "Safari" )) : ( !!window.opera && window.opera.version ? "Opera" : ( nsi ? "Gecko" : null ));
	results.builtinVersion = results.builtin && !!window.opera ? parseFloat( window.opera.version()) : ( nsi ? ( typeof Iterator == "function" ? ( Array.reduce ? 3.0 : 2.0 ) : 1.5 ) : null );

	// Which is active, the plugin or native support? Opera 9 makes it hard to tell..
	if ( !!window.opera && results.builtinVersion >= 9 && ( obj = document.createElement( "object" )) && obj && typeof obj.setAttribute( "type", "image/svg+xml" ) != "undefined" && document.appendChild( obj ))
	{
		results.support = obj.offsetWidth ? "Plugin" : "Builtin";
		document.removeChild( obj );
	}
	else	results.support = results.plugin && !claimed ? "Plugin" : ( results.builtin && claimed ? "Builtin" : null );

	return results;
}
