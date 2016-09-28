/**
 * Convert a single file-input element into a 'multiple' input list
 *
 * Usage:
 *
 *   1. Create a file input element (no name)
 *      eg. <input type="file" id="first_file_element">
 *
 *   2. Create a DIV for the output to be written to
 *      eg. <div id="files_list"></div>
 *
 *   3. Instantiate a MultiSelector object, passing in the DIV and (optionally) the maximum number of files and a boolean
 *      that specifies if the multiple attribute should be used.
 *      eg. var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 3, true );
 *
 *   4. Add the first element
 *      eg. multi_selector.addElement( document.getElementById( 'first_file_element' ) );
 *
 *   5. That's it.
 *
 *   You might (will) want to play around with the addListRow() method to make the output prettier.
 *
 *   You might also want to change the line
 *       element.name = 'file_' + this.count;
 *   ...to a naming convention that makes more sense to you.
 *
 * Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Stickman -- http://www.the-stickman.com
 *      with thanks to:
 *      [for Safari fixes]
 *         Luis Torrefranca -- http://www.law.pitt.edu
 *         and
 *         Shawn Parker & John Pennypacker -- http://www.fuzzycoconut.com
 *      [for duplicate name bug]
 *         'neal'
 *      [for multiple HTML5 attribute use]
 *         'Andrei Costescu'
 */
function MultiSelector( eprefix, list_target, max, useMultipleAttr, del_label ){
	"use strict";

	// Where to write the list
	this.list_target = list_target;
	// How many elements?
	this.count = 0;
	// How many elements?
	this.id = 0;
	// Is there a maximum?
	if( max ){
		this.max = max;
	} else {
		this.max = -1;
	}
	this.useMultipleAttr = useMultipleAttr;
	
	this.delete_label=del_label;
	this.element_name_prefix=eprefix;
	
	/**
	 * Add a new file input element
	 */
	this.addElement = function( element ){

		// Make sure it's a file input element
		if( element.tagName.toLowerCase() === 'input' && element.type.toLowerCase() === 'file' ){

			if (this.useMultipleAttr) {
				element.multiple = this.useMultipleAttr;
				if (Wicket.Browser.isOpera()) {
					// in Opera 12.02, changing 'multiple' this way does not update the field
					element.type = 'button';
					element.type = 'file';
				}
			}

			// Element name -- what number am I?
			element.name = this.element_name_prefix + "_mf_"+this.id++;

			// Add reference to this object
			element.multi_selector = this;

			// What to do when a file is selected
			element.onchange = function(){

				// New file input
				var new_element = document.createElement( 'input' );
				new_element.type = 'file';
				new_element.style.display = "block";

				// Add new element
				this.parentNode.insertBefore( new_element, this );

				// Apply 'update' to element
				this.multi_selector.addElement( new_element );

				// Update list
				this.multi_selector.addListRow( this );

				// Hide this: we can't use display:none because Safari doesn't like it
				this.style.position = 'fixed';
				this.style.left = '-1000px';

			};
			// If we've reached maximum number, disable input element
			if( this.max !== -1 && this.count >= this.max ){
				element.disabled = true;
			}

			// File element counter
			this.count++;
			// Most recent element
			this.current_element = element;
			
		} else if (Wicket && Wicket.Log) {
			// This can only be applied to file input elements!
			Wicket.Log.error( 'Error: not a file input element' );
		}

	};

	/**
	 * Add a new row to the list of files
	 */
	this.addListRow = function( element ){

		// Row div
		var new_row = document.createElement('tr');
		var contentsColumn = document.createElement('td');
		var buttonColumn = document.createElement('td');

		// Delete button
		var new_row_button = document.createElement( 'input' );
		new_row_button.type = 'button';
		new_row_button.value = this.delete_label;

		// References
		new_row.element = element;

		// Delete function
		new_row_button.onclick= function(){

			// Remove element from form
			this.parentNode.parentNode.element.parentNode.removeChild( this.parentNode.parentNode.element );

			// Remove this row from the list
			this.parentNode.parentNode.parentNode.removeChild( this.parentNode.parentNode );

			// Decrement counter
			this.parentNode.parentNode.element.multi_selector.count--;

			// Re-enable input element (if it's disabled)
			this.parentNode.parentNode.element.multi_selector.current_element.disabled = false;

			// Appease Safari
			//    without it Safari wants to reload the browser window
			//    which nixes your already queued uploads
			return false;
		};

		// Set row value
		contentsColumn.innerHTML = this.getOnlyFileNames(element);
		new_row.appendChild( contentsColumn );

		// Add button
		new_row_button.style.marginLeft = '20px';
		buttonColumn.appendChild( new_row_button );
		new_row.appendChild( buttonColumn );

		// Add it to the list
		this.list_target.appendChild( new_row );
		
	};

	this.getOnlyFileNames = function(inputElement)
	{
		if (inputElement.files && inputElement.files.length > 0)
		{
			var files = inputElement.files;
			var retVal = "";
			for (var i = 0; i < files.length; i++)
			{
				retVal += this.getOnlyFileName(files[i].name) + '<br>';
			}
			return retVal.slice(0, retVal.length - 4);
		}
		else
		{
			return this.getOnlyFileName(inputElement.value);
		}
	};

	this.getOnlyFileName = function(stringValue)
	{
		var toEscape = {
			"&": "&amp;",
			"<": "&lt;",
			">": "&gt;",
			'"': '&quot;',
			"'": '&#39;'
		};

		function replaceChar(ch) {
			return toEscape[ch] || ch;
		}

		function htmlEscape(fileName) {
			return fileName.replace(/[&<>'"]/g, replaceChar);
		}

		var separatorIndex1 = stringValue.lastIndexOf('\\');
		var separatorIndex2 = stringValue.lastIndexOf('/');
		separatorIndex1 = Math.max(separatorIndex1, separatorIndex2);
		var fileName = separatorIndex1 >= 0 ? stringValue.slice(separatorIndex1 + 1, stringValue.length) : stringValue;
		fileName = htmlEscape(fileName);
		return fileName;
	};

}
