/***************************************************************************************************
*
*-- Form validation script by Peter Bailey, Copyright (c) 2001-2003
*	Version 5.01b
*	Updated on Feb 07, 2004
*	www.peterbailey.net
*	me@peterbailey.net
*
*	IF YOU USE THIS SCRIPT, GIVE ME CREDIT PLEASE =)
*
*	Visit http://www.peterbailey.net/fValidate/ for more info
*
*	Feel free to contact me with any questions, comments, problems, or suggestions
*
*	Note: This document most easily read with tab spacing set to 4
*
*******************************************************************************************************/

/*	Create static fvalidate object
------------------------------------------- */
if ( typeof fvalidate == 'undefined' )
{
	var fvalidate = new Object();
}

/*	Generic event handling
------------------------------------------- */
fvalidate.addEvent = function( obj, evt, fn, useCapture )
{
	if ( typeof obj.attachEvent != 'undefined' )
	{
		obj.attachEvent( "on" + evt, fn );
	}
	else if ( typeof obj.attachEventListener != 'undefined' )
	{
		obj.addEventListener( evt, fn, Boolean( useCapture ) );
	}
}
fvalidate.addEvents = function( obj, evts, fn, useCapture )
{
	var i = 0, evt;
	while( evt = evts[i++] )
	{
		this.addEvent( obj, evt, fn, Boolean( useCapture ) );
	}
}

/*	Main validation routine
------------------------------------------- */
function validateForm( f, bConfirm, bDisable, bDisableR, groupError, errorMode )
{
	//	Set defaults
	bConfirm	= Boolean( bConfirm );
	bDisable	= Boolean( bDisable );
	bDisableR	= Boolean( bDisableR );
	groupError	= Boolean( groupError );
	errorMode	= ( typeof errorMode != 'undefined' ) ? parseInt( errorMode, 10 ) : 0;

	//	Init vars and fValidate object
	var params, fvCode, type;
	if ( typeof f.fv == 'undefined' )
	{
		f.fv = new fValidate( f, errorMode, groupError );
	} else {		
		f.fv._reset();
		f.fv.errorMode = errorMode;
	}
	
	//	Loop through all form elements	
	var elem, i = 0, attr = f.fv.config.code;
	while ( elem = f.elements[i++] )
	{
		//	Skip fieldsets
		if ( elem.nodeName == "FIELDSET" ) continue;

		//	Does element have validator attribute? (short-circuit check)
		fvCode			= ( elem[attr] ) ? elem[attr] : elem.getAttribute( attr );
		if ( !( typeof fvCode == 'undefined' || fvCode == null || fvCode == "" ) )
		{
			//	Set params, validation type, and validation state
			params			= fvCode.split( "|" );
			type			= params[0];
			elem.validated	= true;
			
			//	Valid validator type?
			if ( typeof f.fv[type] == 'undefined' )
			{				
				f.fv.devError( [type, elem.name], 'notFound' );
				return false;
			}
			
			//	Check for modifiers
			switch( params.last() )
			{				
				case 'bok'	:	//	bok requested
					params = params.reduce( 1, 1 );
					elem.bok = true;
					break;
				case 'if'	:	//	Conditional validation requested
					params = params.reduce( 1, 1 );
					elem._if_ = true;
					break;
				case 'then'	:	//	Conditional validation requested
					params = params.reduce( 1, 1 );
					elem._then_ = true;
					break;
				default		:	//	No modifiers
					params = params.reduce( 1, 0 );
				
			}

			//	Is element an array?
			if ( /radio|checkbox/.test( elem.type ) )
			{
				//	Set group property
				elem.group = f.elements[elem.name];
			}
			
			//	Add events if not already added
			if ( typeof elem.fName == 'undefined' )
			{
				//	If element is an array			
				if ( typeof elem.group != 'undefined' )
				{
					for ( var j = 0; j < elem.group.length; j++ )				
					{
						//	Apply event-function to each child
						if ( f.fv.config.clearEvent != null )
						{
						//	fvalidate.addEvent( elem.group.item( j ), fv.config.clearEvent, fv.revertError, false );
							addEvent( elem.group.item( j ), f.fv.config.clearEvent, f.fv, 'revertError', false );
						}
					}
				}
				else
				{
					//	Apply event-function to element
				//	fvalidate.addEvent( elem, fv.config.clearEvent, fv.revertError, false );
					addEvent( elem, f.fv.config.clearEvent, f.fv, 'revertError', false );
				}
			}
			
			//	Set formatted name, current element
			elem.fName	= elem.name.format();
			f.fv.elem		= elem;
			f.fv.type		= type;

			//	Create function to call the proper validator method of the fValidate class
			var func = new Function( "obj", "method", "obj[method]( " + params.toArgString() + " );" );
			func( f.fv, type );
		
			//	If element test failed AND group error is off, return false
			if ( elem.validated == false && groupError == false ) return false;
			
			//	Clear error if field okay
			if ( elem.validated == true ) f.fv.revertError();
		}
	} //	end of element loop
	
	//	If group error, show it
	if ( groupError ) f.fv.showGroupError();

	//	Return false if errors found
	if ( f.fv.errors.length > 0 ) return false;

	//	Show pre-submission confirmation
	if ( bConfirm && !confirm( f.fv.config.confirmMsg ) )
	{
		if ( f.fv.config.confirmAbortMsg != '' ) alert( f.fv.config.confirmAbortMsg );
		return false;
	}
	
	//	Disable reset and/or submit buttons if requested
	if ( bDisable ) 
	{
		if ( typeof f.fv.config.submitButton == 'object' )
		{
			var sb, j = 0;
			while( sb = f.fv.config.submitButton[j++] )
			{
				if ( f.fv.elementExists( sb ) )
				{
					f.elements[sb].disabled = true;
				}
			}
		}
		else if ( f.fv.elementExists( f.fv.config.submitButton ) )
		{
			f.elements[f.fv.config.submitButton].disabled = true;
		}
	}
	if ( bDisableR && f.fv.elementExists( f.fv.config.resetButton ) )
	{
		f.elements[f.fv.config.resetButton].disabled = true;
	}

	//	Successful Validation.  Submit form
	return true;
	
	function addEvent( elem, evt, obj, method, capture )
	{
		var self = elem;
		if ( typeof elem.attachEvent != 'undefined' )
		{
			elem.attachEvent( "on" + evt, function() { obj[method]( self ) } );
		}
		else if ( typeof elem.addEventListener != 'undefined' )
		{
			elem.addEventListener( evt, function() { obj[method]( self ) }, capture );
		}
		else if ( f.fv.config.eventOverride )
		{
			eleme['on' + evt] = function() { obj[method]( self ) };
		}
	}
}

