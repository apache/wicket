/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.numbers.js

	Included Validators
	-------------------
	number
	numeric
	decimal
	decimalr

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.number = function( type, lb, ub )
{
	if ( this.typeMismatch( 'text' ) ) return;
	var num  = ( type == 0 ) ? parseInt( this.elem.value, 10 ) : parseFloat( this.elem.value );
	lb       = this.setArg( lb, 0 );
	ub       = this.setArg( ub, Number.infinity );
	if ( lb > ub )
	{
		this.devError( [lb, ub, this.elem.name] );
		return;
	}
	var fail = Boolean( isNaN( num ) || num != this.elem.value );
	if ( !fail )
	{
		switch( true )
		{
			case ( lb != false && ub != false ) : fail = !Boolean( lb <= num && num <= ub ); break;
			case ( lb != false ) : fail = Boolean( num < lb ); break;
			case ( ub != false ) : fail = Boolean( num > ub ); break;
		}
	}
	if ( fail )
	{
		this.throwError( [this.elem.fName] );
		return;
	}
	this.elemPass = true;
}

fValidate.prototype.numeric = function( len )
{
	if ( this.typeMismatch( 'text' ) ) return;
	len = this.setArg( len, '*' );
	var regex = new RegExp( ( len == '*' ) ? "^\\d+$" : "^\\d{" + parseInt( len, 10 ) + "}\\d*$" );
	if ( !regex.test( this.elem.value ) )
	{
		if ( len == "*" )
		{
			this.throwError( [this.elem.fName] );
		} else {
			this.throwError( [len, this.elem.fName], 1 );
		}
	}
}

fValidate.prototype.decimal = function( lval, rval )
{
	if ( this.typeMismatch( 'text' ) ) return;
	var regex = '', elem = this.elem;
	if ( lval != '*' ) lval = parseInt( lval, 10 );
	if ( rval != '*' ) rval = parseInt( rval, 10 );
	
	if ( lval == 0 )
		regex = "^\\.[0-9]{" + rval + "}$";	
	else if ( lval == '*' )
		regex = "^[0-9]*\\.[0-9]{" + rval + "}$";
	else if ( rval == '*' )
		regex = "^[0-9]{" + lval + "}\\.[0-9]+$";
	else
		regex = "^[0-9]{" + lval + "}\\.[0-9]{" + rval + "}$";
		
	regex = new RegExp( regex );

	if ( !regex.test( elem.value ) )
	{
		this.throwError( [elem.value,elem.fName] );
	}	
}

fValidate.prototype.decimalr = function( lmin, lmax, rmin, rmax )
{
	if ( this.typeMismatch( 'text' ) ) return;
	lmin = ( lmin == '*' )? 0 : parseInt( lmin, 10 );
	lmax = ( lmax == '*' )? '': parseInt( lmax, 10 );
	rmin = ( rmin == '*' )? 0 : parseInt( rmin, 10 );
	rmax = ( rmax == '*' )? '': parseInt( rmax, 10 );
	var	decReg = "^[0-9]{"+lmin+","+lmax+"}\\.[0-9]{"+rmin+","+rmax+"}$"
	var regex = new RegExp(decReg);
	if ( !regex.test( this.elem.value ) )
	{
		this.throwError( [this.elem.fName] );
	}
	return true;
}
//	EOF