/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.international.js

	Included Validators
	-------------------
	cazip
	ukpost
	germanpost
	swisspost

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.cazip = function()
{
	var elem = this.elem;
	if ( this.typeMismatch( 'text' ) ) return;
	elem.value = elem.value.toUpperCase();
	if ( !( /^[A-Z][0-9][A-Z] [0-9][A-Z][0-9]$/.test( elem.value ) ) )
	{
		this.throwError();
	}
}
fValidate.prototype.capost = fValidate.prototype.cazip;

fValidate.prototype.ukpost = function()
{
	var elem = this.elem;
	if ( this.typeMismatch( 'text' ) ) return;
	elem.value = elem.value.toUpperCase();
	if ( !( /^[A-Z]{1,2}\d[\dA-Z] ?\d[A-Z]{2}$/.test( elem.value ) ) )
	{
		this.throwError();
	}
}

fValidate.prototype.germanpost = function()
{
	var elem = this.elem;
	if ( this.typeMismatch( 'text' ) ) return;
	elem.value = elem.value.toUpperCase();
	if ( !( /^(?:CH\-)\d{4}$/.test( elem.value ) ) )
	{
		this.throwError();
	}
}

fValidate.prototype.swisspost = function()
{
	var elem = this.elem;
	if ( this.typeMismatch( 'text' ) ) return;
	elem.value = elem.value.toUpperCase();
	if ( !( /^(?:D\-)\d{5}$/.test( this.elem.value ) ) )
	{
		this.throwError();
	}
}
//	EOF