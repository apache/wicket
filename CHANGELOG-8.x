This file contains all changes done in releases for Apache Wicket 8.x.

=======================================================================

Release Notes - Wicket - Version 8.12.0

** Bug

    * [WICKET-6815] - Incorrect parsing of html attributes
    * [WICKET-6858] - Do not lower case the session cookie name
    * [WICKET-6860] - ConcatBundleResource double scope processing when CssUrlReplacer is used
    * [WICKET-6863] - Method Component.setVisibilityAllowed should call onVisibleStateChanged()
    * [WICKET-6865] - JS Error on keyup in AutoCompleteTextField
    * [WICKET-6867] - AutoComplete list don't choose any item, if click took more then 500 ms
    * [WICKET-6868] - UploadProcessBar doesn't work anymore with AjaxFormSubmitBehaviour("change") out of the box
    * [WICKET-6869] - StalePageException does not refresh page
    * [WICKET-6870] - Wicket complains about component failing to render
    * [WICKET-6871] - Exception with nested AjaxLazyLoadPanel

** Task

    * [WICKET-6873] - Upgrade jQuery to 3.6.0

=======================================================================

Release Notes - Wicket - Version 8.11.0

** Bug

    * [WICKET-6839] - Component visible-in-hierarchy cache not used but cleared
    * [WICKET-6847] - async page storing fails with flush before detach without session
    * [WICKET-6848] - Session invalidation fails because response is already committed

** Improvement

    * [WICKET-6828] - Wrong tree branch icon with hidden children
    * [WICKET-6844] - Add support for MethodMismatchResponse for Ajax behaviors

** Task

    * [WICKET-6846] - wicket-ajax-jquery.js   ActiveX control discovery - Unpatched Application

=======================================================================

Release Notes - Wicket - Version 8.10.0

** Bug

    * [WICKET-6702] - AsynchronousPageStore with NotDetachedModelChecker - "Not detached model found" exception on several fast sequential Ajax calls
    * [WICKET-6818] - NPE in WicketEndpoint onClose
    * [WICKET-6822] - AsynchronousPageStore Potential Memory Leak

** Improvement

    * [WICKET-6824] - Use concatenation instead of String.format for frequently called methods
    * [WICKET-6826] - Improve performance and reduce allocations for Behaviors
    * [WICKET-6827] - Improve performance of Strings.join and Strings.replaceAll
    * [WICKET-6828] - Wrong tree branch icon with hidden children
    * [WICKET-6829] - Use String.isEmpty() instead of "".equals(...)
    * [WICKET-6830] - Convert Behaviors into a static utility class to reduce allocations
    * [WICKET-6831] - Try to flush the response before detach
    * [WICKET-6833] - Reduce allocations when merging page parameters
    * [WICKET-6835] - Improve performance of AbstractMapper.getPlaceholder

=======================================================================

Release Notes - Wicket - Version 8.9.0

** Bug

    * [WICKET-6742] - Stacktrace in Fragment example
    * [WICKET-6764] - RedirectToUrlException change the second question mark in URL from "?" to "%3F"
    * [WICKET-6771] - Performance issues accessing component metadata while iterating
    * [WICKET-6782] - WebSocket onError/onAbort is not being called
    * [WICKET-6784] - StockQuote example does not work because the web service is no more available
    * [WICKET-6791] - Offload WebSocket push when initiated in Wicket request cycle
    * [WICKET-6793] - OOM in AsynchronousPageStore

** Improvement

    * [WICKET-6767] - Do not log error for broken pipes in websocket connections
    * [WICKET-6772] - Use StandardCharset for URL encoding and decoding
    * [WICKET-6773] - Improve performance of getting behaviors for components
    * [WICKET-6781] - Timezone can be determined on client side (7.x and 8.x)
    * [WICKET-6792] - Packages#absolutePath keeps unnecessary current dir dot "."
    * [WICKET-6796] - Report the component path when failing to set a new object to a read only model
    * [WICKET-6800] - Use LinkedHashSet instead of LinkedList for AjaxRequestHandler#listeners

** Task

    * [WICKET-6779] - Upgrade JQuery 3 to 3.5.1
    * [WICKET-6783] - Utility classes available in JDK should be deprecated/removed

=======================================================================

Release Notes - Wicket - Version 8.8.0

** Bug

    * [WICKET-6746] - HttpsMapper cannot deal with resources over websockets
    * [WICKET-6752] - Some dependencies contain CVEs
    * [WICKET-6753] - res/modal.js using aria-labelledby where it should be using aria-label
    * [WICKET-6754] - Iteration stops with nested containers
    * [WICKET-6755] - MockServletContext does not decode real path
    * [WICKET-6756] - Avoid URL.getFile() when actually expecting paths.
    * [WICKET-6757] - Avoid URL.getFile during mime type detection.
    * [WICKET-6758] - NPE in AbstractWebSocketProcessor after session times out

** Improvement

    * [WICKET-6759] - Support disabling error notification for websockets
    * [WICKET-6760] - Nested Form placeholder should preserve tag name
    * [WICKET-6761] - Support multiple connections to the same websocket resource from a single session
    * [WICKET-6762] - Support manual initialization of websocket connections

=======================================================================

Release Notes - Wicket - Version 8.7.0

** Bug

    * [WICKET-6531] - Crash in Unsafe.getObject when running on the J9 VM
    * [WICKET-6650] - Url decode the name of the file after AjaxDownload with Location == Blob
    * [WICKET-6704] - JavaSerializer.serialize causes the JVM crash !
    * [WICKET-6705] - URL filename not decoded when downloading via AjaxDownload blob mode
    * [WICKET-6706] - Websocket Endpoint logs exception when user leaves page
    * [WICKET-6707] - Property setter parameter type is assumed to be equal to getter return type
    * [WICKET-6708] - FormComponent should read only the GET/POST parameters of the request, not both
    * [WICKET-6713] - BaseWicketTester does not reset componentInPage field

** Improvement

    * [WICKET-6714] - Please add better getResource-Support for MockServletContext
    * [WICKET-6720] - ConcatBundleResource#getResourceStream should not eagerly fetch resources

