/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.ecommerce.js

	Included Validators
	-------------------
	money
	cc

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.money = function( ds, grp, dml )
{
	if ( this.typeMismatch( 'text' ) ) return;
	
	ds  = ( ds == ' ' )  ? false : ds.toPattern();
	grp = ( grp == ' ' ) ? false : grp.toPattern();
	dml = ( dml == ' ' ) ? false : dml.toPattern();
	
	var moneySyntax, pattern;
	
	switch( true )
	{
		case Boolean( ds && grp && dml ):		// Dollar sign, grouping, and decimal
			pattern		= "^" + ds + "(?:(?:[0-9]{1,3}" + grp + ")(?:[0-9]{3}" + grp + ")*[0-9]{3}|[0-9]{1,3})(" + dml + "[0-9]{2})$";
			moneySyntax = ds + "XX" + grp + "XXX" + dml + "XX";
			break;
		case Boolean( ds && grp && !dml ):		// Dollar sign and grouping
			pattern		= "^" + ds + "(?:(?:[0-9]{1,3}" + grp + ")(?:[0-9]{3}" + grp + ")*[0-9]{3}|[0-9]{1,3})$";
			moneySyntax = "" + ds + "XX" + grp + "XXX";
			break;
		case Boolean( ds && !grp && dml ):		// Dollar sign and decimal
			pattern		="^" + ds + "[0-9]*(\\.[0-9]{2})$";
			moneySyntax ="" + ds + "XXXXX" + dml + "XX";
			break;
		case Boolean( !ds && grp && dml ):		// Grouping and decimal
			pattern		="^(?:(?:[0-9]{1,3}" + grp + ")(?:[0-9]{3}" + grp + ")*[0-9]{3}|[0-9]{1,3})(" + dml + "[0-9]{2})?$";
			moneySyntax ="XX" + grp + "XXX" + dml + "XX";
			break;
		case Boolean( ds && !grp && !dml ):		// Dollar sign only
			pattern		="^" + ds + "[0-9]*$";
			moneySyntax ="" + ds + "XXXXX";
			break;
		case Boolean( !ds && grp && !dml ):		// Grouping only
			pattern		="^(?:(?:[0-9]{1,3}" + grp + ")(?:[0-9]{3}" + grp + ")*[0-9]{3}|[0-9]{1,3})$";
			moneySyntax ="XX" + grp + "XXX";
			break;
		case Boolean( !ds && !grp && dml ):		// Decimal only
			pattern		="^[0-9]*(" + dml + "[0-9]{2})$";
			moneySyntax ="XXXXX" + dml + "XX";
			break;
		case Boolean( !ds && !grp && !dml ):	// No params set, all special chars become optional
			pattern		="^.?(?:(?:[0-9]{1,3}.?)(?:[0-9]{3}.?)*[0-9]{3}|[0-9]{1,3})(.[0-9]{2})?$";
			moneySyntax ="[?]XX[?]XXX[?XX]";
	}
			
	var regex = new RegExp( pattern );
	if ( !regex.test( this.elem.value ) )
	{
		this.throwError( [this.elem.fName, moneySyntax.replace( /\\/g, '' )] );
	}
}

fValidate.prototype.cc = function()
{
	if ( this.typeMismatch( 'text' ) ) return;
	var typeElem = this.form.elements[this.config.ccType];
	if ( !typeElem )
	{
		this.devError( 'noCCType' )
		return;
	}
	var ccType   = typeElem.options[typeElem.selectedIndex].value.toUpperCase();
	var types    = {
		'VISA'    : /^4\d{12}(\d{3})?$/,
		'MC'      : /^5[1-5]\d{14}$/,
		'DISC'    : /^6011\d{12}$/,
		'AMEX'    : /^3[4|7]\d{13}$/,        
		'DINERS'  : /^3[0|6|8]\d{12}$/,
		'ENROUTE' : /^2[014|149]\d{11}$/,
		'JCB'     : /^3[088|096|112|158|337|528]\d{12}$/,
		'SWITCH'  : /^(49030[2-9]|49033[5-9]|49110[1-2]|4911(7[4-9]|8[1-2])|4936[0-9]{2}|564182|6333[0-4][0-9]|6759[0-9]{2})\d{10}(\d{2,3})?$/,
		'DELTA'   : /^4(1373[3-7]|462[0-9]{2}|5397[8|9]|54313|5443[2-5]|54742|567(2[5-9]|3[0-9]|4[0-5])|658[3-7][0-9]|659(0[1-9]|[1-4][0-9]|50)|844[09|10]|909[6-7][0-9]|9218[1|2]|98824)\d{10}$/,
		'SOLO'    : /^(6334[5-9][0-9]|6767[0-9]{2})\d{10}(\d{2,3})?$/
		};
	if ( typeElem.validated == false && this.groupError == true ) return;
	if ( typeof types[ccType] == 'undefined' && typeElem.validated == false && this.groupError == false )
	{
		this.devError( [ccType] );
		return;
	}
	this.elem.value = this.elem.value.replace( /[^\d]/g, "" );
	if ( !types[ccType].test( this.elem.value ) || !this.elem.value.luhn() )
	{
		this.throwError( [this.elem.fName] );
	}
}

String.prototype.luhn = function()
{
	var i = this.length;
	var checkSum = "", digit;
	while ( digit = this.charAt( --i ) )
	{
		checkSum += ( i % 2 == 0 ) ? digit * 2 : digit;
	}
	checkSum = eval( checkSum.split('').join('+') );
	return ( checkSum % 10 == 0 );
}
//	EOF