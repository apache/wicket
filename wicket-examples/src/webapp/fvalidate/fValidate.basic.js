/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.basic.js

	Included Validators
	-------------------
	blank
	length

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.blank = function()
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( this.isBlank() )
	{
		this.throwError( [this.elem.fName] );
	}
}

fValidate.prototype.length = function( len, maxLen )
{
	if ( this.typeMismatch( 'text' ) ) return;
	var vlen = this.elem.value.length;
	len		= Math.abs( len );
	maxLen	= Math.abs( this.setArg( maxLen, Number.infinity ) );
	if ( len > maxLen )
	{
		this.devError( [len, maxLen, this.elem.name] );
		return;
	}
	if ( len > parseInt( vlen, 10 ) )
	{
		this.throwError( [this.elem.fName, len] );
	}	
	if ( vlen > maxLen )
	{
		this.throwError( [this.elem.fName, maxLen, vlen], 1 );
	}
}
//	EOF