** Task

    * [WICKET-6698] - Non-security critical dependency updates

=======================================================================

Release Notes - Wicket - Version 8.6.1

** Bug

    * [WICKET-6613] - Wicket 8.1 ModalWindow autosizing problem 
    * [WICKET-6671] - IAjaxLink should be serializable
    * [WICKET-6676] - Quickstart application won't deploy to GlassFish
    * [WICKET-6680] - JavaScriptStripper chokes on template literals that contain two forward slashes
    * [WICKET-6689] - ClientProperties.getTimeZone() has some issue when DST and UTC offsets are different
    * [WICKET-6690] - NullPointerException in KeyInSessionSunJceCryptFactory.<init>
    * [WICKET-6692] - Page deserialization on websocket close - possible performance issue

** Improvement

    * [WICKET-6675] - log4j-slf4j-impl requires version 1.7.25 of slf4j-api while Wicket 8.5 requires version 1.7.26
    * [WICKET-6684] - Make autolabel functionality more flexible by introducing a locator interface that allows to specify the component the wicket:for refers to
    * [WICKET-6695] - Add AjaxEditable*Label#shouldTrimInput() 

=======================================================================

Release Notes - Wicket - Version 8.6.0

** Bug

    * [WICKET-6613] - Wicket 8.1 ModalWindow autosizing problem 
    * [WICKET-6671] - IAjaxLink should be serializable
    * [WICKET-6676] - Quickstart application won't deploy to GlassFish
    * [WICKET-6680] - JavaScriptStripper chokes on template literals that contain two forward slashes
    * [WICKET-6689] - ClientProperties.getTimeZone() has some issue when DST and UTC offsets are different
    * [WICKET-6690] - NullPointerException in KeyInSessionSunJceCryptFactory.<init>
    * [WICKET-6692] - Page deserialization on websocket close - possible performance issue

** Improvement

    * [WICKET-6675] - log4j-slf4j-impl requires version 1.7.25 of slf4j-api while Wicket 8.5 requires version 1.7.26
    * [WICKET-6684] - Make autolabel functionality more flexible by introducing a locator interface that allows to specify the component the wicket:for refers to
    * [WICKET-6695] - Add AjaxEditable*Label#shouldTrimInput() 

=======================================================================

Release Notes - Wicket - Version 8.5.0

** Bug

    * [WICKET-6611] - Missing check for IScopeAwareTextResourceProcessor when concatenating resources
    * [WICKET-6650] - Url decode the name of the file after AjaxDownload with Location == Blob
    * [WICKET-6651] - Redirecting with ResetResponseException does not work anymore
    * [WICKET-6669] - CSS Resource Bundling throws exception when used with CssUrlReplacer

** Improvement

    * [WICKET-6648] - It is impossible to initiate AjaxDownloadBehavior with IPartialPageRequestHandler
    * [WICKET-6655] - ExportToolbar should set cache duration to none on the served file and also provide a way to easily change that
    * [WICKET-6656] - JSR 303 - @NotNull validation problems 
    * [WICKET-6658] - Allow nested forms on non-<form> tag
    * [WICKET-6659] - commons-io:commons-io is used in multiple versions at same time
    * [WICKET-6668] - Sign out the existing session if a sign in attempt has failed

** Task

    * [WICKET-6654] - Upgrade JQuery to 3.4.0
    * [WICKET-6661] - Upgrade jquery to 3.4.x
    * [WICKET-6665] - Upgrade various dependencies

=======================================================================

Release Notes - Wicket - Version 8.4.0

** Bug

    * [WICKET-6637] - Handling exception Wicket 8
    * [WICKET-6639] - PageStoreManager$SessionEntry.clear produces NullPointerException
    * [WICKET-6642] - Form.findSubmittingComponent returns null instead of SubmitLink
    * [WICKET-6645] - Concurrent web socket response message processing on the client

** New Feature

    * [WICKET-6641] - Extract an interface for classes allowing to register feedback messages

** Improvement

    * [WICKET-6634] - Save the closeCode and message in WebSocket's ClosedMessage
    * [WICKET-6638] - RedirectRequestHandler does not support Ajax
    * [WICKET-6640] - Add settings for customizing the ModalWindow's spacing, header height and overflow
    * [WICKET-6644] - AbstractPageableView can only be serialized with Java built-in serialization

** Wish

    * [WICKET-6646] - Upgrade jquery to 3.3.x

** Task

    * [WICKET-6647] - Upgrade asm to 7.1

=======================================================================

Release Notes - Wicket - Version 8.3.0

** Bug

    * [WICKET-6616] - Stateless pages are not rendered anymore before executing a behavior
    * [WICKET-6617] - Header contribution ignore <wicket:header-items/>
    * [WICKET-6623] - Consecutive Temporary Behaviors are not properly removed
    * [WICKET-6628] - wicket-ioc needs to use ASM 7 and CGLIB 3.2.10 to be compatible with JDK11
    * [WICKET-6629] - OOM (and disk) in AsynchronousPageStore
    * [WICKET-6630] - FileUpload.writeToTempFile() fails with commons-fileupload 1.4
    * [WICKET-6631] - AnnotProxyFieldValueFactory does not cache beanNames

** New Feature

    * [WICKET-6578] - StatelessResourceLink
    * [WICKET-6626] - Introduce application-wide Component#onComponentTag listeners

** Improvement

    * [WICKET-6587] - CheckBoxSelector should accept more CheckBoxes to be added later
    * [WICKET-6615] - maven-surefire-plugin need to be updated to work with latest OpenJdk8
    * [WICKET-6621] - Exceeding exception retries should return control back to server

** Wish

    * [WICKET-6569] - LambdaModel.of overload is ambiguous

** Task

    * [WICKET-6624] - Upgrade to commons-filupload 1.4

=======================================================================

Release Notes - Wicket - Version 8.2.0

