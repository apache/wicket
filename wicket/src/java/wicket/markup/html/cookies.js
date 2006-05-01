// this function gets the cookie, if it exists
function getWicketCookie( name ) 
{
	var start = document.cookie.indexOf( name + "=" );
	var len = start + name.length + 1;
	if ( ( !start ) && ( name != document.cookie.substring( 0, name.length ) ) )
	{
		return null;
	}
	if ( start == -1 )
	{
		return null;
	}
	var end = document.cookie.indexOf( ";", len );
	if ( end == -1 ) end = document.cookie.length;
	var value = unescape( document.cookie.substring( len, end ) );
	return value;
	
}
	
// this deletes the cookie when called
function deleteWicketCookie( name ) 
{
	if ( getWicketCookie( name ) ) document.cookie = name + "=;path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function setWicketCookie( name, value ) 
{
	document.cookie = name + "=" +escape( value ) + ";path=/;";
}