This file contains all changes done in releases for Apache Wicket 7.x.

=======================================================================

Release Notes - Wicket - Version 7.0.0

** Bug

    * [WICKET-5909] - Session style is not taken into account when loading mounted resources.
    * [WICKET-5924] - FileUploadField does not work with Servlet 3.0 multipart config
    * [WICKET-5927] - Velocity remote code execution
    * [WICKET-5939] - AjaxEventBehavior should not lower-case the event name
    * [WICKET-5942] - TestCase failure after cfa36fbea621
    * [WICKET-5944] - CSRF prevention does not work with https URLs on the default port
    * [WICKET-5946] - JavaScript/Css PackageResource should use the same charset for compressing

** Improvement

    * [WICKET-5922] - IoC: Optionally use objensis for proxy creation to inject concrete classes without default ctor
    * [WICKET-5926] - Arquillian Support with Container ServletContext in BaseWicketTester/WicketTester.
    * [WICKET-5928] - Move WicketTestCase from tests to main so that it is reusable by other Wicket modules and applications.
    * [WICKET-5929] - Introduce IPartialPageRequestHandler
    * [WICKET-5930] - Upgrade Atmosphere to 2.2.7
    * [WICKET-5931] - Improve generics for ListView: don't use wildcard for T
    * [WICKET-5932] - Allow empty column list for DataTable
    * [WICKET-5933] - Avoid serialization of untouched page after ajax request
    * [WICKET-5935] - IoC Guice: cache proxies and fail on creation when binding is missing
    * [WICKET-5936] - simplify cdata escaping for ajax-response
    * [WICKET-5945] - add a new topic/listener that notifies of Ajax calls done

=======================================================================

Release Notes - Wicket - Version 7.0.0-M6

