function fValConfig()
{
	/*	Globals.  Modify these to suit your setup
	------------------------------------------- */
		
	//	Attribute used for fValidate Validator codes
	this.code = 'alt';
	
	//	Attribute used for custom error messages (override built-in error messages)
	this.emsg = 'emsg';
	
	//	Attribute used for pattern with custom validator type
	this.pattern = 'pattern';
	
	//	Change this to the classname you want for the error highlighting
	this.errorClass = 'errHilite';

	//	If you wish fValidate to use only single classNames for errors
	this.useSingleClassNames = false; // or true
	
	//	This is the even that triggers the clearing of the errorClass hilighting
	this.clearEvent = 'change'; // 'change' | 'blur' | null
	
	//	For browsers that don't support attachEvent or addEventListere - override existing events for error reverting?
	this.eventOverride = false;
	
	//	If the bConfirm flag is set to true, the users will be prompted with CONFIRM box with this message
	//	See your language file for this value
	this.confirmMsg = fvalidate.i18n.config.confirmMsg;
	
	//	If user cancels CONFIRM, then this message will be alerted.  If you don't want this alert to show, then
	//	empty the variable (  this.confirmAbortMsg = '';  )
	//	See your langauge file for this value
	this.confirmAbortMsg = fvalidate.i18n.config.confirmAbortMsg;
	
	//	Enter the name/id of your form's submit button here.  Can be a string or array of strings
	this.submitButton = ['Submit','Submit2'];
	
	//	Enter the name/id of your form's reset button here
	this.resetButton = 'Reset';
	
	//	Ender the name or id of the SELECT object here. Make sure you pay attention to the values (CC Types)
	//	used in the case statement for the function validateCC()
	this.ccType = 'Credit_Card_Type';
	
	//	NOTE: The config value below exists for backwards compatibility with fValidate 3.55b.  If you have a newer 
	//	version, use the above this.ccType instead.
	//	Enter the DOM name of the SELECT object here. Make sure you pay attention to the values (CC Types)
	//	used in the case statement for the function validateCC()
	this.ccTypeObj = 'form1.Credit_Card_Type';
	
	//	Element where box errors will appear
	this.boxError = 'errors';
	
	//	Prefix given to all error paragraphs in box error mode
	this.boxErrorPrefix = 'fv_error_';
}
//	EOF