** Bug

    * [WICKET-4423] - Modal Window dragging failes with iframe
    * [WICKET-5552] - Events to close pop-up on Modal Window are not propagated
    * [WICKET-6586] - Broken JavaScript due to fix charsetName in JavaScriptPackageResource
    * [WICKET-6588] - Under Tomcat (ver. >= 8.5) BaseWebSocketBehavior can't find session id cookie 
    * [WICKET-6589] - ClientInfo fails with NumberFormatException for unusual browser versions
    * [WICKET-6599] - ResponseIOException should never escape from WicketFilter
    * [WICKET-6602] - AuthenticatedWebApplication login Workflow broken with replaceSession
    * [WICKET-6603] - WicketTester.destroy sometimes hangs
    * [WICKET-6604] - Ajax repaint is not correctly handled when component being repainted has an enclosure associated with it and is not a child of the enclosure
    * [WICKET-6606] - data-wicket-placeholder is invalid XHTML
    * [WICKET-6607] - NoSuchMethodError when using Spring-Beans with constructor injection in an AjaxLink#onClick
    * [WICKET-6608] - Stateless page, mix of queue and add can cause unforseen consequences
    * [WICKET-6610] - Incorrect Javadoc: Refering to specific page in Application properties file is not possible

** Improvement

    * [WICKET-6435] - WicketTester should provide assertExists and assertNotExists methods
    * [WICKET-6600] - Error logging in AjaxRequestHandler is too strict
    * [WICKET-6601] - Events to close pop-up on Modal Window are not propagated from caption bar
    * [WICKET-6605] - Allow AjaxFallbackButton to be stateless 

** Task

    * [WICKET-6594] - JavaDoc of redirectToInterceptPage in Component urges to use redirectTo method when in a constructor
    * [WICKET-6609] - Update Guice from 4.1.0 to 4.2.2

=======================================================================

Release Notes - Wicket - Version 8.1.0

** Bug

    * [WICKET-6551] - LazyInitProxyFactory doesn't work correctly at Weblogic
    * [WICKET-6552] - Spring proxy creation fails with IAE inside ASM ClassReader on JDK10
    * [WICKET-6553] - SelectOptions doesn't html-encode option values
    * [WICKET-6564] - PageStoreManager#clear() does not completely clear
    * [WICKET-6568] - Wicket fails / does not encode request header values in AjaxCalls
    * [WICKET-6571] - BaseWebSocketBehavior should check sessionId cookie name dynamically
    * [WICKET-6573] - WicketTesterHelper ignores invalid Ajax-Event names starting with "on"
    * [WICKET-6574] - JQueryResourceReference#get() (still) return V1
    * [WICKET-6584] - Import Junit Package as optional

** New Feature

    * [WICKET-6577] - Introduce class GenericWebMarkupContainer

** Improvement

    * [WICKET-6560] - Improve serialization warnings in ChainingModel
    * [WICKET-6565] - ResponseIOException logged as an error in DefaultExceptionMapper
    * [WICKET-6575] - Ajax requests are still firing even when placeholder tag is written only
    * [WICKET-6576] - Support multiple dateFormats for LocalDateTextfield
    * [WICKET-6580] - org.apache.wicket.util.lang.Bytes - toString()

=======================================================================

Release Notes - Wicket - Version 8.0.0

** Bug

    * [WICKET-6473] - Double slash break 404page
    * [WICKET-6525] - SubmitLink.onSubmit not invoked when using GET-method
    * [WICKET-6528] - Component part of Page check in AjaxRequestHandler breaks applications
    * [WICKET-6533] - Error while parsing xml using AjaxButton
    * [WICKET-6540] - When form.setDefaultButton(button) is called, the form is not displayed
    * [WICKET-6542] - Wrong message for IllegalArgumentException in ConverterLocator.java
    * [WICKET-6543] - onConfigure() Javadoc claims Overrides must call super, but there is no check to enforce it
    * [WICKET-6545] - Argument 'markup' may not be null.
    * [WICKET-6546] - CssContentHeaderItem comments CSS code with HTML comments

** Improvement

    * [WICKET-6055] - AjaxLazyLoadPanel should provide non-blocking lazy load
    * [WICKET-6321] - Support Integrity and Crossorigin attributes for JavaScriptUrlReferenceHeaderItem 
    * [WICKET-6503] - Ajax refresh and feedback panel
    * [WICKET-6527] - no possibility to override fixed html appended directly to response by FilterForm
    * [WICKET-6541] - Allow wizard finish before last step
    * [WICKET-6548] - CSVDataExporter custom converters and resource strings

=======================================================================

Release Notes - Wicket - Version 8.0.0-M9

** Bug

    * [WICKET-6332] - NullPointerException in PageParameters#equals()
    * [WICKET-6441] - MockHttpSession and MockSessionStore don't call onInvalidate() on invalidate()
    * [WICKET-6448] - Provide behavior that disables a button after click
    * [WICKET-6477] - Component.getDefaultModelObject() wraps in RuntimeException instead of WicketRuntimeException
    * [WICKET-6484] - Wicket.Cookie.set does not set 'secure' flag
    * [WICKET-6489] - Exception when "identifier|code" javascript is not start on PrependJavaScript
    * [WICKET-6491] - AjaxDownload is not working in blob mode for Microsoft browsers
    * [WICKET-6492] - javascript files are not minified in deployment mode and not united
    * [WICKET-6493] - WebSocket SessionIds are wrong (HttpSession one used instead of Websocket one) + NPE if no HttpSession is found during Handshake Request
    * [WICKET-6496] - Duplicate definition of interface JSONString
    * [WICKET-6501] - DefaultPageManagerProvider does not honour StoreSettings.setAsynchronous(false)
    * [WICKET-6506] - Performance issue when large component isn't visible
    * [WICKET-6512] - pageId is being reset during Session::replaceSession() call
    * [WICKET-6513] - NullPointerException at PageStoreManager$SessionEntry after login
    * [WICKET-6518] - Memory leaks on quickstart restart in tomcat
    * [WICKET-6522] - ThreadLocal leak in PageStoreManager
    * [WICKET-6523] - Two AbstractAjaxTimerBehaviors on same component timeId conflict
    * [WICKET-6524] - Do not require bean validation 2.0.0
    * [WICKET-6529] - Feedback from onSubmit not rendered on stateless pages
    * [WICKET-6530] - Race-condition in session invalidation