** Bug

    * [WICKET-5790] - VariableInterpolator & #getThrowExceptionOnMissingResource
    * [WICKET-5814] - CryptoMapper clears feedback messages
    * [WICKET-5816] - Apps can't use Application.setName instead of WicketFilter for e.g. JMX names
    * [WICKET-5822] - AjaxSelfUpdatingTimer stops working after ajax download
    * [WICKET-5825] - Deployment of wicket-examples.war fails in Tomcat
    * [WICKET-5828] - PageProvider not serializable
    * [WICKET-5834] - NPE in DefaultPropertyResolver
    * [WICKET-5835] - InlineEnclosure doesn't call child.configure() before updating its visilbity
    * [WICKET-5837] - JUnit tests may fail because of AbstractDefaultAjaxBehavior
    * [WICKET-5838] - Last-modified header of external markup is ignored
    * [WICKET-5841] - continueToOriginalDestination() discards new cookies
    * [WICKET-5843] - CryptoMapper doesn't work with context relative UrlResourceReferences
    * [WICKET-5845] - AuthenticatedWebSession.get() returns a new session with signedIn false
    * [WICKET-5850] - LazyInitProxyFactory causes NoClassDefFound org/apache/wicket/proxy/ILazyInitProxy in case of multimodule deployment
    * [WICKET-5851] - PackageResourceTest#packageResourceGuard test fails under Windows
    * [WICKET-5853] - LongConverter converts some values greater than Long.MAX_VALUE
    * [WICKET-5855] - RememberMe functionality seems to be broken after the change of the default crypt factory
    * [WICKET-5856] - StackOverFlowError when working with transparent containers
    * [WICKET-5857] - PagingNavigator invalid HTML (rel attribute on span tag)
    * [WICKET-5858] - AjaxRequestTarget.focusComponent does not work in modal window
    * [WICKET-5861] - BigDecimalConverter does not allow parsing of values great than Double.MAX_VALUE
    * [WICKET-5862] - Wicket Container visibility bug
    * [WICKET-5864] - Multipart Ajax form submit does not release the channel in case of connection failure
    * [WICKET-5869] - Kittencaptcha doesn't calculate click y-coordinate correctly
    * [WICKET-5870] - wicket-event-jquery.js: Wicket.Browser.isIE11() does not return boolean
    * [WICKET-5874] - WicketTester TagTester does not work as expected when using non self closing tags
    * [WICKET-5879] - Using an AjaxSubmitLink to hide its form results in an exception
    * [WICKET-5881] - NPE in FormComponent#updateCollectionModel in case of no converted input and unmodifiable collection
    * [WICKET-5883] - Feedback messages not cleared for invisible/disabled form components on submit.
    * [WICKET-5887] - wicket.xsd refers to non-existing xhtml.label:attlist
    * [WICKET-5891] - Parsing of ChinUnionPay credit card should use the first 6 characters
    * [WICKET-5893] - CookieUtils should use the original response when saving a cookie
    * [WICKET-5895] - validateHeaders fails to detect missing head/body (regression)
    * [WICKET-5898] - StackOverflowError after form submit with a validation error
    * [WICKET-5900] - Add WicketTester support for IAjaxLink
    * [WICKET-5903] - Regression in mount resolution when using optional parameters
    * [WICKET-5904] - NPE after editing a markup file in debug mode
    * [WICKET-5906] - Use default on missing resource does not work
    * [WICKET-5908] - A new HtmlHeaderContainer is added each time a page instance is rendered
    * [WICKET-5910] - CGLib proxy should not intercept protected methods
    * [WICKET-5911] - Re-rendering page after exception in render phase does not call onBeforeRender()
    * [WICKET-5912] - NPE in Page#hasInvisibleTransparentChild
    * [WICKET-5915] - The application can not find  /META-INF/wicket/**.properties on Windows systems
    * [WICKET-5916] - StackOverflowError when calling getObject() from load() in LDM
    * [WICKET-5917] - Do not use jQuery's $ in String snippets in Java code

** Improvement

    * [WICKET-5314] - AbstractAutoCompleteBehavior does not support AjaxChannels
    * [WICKET-5749] - Wicket-auth-roles should deal with resource authorization
    * [WICKET-5789] - Make org.apache.wicket.protocol.ws.javax.WicketServerEndpointConfig publicly visible
    * [WICKET-5801] - Responsive Images
    * [WICKET-5823] - DefaultAuthenticationStrategy should be modified to reduce copy/paste while extending it's functionality
    * [WICKET-5829] - rename PageSettings#recreateMountedPagesAfterExpiry
    * [WICKET-5831] - Improve unsafe Id reporting in the AbstractRepeater
    * [WICKET-5832] - Do not fail at CDI's ConversationPropagator when running in non-http thread
    * [WICKET-5833] - Add a way to get all opened web socket connections per user session
    * [WICKET-5840] - WicketTester doesn't support #clickLink() for ExternalLink component
    * [WICKET-5859] - Add Hebrew and Arabic translations
    * [WICKET-5860] - Cross-Site Websocket Hijacking protection
    * [WICKET-5863] - Overiding disableCaching in ServletWebResponse is ignored when responce is buffered
    * [WICKET-5865] - AjaxEditableLabel should implement IGenericComponent
    * [WICKET-5872] - wicket extensions initializer.properties for greek language
    * [WICKET-5875] - ComponentRenderer.renderComponent() unexpectedly produces a WicketRuntimeException when called with a nested Component which contains a nested wicket:message
    * [WICKET-5889] - Ability to not submit a nested form
    * [WICKET-5892] - add ClientProperties#isJavaScriptEnabled()
    * [WICKET-5894] - Support *.woff2 webfonts in SecurePackageResourceGuard as well
    * [WICKET-5901] - Leaving veil when ajax processing ends with redirect
    * [WICKET-5905] - allow listening to Ajax calls before scheduling
    * [WICKET-5921] - Provide a default implementation of IModelComparator that always returns false

** New Feature

    * [WICKET-5819] - Support for HTML 5 media tags (audio / video)
    * [WICKET-5827] - Allow to apply multiple Javascript / CSS compressors
    * [WICKET-5897] - Use the #isEnabled() method with validators
    * [WICKET-5918] - Create an Image component that uses the new data: protocol (an InlineImage)
    * [WICKET-5919] - Add support for CSRF prevention

** Task

    * [WICKET-5896] - Upgrade jQuery to latest stable versions (1.11.4 & 2.1.3)

** Wish

    * [WICKET-5848] - Remove .settings folders of projects

=======================================================================

Release Notes - Wicket - Version 7.0.0-M5

** Bug

    * [WICKET-5584] - DiskDataStore error
    * [WICKET-5747] - Wicket Ajax Click handling gets requeued in OnDomReady so fire out of order
    * [WICKET-5748] - StackOverflowError while trying to get the Markup of WicketHeadContainer
    * [WICKET-5751] - NullPointerException in IntHashMap
    * [WICKET-5752] - ReplacementResourceBundleReference should return the dependencies for the replacing resource ref
    * [WICKET-5759] - AjaxRequestAttributes extra parameters aren't properly handled in getCallbackFunction()
    * [WICKET-5770] - PageParametersEncoder should not decode parameters with no name
    * [WICKET-5772] - LoadableDetachableModel caches null value if load() fails, bug in getObject() {attached = true;}
    * [WICKET-5777] - Reload of page leads to WicketRuntimeException
    * [WICKET-5782] - Missing escaping in MultiFileUploadField.js - sort of XSS
    * [WICKET-5783] - Multiple events in AjaxEventBehavior with prefix 'on'
    * [WICKET-5784] - arraycopy with bad length in AbstractRequestLogger:172
    * [WICKET-5793] - Request for static resource creating a session in 6.13.0+
    * [WICKET-5800] - wicket:enclosure within FormComponentPanel causes exception related to dequeueing
    * [WICKET-5809] - URL IPv6 parsing
    * [WICKET-5811] - Infinite loop issue in PropertyValidator#createUnresolvablePropertyMessage(FormComponent<>) 
    * [WICKET-5812] - AtmosphereBehavior wrongly sets Ajax base url to '.'
    * [WICKET-5817] - Wicket-JMX should depend on asm-util

** Improvement

    * [WICKET-4703] - StringResourceModel should provide an overridable getString(Component) method
    * [WICKET-5746] - Fire an event once all JS event listeners are registered
    * [WICKET-5753] - It is impossible to determine the form submitting component's inputName when AjaxFormSubmitBehavior is used
    * [WICKET-5754] - (String)ResourceModel's defaultValue could be an IModel<String>
    * [WICKET-5756] - Allow to use custom ciphers when using SunJceCrypt class
    * [WICKET-5758] - Portuguese translation
    * [WICKET-5760] - Add constructor (String, Serializable, String) to AttributeAppender
    * [WICKET-5774] - UrlRenderer should render full and absolute urls in their canonical form
    * [WICKET-5775] - Replace the session upon successful signin for better support for Session Fixation
    * [WICKET-5776] - Add information about the component when it fail in detach phase
    * [WICKET-5778] - Pass the IModifiable to the IChangeListener in ModificationWatcher
    * [WICKET-5780] - Add a resource reference for ContextRelativeResource
    * [WICKET-5789] - Make org.apache.wicket.protocol.ws.javax.WicketServerEndpointConfig publicly visible
    * [WICKET-5794] - Make DefaultExceptionMapper extensible
    * [WICKET-5797] - Convenience method to call setResponsePage with forward option
    * [WICKET-5799] - Add rel=prev/next in PagingNavigator.html
    * [WICKET-5802] - HTML Import
    * [WICKET-5803] - Reduce log for "o.a.w.r.PropertiesFactory | Loading properties files from ..."
    * [WICKET-5806] - Wicket.Log should log (at least errors) in the browser console even when Wicket Ajax Debug window is disabled
    * [WICKET-5808] - SpringBean, support generic beans
    * [WICKET-5818] - Add support for httpOnly cookies to CookieDefaults

** New Feature

    * [WICKET-5771] - Ability to escape resource bundle messages added with wicket:message

** Task

    * [WICKET-5732] - Improve component queuing and auto component
    * [WICKET-5791] - Update JQuery to 1.11.2 and 2.1.3

=======================================================================

Release Notes - Wicket - Version 7.0.0-M4

** Bug

    * [WICKET-5265] - FencedFeedbackPanel is broken with RefreshingView(and it's implementations)
    * [WICKET-5326] - Wicket doesn't encrypt links and Ajax URLs for mounted pages when CryptoMapper is used
    * [WICKET-5689] - Nested Redirects and REDIRECT_TO_BUFFER
    * [WICKET-5698] - WebApplication#unmount() unmounts the whole compound mapper if some of its inner ones matches
    * [WICKET-5701] - WebSocketRequestHandler is not set as a scheduled and thus RequestCycle#find(AjaxRequestTarget.class) doesn't work 
    * [WICKET-5704] - IllegalArgument exception with wicket:child in ajaxrequest
    * [WICKET-5706] - ResourceUtils.getLocaleFromFilename cannot handle filenames with classifiers
    * [WICKET-5711] - OnChangeAjaxBehavior should listen for both 'inputchange' and 'change' events for TextField and TextArea
    * [WICKET-5712] - SecuritySettings.setEnforceMounts() does not work when the mounted mapper is not in the root compound mapper
    * [WICKET-5714] - MockHttpServletRequest.buildRequest() should work for parameters with multiple values with multipart content type
    * [WICKET-5716] - wicket-autocomplete prevents "change"-listener when leaving input via mouse click
    * [WICKET-5717] - Url.parse/toString should support fragment
    * [WICKET-5719] - Wicket-atmosphere should warn about phone home
    * [WICKET-5720] - Method Strings.join doesn't work correctly if separator is empty.
    * [WICKET-5723] - CheckGroupSelector with disabled checks
    * [WICKET-5724] - Queueing component in autocomponent
    * [WICKET-5728] - Component queuing breaks with html tags that don't require close tag.
    * [WICKET-5730] - Dequeue auto component can't resolve components if they are nested in child markup
    * [WICKET-5731] - Using "Submit" button in Ajax DropDownChoice example leads to broken HTML page
    * [WICKET-5733] - ContextNotActiveException thrown when using both CDI & Native WebSocket
    * [WICKET-5734] - Problem with WICKET-4441 and RestartResponseAtInterceptPageException
    * [WICKET-5736] - Atmosphere Eventbus throws Concurrent Modification Exception
    * [WICKET-5741] - Ajax Form example is broken due to 9716f2a7e799133fdf3f7927d0093e6dfe16f529

** Improvement

    * [WICKET-5350] - Enhancement for AbstractChoice and WildcardListModel API
    * [WICKET-5691] - Wicket FileUploadField.getFileUploads() should never return null.
    * [WICKET-5692] - Misleading message in PropertyValidator
    * [WICKET-5694] - Add WicketTester assertion methods for component's markup style, variation and locale 
    * [WICKET-5695] - Use Log4j 2.x for the archetype
    * [WICKET-5697] - Rework Spring application to use annotations based configuration
    * [WICKET-5708] - Making FormComponent.convertInput() public
    * [WICKET-5710] - StringValidator should set 'minlength' attribute to input and textarea
    * [WICKET-5713] - Move /wicket.properties to /META-INF/wicket/xyz.properties
    * [WICKET-5715] - Allow setting 'async' attribute on reference <script> elements
    * [WICKET-5718] - Upgrade Atmosphere to 2.2.2
    * [WICKET-5725] - Add NumberTextField(String,Class<T>) constructor
    * [WICKET-5729] - Avoid using input names that conflict with JavaScript DOM API method and attribute names
    * [WICKET-5735] - Propagate fileSizeMax attribute to org.apache.commons.fileupload.FileUploadBase
    * [WICKET-5737] - Log a warning when WebSocketResponse#sendError() is used
    * [WICKET-5739] - Add a factory method for WebSocketRequestHandler
    * [WICKET-5740] - Provide a way to adapt the lock timeout based on the page class/id

** New Feature

    * [WICKET-5677] - Components should have onAdd to complement onRemove

** Task

    * [WICKET-5705] - Disable Javadoc linter in JDK 1.8 because it is too strict

** Test

    * [WICKET-5722] - Queueing in merged markup with transparent resolver in the base class

=======================================================================

Release Notes - Wicket - Version 7.0.0-M3

** Sub-task

    * [WICKET-5633] - Make JavaScriptFilteredIntoFooterHeaderResponse non-final to be able to create custom filters

** Bug

    * [WICKET-5241] - RequestLogger's server duration does not include 'detach duration'
    * [WICKET-5371] - IllegalArgumentException: Argument 'page' may not be null. - when sending event from asynchronous process
    * [WICKET-5564] - AjaxRequestTarget.focusComponent() does not work when two Ajax responses arrive next to each other
    * [WICKET-5578] - Stateless/Statefull pages - incorrect behaviour
    * [WICKET-5603] - OnChangeAjaxBehavior attached to DropDownChoice produces two Ajax requests in Chrome v35
    * [WICKET-5607] - Wicket Ajax fires calls scheduled by AbstractAjaxTimerBehavior even after unload of the page
    * [WICKET-5609] - AutoCompleteTextField can only complete text that is visible on screen browser screen 
    * [WICKET-5615] - UploadProgressBar does not show up if no FileUploadField is given
    * [WICKET-5616] - CLONE - ModalWindow is not visible in Safari when opened from a link at the bottom of a large page
    * [WICKET-5619] - ConcurrentModificationException may occur when calling EventBus.post()
    * [WICKET-5624] - Do not throw when WebSocket is not supported
    * [WICKET-5626] - ConcatBundleResource#reportError() doesn't print the resource attributes
    * [WICKET-5630] - Fix last button translation germany of wizard to 'Letzter'
    * [WICKET-5631] - Allow submitting with POST method for PhantomJS
    * [WICKET-5636] - Update StatelessForm's and AbstractRepeater's javadoc that FormComponents should be repeated only with RepeatingView
    * [WICKET-5637] - Fix the encoding of the Chinese translations for Wizard component
    * [WICKET-5639] - ResourceResponse does not write headers when status code is set
    * [WICKET-5643] - WebPageRenderer should bind a Session if redirect is required and the session is temporary.
    * [WICKET-5644] - AjaxWizardButtonBar broken since ARA#preventDefault is false
    * [WICKET-5647] - missing generic cast causes compile error on OS X / jdk 8
    * [WICKET-5655] - Problem with setting of IComponentInheritedModel and FLAG_INHERITABLE_MODEL
    * [WICKET-5656] - PropertyResolver does not scan for NotNull in annotation tree
    * [WICKET-5657] - wicket-autocomplete may fail after preceeding Ajax request
    * [WICKET-5662] - @SpringBean(name="something", required=false) still throws org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'something' is defined
    * [WICKET-5670] - org.apache.wicket.protocol.ws.api.registry.IKey should be Serializable (IClusterable)
    * [WICKET-5679] - RenderStrategy REDIRECT_TO_RENDER lets fail test with BaseWicketTester#startComponentInPage
    * [WICKET-5680] - AjaxEditableLabel keeps raw input after cancel following a validation failure 
    * [WICKET-5682] - AbstractAjaxTimerBehavior fails to trigger
    * [WICKET-5684] - Autocomplete example renders the search term
    * [WICKET-5686] - @Inject should require the bean dependency instead of setting null
    * [WICKET-5687] - content type in header is being lost when using a AbstractTransformerBehavior
    * [WICKET-5688] - Restore the functionality an IPageManager to be able to clean all data/pages for the current session 

** Improvement

    * [WICKET-4344] - Implement onValidateModelObjects() and beforeUpdateFormComponentModels() for nested forms
    * [WICKET-4660] - Make it possible to notify about Atmosphere internal events
    * [WICKET-5349] - Replace <table> usage in non-tabular markup
    * [WICKET-5452] - Make Wicket-Atmosphere testable - AtmosphereTester
    * [WICKET-5602] - DynamicImageResource should set the mime type after reading the image data
    * [WICKET-5605] - Store browser capabilities in local variables in wicket-event-jquery.js
    * [WICKET-5611] - Add AjaxChannel.DEFAULT constant = "0" and type "Queue"
    * [WICKET-5617] - Make it possible to set markup id to CSS header contributions
    * [WICKET-5620] - Remove second <listener-class> for CDI related stuff in wicket-examples' web.xml
    * [WICKET-5627] - broadcastMessage(): hook to set more thread-local context before rendering components
    * [WICKET-5628] - Introduce a marker interface for exception which are recommended to be handler by the framework
    * [WICKET-5629] - Add an HeaderItem for meta data tags such as <meta> or canonical <link>
    * [WICKET-5634] - Add IObjectCheckers that fails the serialization when the Session or another Page are serialized
    * [WICKET-5635] - Provide a way to modify ResourceReferenceRegistry.DefaultResourceReferenceFactory externally to be used by wicket-bootstrap-less
    * [WICKET-5640] - Reduce the mangling of HTML markup in the Java code as much as possible
    * [WICKET-5642] - CheckingOutputObjectStream should filter duplicates by identity, not by equality
    * [WICKET-5645] - Markup String of IMarkupResourceStreamProvider throws NPE for inherited markup
    * [WICKET-5646] - Allow subclasses of ComponentStringResourceLoader to stop at specific components
    * [WICKET-5648] - CookieUtils - multivalue related methods are broken due to the usage of ";" as a separator for the values
    * [WICKET-5650] - Make is possible to position the choice label before/after/around the choice
    * [WICKET-5651] - Add TagTester#getChild(String tagName) method
    * [WICKET-5652] - Improve Javadoc of Ajax behaviors concerning their onXyz() methods
    * [WICKET-5653] - Add a setter for IViolationTranslator to BeanValidationConfiguration
    * [WICKET-5654] - DefaultViolationTranslator should maybe use getMessage()
    * [WICKET-5658] - AjaxFormComponentUpdatingBehavior should not clear the rawInput when updateModel is false
    * [WICKET-5659] - Add a setting to MultiFileUploadField to not close the file uploads' streams
    * [WICKET-5660] - Throw more specific exception when a component cannot be found by ListenerInterfaceRequestHandler
    * [WICKET-5667] - Preserve the NotSerializableException if an error occur while using the IObjectCheckers
    * [WICKET-5668] - StringResourceModel with custom locale 
    * [WICKET-5669] - Mark page parameters with a flag where they have been read from
    * [WICKET-5671] - Rename log4j.properties in the quickstart when creating a project for WildFly
    * [WICKET-5672] - Form#findSubmittingComponent() should not throw exceptions if the submitter is disabled/invisible
    * [WICKET-5673] - Improve BookmarkableMapper and BasicResourceReferenceMapper to not match when the last segment is empty
    * [WICKET-5674] - Use jquery.atmosphere.js as a Webjar
    * [WICKET-5683] - PreviousButton isEnabled() should bo logical conjunction of getWizardModel().isPreviousAvailable() and super.isEnabled()

** Task

    * [WICKET-5632] - Use frontend-maven-plugin to run the JavaScript tests
    * [WICKET-5649] - Create Eclipse lifecycle mapping for clirr-maven-plugin.
    * [WICKET-5664] - Log a warning if the name of the JavaScript event starts with 'on' 
    * [WICKET-5665] - WicketTester#assertComponentOnAjaxResponse() cannot test invisible components

** Wish

    * [WICKET-4551] - Enable components underneath disabled components
    * [WICKET-5340] - CssAttributeModifier and StyleAttributeModifier

============================================================================

Release Notes - Wicket - Version 7.0.0-M2

** Bug

    * [WICKET-4545] - MarkupNotFoundException for Fragment and TransparentWebMarkupContainer
    * [WICKET-5241] - RequestLogger's server duration does not include 'detach duration'
    * [WICKET-5560] - A 404 error occurs when using a CryptoMapper
    * [WICKET-5569] - Unable to find markup for children of deeply nested IComponentResolvers during Ajax response
    * [WICKET-5573] - FilterToolbar generics broken
    * [WICKET-5579] - wicket:enclosure on wicket:extend - Detach called on component with id '0' while it had a non-empty queue
    * [WICKET-5581] - CachingRes​ourceStrea​mLocator is not extension-​aware
    * [WICKET-5582] - ServletWebResponse#encodeUrl() makes absolute Urls relative
    * [WICKET-5589] - Upgrade wicket-atmosphere to the latest version of atmosphere
    * [WICKET-5591] - Missing translation for HoursValidator.range (in DateTimeField)
    * [WICKET-5592] - Add a method to clear the cache of CachingResourceStreamLocator
    * [WICKET-5593] - AjaxFormValidatingBehavior attempts to update non-visible feedback panels
    * [WICKET-5595] - Atmosphere: updates infinitly with long polling transport
    * [WICKET-5596] - DropDownChoice#wantsOnSelectionChangedNotifications(T) not being called on unmounted page
    * [WICKET-5597] - button behaviour changed
    * [WICKET-5598] - AjaxFallbackButton does not call #onError(ART, Form) when JavaScript disabled

** Improvement

    * [WICKET-5574] - ComponentRenderer should use Application#createRequestCycle
    * [WICKET-5575] - Add support in FormTester#submit(String|Component) for Ajax submitters
    * [WICKET-5577] - Generation of wicket ids with prefix / suffix
    * [WICKET-5580] - Allow markup to find child fragments when wicket:child is inside a component tag
    * [WICKET-5585] - Wicket Extension Automplete does not work well with JavaScriptFilteredIntoFooterHeaderResponse
    * [WICKET-5586] - NextButton isEnabled() should bo logical conjunction of getWizardModel().isNextAvailable() and super.isEnabled()
    * [WICKET-5594] - AjaxButton #onSubmit() #onError() intricacies
    * [WICKET-5600] - Introduce CharSequenceResource similar to ByteArrayResource
    * [WICKET-5606] - SelectOptions with #setRecreateChoices(true) loses selection on form errors

** New Feature

    * [WICKET-5590] - Add a method to clear the cache of CachingResourceVersion

** Task

    * [WICKET-5172] - Add entry for Wicket 7 Javadocs in the website
    * [WICKET-5587] - Upgrade JQuery to latest releases - 1.11.1 & 2.1.1

** Wish

    * [WICKET-5340] - CssAttributeModifier and StyleAttributeModifier

