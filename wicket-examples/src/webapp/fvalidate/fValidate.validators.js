/*< blank basic *******************************************************************/
fValidate.prototype.blank = function()
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( this.isBlank() )
	{
		this.throwError( [this.elem.fName] );
	}
}
/*/>*/
/*< number numbers *******************************************************************/
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
/*/>*/
/*< numeric numbers *******************************************************************/
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
/*/>*/
/*< length basic *******************************************************************/
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
/*/>*/
/*< alnum extended *******************************************************************/
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
/*/>*/
/*< equalto logical *******************************************************************/
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
/*/>*/
/*< ssn extended *******************************************************************/
fValidate.prototype.ssn = function()
{
	if ( this.typeMismatch( 'text' ) ) return;
	if ( !( /^\d{3}\-\d{2}\-\d{4}$/.test( this.elem.value ) ) )
		this.throwError();
}
/*/>*/
/*< select controls *******************************************************************/
fValidate.prototype.select = function()
{
	if ( this.typeMismatch( 's1' ) ) return;
	if ( this.elem.selectedIndex == 0 )
	{
		this.throwError( [this.elem.fName] );
	}
}
/*/>*/
/*< selectm controls *******************************************************************/
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
/*/>*/
/*< selecti controls *******************************************************************/
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
/*/>*/
/*< cazip international *******************************************************************/
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
/*/>*/
/*< ukpost international *******************************************************************/
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
/*/>*/
/*< germanpost international *******************************************************************/
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
/*/>*/
/*< swisspost international *******************************************************************/
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
/*/>*/
/*< email web *******************************************************************/
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
/*/>*/
/*< url web *******************************************************************/
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
/*/>*/
/*< ip web *******************************************************************/
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
/*/>*/
/*< decimal numbers *******************************************************************/
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
/*/>*/
/*< decimalr numbers *******************************************************************/
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
/*/>*/
/*< zip extended *******************************************************************/
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
/*/>*/
/*< phone extended *******************************************************************/
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
/*/>*/
/*< date datetime *******************************************************************/
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
/*/>*/
/*< money ecommerce *******************************************************************/
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
/*/>*/
/*< checkbox controls *******************************************************************/
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
/*/>*/
/*< radio controls *******************************************************************/
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
/*/>*/
/*< eitheror logical *******************************************************************/
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
/*/>*/
/*< atleast logical *******************************************************************/
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
/*/>*/
/*< allornone logical *******************************************************************/
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
/*/>*/
/*< comparison logical *******************************************************************/
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
/*/>*/
/*< file controls *******************************************************************/
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
/*/>*/
/*< custom special *******************************************************************/
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
/*/>*/
/*< cc ecommerce *******************************************************************/
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
/*/>*/
/*< ccDate ecommerce *******************************************************************/
fValidate.prototype.ccDate = function( month, year )
{
	if ( this.typeMismatch( 's1' ) ) return;
	year	= parseInt( this.getValue( this.form.elements[year] ), 10 ) + 2000;
	month	= parseInt( this.getValue( this.form.elements[month] ), 10 );

	var today	= new Date();
	var expDate = new Date( year, month )

	if ( expDate < today )
	{
		alert( ["Card Expired",today,expDate].join( "\n" ) );
	}
}
/*/>*/
/*	EOF */