** New Feature

    * [WICKET-6497] - unify javascript files
    * [WICKET-6498] - wicket 8 - js to asnyc and or defer

** Improvement

    * [WICKET-6055] - AjaxLazyLoadPanel should provide non-blocking lazy load
    * [WICKET-6284] - Introduce lambda-enhanced factory method in ResourceReference 
    * [WICKET-6499] - Support for Bean Validation 2.0
    * [WICKET-6503] - Ajax refresh and feedback panel
    * [WICKET-6504] - Use a serializable model for FileSystemResource's path
    * [WICKET-6509] - Import junit packages as optional
    * [WICKET-6514] - FeedbackCollector(Component) should not collect session-scoped feedback messages
    * [WICKET-6517] - use Ajax for multipart instead of iframe

** Task

    * [WICKET-6148] - Remove AjaxEventBehavior#onCheckEvent() before Wicket 8.0.0

=======================================================================

Release Notes - Wicket - Version 8.0.0-M8

** Bug

    * [WICKET-6455] - AjaxFormSubmitBehavior doesn't submit inner forms
    * [WICKET-6457] - PageStore not cleared at session end
    * [WICKET-6459] - Ajax re-renders of enclosures do not render their children's header contributions
    * [WICKET-6461] - Default constructor is incorrectly called if optional param is not provided in parameter placeholder URL with additional required parameter
    * [WICKET-6462] - When an Ajax Button is submitted, AjaxFormSubmitBehavior # onSubmit is called twice
    * [WICKET-6465] - PageStore not cleared at session end
    * [WICKET-6471] - FileSystemResource file descriptor leak
    * [WICKET-6476] - It is impossible to use multiple WebSocketTester with the same WebApplication
    * [WICKET-6479] - AjaxNewWindowNotifyingBehavior erroneously reports new window
    * [WICKET-6481] - NullPointerException in MountedMapper
    * [WICKET-6485] - IllegalArgumentException: Argument 'pageClass' may not be null

** Improvement

    * [WICKET-6396] - Model should provide other Optional's methods
    * [WICKET-6451] - Components with collection models do not support unmodifiable or empty sets
    * [WICKET-6460] - Rename AjaxDownload to AjaxDownloadBehavior
    * [WICKET-6463] - Please add additional constructor to Roles
    * [WICKET-6482] - CompoundValidator should implement all Behavior methods

** Task

    * [WICKET-6105] - Decommission wicket-datetime

=======================================================================

Release Notes - Wicket - Version 8.0.0-M7

** Bug

    * [WICKET-4324] - [wicket-ioc] LazyInitProxyFactory CGLIB proxies naming strategy may cause java.lang.IllegalAccessError
    * [WICKET-6366] - Autocomplete race condition makes page unresponsive
    * [WICKET-6373] - Edge not recognized in UserAgent
    * [WICKET-6374] - Exception caused by border extending another border with <wicket:extend>
    * [WICKET-6376] - Ajax redirects to non http(s)-urls are interpreted as relative
    * [WICKET-6377] - Autolinking breaks hierarchy for nested elements
    * [WICKET-6379] - IModel's default models should detach the target model
    * [WICKET-6386] - Simplification of the code in WICKET-3347 is not consistent with previous behavior
    * [WICKET-6387] - ModalWindow PageReference broken
    * [WICKET-6393] - NestedStringResourceLoader ignores hard-coded default values and throws MissingResourceException
    * [WICKET-6398] - WICKET-6204 breaks jQuery.noConflict()
    * [WICKET-6399] - Dequeuing of Border component with nested body fails
    * [WICKET-6400] - Object with array property fails on NPE when construction JSON object
    * [WICKET-6402] - OSGi-Manifest: wicket does not declare BSN
    * [WICKET-6406] - Each path has to be pushed directly
    * [WICKET-6419] -  Localization of PageParameters 
    * [WICKET-6428] - PageProvider#getPageInstance() may return null
    * [WICKET-6429] - AbstractRequestLogger should not create new Sessions
    * [WICKET-6434] - Fixed WicketTester to detect components in enclosure when doing isComponentOnAjaxResponse.

** Improvement

    * [WICKET-6372] - Consider to switch to a sans-serif typeface in the Wicket guide
    * [WICKET-6385] - Allow using custom port for web socket connections
    * [WICKET-6388] - MockServletContext should define non-null session tracking modes
    * [WICKET-6389] - Introduce CsrfPreventionRequestCycleListener that is aware of Web Socket requests
    * [WICKET-6401] - OSGi-Manifest: wicket-util should decalre its dependency to 'commons-io' and 'commons-fileupload'
    * [WICKET-6412] - Model#orElse behavior is inconsistent!
    * [WICKET-6421] - WicketTester should provide assertBehavior method
    * [WICKET-6433] - Allow to set the rel attribute with CssHeaderItem
    * [WICKET-6436] - Please add CompoundPropertyModel.of(T object) method
    * [WICKET-6437] - Libraries should be updated to most recent versions

** Task

    * [WICKET-6390] - Update Tomcat to 8.5.15 to get the latest HTTP2 features and APIs

=======================================================================

Release Notes - Wicket - Version 8.0.0-M6

** Bug

    * [WICKET-6177] - Introduce AsynchronousPageStore
    * [WICKET-6339] - Wrong implementation of MarkupFragment.iterator()
    * [WICKET-6353] - Proxy deserialization fails due to classloading issues
    * [WICKET-6356] - Clustering failover not working on Tomcat
    * [WICKET-6360] - WebSocket fails when url-based jsessionid is used
    * [WICKET-6361] - MarkupContainer#queue doesn't work in table column
    * [WICKET-6362] - HeaderItems with different PageParameters are treated as identical
    * [WICKET-6365] - Links on "Contribute to the User Guide" are broken

** Improvement

    * [WICKET-6347] - IChoiceRenderer implements IDetachable
    * [WICKET-6348] - New FormComponentUpdatingBehavior to replace wantOnSelectionChangedNotifications()
    * [WICKET-6351] - Form.anyFormComponentError performance degradation
    * [WICKET-6354] - Add JavaScriptResourceReference for JQuery 3.x
    * [WICKET-6355] - Pass the request attributes to FileSystemResource#createResourceResponse()
    * [WICKET-6364] - The autogenerated input field when a form has a default button should not gain the focus when navigating with tab

