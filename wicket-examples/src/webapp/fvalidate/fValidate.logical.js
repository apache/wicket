/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.logical.js

	Included Validators
	-------------------
	equalto
	eitheror
	atleast
	allornone
	comparison

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.equalto = function( oName )
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( typeof oName == 'undefined' )
	{
		this.paramError( 'oName' );
	}
	var otherElem = this.form.elements[oName];
	if ( this.elem.value != otherElem.value )
	{
		this.throwError( [this.elem.fName,otherElem.fName] );			
	}
}

fValidate.prototype.eitheror = function()
{
	if ( this.typeMismatch( 'hidden' ) ) return;
	if ( typeof arguments[0] == 'undefined' )
	{
		this.paramError( 'delim' );
		return;
	}
	if ( typeof arguments[1] == 'undefined' )
	{
		this.paramError( 'fields' );
		return;
	}
	var arg, i  = 0,
		fields  = new Array(),
		field,
		nbCount = 0,		
		args    = arguments[1].split( arguments[0] );		

	this.elem.fields = new Array();
	
	while ( arg = args[i++] )
	{
		field = this.form.elements[arg];
		fields.push( field.fName );
		this.elem.fields.push( field );

		if ( !this.isBlank( arg ) )
		{
			nbCount++;
		}
	}
	if ( nbCount != 1 )
	{
		this.throwError( [fields.join( "\n\t-" )] );
	}
}

fValidate.prototype.atleast = function()
{
	if ( this.typeMismatch( 'hidden' ) ) return;
	if ( typeof arguments[0] == undefined )
	{
		this.paramError( 'qty' );
		return;
	}
	if ( typeof arguments[1] == undefined )
	{
		this.paramError( 'delim' );
		return;
	}
	if ( typeof arguments[2] == undefined )
	{
		this.paramError( 'fields' );
		return;
	}
	var arg, i  = 0,
		fields  = new Array(),
		field,
		nbCount = 0,
		args    = arguments[2].split( arguments[1] );

	this.elem.fields = new Array();
	
	while ( arg = args[i++] )
	{
		field = this.form.elements[arg];
		fields.push( field.fName );
		this.elem.fields.push( field );

		if ( !this.isBlank( arg ) )
		{
			nbCount++;
		}
	}
	if ( nbCount < arguments[0] )
	{
		this.throwError( [arguments[0], fields.join( "\n\t-" ), nbCount] );
	}
}

fValidate.prototype.allornone = function()
{
	if ( this.typeMismatch( 'hidden' ) ) return;
	if ( typeof arguments[0] == 'undefined' )
	{
		this.paramError( 'delim' );
		return;
	}
	if ( typeof arguments[1] == 'undefined' )
	{
		this.paramError( 'fields' );
		return;
	}
	var arg, i  = 0,
		fields  = new Array(),
		field,
		nbCount = 0,
		args    = arguments[1].split( arguments[0] );
	
	this.elem.fields = new Array();

	while ( arg = args[i++] )
	{
		field = this.form.elements[arg];
		fields.push( field.fName );
		this.elem.fields.push( field );

		if ( !this.isBlank( arg ) )
		{
			nbCount++;
		}
	}
	if ( nbCount > 0 && nbCount < args.length )
	{
		this.throwError( [fields.join( "\n\t-" ), nbCount] );
	}
}

fValidate.prototype.comparison = function( field1, operator, field2 )
{
	if ( this.typeMismatch( 'hidden' ) ) return;
	var elem1	= this.form.elements[field1],
		elem2	= this.form.elements[field2],
		value1	= this.getValue( elem1 ),
		value2	= this.getValue( elem2 );
		i18n	= this.i18n.comparison;
		i		= -1;
	
	var operators =
	[
		['>',	i18n.gt],
		['<',	i18n.lt],
		['>=',	i18n.gte],
		['<=',	i18n.lte],
		['==',	i18n.eq],
		['!=',	i18n.neq]
	];
	while( operators[++i][0] != operator ){ }
	this.elem.fields = [elem1, elem2];
	if ( ! eval( value1 + operator + value2 ) )
	{
		this.throwError( [elem1.fName, operators[i][1], elem2.fName] );
	}
}
//	EOF