/*	Constructor
------------------------------------------- */
function fValidate( f, errorMode, groupError )
{
	var self        = this;
	this.form       = f;
	this.errorMode  = errorMode;
	this.groupError = groupError;
	this.errors     = new Array();
	this.validated  = true;
	this.config     = new fValConfig();
	this.i18n		= fvalidate.i18n;
	
	//	Add reset action to clear visual error cues
	f.onreset = function()
	{
		var elem, i = 0;
		while ( elem = this.elements[i++] )
		{
			self.revertError( elem );
		}
	}
	
	addLabelProperties();
	
	//	Parses form and adds label properties to elements that have one specified
	function addLabelProperties()
	{
		//	Collect all label elements in form, init vars		
		if ( typeof f.getElementsByTagName == 'undefined' ) return;
		var labels = f.getElementsByTagName( "label" );
		var label, i = j = 0;
		var elem;

		//	Loop through labels retrieved
		while ( label = labels[i++] )
		{
			//	For Opera 6
			if ( typeof label.htmlFor == 'undefined' ) return;
			
			//	Retrieve element
			elem = f.elements[label.htmlFor];
			if ( typeof elem == 'undefined' )
			{	//	No element found for label				
				self.devError( [label.htmlFor], 'noLabel' );
			}
			else if ( typeof elem.label != 'undefined' )
			{	//	label property already added
				continue;
			}
			else if ( typeof elem.length != 'undefined' && elem.length > 1 && elem.nodeName != 'SELECT' )
			{	//	For arrayed elements
				for ( j = 0; j < elem.length; j++ )
				{
					elem.item( j ).label = label;
				}
			}
			//	Regular label
			elem.label = label;
		}
	}		
}