** New Feature

    * [WICKET-6286] - Would be good to have AjaxDownload available out of the box 

** Task

    * [WICKET-4201] - IPageProvider and its implementations need to be improved
    * [WICKET-6322] - Remove lambda factories
    * [WICKET-6363] - Do not use jetty-all but specific dependencies

=======================================================================

Release Notes - Wicket - Version 8.0.0-M5

** Bug

    * [WICKET-6317] - AuthenticatedWebSession#signOut() calls twice after session invalidation
    * [WICKET-6319] - AutoCompleteTextField: popup is hidden when clicking on scrollbar in IE
    * [WICKET-6329] - org.json migration issue
    * [WICKET-6337] - Wrong class type in PageAccessSynchronizer
    * [WICKET-6340] - The Ajax reponse of an AjaxSubmitButton creates invalid XHTML markup for multipart forms
    * [WICKET-6342] - Wrong baseUrl in BaseWebSocketBehavior

** Improvement

    * [WICKET-6212] - CheckChoice / add a getAdditionalAttributes() also for <label>-tag
    * [WICKET-6265] - Make it possible to authorize component instantiations both with OR and AND rules
    * [WICKET-6328] - Add 'pathInfo' to ForwardAttributes
    * [WICKET-6333] - Use onConfigure() to set the visibility of ExportToolbar instead of overriding isVisible()
    * [WICKET-6334] - WicketObjects#sizeof() and #cloneObject() should not use IObjectCheckers 
    * [WICKET-6335] - tree.Node calls virtual methods from constructor
    * [WICKET-6336] - Add #removePage(IManageablePage) to IPageManager
    * [WICKET-6343] - Add hook method for exception handling to StatelessChecker
    * [WICKET-6345] - Check for non-null PushBuilder before trying to use it

** New Feature

    * [WICKET-6286] - Would be good to have AjaxDownload available out of the box 

=======================================================================

Release Notes - Wicket - Version 8.0.0-M4

** Bug

    * [WICKET-6165] - Inconsistent behavior of Markupstream.hasMore vs. MarkupStream.next.
    * [WICKET-6288] - StatelessLink not working
    * [WICKET-6303] - renderHead method of a Behavior added to a Border body is not called
    * [WICKET-6306] - Changing model object of Panel added to a Border not allowed
    * [WICKET-6310] - Once invalidated the Session will be invalidated on every detach
    * [WICKET-6311] - SignOutPage_ru.html is missing
    * [WICKET-6314] - 7.6 release references 7.5-SNAPSHOT poms

** Improvement

    * [WICKET-6307] - SubmitLink does not work in Chrome "ff.onsubmit is not a function"
    * [WICKET-6308] - Add an extra constructor to EmailTextField with a custom validator but without model
    * [WICKET-6315] - Optimize LinkParser by caching the compiled regex Pattern

** Task

    * [WICKET-6287] - Switch from json.org to open-json
    * [WICKET-6304] - Remove Tomcat7 support in Native WebSockets
    * [WICKET-6305] - Remove Atmosphere module
    * [WICKET-6309] - Move the Native WebSocket demo to wicket-examples module

=======================================================================

Release Notes - Wicket - Version 8.0.0-M3

** Bug

    * [WICKET-6041] - Nested forms / parent FormComponents do not reflect updated model when nested form submitted
    * [WICKET-6256] - 8.0.0-M1 <wicket:link> MarkupNotFoundException
    * [WICKET-6257] - Page instance isn't mapped to an URL just after the 'cid' parameter is add
    * [WICKET-6262] - IllegalArgumentException: Argument 'filterPrefix' may not be null or empty with WebSocketBehavior
    * [WICKET-6267] - Native Websocket exception when the page is expired
    * [WICKET-6270] - No upload is seen as empty upload after WICKET-6210
    * [WICKET-6277] - Broadcasting ClosedMessage about the JSR 356 WebSocket connection after the container was turned off
    * [WICKET-6279] - AttributeModifier.VALUELESS_ATTRIBUTE_REMOVE does not work after deserialisation
    * [WICKET-6283] - Page parameter equality should not depend on named parameters order
    * [WICKET-6285] - NoRecordsToolbar should override onConfigure rather than isVisible
    * [WICKET-6289] - Autolinking adds onclick attribute to <img> tags
    * [WICKET-6290] - CssUrlReplacer doesn't understand data: urls and breaks them
    * [WICKET-6292] - Button.onSubmit not called for multipart requests
    * [WICKET-6296] - Not possible to add WebSocketBehavior in ajax request
    * [WICKET-6298] - Markup not found for Component id =_header_ and WICKET-6231, regression ?

** Improvement

    * [WICKET-5920] - roll a version of ListDataProvider implementing ISortableDetachable model
    * [WICKET-6056] - Improvements to browser info gathering implementation
    * [WICKET-6258] - Repeater example page show the back button with the enable link style when disabled
    * [WICKET-6260] - Revert Ajax detection needed for character encoding to WebRequest.isAjax()
    * [WICKET-6261] - CheckGroupSelector default selection state incorrect when the list of Checks is empty
    * [WICKET-6263] - Fix JavaScript tests to pass with jQuery 2.x. and 3.x
    * [WICKET-6264] - Form: improve error message for disabled/invisible IFormSubmittingComponent
    * [WICKET-6269] - Use jdk-serializable-functional voor Serializable functional interfaces
    * [WICKET-6271] - IRequestableComponent getPage() javaDoc and Component implementation mismatch
    * [WICKET-6274] - Add origin header to ajax requests in BaseWicketTester
    * [WICKET-6276] - Reduce memory footprint for LambdaModel
    * [WICKET-6281] - Listener interfaces' methods should use empty default methods for friendlier extension
    * [WICKET-6282] - Make native web socket message classes serializable
    * [WICKET-6284] - Introduce lambda-enhanced factory method in ResourceReference 
    * [WICKET-6293] - Behavior#onTag() should pass the Component as well
    * [WICKET-6297] - Add wicket:label tag in wicket.xsd

