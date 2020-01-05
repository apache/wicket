This file contains all changes done in releases for Apache Wicket 9.x.

=======================================================================

Release Notes - Wicket - Version 9.0.0-M4

** Bug

    * [WICKET-6531] - Crash in Unsafe.getObject when running on the J9 VM
    * [WICKET-6704] - JavaSerializer.serialize causes the JVM crash !
    * [WICKET-6706] - Websocket Endpoint logs exception when user leaves page
    * [WICKET-6707] - Property setter parameter type is assumed to be equal to getter return type
    * [WICKET-6708] - FormComponent should read only the GET/POST parameters of the request, not both
    * [WICKET-6713] - BaseWicketTester does not reset componentInPage field
    * [WICKET-6717] - Automatic-Module-Name should be valid Java identifier

** New Feature

    * [WICKET-6666] - Rewrite ModalWindow

** Improvement

    * [WICKET-3404] - Improve ModalWindow form handling
    * [WICKET-6321] - Support Integrity and Crossorigin attributes for JavaScriptUrlReferenceHeaderItem 
    * [WICKET-6682] - Improve JavaScriptContentHeaderItem and JavaScriptUtils to support nonce
    * [WICKET-6701] - DownloadLink make content disposition configurable
    * [WICKET-6703] - Eliminate window.eval from wicket-ajax-jquery
    * [WICKET-6709] - Cache the value of WebRequest#isAjax() 
    * [WICKET-6712] - Timezone can be determined on client side
    * [WICKET-6714] - Please add better getResource-Support for MockServletContext
    * [WICKET-6718] - AjaxFormChoiceComponentUpdatingBehavior uses "click" instead of "change"
    * [WICKET-6720] - ConcatBundleResource#getResourceStream should not eagerly fetch resources

=======================================================================

Release Notes - Wicket - Version 9.0.0-M3

** Bug

    * [WICKET-6613] - Wicket 8.1 ModalWindow autosizing problem 
    * [WICKET-6676] - Quickstart application won't deploy to GlassFish
    * [WICKET-6678] - Instant : Unsupported field: YearOfEra when cookieUtils.cookieToDebugString() is called
    * [WICKET-6680] - JavaScriptStripper chokes on template literals that contain two forward slashes
    * [WICKET-6689] - ClientProperties.getTimeZone() has some issue when DST and UTC offsets are different
    * [WICKET-6690] - NullPointerException in KeyInSessionSunJceCryptFactory.<init>
    * [WICKET-6692] - Page deserialization on websocket close - possible performance issue
    * [WICKET-6697] - Wicket.DOM.toggleClass JS method is missing

** New Feature

    * [WICKET-6559] - Encrypted page store

** Improvement

    * [WICKET-6558] - Prevent package locks after commitRequest
    * [WICKET-6672] - Restore constructors with Wicket Duration in 9.x branch for easier migration
    * [WICKET-6673] - PriorityHeaderItem ordering is wrong for siblings
    * [WICKET-6675] - log4j-slf4j-impl requires version 1.7.25 of slf4j-api while Wicket 8.5 requires version 1.7.26
    * [WICKET-6682] - Improve JavaScriptContentHeaderItem and JavaScriptUtils to support nonce
    * [WICKET-6683] - triggered events should bubble
    * [WICKET-6684] - Make autolabel functionality more flexible by introducing a locator interface that allows to specify the component the wicket:for refers to
    * [WICKET-6693] - Mark FormComponent#setModelValue(String[]) as not being part of the public API
    * [WICKET-6695] - Add AjaxEditable*Label#shouldTrimInput() 
    * [WICKET-6696] - Unify AjaxEditable*#getConverter()

** Task

    * [WICKET-6698] - Non-security critical dependency updates

=======================================================================

Release Notes - Wicket - Version 9.0.0-M2

** Bug

    * [WICKET-6611] - Missing check for IScopeAwareTextResourceProcessor when concatenating resources
    * [WICKET-6669] - CSS Resource Bundling throws exception when used with CssUrlReplacer
    * [WICKET-6671] - IAjaxLink should be serializable

** Improvement

    * [WICKET-6618] - Stateless pages and access to unconfigured components
    * [WICKET-6656] - JSR 303 - @NotNull validation problems 
    * [WICKET-6657] - change replaceAll() to replace() when a regex is not used
    * [WICKET-6658] - Allow nested forms on non-<form> tag
    * [WICKET-6659] - commons-io:commons-io is used in multiple versions at same time
    * [WICKET-6662] - Remove legacy package org.apache.wicket.util.time from wicket-util
    * [WICKET-6667] - Ajax JavaScript clean-up
    * [WICKET-6668] - Sign out the existing session if a sign in attempt has failed

** Task

    * [WICKET-6653] - Upgrade Velocity to 2.x
    * [WICKET-6654] - Upgrade JQuery to 3.4.0
    * [WICKET-6661] - Upgrade jquery to 3.4.x
    * [WICKET-6665] - Upgrade various dependencies

=======================================================================

Release Notes - Wicket - Version 9.0.0-M1