/*	Reset for another validation
------------------------------------------- */
fValidate.prototype._reset = function()
{
	this.errors		= new Array();
	this.showErrors	= new Array();
}

/*	Checks if element exists in form
------------------------------------------- */
fValidate.prototype.elementExists = function( elemName )
{
	return Boolean( typeof this.form.elements[elemName] != 'undefined' );
}

/*	Receives error message and determines action
------------------------------------------- */
fValidate.prototype.throwError = function( args, which )
{
	var elem  = this.elem;

	//	Arrayed element?
	if ( typeof elem.name == 'undefined' )
	{
		elem = elem[0];
	}

	//	Bok requested AND element blank OR conditional validation?
	if ( elem.bok && this.isBlank() )
	{	//	skip		
		elem.validated = true;
		return;
	}

	//	Part of a conditional validation?
	if ( elem.cv )
	{
		return;
	}
	
	//	Set failsafe to false	
	elem.validated = false;

	//	Create error message
	which	= this.setArg( which, 0 );
	args	= this.setArg( args, [] );
	emsgElem = ( typeof this.elem.getAttribute == "undefined" ) ?
			this.elem[0]:
			this.elem;
	if ( emsgElem.getAttribute( this.config.emsg ) )
	{
		var error = emsgElem.getAttribute( this.config.emsg );
	}
	var error = this.translateMessage( args, this.i18n.errors[this.type][which] );

	//	Group error mode?
	if ( this.groupError )
	{
		//	Push error onto stack
		this.errors.push( {'elem':elem, 'msg': error} );		
	}
	else
	{
		//	Process error message		
		this.showError( error, false, emsgElem );

		var focusElem = ( typeof elem.fields != 'undefined' )?
			elem.fields[0]:
			elem;
		
		//	Focus and select elements, if possible
		this.selectFocus( focusElem );
	}
}


