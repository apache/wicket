/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.extended.js

	Included Validators
	-------------------
	alnum
	ssn
	zip
	phone

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.alnum = function( minLen, tCase, numbers, spaces, puncs )
{
	if ( this.typeMismatch( 'text' ) ) return;

	tCase = this.setArg( tCase, "a" );
	
	//alert( [minLen,tCase,numbers,spaces,puncs] );

	numbers = ( numbers == "true" || numbers == "1" );
	spaces = ( spaces == "true" || spaces == "1" );

	//alert( [minLen,tCase,numbers,spaces,puncs] );
		
	var okChars = "",
		arrE	= ['None','Any','No','No','Any'];

	if ( minLen != '*' )
	{
		minLen =  parseInt( minLen, 10 );
		arrE[0] = minLen;
	} else {
		minLen = 0;
	}

	switch( tCase.toUpperCase() )
	{
		case 'U':
			okChars += 'A-Z';
			arrE[1] =  'UPPER';
			break;
		case 'L':
			okChars += 'a-z';
			arrE[1] =  'lower';
			break;
		case 'C':
			okChars += 'A-Z][a-z';
			arrE[1] =  'Intial capital';
			minLen--;
			break;
		default:
			okChars += 'a-zA-Z';
			break;		
	}

	if ( numbers == true )
	{
		okChars += '0-9';
		arrE[2] =  'Yes';
	}
	if ( spaces == true )
	{
		okChars += ' ';
		arrE[3] =  'Yes';
	}
	if ( puncs == "any" )
	{
		arrE[4]  = "Any";
	}
	else if ( puncs == "none" )
	{
		arrE[4] = "None";
	}
	else 
	{
		puncs = puncs.replace( /pipe/g, "|" );
		okChars += puncs;
		arrE[4] =  puncs; //.toPattern().replace( /\\/g, "" );
	}
	var length = ( minLen != "*" )?
		"{" + minLen + ",}":
		"+";
	var regex = ( puncs == "any" ) ?
		new RegExp( "^([" + okChars + "]|[^a-zA-Z0-9\\s])" + length + "$" ):
		new RegExp( "^[" + okChars + "]" + length + "$" );
	
	if ( !regex.test( this.elem.value ) )
	{
		this.throwError( [this.elem.value, this.elem.fName, arrE[0], arrE[1], arrE[2], arrE[3], arrE[4]] );
	}
}

fValidate.prototype.ssn = function()
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( !( /^\d{3}\-\d{2}\-\d{4}$/.test( this.elem.value ) ) )
		this.throwError();
}

fValidate.prototype.zip = function( sep )
{
	if ( this.typeMismatch( 'text' ) ) return;
	sep = this.setArg( sep, "- " );
	var regex = new RegExp( "^[0-9]{5}(|[" + sep.toPattern() + "][0-9]{4})?$" );
	if ( ! regex.test( this.elem.value ) )
	{
		this.throwError();
	}
}

fValidate.prototype.phone = function( format )
{
	if ( this.typeMismatch( 'text' ) ) return;
	format       = this.setArg( format, 0 );
	var patterns = [
		/^(\(?\d\d\d\)?)?[ -]?\d\d\d[ -]?\d\d\d\d$/,	//	loose
		/^(\(\d\d\d\) )?\d\d\d[ -]\d\d\d\d$/			//	strict
		];
	if ( !patterns[format].test( this.elem.value ) )
	{
		if ( format == 1 )
		{
			this.throwError();
		} else {
			this.throwError( [], 1 );
		}
	}
}
//	EOF