** New Feature

    * [WICKET-6275] - Stream support for MarkupContainer

=======================================================================

Release Notes - Wicket - Version 8.0.0-M2

** Sub-task

    * [WICKET-6243] - ResourceReferenceAutolink component resolved by AutoLinkResolver ignores session locale changes

** Bug

    * [WICKET-5836] - Update the version of clirr-maven-plugin (current 2.6.1)
    * [WICKET-5972] - Datepicker "Close" text overlays 'x' icon.
    * [WICKET-6136] - AutoCompleteTextField issue in Android 5.1.1
    * [WICKET-6161] - SecuritySettings.setEnforceMounts() should be applicable for all kind of pages
    * [WICKET-6190] - Update user guide to cover lambda support
    * [WICKET-6192] - Remove recreateBookmarkablePagesAfterExpiry check in AbstractBookmarkableMapper#mapHandler
    * [WICKET-6196] - CheckingObjectOutputStream broken in Wicket 7
    * [WICKET-6198] - Unable to disable a MultiFileUploadField
    * [WICKET-6202] - Guide: 26.1 Page storing, section HttpSessionDataStore - example code is not correct
    * [WICKET-6204] - Copy only the provided attributes for Ajax link inclusion
    * [WICKET-6209] - requesting focus on disabled field fails with error in IE8
    * [WICKET-6214] - ModalWindow broken on IE
    * [WICKET-6215] - Test fail when non empty model is set to PasswordTextField
    * [WICKET-6216] - Problem with queued components and border
    * [WICKET-6217] - Enclosure broken within Border/Panel
    * [WICKET-6219] - Fragment fails to report an error in development mode
    * [WICKET-6220] - TagTester incomplete support for void elements
    * [WICKET-6221] - WicketTester - missing border path
    * [WICKET-6222] - renderHead not called with anonymous inner Border class
    * [WICKET-6225] - Button wrongly sets its model object as 'value' attribute
    * [WICKET-6227] - CharSequenceResource calculates wrong length when there are unicode symbols
    * [WICKET-6230] - Infinite redirection when using UrlPathPageParametersEncoder
    * [WICKET-6231] - wicket:enclosure and getVariation().
    * [WICKET-6232] - When sending binary data from server to client, wicket-websocket-jquery.js throws error "message.indexOf is not a function"
    * [WICKET-6235] - TableTree#updateNode() fails if no corresponding node is visible
    * [WICKET-6236] - Files.remove() causes a 5 seconds delay instead of 500ms as was intended
    * [WICKET-6237] - PageRequestHandlerTracker doesn't work with IRequestHandlerDelegate
    * [WICKET-6238] - pub2 Wicket example isn't switching the beer images
    * [WICKET-6241] - CheckingObjectOutputStream should track the original instance, before writeReplace()
    * [WICKET-6242] - Weak concurrency management in AuthenticatedWebSession#signedIn
    * [WICKET-6244] - Palette does not list unselected options
    * [WICKET-6245] - Open up CsrfPreventionRequestCycleListener for extension
    * [WICKET-6249] - Invalid state of LoadableDetachableModel with exception during load()
    * [WICKET-6250] - FileUploadField does not deteach models and fails to null the reference to the transient fileUploads field if forceCloseStreamsOnDetach is false
    * [WICKET-6254] - Wicket WebSockets problem behind HTTP proxy without servlet context

** Improvement

    * [WICKET-5866] - Reconsider generics of IConverterLocator#getConverter()
    * [WICKET-6200] - Add default converters for Java 8 classes
    * [WICKET-6203] - Rename IModel#mapWith() to IModel#combineWith() and reorder the parameters
    * [WICKET-6206] - Allow to use custom anticache parameter value for Image component
    * [WICKET-6210] - FileUpload does not support files of zero size
    * [WICKET-6211] - PasswordTextField should clear password by default
    * [WICKET-6226] -  DOCTYPE URL in properties.xml example in wicket documentation won't work.
    * [WICKET-6228] - Hide AuthenticatedWebSession#signOut() from the public API
    * [WICKET-6229] - Introduce a new setting in ExceptionSettings to control whether to throw exception or log a WARN when requesting for markup id on non-renderable component
    * [WICKET-6233] - Add component info in the error messages related to WicketTester#assertComponentOnAjaxResponse() 
    * [WICKET-6234] - Log the decrypted url in CryptoMapper for debugging purposes
    * [WICKET-6239] - Use Response#addHeader() instead of #setContentLength()
    * [WICKET-6240] - Hook method to display more information on ExceptionErrorPage

** New Feature

    * [WICKET-5623] - Custom Getters and Setters for PropertyResolver
    * [WICKET-6194] - PushBuilder API integration
    * [WICKET-6299] - Autofill support based on whatwg standard

=======================================================================

Release Notes - Wicket - Version 8.0.0-M1

