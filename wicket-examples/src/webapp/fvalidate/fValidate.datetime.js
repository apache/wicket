/***************************************************	

	fValidate
	Copyright (c) 2000-2003
	by Peter Bailey
	www.peterbailey.net/fValidate/

	fValidate.datetime.js

	Included Validators
	-------------------
	date

	This file is only part of a larger validation
	library	and will not function autonomously.

	Created at a tab-spacing of four (4)

****************************************************/

fValidate.prototype.date = function( formatStr, delim, code, specDate )
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( typeof formatStr == 'undefined' )
	{
		this.paramError( 'formatStr' );
		return;
	}

	delim = this.setArg( delim, "/" );

	var error	= [this.elem.fName, formatStr.replace( /\//g, delim )];
	var format  = formatStr.split( "/" );
	var compare = this.elem.value.split( delim );
	var order   = new Object();
	
	for ( var i = 0; i < format.length; i++ )
	{
		switch( format[i].charAt( 0 ).toLowerCase() )
		{
			case 'm' :
				order.months = i;
				break;
			case 'd' :
				order.days = i;
				break;
			case 'y' :
				order.years = i;
				break;
		}
	}
	var thisDate = new Date( compare[order.years], compare[order.months]-1, compare[order.days] );
	
	if ( isNaN( thisDate ) || thisDate.getDate() != compare[order.days] || thisDate.getMonth() != compare[order.months]-1 || thisDate.getFullYear().toString().length != formatStr.match( /y/g ).length )
	{
		this.throwError( error );
		return;
	}
	
	var compareElem = this.form.elements[specDate];
	if ( typeof compareElem != 'undefined' )
	{
		specDate = compareElem.validDate || compareElem.value;
	}
	var compareDate = ( specDate == 'today' )?
		new Date():
		new Date( specDate );
	compareDate.setHours(0);
	compareDate.setMinutes(0);
	compareDate.setSeconds(0);
	compareDate.setMilliseconds(0);
	
	var timeDiff = compareDate.getTime() - thisDate.getTime();
	var dateOk   = false;
	
	switch ( parseInt( code ) ) {
		case 1 :	// Before specDate
			dateOk	= Boolean( timeDiff > 0 );
			error	= 1;
			break;
		case 2 :	// Before or on specDate
			dateOk	= Boolean( ( timeDiff + 86400000 ) > 0 );
			error	= 2;
			break;
		case 3 :	// After specDate
			dateOk	= Boolean( timeDiff < 0 );
			error	= 3;
			break;
		case 4 :	// After or on specDate
			dateOk	= Boolean( ( timeDiff - 86400000 ) < 0 );
			error	= 4;
			break;
		default : dateOk = true;
		}
	if ( !dateOk )
	{
		this.throwError( [specDate], error );
	}
	this.elem.validDate = thisDate.toString();
}	
//	EOF