/*	Shows error message to user
------------------------------------------- */
fValidate.prototype.showError = function( emsg, last, elem )
{
	//	Set variables
	var self		= this,
		elem		= this.setArg( elem, this.elem ),
		isHidden	= Boolean( elem.type == 'hidden' ),
		label		= ( isHidden ) ? null : elem.label || null,
		emsg		= ( elem.getAttribute( this.config.emsg ) ) ? elem.getAttribute( this.config.emsg ).replace( /\\n/g, "\n" ) : emsg,
		errorClass	= this.config.errorClass,
		singleCSS	= this.config.useSingleClassNames;

	if ( typeof this.showErrors == 'undefined' ) this.showErrors = new Array();	
	
	//	Determine which error modes to use
	switch( this.errorMode )
	{	//	This represents all possible combinations
		case 0  : alertError(); break;
		case 1  : inputError(); break;
		case 2  : labelError(); break;
		case 3  : appendError(); break;
		case 4  : boxError(); break;
		case 5  : inputError(); labelError(); break;
		case 6  : inputError(); appendError(); break;
		case 7  : inputError(); boxError(); break;
		case 8  : inputError(); alertError(); break;
		case 9  : labelError(); appendError(); break;
		case 10 : labelError(); boxError(); break;
		case 11 : labelError(); alertError(); break;
		case 12 : appendError(); boxError(); break;
		case 13 : appendError(); alertError(); break;
		case 14 : boxError(); alertError(); break;
		case 15 : inputError(); labelError(); appendError(); break;
		case 16 : inputError(); labelError(); boxError(); break;
		case 17 : inputError(); labelError(); alertError(); break;
		case 18 : inputError(); appendError(); boxError(); break;
		case 19 : inputError(); appendError(); alertError(); break;
		case 20 : inputError(); boxError(); alertError(); break;
		case 21 : labelError(); appendError(); boxError(); break;
		case 22 : labelError(); appendError(); alertError(); break;
		case 23 : appendError(); boxError(); alertError(); break;
		case 24 : inputError(); labelError(); appendError(); boxError(); break;
		case 25 : inputError(); labelError(); appendError(); alertError(); break;
		case 26 : inputError(); appendError(); boxError(); alertError(); break;
		case 27 : labelError(); appendError(); boxError(); alertError(); break;
		case 28 : inputError(); labelError(); appendError(); boxError(); alertError(); break;		
	}
	//	Regular alert error
	function alertError()
	{
		if ( self.groupError ) self.showErrors.push( emsg );
		else alert( emsg );
		if ( last ) alert( self.i18n.groupAlert + self.showErrors.join( "\n\n- " ) );			
	}
	//	Applies class to form element
	function inputError()
	{
		if ( ( typeof elem.length != 'undefined' && elem.length > 1 && elem.nodeName != 'SELECT' ) || isHidden )
		{
			var subelem, i = 0;
			while( subelem = ( isHidden ) ? elem.fields[i++] : elem.item( i++ ) )			
			{
				if ( subelem.className != '' && singleCSS )
				{
					subelem.revertClass = subelem.className;
					subelem.className = errorClass;
				} else {
					self.addCSSClass( subelem, errorClass );
				}				
			}
		}
		else
		{
			if ( singleCSS )
			{
				elem.revertClass = elem.className;
				elem.className = errorClass;
			} else {
				self.addCSSClass( elem, errorClass );
			}
		}
	}
	//	Applies class to element's label
	function labelError()
	{
		if ( label == null ) return;
		if ( self.config.useSingleClassNames )
		{
			label.className = errorClass;
		} else {
			self.addCSSClass( label, errorClass );
		}
		
	}
	//	Appends error message to element's label
	function appendError()
	{
		if ( label == null || typeof label.innerHTML == 'undefined' ) return;
		if ( typeof label.original == 'undefined' )
			label.original = label.innerHTML;
		label.innerHTML = label.original + " - " + emsg.toHTML();
	}
	//	Appends Error message to pre-defined element
	function boxError()
	{
		if ( typeof self.boxError == 'undefined' ) self.boxError = document.getElementById( self.config.boxError );
		if ( self.boxError == null )
		{			
			self.devError( [self.config.boxError], 'noBox' );
			return;
		}
		if ( typeof self.elem.name == 'undefined' || self.elem.name == "" )
		{
			self.devError( [self.elem[self.config.code]], 'missingName' );
			return;
		}
		var errorId = self.config.boxErrorPrefix + self.elem.name,
			errorElem;
		if ( errorElem = document.getElementById( errorId ) ) // short-circuit
		{
			errorElem.firstChild.nodeValue = emsg.toHTML();
		}
		else
		{
			errorElem = document.createHTMLElement( 'li', { id: errorId, 'innerHTML': emsg.toHTML(), title: self.i18n.boxToolTip } );
			self.boxError.appendChild( errorElem );
			errorElem.onclick = function()
			{
				var elem = self.form.elements[this.id.replace( self.config.boxErrorPrefix, "" )];
				if ( typeof elem.fields != 'undefined' ) elem = elem.fields[0];
				if ( typeof elem.select != 'undefined' ) elem.select();
				if ( typeof elem.focus != 'undefined' ) elem.focus();
			}
		}
		self.boxError.style.display = "block";
	}
}

/*	Handles element className manipulation
------------------------------------------- */
fValidate.prototype.removeCSSClass = function( elem, className )
{
	elem.className = elem.className.replace( className, "" ).trim();
}
fValidate.prototype.addCSSClass = function( elem, className )
{
	this.removeCSSClass( elem, className );
	elem.className = ( elem.className + " " + className ).trim();
}

/*	Processes errors in stack for group error mode
------------------------------------------- */
fValidate.prototype.showGroupError = function()
{
	for ( var error, firstElem, i = 0; ( error = this.errors[i] ); i++ )
	{
		if ( i == 0 ) firstElem = error.elem;
		this.elem = error.elem;
		this.showError( error.msg, Boolean( i == ( this.errors.length - 1 ) ) );
	}
	var focusElem = ( typeof firstElem.fields != 'undefined' )?
		firstElem.fields[0]:
		firstElem;
	this.selectFocus( focusElem );
}

