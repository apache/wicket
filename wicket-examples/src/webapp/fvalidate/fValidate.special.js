/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.special.js

	Included Validators
	-------------------
	custom

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.custom = function( flags, reverseTest )
{
	if ( this.typeMismatch( 'text' ) ) return;
	flags     = ( flags ) ? flags.replace( /[^gim]/ig ) : "";
	var regex = new RegExp( this.elem.getAttribute( this.config.pattern ), flags );
	if ( !regex.test( this.elem.value ) )
	{
		this.throwError( [this.elem.fName] );
	}	
}
//	EOF