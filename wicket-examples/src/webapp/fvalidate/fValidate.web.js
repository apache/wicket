/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.web.js

	Included Validators
	-------------------
	email
	url
	ip

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.email = function( level )
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( typeof level == 'undefined' ) level = 0;
	var emailPatterns = [
		/.+@.+\..+$/i,
		/^\w.+@\w.+\.[a-z]+$/i,
		/^\w[-_a-z~.]+@\w[-_a-z~.]+\.[a-z]{2}[a-z]*$/i,
		/^\w[\w\d]+(\.[\w\d]+)*@\w[\w\d]+(\.[\w\d]+)*\.[a-z]{2,7}$/i
		];
	if ( ! emailPatterns[level].test( this.elem.value ) )
	{
		this.throwError();
	}	
}	

fValidate.prototype.url = function( hosts, hostOptional, allowQS )
{
	if ( this.typeMismatch( 'text' ) ) return;

	this.setArg( hosts, "http" );
	
	var front = "^(?:(" + hosts.replace( /\,/g, "|" ) + ")\\:\\/\\/)";
	var end   = ( Boolean( allowQS ) == true ) ? "(\\?.*)?$" : "$";

	if ( Boolean( hostOptional ) == true ) front += "?";
	var regex = new RegExp( front + "([\\w\\d-]+\\.?)+" + end );
	
	if ( !regex.test( this.elem.value ) )
	{
		this.throwError( [this.elem.fName] );
	}
}	

fValidate.prototype.ip = function( portMin, portMax )
{
	if ( this.typeMismatch( 'text' ) ) return;
	portMin = this.setArg( portMin, 0 );
	portMax = this.setArg( portMax, 99999 );
	if ( !( /^\d{1,3}(\.\d{1,3}){3}(:\d+)?$/.test( this.elem.value ) ) )
	{
		this.throwError();
	}
	else
	{
		var part, i = 0, parts = this.elem.value.split( /[.:]/ );
		while ( part = parts[i++] )
		{
			if ( i == 5 ) // Check port
			{
				if ( part < portMin || part > portMax )
				{
					this.throwError( [part, portMin, portMax], 1 );
				}
			}
			else if ( part < 0 || part > 255 )
			{
				this.throwError();
			}
		}
	}
}
//	EOF