/*	Reverts any visible error notification upon event
------------------------------------------- */
fValidate.prototype.revertError = function( elem )
{
	elem = this.setArg( elem, this.elem );
	var isHidden	= Boolean( elem.type == 'hidden' ),
		errorClass	= this.config.errorClass,
		i			= 0,
		errorElem,
		subelem;

	if ( ( typeof elem.length != 'undefined' && elem.length > 1 && elem.nodeName != 'SELECT' ) || isHidden )
	{
		if ( isHidden && typeof elem.fields != 'undefined' )
		{		
			while( subelem = ( isHidden ) ? elem.fields[i++] : elem.item( i++ ) )		
			{
				if ( typeof subelem.revertClass != 'undefined' )
				{
					subelem.className = subelem.revertClass;
				}
			}
		}
	} else {
		if ( this.config.useSingleClassNames )
		{
			if ( typeof subElement.revertClass != 'undefined' )
			{
				elem.className = elem.revertClass;
			}
		} else {
			this.removeCSSClass( elem, errorClass );
		}		
	}
	if ( typeof elem.label != 'undefined' )
	{
		if ( this.config.useSingleClassNames )
		{
			elem.label.className = '';
		} else {
			this.removeCSSClass( elem.label, errorClass );
		}
		elem.label.innerHTML = ( elem.label.original || elem.label.innerHTML );
	}
	if ( typeof this.boxError != 'undefined' )
	{
		if ( typeof this.boxError.normalize != 'undefined' ) this.boxError.normalize();
		if ( errorElem = document.getElementById( this.config.boxErrorPrefix + elem.name ) )
		{
			this.boxError.removeChild( errorElem );
		}
		if ( this.boxError.childNodes.length == 0 ) this.boxError.style.display = "none";
	}
}

/*	Focus and select elements, if possible
------------------------------------------- */
fValidate.prototype.selectFocus = function( elem )
{
	if ( typeof elem.select != 'undefined' ) elem.select();
	if ( typeof elem.focus != 'undefined' )  elem.focus();
}

/*	Developer assistance method - shows error if validator/element-type mismatch
------------------------------------------- */
fValidate.prototype.typeMismatch = function()
{
	var pats = {
		'text':		'text|password|textarea',
		'ta':		'textarea',
		'hidden':	'hidden',
		's1':		'select-one',
		'sm':		'select-multiple',
		'select':	'select-one|select-multiple',
		'rg':		'radio',
		'radio':	'radio',
		'cb':		'checkbox',
		'file':		'file'
		};
	var fail		= false,
		expected	= new Array(),
		result = key = type = regex = "";
	for ( var i = 0; i < arguments.length; i++ )
	{
		type	= pats[arguments[i]];
		regex	= new RegExp( type );
		result	+= ( regex.test( this.elem.type ) ) ? "1" : "0";
		key		+= "0";
		expected.push( type );		
	}
	if ( key ^ result == 0 )
	{
		this.devError( [this.elem.fName, this.elem.type, expected.join( "|" ).replace( /\|/g, this.i18n.or )], 'mismatch' );
		this.elem.validated = false;
		return true;
	}
	return false;
}

/*	Returns value(s) of reference element passed
------------------------------------------- */
fValidate.prototype.getValue = function( elem )
{
	switch ( elem.type )
	{
		case 'text' :
		case 'password' :
		case 'textarea' :
		case 'hidden' :
		case 'file' :
			return elem.value;
		case 'radio':
		case 'select-single':
			if ( typeof elem.length == 'undefined' )
			{
				return elem.value;
			} else {
				for ( var i = 0; i < elem.length; i++ )
				{
					choice = ( elem.type == 'radio' ) ? "checked" : "selected";
					if ( elem[i][choice] )
					{
						return elem[i].value;
					}
				}
			}
		case 'select-multiple' :
		case 'checkbox' :
			if ( typeof elem.length == 'undefined' )
			{
				return elem.value
			} else {
				var returnValues = new Array();
				for ( var i = 0; i < elem.length; i++ )
				{
					choice = ( elem.type == 'checkbox' ) ? "checked" : "selected";
					if ( elem[i][choice] )
					{
						returnValues.push( elem[i].value );
					}
				}
				return returnValues;
			}
		default: return null;
	}
}