** Bug

    * [WICKET-5552] - Events to close pop-up on Modal Window are not propagated
    * [WICKET-6568] - Wicket fails / does not encode request header values in AjaxCalls
    * [WICKET-6570] - Unable to read cookies containing '.' characters in names when using CookieUtils 
    * [WICKET-6574] - JQueryResourceReference#get() (still) return V1
    * [WICKET-6584] - Import Junit Package as optional
    * [WICKET-6586] - Broken JavaScript due to fix charsetName in JavaScriptPackageResource
    * [WICKET-6588] - Under Tomcat (ver. >= 8.5) BaseWebSocketBehavior can't find session id cookie 
    * [WICKET-6599] - ResponseIOException should never escape from WicketFilter
    * [WICKET-6602] - AuthenticatedWebApplication login Workflow broken with replaceSession
    * [WICKET-6603] - WicketTester.destroy sometimes hangs
    * [WICKET-6606] - data-wicket-placeholder is invalid XHTML
    * [WICKET-6607] - NoSuchMethodError when using Spring-Beans with constructor injection in an AjaxLink#onClick
    * [WICKET-6610] - Incorrect Javadoc: Refering to specific page in Application properties file is not possible
    * [WICKET-6614] - AbstractRangeValidator looks up string with the wrong key if locale is Turkish
    * [WICKET-6617] - Header contribution ignore <wicket:header-items/>
    * [WICKET-6623] - Consecutive Temporary Behaviors are not properly removed
    * [WICKET-6629] - OOM (and disk) in AsynchronousPageStore
    * [WICKET-6630] - FileUpload.writeToTempFile() fails with commons-fileupload 1.4
    * [WICKET-6631] - AnnotProxyFieldValueFactory does not cache beanNames
    * [WICKET-6637] - Handling exception Wicket 8
    * [WICKET-6639] - PageStoreManager$SessionEntry.clear produces NullPointerException
    * [WICKET-6642] - Form.findSubmittingComponent returns null instead of SubmitLink
    * [WICKET-6645] - Concurrent web socket response message processing on the client
    * [WICKET-6650] - Url decode the name of the file after AjaxDownload with Location == Blob
    * [WICKET-6651] - Redirecting with ResetResponseException does not work anymore

** New Feature

    * [WICKET-6577] - Introduce class GenericWebMarkupContainer
    * [WICKET-6578] - StatelessResourceLink
    * [WICKET-6626] - Introduce application-wide Component#onComponentTag listeners
    * [WICKET-6641] - Extract an interface for classes allowing to register feedback messages

** Improvement

    * [WICKET-6435] - WicketTester should provide assertExists and assertNotExists methods
    * [WICKET-6550] - Unify all metadata capable objects.
    * [WICKET-6555] - AbstractChoice subclasses code duplication
    * [WICKET-6556] - Change DataTable's HTML order
    * [WICKET-6557] - Allow meta tags to be contributed during AJAX request
    * [WICKET-6560] - Improve serialization warnings in ChainingModel
    * [WICKET-6562] - Remove from wicket-core all the deprecated classes
    * [WICKET-6563] - Rework page and data storage
    * [WICKET-6565] - ResponseIOException logged as an error in DefaultExceptionMapper
    * [WICKET-6575] - Ajax requests are still firing even when placeholder tag is written only
    * [WICKET-6576] - Support multiple dateFormats for LocalDateTextfield
    * [WICKET-6579] - Upgrade Spring to 5.x version
    * [WICKET-6580] - org.apache.wicket.util.lang.Bytes - toString()
    * [WICKET-6581] - Upgrade wicket-cdi to cdi version 2
    * [WICKET-6587] - CheckBoxSelector should accept more CheckBoxes to be added later
    * [WICKET-6595] - Upgrade JUnit to v5.x
    * [WICKET-6600] - Error logging in AjaxRequestHandler is too strict
    * [WICKET-6601] - Events to close pop-up on Modal Window are not propagated from caption bar
    * [WICKET-6605] - Allow AjaxFallbackButton to be stateless 
    * [WICKET-6618] - Stateless pages and access to unconfigured components
    * [WICKET-6621] - Exceeding exception retries should return control back to server
    * [WICKET-6634] - Save the closeCode and message in WebSocket's ClosedMessage
    * [WICKET-6635] - Move AbstractPropertyModel#getInnermostModelOrObject() to ChainingModel
    * [WICKET-6638] - RedirectRequestHandler does not support Ajax
    * [WICKET-6640] - Add settings for customizing the ModalWindow's spacing, header height and overflow
    * [WICKET-6644] - AbstractPageableView can only be serialized with Java built-in serialization
    * [WICKET-6648] - It is impossible to initiate AjaxDownloadBehavior with IPartialPageRequestHandler

** Wish

    * [WICKET-6539] - Scope fix for DataTable toolbars
    * [WICKET-6569] - LambdaModel.of overload is ambiguous
    * [WICKET-6646] - Upgrade jquery to 3.3.x

** Task

    * [WICKET-6583] - Upgrade Tests to Junit5
    * [WICKET-6594] - JavaDoc of redirectToInterceptPage in Component urges to use redirectTo method when in a constructor
    * [WICKET-6596] - Use JQuery 3.x as default
    * [WICKET-6598] - Upgrade Objenesis to 3.x for better support of Java 11
    * [WICKET-6609] - Update Guice from 4.1.0 to 4.2.2
    * [WICKET-6620] - @Deprecated classes/methods need to be removed
    * [WICKET-6624] - Upgrade to commons-filupload 1.4
    * [WICKET-6647] - Upgrade asm to 7.1

=======================================================================