** Bug

    * [WICKET-5836] - Update the version of clirr-maven-plugin (current 2.6.1)
    * [WICKET-5993] - AjaxButton - image is not shown even though type="image" is in html-template 
    * [WICKET-5994] - Mounted TemplateResourceReference throws  org.apache.wicket.WicketRuntimeException when https is used
    * [WICKET-5995] - "Range" header parsing is broken
    * [WICKET-5996] - Mounted packages throw IllegalArgumentException when visiting base package url.
    * [WICKET-5997] - Compatibility problem with Websphere liberty profile
    * [WICKET-5999] - AjaxFormValidatingBehavior not updates initially hidden feedback component
    * [WICKET-6001] - Exception raised while refreshing a page with queued components missing in the markup
    * [WICKET-6002] - FileUploadField makes form-component models become null on submit
    * [WICKET-6005] - WicketRuntimeException from AjaxPagingNavigator#onAjaxEvent
    * [WICKET-6006] - ModalWindow.closeCurrent() causes 414 status error
    * [WICKET-6007] - PageableListView constructor argument and set/getItemsPerPage are inconsistent
    * [WICKET-6010] - Downloading filenames containing ',' or ';' gives problems
    * [WICKET-6011] - NPE in case DebugBar is added to AjaxRequestTarget
    * [WICKET-6013] - CLONE - AjaxFallbackOrderByBorder wicketOrder[Up|Down|None] class missing in 7.1.0
    * [WICKET-6014] - TransparentWebMarkupContainer breaks OnChangeAjaxBehavior for Select2
    * [WICKET-6017] - Tests fail when executed with not expected locale
    * [WICKET-6018] - TransparentWebMarkupContainer is not really "transparent"
    * [WICKET-6020] - GuiceFieldValueFactory returns the NULL_SENTINEL from the cache
    * [WICKET-6021] - ConcurrentModificationException in MarkupContainer#iterator#next
    * [WICKET-6024] - Possible issue with Border and LoadableDetachableModel in 7.1.0
    * [WICKET-6026] - Problem in detecting child id on nested <wicket:enclosure>
    * [WICKET-6027] - Nested TransparentWebMarkupContainer, markup of inner component not found
    * [WICKET-6028] - Detach called on enclosure component while it had a non-empty queue
    * [WICKET-6031] - NPE in PackageResourceReference#getResource() when there is no request
    * [WICKET-6032] - Wicket.Ajax.done() called twice on redirect
    * [WICKET-6036] - Failure to process markup with nested tags inside a Label
    * [WICKET-6037] - ModalWindow vulnerable to Javascript injection through title model
    * [WICKET-6041] - Nested forms / parent FormComponents do not reflect updated model when nested form submitted
    * [WICKET-6043] - Cannot set wicket:enclosure on queued component in ListView
    * [WICKET-6044] - AjaxFormChoiceComponentUpdatingBehavior: Duplicate input values according to WICKET-5948
    * [WICKET-6045] - ListView NullPointerException when viewSize is set explicitly
    * [WICKET-6048] - German Translation for EqualInputValidator wrong
    * [WICKET-6050] - Wicket Ajax (Wicket.From.serializeElement) causes 400 bad request
    * [WICKET-6052] - CSS header contribution overlap
    * [WICKET-6058] - Error in calculation of byte ranges
    * [WICKET-6059] - TransparentWebMarkupContainer can not resolve autocomponents in its parent
    * [WICKET-6062] - MockHttpSession should renew its id after invalidation
    * [WICKET-6063] - Add support for WebSocketRequest#getUrl() and other properties which are available in the handshake request
    * [WICKET-6064] - WebSocketResponse.sendRedirect could be supported with <ajax-response><redirect>...</></>
    * [WICKET-6065] - Calling http://examples7x.wicket.apache.org/resourceaggregation/ generate Internal error
    * [WICKET-6068] - The key RangeValidator.exact is not mapped in Application_de.properties
    * [WICKET-6069] - OnChangeAjaxBehavior does not work if the url contains a request parameter with same name as wicket id
    * [WICKET-6076] - Problem with queued components and enclosure
    * [WICKET-6078] - Problem with queued components and auto linking
    * [WICKET-6079] - Problem with queued components and label
    * [WICKET-6080] - Encapsulation of 3 enclosures leads to WicketRuntimeException
    * [WICKET-6084] - ajax request failure handler receives incorrect arguments
    * [WICKET-6085] - AjaxTimerBehavior with failure handler cause memory leak in browser
    * [WICKET-6087] - Invalid AbstractRequestWrapperFactory.needsWrapper method scope: package - cannot create a custom implementation
    * [WICKET-6088] - Problem with queued components and setting the model
    * [WICKET-6091] - NPE in RequestLoggerRequestCycleListener when using native-websockets
    * [WICKET-6094] - Find adequate ResourceReference with mount parameters
    * [WICKET-6097] - JsonRequestLogger --> JsonMappingException --> StackOverflowError Infinite recursion
    * [WICKET-6102] - StackoverflowError related to enclosures
    * [WICKET-6108] - Closing a ModalWindow with jQuery 2.2.0 produces javascript errors
    * [WICKET-6109] - Enclosure - "IllegalArgumentException: Argument 'markup' may not be null" after app restart
    * [WICKET-6111] - Empty redirect on redirect to home page if home page already shown
    * [WICKET-6116] - Exception 'A child already exists' when backing to a page with some markups in a Border
    * [WICKET-6129] - IRequestCycleListener not notified of all executed handlers
    * [WICKET-6131] - IndexOutOfBoundsException in org.apache.wicket.core.request.mapper.CryptoMapper.decryptEntireUrl
    * [WICKET-6133] - Failing test SpringBeanWithGenericsTest in 7.3.0.0 SNAPSHOT
    * [WICKET-6134] - NPE when using ListView with missing markup
    * [WICKET-6135] - There is no good way to get POST body content
    * [WICKET-6139] - AjaxButton forces rendering type="button" 
    * [WICKET-6141] - Runtime Exception rendering ComponentTag with RelativePathPrefixHandler
    * [WICKET-6151] - DebugBar/PageSizeDebugPanel throws NullPointerException (need wrapper exception with more detail)
    * [WICKET-6154] - Performance bottleneck when using KeyInSessionSunJceCryptFactory
    * [WICKET-6155] - Newline in ModalWindow title 
    * [WICKET-6157] - WicketTester and application servers are destroying app differently
    * [WICKET-6160] - Missing type for MediaComponent causing iOS devices not to be able to play videos
    * [WICKET-6161] - SecuritySettings.setEnforceMounts() should be applicable for all kind of pages
    * [WICKET-6162] - Reload leads to unexpected RuntimeException 'Unable to find component with id'
    * [WICKET-6169] - NullPointerException accessing AbstractRequestLogger.getLiveSessions
    * [WICKET-6170] - Wrong requestmapper used for cache decorated resources
    * [WICKET-6171] - Problem with nested dialog with multipart form
    * [WICKET-6172] - Inconsistent results from getTag[s]ByWicketId
    * [WICKET-6173] - WICKET-6172 makes TagTester.createTagsByAttribute stop working
    * [WICKET-6174] - Browser/Client info navigatorJavaEnabled property returns undefined
    * [WICKET-6175] - Aautocomplete suggestion window is not closing in IE11
    * [WICKET-6180] - JMX Initializer's usage of CGLIB makes it impossible to upgrade to CGLIB 3.2.3
    * [WICKET-6185] - Border body not reachable for visitors
    * [WICKET-6187] - Enclosures rendered twice in derived component
    * [WICKET-6191] - AjaxTimerBehavior will stop after ajax update of component it is attached to