/*	Generic argument setting method
------------------------------------------- */
fValidate.prototype.setArg = function( arg, def )
{
	return ( typeof arg == 'undefined' || arg == '' || arg == null ) ? def : arg;
}

/*	Blank checker.  Optional string argument for evaluating element other than current
------------------------------------------- */
fValidate.prototype.isBlank = function( el )
{
	var elem = this.form.elements[el] || this.elem;
	return Boolean( /^\s*$/.test( elem.value ) );
}

/*	Translates messages using language file
------------------------------------------- */
fValidate.prototype.translateMessage = function( args, format )
{
	var msg		= ""
	for ( var i = 0; i < format.length; i++ )
	{			
			msg += ( typeof format[i] == 'number' ) ? args[format[i]] : format[i];
	}
	return msg;
}

/*	Throws developer errors
------------------------------------------- */
fValidate.prototype.devError = function( args, which )
{
	if ( typeof args == 'string' )
	{
		which = args;
		args = [];
	}
	which = this.setArg( which, this.type );
	var format = this.i18n.devErrors[which];
	var a = [
		this.i18n.devErrors.lines[0],
		'----------------------------------------------------------------------------------------------',
		this.translateMessage( args, format ),
		'----------------------------------------------------------------------------------------------',
		this.i18n.devErrors.lines[1]
		];
	alert( a.join( "\n" ) );
}

/*	Throws specific developer error
------------------------------------------- */
fValidate.prototype.paramError = function( param, elemName )
{
	elemName = this.setArg( elemName, this.elem.name );
	this.devError( [param, this.type, elemName], 'paramError' );
}
/* Non-fValidate methods *****************************************/

/*	For easy creation of DOM nodes
------------------------------------------- */
document.createHTMLElement = function( elemName, attribs )
{
	if ( typeof document.createElement == 'undefined' ) return;
	var elem = document.createElement( elemName );
	if ( typeof attribs != 'undefined' )
	{
		for ( var i in attribs )
		{
			switch ( true )
			{
				case ( i == 'text' )  : elem.appendChild( document.createTextNode( attribs[i] ) ); break;
				case ( i == 'class' ) : elem.className = attribs[i]; break;
				default : elem.setAttribute( i, '' ); elem[i] = attribs[i];
			}
		}
	}
	return elem;    
}

/*	Trims b items from the beginning of the array, e items from the end
------------------------------------------- */
Array.prototype.reduce = function( b, e )
{
	var a = new Array();
	var count = 0;
	for ( var i = b; i < this.length - e; i++ )
	{
		a[count++] = this[i];
	}
	return a;
}

/*	Returns array as argument-compatible string
------------------------------------------- */
Array.prototype.toArgString = function()
{
	var a = new Array();
	for ( var i = 0; i < this.length; i++ )
	{
		a.push( "'" + this[i] + "'" );
	}	
	return a.toString();
}

/*	Prototype push if missing
------------------------------------------- */
if ( typeof Array.push == 'undefined' )
Array.prototype.push = function()
{
	var arg, i = 0;
	while( arg = arguments[i++] )
	{
		this[this.length] = arg;
	}
	return this.length;
}

/*	Returns last item of the array
------------------------------------------- */
Array.prototype.last = function()
{
	return this[this.length-1];
}

/*	Removes the follow charaters _[] from an elements name for human-reading
------------------------------------------- */
String.prototype.format = function()
{
	return this.replace( /\_/g, " ").replace( /\[|\]/g, "" );
}

/*	Replaces newline characters with XHTML BR tags
------------------------------------------- */
String.prototype.toHTML = function()
{
	return this.replace( /\n/g, "<br />" ).replace( /\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;" );
}

/*	Trims leading and trailing whitespace from string
------------------------------------------- */
String.prototype.trim = function()
{
	return this.replace( /^\s+|\s+$/, "" );
}

/*	Escapes necessary charactes for string-generated regular expressions
------------------------------------------- */
String.prototype.toPattern = function()
{
	return this.replace( /([\.\*\+\{\}\(\)\<\>\^\$\\])/g, "\\$1" );
}
//	EOF