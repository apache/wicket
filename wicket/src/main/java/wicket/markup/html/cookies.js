/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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