** Improvement

    * [WICKET-5866] - Reconsider generics of IConverterLocator#getConverter()
    * [WICKET-5920] - roll a version of ListDataProvider implementing ISortableDetachable model
    * [WICKET-5950] - Model and GenericBaseModel could both implement IObjectClassAwareModel
    * [WICKET-5969] - Please give us access to PageTable.index pageId queue
    * [WICKET-5986] - NumberTextField<N> should use Models for minimum, maximum and step
    * [WICKET-6015] - AjaxFallbackOrderByBorder/Link should support updateAjaxAttributes() idiom
    * [WICKET-6019] - Remove 'final' modifier for Localizer#getStringIgnoreSettings() methods
    * [WICKET-6023] - small tweak for component queuing for the AbstractRepeater
    * [WICKET-6029] - Make Border's methods consistent with commit f14e03f
    * [WICKET-6046] - Wicket Quickstart Example Application shows deployment memory leak in Tomcat
    * [WICKET-6051] - Improve performance of CssUrlReplacer
    * [WICKET-6053] - Allow to reuse the same application instance in several tests
    * [WICKET-6054] - Provide a factory method for the WebSocketResponse & WebSocketRequest
    * [WICKET-6060] - Deprecate org.apache.wicket.util.IProvider
    * [WICKET-6061] - Improved PackageResource#getCacheKey
    * [WICKET-6070] - Provide factory methods for WizardButtonBar buttons
    * [WICKET-6072] - Improve the quickstart to make it easier to use JSR-356 web sockets
    * [WICKET-6081] - Add "assertNotRequired" to the WicketTester
    * [WICKET-6098] - Add logging to HttpSessionDataStore
    * [WICKET-6100] - Upgrade jQuery to 1.12.3/2.2.3
    * [WICKET-6103] - Synchronization on JSR 356 connection
    * [WICKET-6104] - Rework AjaxFallback** components to use java.util.Optional for their #onEvent methods
    * [WICKET-6106] - Propagate JSR 356 WebSocket connection error to a page 
    * [WICKET-6107] - Broadcast onClose event regardless of the JSR 356 WebSocket connection closed state
    * [WICKET-6110] - Add a message to StalePageException for better debugging
    * [WICKET-6113] - Improve ResourceStreamResource API by passing Attributes to #getResourceStream()
    * [WICKET-6114] - FormComponentPanel#clearInput() should delegate to its FormComponent children
    * [WICKET-6115] - Provide default implementation of IDetachable#detach() in IModel
    * [WICKET-6117] - Make IGenericComponent a mixin/trait so it could be easily reused in custom components
    * [WICKET-6118] - Deprecate org.apache.wicket.util.IContextProvider
    * [WICKET-6122] - Add .map to the list of allowed file extensions in SecurePackageResourceGuard
    * [WICKET-6123] - Remove 'abstract' from ChainingModel
    * [WICKET-6127] - Add metrics for request duration
    * [WICKET-6128] - Add metrics for currently active sessions
    * [WICKET-6130] - Make it easier to override parts of SystemMapper
    * [WICKET-6132] - AbstractChoice#getChoices() should be final
    * [WICKET-6137] - ListenerInterfaceRequestHandler simplification
    * [WICKET-6140] - Ajax should prevent updating components which are not on page
    * [WICKET-6144] - Wicket-ajax parameter / header may be used to bypass proper exception handling
    * [WICKET-6145] - Enable DeltaManager to replicate PageTable in Sessions
    * [WICKET-6146] - Provide default implementation of IRequestHandler#detach()
    * [WICKET-6152] - Allow to add more than one WebSocketBehavior in the component tree
    * [WICKET-6153] - WicketTester's MockHttpServletRequest doesn't expose setLocale(aLocale) method
    * [WICKET-6178] - MetaDataHeaderItem # generateString() should return specials characters escaped like StringEscapeUtils.escapeHtml(s) does
    * [WICKET-6182] - Remove recreateBookmarkablePagesAfterExpiry check in Component#createRequestHandler
    * [WICKET-6183] - Improve stateless support for AJAX
    * [WICKET-6184] - Remove form argument from AjaxButton and AjaxLink callbacks
    * [WICKET-6188] - Use DynamicJQueryResourceReference by default
    * [WICKET-6189] - Return Optional<T> from RequestCycle.find(Class<T>)

** New Feature

    * [WICKET-5991] - Introduce models which use Java 8 supplier/consumer
    * [WICKET-6025] - Read resource files with Java's NIO API
    * [WICKET-6042] - Implementation of ExternalImage component
    * [WICKET-6112] - Microservices support (decoupled component usage)
    * [WICKET-6120] - Wicket Metrics
    * [WICKET-6121] - use lambdas for columns
    * [WICKET-6193] - NestedStringResourceLoader - replaces nested keys within property files
    * [WICKET-6194] - PushBuilder API integration

** Task

    * [WICKET-5990] - Upgrade Jetty usage in Wicket tests/quickstart to Jetty 9.3.x
    * [WICKET-6004] - Wicket 8 cleanup - TODOs and deprecated methods
    * [WICKET-6057] - Upgrade commons-collections to 4.1
    * [WICKET-6071] - Upgrade jQuery to 1.12 / 2.2.0
    * [WICKET-6119] - Deprecate HtmlDocumentValidator
    * [WICKET-6147] - Remove the support for the deprecated /wicket.properties and /META-INF/wicket/**.properties
    * [WICKET-6150] - Deprecate org.apache.wicket.util.crypt.Base64 and use java.util.Base64

** Wish

    * [WICKET-6067] - Provide an Ajax Behavior that prevents form submit on ENTER
    * [WICKET-6095] - Multiline headers in DataTable

=======================================================================

