/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.controls.js

	Included Validators
	-------------------
	select
	selectm
	selecti
	checkbox
	radio
	file

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.select = function()
{
	if ( this.typeMismatch( 's1' ) ) return;
	if ( this.elem.selectedIndex == 0 )
	{
		this.throwError( [this.elem.fName] );
	}
}

fValidate.prototype.selectm = function( minS, maxS )
{
	if ( this.typeMismatch( 'sm' ) ) return;
	if ( typeof minS == 'undefined' )
	{
		this.paramError( 'minS' );
	}
	if ( maxS == 999 || maxS == '*' || typeof maxS == 'undefined' || maxS > this.elem.length ) maxS = this.elem.length;

	var count = 0;	
	for ( var opt, i = 0; ( opt = this.elem.options[i] ); i++ )
	{
		if ( opt.selected ) count++;
	}

	if ( count < minS || count > maxS )
	{
		this.throwError( [minS, maxS, this.elem.fName, count] );
	}
}

fValidate.prototype.selecti = function( indexes )
{
	
	if ( this.typeMismatch( 's1' ) ) return;
	if ( typeof indexes == 'undefined' )
	{
		this.paramError( 'indexes' );
		return;
	}
	indexes = indexes.split( "," );
	var selectOK = true;

	for ( var i = 0; i < indexes.length; i++ )
	{
		if ( this.elem.options[indexes[i]].selected )
		{
			selectOK = false;
			break;
		}
	}
	if ( !selectOK )
	{
		this.throwError( [this.elem.fName] );
	}
}

fValidate.prototype.checkbox = function( minC, maxC )
{
	if ( this.typeMismatch( 'cb' ) ) return;
	if ( typeof minC == 'undefined' )
	{
		this.paramError( 'minC' );
		return;
	}
	if ( this.elem == this.form.elements[this.elem.name] && !this.elem.checked )
	{
		this.throwError( [this.elem.fName] );
	}
	else
	{
		this.elem = this.form.elements[this.elem.name];
		var len   = this.elem.length;
		var count = 0;
		
		if ( maxC == 999 || maxC == '*' || typeof maxC == 'undefined' || maxC > this.elem.length )
		{
			maxC == len;
		}
		var i = len;
		while( i-- > 0 )
		{
			if ( this.elem[i].checked )
			{
				count++;
			}
		}
		if ( count < minC || count > maxC )
		{
			this.throwError( [minC, maxC, this.elem[0].fName, count] );
		}			
	}
}

fValidate.prototype.radio = function()
{
	if ( this.typeMismatch( 'rg' ) ) return;
	if ( this.elem == this.form.elements[this.elem.name] && !this.elem.checked )
	{
		this.throwError( [this.elem.fName] );
	}
	else
	{
		this.elem = this.form.elements[this.elem.name];
		
		for ( var i = 0; i < this.elem.length; i++ )
		{
			if ( this.elem.item( i ).checked )
			{
				return;
			}
		}
		this.throwError( [this.elem[0].fName] );
	}
}

fValidate.prototype.file = function( extensions, cSens )
{
	if ( this.typeMismatch( 'file' ) ) return;
	if ( typeof extensions == 'undefined' )
	{
		this.paramError( 'extensions' );
		return;
	}
	cSens = Boolean( cSens ) ? "" : "i";
	var regex = new RegExp( "^.+\\.(" + extensions.replace( /,/g, "|" ) + ")$", cSens );
	if ( ! regex.test( this.elem.value ) )
	{
		this.throwError( [extensions.replace( /,/g, "\n" )] );
	}
}
//	EOF