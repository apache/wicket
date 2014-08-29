This file contains all changes done in releases for Apache Wicket 6.x.

=============================================================================

Release Notes - Wicket - Version 6.17.0

** Sub-task

    * [WICKET-5633] - Make JavaScriptFilteredIntoFooterHeaderResponse non-final to be able to create custom filters

** Bug

    * [WICKET-5371] - IllegalArgumentException: Argument 'page' may not be null. - when sending event from asynchronous process
    * [WICKET-5539] - Allow preserving of the parsed PageParameters when re-creating an expired page
    * [WICKET-5564] - AjaxRequestTarget.focusComponent() does not work when two Ajax responses arrive next to each other
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
    * [WICKET-5685] - jquery.wicketatmosphere.js is not packed in wicket-atmosphere.jar and thus leads to 404
    * [WICKET-5686] - @Inject should require the bean dependency instead of setting null
    * [WICKET-5687] - content type in header is being lost when using a AbstractTransformerBehavior
    * [WICKET-5688] - Restore the functionality an IPageManager to be able to clean all data/pages for the current session 

** Improvement

    * [WICKET-4344] - Implement onValidateModelObjects() and beforeUpdateFormComponentModels() for nested forms
    * [WICKET-4660] - Make it possible to notify about Atmosphere internal events
    * [WICKET-5452] - Make Wicket-Atmosphere testable - AtmosphereTester
    * [WICKET-5602] - DynamicImageResource should set the mime type after reading the image data
    * [WICKET-5605] - Store browser capabilities in local variables in wicket-event-jquery.js
    * [WICKET-5611] - Add AjaxChannel.DEFAULT constant = "0" and type "Queue"
    * [WICKET-5627] - broadcastMessage(): hook to set more thread-local context before rendering components
    * [WICKET-5628] - Introduce a marker interface for exception which are recommended to be handler by the framework
    * [WICKET-5629] - Add an HeaderItem for meta data tags such as <meta> or canonical <link>
    * [WICKET-5634] - Add IObjectCheckers that fails the serialization when the Session or another Page are serialized
    * [WICKET-5635] - Provide a way to modify ResourceReferenceRegistry.DefaultResourceReferenceFactory externally to be used by wicket-bootstrap-less
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
    * [WICKET-5671] - Rename log4j.properties in the quickstart when creating a project for WildFly
    * [WICKET-5673] - Improve BookmarkableMapper and BasicResourceReferenceMapper to not match when the last segment is empty
    * [WICKET-5674] - Use jquery.atmosphere.js as a Webjar
    * [WICKET-5683] - PreviousButton isEnabled() should bo logical conjunction of getWizardModel().isPreviousAvailable() and super.isEnabled()

** Task

    * [WICKET-5604] - Add a page to the site that lists other Apache projects that use Wicket
    * [WICKET-5632] - Use frontend-maven-plugin to run the JavaScript tests
    * [WICKET-5665] - WicketTester#assertComponentOnAjaxResponse() cannot test invisible components

=============================================================================

Release Notes - Wicket - Version 6.16.0

** Bug

    * [WICKET-4545] - MarkupNotFoundException for Fragment and TransparentWebMarkupContainer
    * [WICKET-5553] - When using an Ajax request to display initially hidden components inside inline enclosures, only the first one appears.
    * [WICKET-5560] - A 404 error occurs when using a CryptoMapper
    * [WICKET-5569] - Unable to find markup for children of deeply nested IComponentResolvers during Ajax response
    * [WICKET-5570] - Rescheduling the same ajax timer behavior causes memory leak in the browser
    * [WICKET-5573] - FilterToolbar generics broken
    * [WICKET-5581] - CachingResourceStreamLocator is not extension-aware
    * [WICKET-5582] - ServletWebResponse#encodeUrl() makes absolute Urls relative
    * [WICKET-5589] - Upgrade wicket-atmosphere to the latest version of atmosphere
    * [WICKET-5591] - Missing translation for HoursValidator.range (in DateTimeField)
    * [WICKET-5592] - Add a method to clear the cache of CachingResourceStreamLocator
    * [WICKET-5593] - AjaxFormValidatingBehavior attempts to update non-visible feedback panels
    * [WICKET-5595] - Atmosphere: updates infinitly with long polling transport
    * [WICKET-5596] - DropDownChoice#wantsOnSelectionChangedNotifications(T) not being called on unmounted page
    * [WICKET-5601] - AbstractSingleSelectChoice fails with NullPointerException when its choice renderer returns null from #getIdValue() 

** Improvement

    * [WICKET-5563] - RestartResponseAtInterceptPageException - add public function to retrieve originalUrl
    * [WICKET-5574] - ComponentRenderer should use Application#createRequestCycle
    * [WICKET-5575] - Add support in FormTester#submit(String|Component) for Ajax submitters
    * [WICKET-5577] - Generation of wicket ids with prefix / suffix
    * [WICKET-5580] - Allow markup to find child fragments when wicket:child is inside a component tag
    * [WICKET-5585] - Wicket Extension Automplete does not work well with JavaScriptFilteredIntoFooterHeaderResponse
    * [WICKET-5586] - NextButton isEnabled() should bo logical conjunction of getWizardModel().isNextAvailable() and super.isEnabled()
    * [WICKET-5600] - Introduce CharSequenceResource similar to ByteArrayResource
    * [WICKET-5606] - SelectOptions with #setRecreateChoices(true) loses selection on form errors

** Task

    * [WICKET-5587] - Upgrade JQuery to latest releases - 1.11.1 & 2.1.1

=============================================================================

Release Notes - Wicket - Version 6.15.0

** Sub-task

    * [WICKET-5510] - Avoid using jQuery.text() when possible. It is very slow in IE
    * [WICKET-5554] - Disable second level pages store because it is efficient only when manually configured

** Bug

    * [WICKET-5243] - JS: High stack size in Function Executor causes "too much recursion"
    * [WICKET-5284] - Too deep recursion in AbstractHierarchyIterator
    * [WICKET-5504] - AjaxRequestTarget.append/prependJavaScript cannot handle scripts with new-lines anymore
    * [WICKET-5505] - DefaultPropertyResolver does not respect JavaBean conventions
    * [WICKET-5506] - DefaultPropertyResolver should resolve the Property according to the getter first
    * [WICKET-5509] - Wicket examples' MailTemplate from Page is broken
    * [WICKET-5517] - IE11 returns false for Wicket.Browser.isIE()
    * [WICKET-5518] - FormComponent.updateCollectionModel  does not handle unmodifiableList
    * [WICKET-5521] - Stateless forms does not work when RecreateMountedPagesAfterExpiry turned off
    * [WICKET-5522] - Failing HTTPS redirect to RequireHttps annotated pages with ONE_PASS_RENDER strategy
    * [WICKET-5523] - Ajax indicator not working when display is set to none in css
    * [WICKET-5532] - NPE calling PackageResource.getResourceStream() if there is no RequestCycle defined
    * [WICKET-5534] - DataTable component must throw an exception when attached to non-<table> element
    * [WICKET-5536] - Error message without parmeters filled.
    * [WICKET-5537] - Wicket.DOM.toggleClass is called with additional "#" inside AutoLabelResolver
    * [WICKET-5541] - NullPointerException in SubscribeAnnotationEventSubscriptionInvoker on remove of component from page
    * [WICKET-5545] - Cannot use redirect in ModalWindow's page#onInitialize()
    * [WICKET-5546] - Adding behavior in component instantiation listener causes Page.onInitialize() being called even if constructor throws an exception
    * [WICKET-5547] - Javadoc for ReuseIfModelsEqualStrategy should mention that models must implement hashCode() method
    * [WICKET-5548] - Websocket initialization URL is not valid when filter is not mapped to root.

** Improvement

    * [WICKET-5508] - Memory model improvements for Session fields
    * [WICKET-5512] - Allow using child selector for JS event bindings
    * [WICKET-5520] - improve reusability of DataTable and AbstractPageableView
    * [WICKET-5528] - Allow models of subtypes of Select type parameter in SelectOption constructor
    * [WICKET-5529] - Add WebSocketBehavior/Resource#onPush() callback method
    * [WICKET-5531] - Create new placeholder tag to indicate where header contributions should appear
    * [WICKET-5538] - When using Component.setDefaultModel, only detach the previous model if the new one is different
    * [WICKET-5549] - continueToOriginalDestination() fails after redirectToInterceptPage() on AjaxRequest 

** New Feature

    * [WICKET-831] - Return response status 404 when a mapper cannot decode a request url
    * [WICKET-2542] - Provide ajax buttons for wizard

** Task

    * [WICKET-5514] - Update Wicket fragment example

** Wish

    * [WICKET-5516] - RadioChoice / add a getAdditionalAttributes() also for <label>-tag
    * [WICKET-5542] - Remove final from TextArea.onComponentTagBody()


=============================================================================

Release Notes - Wicket - Version 6.14.0

** Bug

    * [WICKET-4697] - Autolinking not working as soon as component gets a wicket:id
    * [WICKET-5043] - Page not mounted with WebApplication#mountPackage
    * [WICKET-5449] - Missing chapter 2 is causing off-by-one page names in the guides URL links
    * [WICKET-5460] - onBeforeRender called too early on stateless page
    * [WICKET-5462] - Ajax form-component-label repainting fails when setResponsePage() is used to navigate away from the page that has previously failed form validaiton
    * [WICKET-5464] - AbstractAjaxTimerBehavior does not work in combination with Wizards
    * [WICKET-5466] - ListenerInterfaceRequestHandler#respond throws ComponentNotFoundException as a side-effect
    * [WICKET-5467] - NumberTextField should support "any" as valid step attribute value
    * [WICKET-5468] - UrlRenderer#renderRelativeUrl potentially appends "/" after query parameters
    * [WICKET-5469] - ModalWindow unload warning always displayed (even if window is closed)
    * [WICKET-5472] - PackageResource#internalGetResourceStream() should return ProcessingResourceStream only when the resource is existing
    * [WICKET-5473] - Wicket does not handle non in-memory Httpsessions correctly
    * [WICKET-5477] - CSS class is not applied to <TD> for data filter
    * [WICKET-5478] - Wrong JavaDoc for WicketTester
    * [WICKET-5480] - AutoLabelResolver creates null pointer exception in 6.13
    * [WICKET-5482] - Wicket-guice doesn't support @javax.inject.Named annotations
    * [WICKET-5484] - WebPageRenderer must not render full page in Ajax requests
    * [WICKET-5486] - WebPageRenderer should honor RedirectPolicy.ALWAYS_REDIRECT more consistently
    * [WICKET-5491] - Wicket.DateTime.getViewportHeight() returning undefined on IE8, positions calendar out of viewport
    * [WICKET-5492] - WebApplication ignores a SecurityException when reading the configuration type
    * [WICKET-5496] - Wrong translation of RangeValidator.minimum and RangeValidator.maximum
    * [WICKET-5497] - NPE in JsonUtils when the value is null
    * [WICKET-5499] - Page is not touched during initialization
    * [WICKET-5500] - Ignore the path parameters when reading the page class
    * [WICKET-5502] - Patch FileUploadBase to fix CVE-2014-0050

** Improvement

    * [WICKET-5288] - Allow <script>-Tags act as WebMarkUpContainer to add Child-Components
    * [WICKET-5439] - Allow restarting AbstractAjaxTimerBehavior without updating its hosting component
    * [WICKET-5463] - RequestUtil.getCharset(HttpServletRequest request) Resilience against unsupported Charsets in Request
    * [WICKET-5470] - Remove 'final' from org.apache.wicket.markup.html.form.EnumChoiceRenderer#getDisplayValue
    * [WICKET-5471] - Store the page if it is known that it will be used in a following request
    * [WICKET-5493] - Disable jar indexing

** New Feature

    * [WICKET-3491] - Introduce IComponentOnConfigureListener

** Task

    * [WICKET-5488] - Promote experimental modules to become stable
    * [WICKET-5494] - Upgrade JQuery to latest stable - 1.11.0 and 2.1.0

=============================================================================

Release Notes - Wicket - Version 6.13.0

** Bug

    * [WICKET-4294] - AutoCompleteTextField does not show suggestions when it is used on ModalWindow in InternetExplorer
    * [WICKET-4997] - Mounted bookmarkable Page not recreated on Session Expiry
    * [WICKET-5084] - ClassNotFoundException with Atmosphere on Bookmarkable pages
    * [WICKET-5224] - ModalWindow is not visible in Safari when opened from a link at the bottom of a large page
    * [WICKET-5321] - Incorporate Wicket Guide into website
    * [WICKET-5379] - IE7: AutoCompleteTextField inside a ModalWindow shows auto complete dropdown behind ModalWindow
    * [WICKET-5387] - Page#onInitialize called after an exception in the constructor of Page
    * [WICKET-5400] - Behaviors#internalAdd(Behavior) erroneously gets id for stateless behaviors
    * [WICKET-5402] - NullPointerException when logging out with wicket-atmosphere
    * [WICKET-5404] - JQuery 1.10.1 does not work well with IE11, upgrade to 1.10.2
    * [WICKET-5409] - wicket-native-websocket does not work with Safari/Safari iOS
    * [WICKET-5413] - Positioning AutoCompleteTextField not correct on resized window
    * [WICKET-5416] - BOM in UTF markup file breaks encoding detection
    * [WICKET-5418] - PropertyValidator ignoring groups with the @NotNull annotation only
    * [WICKET-5422] - ComponentRenderer.renderComponent() adds a page to DataStore
    * [WICKET-5424] - Page.isPageStateless() returning true in regular run but false in WicketTester
    * [WICKET-5425] - WicketTester doesn't preserve the request cookies when #executeListener() is used
    * [WICKET-5426] - Page not recognized as stateless although stateful component is hidden in #onConfigure()
    * [WICKET-5429] - ResourceReference's properties are not preserved when using reference replacement
    * [WICKET-5437] - When a ConversionException is thrown, its message is not used in the ValidationError
    * [WICKET-5441] - IResourceCachingStrategy implementations should only set caching if version matches
    * [WICKET-5442] - TimeOfDay.valueOf(Calendar, Time) and TimeOfDay.valueOf(Time) incorrectly use 12-hour clock
    * [WICKET-5444] - Make org.apache.wicket.pageStore.memory.PageTable public so custom impls of IDataStoreEvictionStrategy can be made
    * [WICKET-5447] - TableTree's NodeBorder does not properly close divs
    * [WICKET-5450] - Double escaping when using EnumChoiceRenderer and DropDownChoice

** Improvement

    * [WICKET-4268] - Quality of ThumbnailImageResource
    * [WICKET-4762] - IResourceVersion should be based on final result
    * [WICKET-4905] - PagingNavigation: possible to call setStartIndex() earlier than onBeforeRender?
    * [WICKET-4909] - ModalWindow overwrites window.onbeforeunload
    * [WICKET-5364] - Reduce logging for wicket-atmosphere
    * [WICKET-5367] - migrate Wicket-Atmosphere to Atmosphere 2
    * [WICKET-5401] - provide no-argument constructor in string based header items
    * [WICKET-5411] - Improve AutoLabels by updating their CSS classes automatically during Ajax requests
    * [WICKET-5415] - Do not initialize pages which were not rendered
    * [WICKET-5420] - Make UploadProgressBar easier to extend
    * [WICKET-5423] - Create IResource based implementation of Native Web Sockets
    * [WICKET-5435] - Ajaxified version of WebSession#getClientInfo should be added
    * [WICKET-5436] - Implement BigIntegerConverter
    * [WICKET-5443] - Increase randomness and reduce predictability of CaptchaImageResource rendered images
    * [WICKET-5445] - Make CaptchaImageResource easier to extend and reuse
    * [WICKET-5455] - Allow access to the JS ResourceReference of AbstractAutoCompleteBehavior
    * [WICKET-5456] - Make RawMarkup extendable

** Task

    * [WICKET-5431] - Download pages must use ASF mirror system for current releases

** Wish

    * [WICKET-1197] - add/remove all buttons in Palette component
    * [WICKET-4951] - Wicket-cdi and OpenWebBeans 1.1.x incompatibility

=============================================================================

Release Notes - Wicket - Version 6.12.0

** Bug

    * [WICKET-4862] - AjaxPagingNavigationLink and AjaxPagingNavigationIncrementLink output inline onclick attributes in addition to Wicket.Ajax.ajax event registration
    * [WICKET-5101] - Could not open second modal window after closing first
    * [WICKET-5356] - AutoCompleteSettings.setShowListOnEmptyInput(true) is not working anymore
    * [WICKET-5359] - org.apache.wicket.util.string.StringValue#equals broken
    * [WICKET-5366] - ResourceAggregator looses information about priority/filtering/... when using a bundle
    * [WICKET-5369] - Can't set a cookie using CookieUtils during an ajax request due to java.lang.ClassCastException: org.apache.wicket.ajax.AbstractAjaxResponse$AjaxResponse cannot be cast to org.apache.wicket.request.http.WebResponse
    * [WICKET-5374] - SourcesPage fails on resources with non-ASCII characters
    * [WICKET-5375] - Improve ConcatBundleResource error handling when a resource is missing
    * [WICKET-5378] - AutoCompleteTextField inside a ModalWindow shows auto complete dropdown in the wrong location
    * [WICKET-5379] - IE7: AutoCompleteTextField inside a ModalWindow shows auto complete dropdown behind ModalWindow
    * [WICKET-5380] - Wicket rebinds the SessionEntry session attribute and this causes problems in Glassfish
    * [WICKET-5382] - AutoComplete JavaScript errors
    * [WICKET-5385] - wicket-bean-validation PropertyValidator_fr.properties.xml : org.xml.sax.SAXParseException
    * [WICKET-5386] - ChainingModel should not check if the modelObject given as argument is null
    * [WICKET-5397] - target.prependJavaScript and JQuery-Animation in IE8
    * [WICKET-5398] - XmlPullParser fails to properly parse from String with encoding declaration

** Improvement

    * [WICKET-5346] - Support MultiFileUploadField in FormTester
    * [WICKET-5362] - Add support for parsing IE 11 user agent
    * [WICKET-5376] - Log the exception if the application cannot start properly
    * [WICKET-5381] - Add Wicket.Event.unsubscribe method
    * [WICKET-5384] - Allow form components to trim the input themselves
    * [WICKET-5388] - Allow DebugBar contributors to be removed
    * [WICKET-5390] - Session management doesn't work with Jetty's JDBCSessionManager 
    * [WICKET-5395] - HeaderItem to be Serializable

** Wish

    * [WICKET-5393] - Remove 'final' from InlineFrame#onComponentTag()

=============================================================================

Release Notes - Wicket - Version 6.11.0

** Bug

    * [WICKET-5313] - Wrong HTML attribute used in SizeTagModifier (wicket-bean-validation)
    * [WICKET-5315] - PriorityHeaderItem+OnDomReadyHeaderItem has no priority in Ajax response
    * [WICKET-5316] - Wicket Atmosphere CouldNotLockPageException
    * [WICKET-5319] - CryptoMapper encrypts external URLs in ResourceReferences making the resources inaccessible
    * [WICKET-5322] - "Source code" link doesn't work at wicket-library.com
    * [WICKET-5325] - ComponentRenderer.renderComponent does not render markup for ListView
    * [WICKET-5329] - "Required" flag initialized too early in PropertyValidator
    * [WICKET-5332] - Autocomplete throws error when initialized in an AjaxLazyLoadPanel
    * [WICKET-5334] - KittenCaptcha example is broken in IE10
    * [WICKET-5335] - After selecting a file MultiFileUploadField does not render properly input field
    * [WICKET-5339] - Broadcast.EXACT does not notify Behaviors of target Component
    * [WICKET-5343] - AutoCompleteTextField suggestions popup position is wrong when its container has scrollbar
    * [WICKET-5345] - Url.canonical() breaks when there are two consecutive "parent" segments followed by a normal segment
    * [WICKET-5348] - JavaDoc for IColumn#getSortProperty() in misleading

** Improvement

    * [WICKET-5327] - CryptoMapper: insecure default encryption provider
    * [WICKET-5331] - Make html5 form validation configurable
    * [WICKET-5333] - wicket-bean-validation french translation
    * [WICKET-5341] - JavaDoc for IFormValidator
    * [WICKET-5354] - Make ResourceBundles and related classes easier for extension

=============================================================================

Release Notes - Wicket - Version 6.10.0

** Sub-task
    * [WICKET-5248] - OnEventHeaderItem is still broken in 6.9.0

** Bug

    * [WICKET-5135] - wicket-atmosphere EventBus constructor throws NullPointerException under invalid state
    * [WICKET-5164] - PageStoreManager.SessionEntry keeps outdated sessionId when container changes sessionId
    * [WICKET-5224] - ModalWindow is not visible in Safari when opened from a link at the bottom of a large page
    * [WICKET-5250] - Minified css/js gets compressed
    * [WICKET-5251] - Minified name resolves incorrectly if default resource reference is used
    * [WICKET-5256] - Allow configuring the resource used by ResourceStreamRequestHandler
    * [WICKET-5259] - Url can't parse urls with username and password
    * [WICKET-5260] - UrlRenderer#renderUrl() doesn't preserve the host
    * [WICKET-5263] - FilenameWithVersionResourceCachingStrategy breaks silently when the versionPrefix is contained in the original filename
    * [WICKET-5266] - Issue with TomcatWebSocketFilter and Spring Security
    * [WICKET-5268] - ComponentRenderer.renderComponent fails to render two components with different component IDs
    * [WICKET-5269] - XmlAjaxResponse restores wrong response
    * [WICKET-5272] - NPE on unmount in BookmarkableMapper.java:189
    * [WICKET-5273] - Tabindex overwritten in modal window for IE - Works in FF, Chrome
    * [WICKET-5279] - o.a.w.a.AtmosphereBehavior onBroadcast should log DEBUG, not INFO
    * [WICKET-5287] - NumberFormatException when parsing the port from Requests with invalid URIs
    * [WICKET-5291] - Dependencies of Resource Bundle References are ignored
    * [WICKET-5294] - MarkupCache ignores enforceReload flag
    * [WICKET-5298] - Wicket Example for effects with Prototype is broken
    * [WICKET-5300] - ComponentRenderingRequestHandler does not clear renderedComponents (PrototypeApplication)
    * [WICKET-5302] - Using InlineEnclosure leads to two re-renderings of the controlling child component
    * [WICKET-5306] - Dynamic and static extra paramaters not evaluated in ajax submitting behavior in a multipart form
    * [WICKET-5309] - No page id appended to some stateful pages using repeaters
    * [WICKET-5310] - if wicket is an external lib, BundleStringResourceLoader doesn't work

** Improvement

    * [WICKET-4993] - Add a minInputLength parameter to AutoCompleteTextField
    * [WICKET-5270] - Do not set the location.hash when clicking on a link
    * [WICKET-5276] - JavaDoc & API for IConverter
    * [WICKET-5277] - PropertiesFactory - customizable properties loading
    * [WICKET-5289] - setRequired(true) should add the HTML5 "required" attribute on <input> and <textarea> elements
    * [WICKET-5295] - UrlResourceStream#getData content-type detection relies on flawed logic in URLConnection#getContentType
    * [WICKET-5296] - OnEventHeaderItem should remove 'on' prefix for the event name
    * [WICKET-5303] - Static id counter AbstractToolbar prevents stable component paths for testing
    * [WICKET-5304] - AbstractResource should check for error code first before for modifications
    * [WICKET-5307] - Export resources' version cache attribute as request cycle metadata

=============================================================================

Release Notes - Wicket - Version 6.9.0

** Bug

    * [WICKET-4827] - Modal panel mouse pointer in resize style for MSIE8 and MSIE9
    * [WICKET-5056] - Page mount with an optional named parameter overtakes a mount with more specific path
    * [WICKET-5071] - 404 Error on Nested ModalWindows in IE7 and IE8
    * [WICKET-5165] - Session should be bound when adding messages to it
    * [WICKET-5194] - Adding AjaxFormValidatingBehavior to the form prevents form from being filled
    * [WICKET-5200] - Upgrade atmosphere-runtime to 1.0.13
    * [WICKET-5202] - Page redirects can fail when using url encoding
    * [WICKET-5203] - Base url is incorrect for error dispatched pages
    * [WICKET-5204] - The DateTimeField.onBeforeRender() method does not format the fields correctly.
    * [WICKET-5205] - AbstractAjaxBehavior.unbind does not clean stored reference to host component
    * [WICKET-5209] - NPE when using ComponentRenderer.renderComponent on a panel with <wicket:enclosure>
    * [WICKET-5218] - EnclosureContainer calls child#configure() way before rendering
    * [WICKET-5220] - AjaxEditableLabel not working well in form
    * [WICKET-5222] - Direct iframe removal not supported by Firefox: tab title stays as "Connecting"
    * [WICKET-5223] - AjaxEditableLabel doesn't provide a way to configure Ajax attributes for its Ajax behaviors
    * [WICKET-5226] - CDI integration fails in Glassfish 4.0 with WELD-000070
    * [WICKET-5230] - AjaxFormChoiceComponentUpdatingBehavior fails for choices containing other invalid FormComponents
    * [WICKET-5234] - OnEventHeaderItem can't be applied via AJAX
    * [WICKET-5237] - Wicket generates invalid HTML by expanding col tags
    * [WICKET-5247] - Broken Link in Tomcat because of Page Mount

** Improvement

    * [WICKET-5199] - Component.getBehaviors(Class<M> type) documentation is obsolete
    * [WICKET-5207] - Use documented jQuery APIs for triggering the special event 'inputchange'
    * [WICKET-5208] - Contribute jQuery 1.x or 2.x depending on the user agent
    * [WICKET-5210] - JQuery problem because component appears twice in DOM while Ajax refresh 
    * [WICKET-5211] - Make CryptoMapper extendable
    * [WICKET-5215] - Better exception message when Page instantiation fails in DefaultPageFactory
    * [WICKET-5219] - ServletWebResponse should call #setHeader() to set Ajax-Location for redirect
    * [WICKET-5228] - Add method getFirstDayOfWeek to DatePicker class
    * [WICKET-5232] - ComponentRenderer.renderComponent could accept components with any markupId, not just "compId"
    * [WICKET-5233] - Component.getBehaviors() can be implemented slightly more efficiently
    * [WICKET-5238] - Upgrade bootstrap from 2.3.1 to 2.3.2

=============================================================================

Release Notes - Wicket - Version 6.8.0

** Sub-task

    * [WICKET-5162] - InlineEnclosure markup id could collide in the final page markup
    * [WICKET-5185] - JavaScript text is spread in several DOM text nodes 

** Bug

    * [WICKET-5083] - Page#isPageStateless() may return wrong value
    * [WICKET-5103] - Wicket session id not up to date when container changes session id
    * [WICKET-5119] - PopupSettings IE8 - dosen't work second time a link is clicked.
    * [WICKET-5140] - InterceptData never gets cleared from session after continueToOriginalDestination is called and another page is requested afterwards
    * [WICKET-5142] - Generating invalid JavaScript for ajax update
    * [WICKET-5145] - do not post an Atmosphere event if the filtered subscription set is empty
    * [WICKET-5146] - Application not destroyed if WicketFilter#init() fails
    * [WICKET-5147] - WicketTester MockHttpRequest.getCookies very slow / OutOfMemory
    * [WICKET-5149] - PageRequestHandlerTracker doesn't track resolves of handlers caused by Exception 
    * [WICKET-5154] - Positioning of autocomplete popup does not take into account borders
    * [WICKET-5157] - URL query parameter values containing equals sign get cut off
    * [WICKET-5163] - Implementing markup loading by extending ResourceStreamLocator produces errors with inherited markup
    * [WICKET-5176] - StringResourceModel doesn't detach model in some cases
    * [WICKET-5178] - StopPropagation functionality on link is broken
    * [WICKET-5181] - Problem with OnEventHeaderItem and Wicket.Event.add
    * [WICKET-5187] - Unknown tag name with Wicket namespace: 'panel'
    * [WICKET-5191] - Session is created unnecessarily

** Improvement

    * [WICKET-5143] - Create an interface for the roles replacing the current Roles class
    * [WICKET-5150] - Log additional info when FormComponent fails in updateCollectionModel
    * [WICKET-5158] - Ignore missing Page in StatelessForm#process()
    * [WICKET-5159] - Replace usage of JavaScript eval() with plain JS in headed contribution decoding
    * [WICKET-5160] - Throttle the animation in DebugWindow when there are many errors
    * [WICKET-5161] - The url gets longer when using StatelessForm with GET method
    * [WICKET-5166] - Relax the requirements for using FilteringHeaderResponse
    * [WICKET-5186] - Use arrays instead of String concatenation in JavaScript for better performance
    * [WICKET-5188] - Use a separate logger for the extra information logged in RequestCycle#onException()
    * [WICKET-5189] - preregister known wicket tag names 

** Task

    * [WICKET-5169] - Create BuildBot config for wicket-6.x branch 

=============================================================================

Release Notes - Wicket - Version 6.7.0

** Bug

    * [WICKET-4658] - TabbedPanel CSS "last" is wrong if last step is not visible
    * [WICKET-4803] - UrlDecoder should log a message when invalid input is provided
    * [WICKET-4871] - wicket-atmosphere version mismatch with wicket-example-jar
    * [WICKET-4893] - AutoCompleteTextField removes DropDownChoice from Page
    * [WICKET-4903] - relativeUrl's begins with ./
    * [WICKET-4907] - UrlResourceReference generates broken relative URLs
    * [WICKET-4942] - Double slash '//' in starting url makes Url.parse output a relative Url
    * [WICKET-4989] - WicketTester should send copies of its cookies 
    * [WICKET-4995] - Using CryptoMapper causes page to render twice
    * [WICKET-5039] - Manual invocation of FunctionsExecutor#notify() is broken
    * [WICKET-5041] - DefaultCssAutoCompleteTextField should have Constructor (String id)
    * [WICKET-5044] - Atmosphere + DateLabel
    * [WICKET-5045] - Upgrade Atmosphere to 1.0.10
    * [WICKET-5047] - Wicket Ajax: Inline script header contribution issue
    * [WICKET-5048] - Inline enclosures don't work with different namespace
    * [WICKET-5049] - Wicket Session may be null in SessionBindingListener after deserialization
    * [WICKET-5052] - @SpringBean fails to invoke methods on bean with non-public methods
    * [WICKET-5053] - "random" parameters is now "_" with jQuery Ajax
    * [WICKET-5054] - Possible bug in org.apache.wicket.util.lang.Packages when building path with repeating names
    * [WICKET-5055] - AutoComplete still triggers redundant events to registered change listener
    * [WICKET-5061] - EnclosureHandler ignores wicket:xyz elements as children
    * [WICKET-5067] - SelectOptions fails to render text on openClose tag
    * [WICKET-5072] - Cookies#isEqual(Cookie, Cookie) may fail with NullPointerException
    * [WICKET-5073] - UrlRenderer#removeCommonPrefixes() fails when contextPath+filterPrefix has more segments than the argument
    * [WICKET-5075] - When modal window is closed page scrolls to top
    * [WICKET-5076] - form#onSubmit() is called on just replaced nested forms
    * [WICKET-5078] - RestartResponseException broken with page instance and bookmarkable page
    * [WICKET-5080] - FilterToolbar.html contains javascript that is used elsewhere
    * [WICKET-5082] - Ajax update renders parent/child JS in different order than initial Page render
    * [WICKET-5085] - InlineEnclosure are piling up on each render
    * [WICKET-5086] - FormTester throws an exception when a Palette component is added to a Form associated with a compound property model
    * [WICKET-5093] - The event listener in Wicket.Ajax.ajax() should not return the value of attrs.ad (allowDefault)
    * [WICKET-5094] - ISecuritySettings#getEnforceMounts(true) prevents access to *all* non-mounted bookmarkable pages
    * [WICKET-5098] - PackageResourceBlockedException under Windows for *.js files in web app's own packages, not in jars
    * [WICKET-5102] - wicket-bean-validation: Bean validation PropertyValidator only works with direct field access
    * [WICKET-5103] - Wicket session id not up to date when container changes session id
    * [WICKET-5104] - AjaxSelfUpdatingTimerBehavior in hidden component in ModalWindow causes Ajax Response Error
    * [WICKET-5112] - Parantheses problem with UrlValidator
    * [WICKET-5114] - Url#toString(StringMode.FULL) throws exception if a segment contains two dots
    * [WICKET-5116] - TabbedPanel.setSelectedTab() does not behave as specified in JavaDoc
    * [WICKET-5117] - Wicket ignores allowDefault:false attribute in multipart ajax requests
    * [WICKET-5123] - Component.continueToOriginalDestination() can redirect to ./.
    * [WICKET-5125] - IE8 error in wicket-date.js when used in a modalwindow
    * [WICKET-5126] - SecurePackageResourceGuard is blocking access to web fonts
    * [WICKET-5131] - Problems with cookies disabled when using 301/302 and also 303 (even with cookies)
    * [WICKET-5132] - Evaluation of returned data (which includes alot of javascript) very slow after ajax call in IE10.
    * [WICKET-5134] - java.lang.NullPointerException at org.apache.wicket.markup.html.form.Form.onComponentTag(Form.java:1520) during Atmosphere eventbus.post() if using WebSockets
    * [WICKET-5136] - CheckingObjectOutputStream#check(Object) swallows Exception without logging the cause
    * [WICKET-5138] - Wicket does not correctly handle http OPTIONS requests
    * [WICKET-5141] - Exception while rendering absolute URL with UrlResourceReference

** Improvement

    * [WICKET-4115] - SignInPanel should not always redirect to the Home page when the user is signed-in automaticaly with the remember-me feature.
    * [WICKET-5005] - Add Utility to allow conversion from Panel (or Component) to html String
    * [WICKET-5038] - Add equals() and hashcode() implementation to INamedParameters.NamedPair
    * [WICKET-5046] - NumberTextField should use appropriate validator
    * [WICKET-5050] - AutoComplete should not request suggestions if input is no longer active element
    * [WICKET-5057] - FilteringHeaderResponse requires a usage of FilteringHeaderResponse.IHeaderResponseFilter for no reason when FilteredHeaderItem is used
    * [WICKET-5062] - Update the list of HTML void elements with the latest HTML5 ones
    * [WICKET-5065] - Improve UrlRenderer to be able to render urls without scheme and/or host 
    * [WICKET-5066] - Allow PackageResource to decide itself whether it could be accepted or not
    * [WICKET-5074] - Improvement for MockHttpServletRequest and FormTester to support 'multiple' input type of fileUpload
    * [WICKET-5079] - Allow WebApplication-specific Spring configuration
    * [WICKET-5088] - The Bootstrap class does not provide a handle for the CSSResourceReferences.
    * [WICKET-5089] - Make MultiFileUploadField's ResourceReference JS public to be able to include in Application's getResourceBundles.
    * [WICKET-5090] - Add path syntax to MarkupContainer#get( String )
    * [WICKET-5091] - Extract the short names of the ajax attributes as a constants
    * [WICKET-5095] - Upgrade Wicket Native WebSocket to Jetty 9.0.0
    * [WICKET-5096] - Add setStep method to NumberTextField
    * [WICKET-5105] - JavaDoc of IHeaderContributor is outdated
    * [WICKET-5111] - Upgrade bootstrap to 2.3.1
    * [WICKET-5113] - Set a different TimeZone for the RequestLogger
    * [WICKET-5121] - Log warning message if a component that is not on the page associated with the AjaxRequestTarget is added
    * [WICKET-5122] - Add the free Wicket guide in the books page on the official site.
    * [WICKET-5127] - Dont use sun-internal packages to allow easy jdk7 compilation
    * [WICKET-5137] - Improve TagTester support for Ajax responses
    * [WICKET-5139] - Missing French translations

** Task

    * [WICKET-5097] - Deprecate BaseWicketTester#startComponent(Component)

** Test

    * [WICKET-5042] - Cleanup FilteringHeaderResponseTest to make it more readable

=============================================================================

Release Notes - Wicket - Version 6.6.0

** Bug

    * [WICKET-4723] - tracking id retrieved in AtmosphereBehavior#onRequest() is always 0
    * [WICKET-4724] - the option name "maxRequests" is wrong in jquery.wicketatmosphere.js
    * [WICKET-4926] - CheckGroupSelector does not work in nested forms in modal windows
    * [WICKET-4984] - Update quickstart info for changed workflow in IntelliJ idea
    * [WICKET-4990] - Problem using AutoCompleteBehavior with AjaxFormComponentUpdatingBehavior("onchange")
    * [WICKET-4998] - AjaxFormComponentUpdatingBehavior("onkeypress") is triggered when Enter is used on autocomplete list
    * [WICKET-5011] - Allow Select to work with non-equals objects
    * [WICKET-5013] - Wicket Enclosure fails with more than one component
    * [WICKET-5014] - Changes in WicketObjects.sizeof(final Serializable object) clashes with <header-contribution> in AjaxResponse
    * [WICKET-5019] - Handling of NO_MINIFIED_NAME in PackageResourceReference#internalGetMinifiedName()   
    * [WICKET-5020] - InlineEnclosureHandler always uses "wicket" namespace
    * [WICKET-5024] - Global ajax event for precondition is not called
    * [WICKET-5027] - FormTester#getInputValue() does not support Select/SelectOption nor other custom components
    * [WICKET-5029] - Palette does not allow to turn off localization
    * [WICKET-5031] - Upgrade bootstrap to 2.3
    * [WICKET-5035] - script tag with wicket:id does not throw exception if it was not added in the code
    * [WICKET-5036] - Post Parameters are lost when continueToOriginalDestination() is called
    * [WICKET-5040] - Session.exists() returns false when ThreadContext#session is not set to current session

** Improvement

    * [WICKET-4444] - Add a callback to the Session which is called when the HttpSession is invalidated
    * [WICKET-4861] - Making MultiFileUploadField use HTML5 multiple attr & remove confusing fakepath
    * [WICKET-4945] - Wicket-atmosphere filters should be functions on AtmosphereResource
    * [WICKET-4946] - Allow passing parameters to atmosphere JS
    * [WICKET-5003] - Add wicket:for attribute in wicket.xsd
    * [WICKET-5006] - Improve null display value in AbstractSingleSelectChoice
    * [WICKET-5010] - Improve wicket-ajax.js to be able to work with jQuery 1.9+
    * [WICKET-5016] - Updated Application_el.properties for 6.x
    * [WICKET-5017] - BaseWicketTester#clickLink() doesn not serialize form to request for SubmitLink but does for AjaxSubmitLink
    * [WICKET-5022] - Improve Application_es.properties
    * [WICKET-5025] - Set the component as a context to the ajax listeners

=============================================================================

Release Notes - Wicket - Version 6.5.0

** Sub-task

    * [WICKET-4976] - WicketTester#startComponent(Component) doesn't detach the component and request cycle

** Bug

    * [WICKET-4906] - Form#visitFormComponents can cause ClassCastException
    * [WICKET-4925] - AbstractAjaxBehavior should clean stored reference to a component on unbind
    * [WICKET-4927] - Header can not be set from IRequestCycleListener#onEndRequest()
    * [WICKET-4928] - Error adding links to WebSocketRequestHandler
    * [WICKET-4935] - Rendered URL is resulting with double slash when using AuthenticatedWebApplication
    * [WICKET-4939] - AbstractAjaxTimerBehavior never triggers if attached to WebPage
    * [WICKET-4948] - Modal window does not center correctly when window is scrolled in safari
    * [WICKET-4950] - ResourceStreamLocator#newResourceNameIterator isn't a factory method anymore
    * [WICKET-4953] - RangeValidator#decorate mixes error keys
    * [WICKET-4954] - Issue with file upload with progress bar via AJAX and Firefox
    * [WICKET-4955] - SessionData violates comparison contract
    * [WICKET-4956] - compareTo methods of Actions in BufferedWebResponse violate Comparable contract
    * [WICKET-4959] - Notify behaviors when a component is removed from the tree
    * [WICKET-4961] - wicket ajax submit does not serialize elements of parental forms
    * [WICKET-4962] - AjaxFormChoiceComponentUpdatingBehavior cannot be triggered with BaseWicketTester#executeAjaxEvent()
    * [WICKET-4965] - NPE when stopping Tomcat
    * [WICKET-4968] - NPE in FencedFeedbackPanel#onRemove
    * [WICKET-4971] - AtmosphereEventSubscriptionCollector is slow
    * [WICKET-4973] - AbstractRequestLogger - infinite ArrayIndexOutOfBoundsException when requestWindow size is 0
    * [WICKET-4975] - client side memory leak on  date picker
    * [WICKET-4986] - wicket-ajax-jquery.js fails with 'member not found' on IE for delayed ajax requests

** Improvement

    * [WICKET-4919] - AjaxLazyLoadPanel needs a method to add components to the AjaxRequestTarget when the component is rendered
    * [WICKET-4933] - Palette does not handle disabled choices correctly
    * [WICKET-4937] - Add IResponseFilter that can filter out invalid XML characters
    * [WICKET-4940] - Make MountedMapper#getMatchedSegmentSizes(url) protected
    * [WICKET-4957] - Listener needed for registration and removal of pages
    * [WICKET-4958] - It should be possible to manipulate AjaxRequestAttributes globally
    * [WICKET-4963] - ComponentModel "setObject" methods should take generic "T" type instead of "Object"
    * [WICKET-4970] - Move the logic for creating the proper PackageResource from PackageResourceReference to ResourceReferenceRegistry
    * [WICKET-4982] - StatelessChecker: add helpful information to find stateful behavior (patch included)
    * [WICKET-4983] - extra recursion on Wicket.DOM.get

=============================================================================

Release Notes - Wicket - Version 6.4.0

** Sub-task
    * [WICKET-4880] - Make it possible to override the Ajax behavior of AjaxSubmitLink and AjaxButton

** Bug
    * [WICKET-4869] - Wicket-Atmosphere track message length
    * [WICKET-4872] - IllegalArgumentException on ReloadingWicketFilter and inheritance markup
    * [WICKET-4877] - encodeUrl fails parsing jsessionid when using root context
    * [WICKET-4878] - Rendering of feedback messages fails with DebugBar in page
    * [WICKET-4881] - IE 8 : error when handling Wicket Ajax Response
    * [WICKET-4884] - ValidationError messages for NumberTextFields with minimum/maximum are always English
    * [WICKET-4886] - Do not register Ajax timer if the component is removed
    * [WICKET-4890] - Bad validation messages after WICKET-2128
    * [WICKET-4891] - UrlRenderer.renderRelativeUrl misbehavior if the filterPath is composed.
    * [WICKET-4894] - Internet Explorer fails fails to properly include conditional stylesheet links added via AjaxRequestTarget
    * [WICKET-4895] - WicketRuntimeException: addOrReplace for feedback panel does not clear Component.FEEDBACK_LIST - feedback from replaced component causes error.
    * [WICKET-4899] - autocomplete shows strings with quotes strings as string2 with &quot;quote&quot;
    * [WICKET-4900] - Setting a status code on an AbstractResource results in no HTTP body
    * [WICKET-4908] - Wrong charset or screwed up characters in Norwegian properties-file
    * [WICKET-4911] - Palette Ajax update does not work
    * [WICKET-4913] - HtmlDocumentParser does not support tags containing number (e.g. h1-h6)
    * [WICKET-4915] - org.apache.wicket.util.resource.Patht#find fails on Windows
    * [WICKET-4916] - AbstractTree$TreeItem renderHead does not call renderHead for child TreeItems.
    * [WICKET-4917] - Websockets are not working if URL has a hash in it
    * [WICKET-4918] - LazyInitProxyFactory prevents using package private interfaces
    * [WICKET-4920] - Rendered Url on root context with cookies disabled might result in double slash //
    * [WICKET-4922] - Cloned ServletWebRequest returns wrong OriginalUrl
    * [WICKET-4923] - CryptoMapper ignores original queryString parameters

** Improvement
    * [WICKET-4873] - Support different session id parameter
    * [WICKET-4876] - CheckBoxMultipleChoice should implement getAdditionalAttributes as RadioChoice
    * [WICKET-4887] - Use a Set to keep the supported wicket elements in WicketTagIdentifier
    * [WICKET-4889] - Label constructor should accept Serializable as label
    * [WICKET-4892] - Provide helpful exception message in RequestCycle#urlFor()
    * [WICKET-4901] - AjaxPagingNaviagtionLink should provide updateAjaxAttributes like AjaxLink
    * [WICKET-4902] - ConcatBundleResource should use the respective ITextResourceCompressor
    * [WICKET-4924] - Websocket broadcast support does not work with OSGi

** New Feature
    * [WICKET-4832] - Websocket broadcast support
    * [WICKET-4879] - Implementing channels in wicket-atmosphere
    * [WICKET-4883] - Out of the box bean-validation (JSR 303) integration
    * [WICKET-4888] - Introduce a hierarchical feedback panel (FencedFeedbackPanel)

** Task
    * [WICKET-4885] - Upgrade jQuery to its latest stable version (1.8.3)

=============================================================================

Release Notes - Wicket - Version 6.3.0

** Bug
    * [WICKET-4623] - UploadProgressBar does not show up if the form submitted by AjaxButton or AjaxLink
    * [WICKET-4826] - PaletteButton#onComponentTag(ComponentTag) does not call super
    * [WICKET-4829] - ComponentResolvers created in app init ignore markup's namespace
    * [WICKET-4836] - Unmount a page does not work if the path starts with /
    * [WICKET-4837] - SmartLinkMultiLineLabel does not display email addresses or web URLs as hyperlinks
    * [WICKET-4841] - Return error code 400 when an Ajax request has no base url set in header/request parameters.
    * [WICKET-4842] - WicketRuntimeException when Tomcat cleans up a session later on
    * [WICKET-4844] - AbstractResourceReferenceMapper doesn't escape separators in style/variation names
    * [WICKET-4848] - Reporter of FeedbackMessage should not be set to 'null' on detach
    * [WICKET-4850] - BaseWicketTester discards cookies with MaxAge = -1 when processing a new request
    * [WICKET-4851] - IE8, IE7 javascript errors with Wicket 6
    * [WICKET-4857] - AutoCompleteTextFields submits Form if a choice is selected via enter-key
    * [WICKET-4859] - Integer overflow in AbstractToolbar
    * [WICKET-4864] - 'format' not set in ConversionException
    * [WICKET-4865] - Page parameters not working with CryptoMapper

** Improvement
    * [WICKET-4831] - Append the feedback message CSS class instead of overriding it
    * [WICKET-4835] - Add debug log messages in CompoundRequestMapper#mapRequest
    * [WICKET-4845] - Make BasicResourceReferenceMapper public so it is easy to extend it
    * [WICKET-4853] - Change FormComponent#reportRequiredError() from private to protected
    * [WICKET-4856] - Support SVG extension in SecurePackageResourceGuard
    * [WICKET-4863] - Customize ValidationError creation by FormComponent
    * [WICKET-4867] - Detach the object before calculating its size

** Task
    * [WICKET-4855] - Upgrade JQuery to 1.8.2

=============================================================================

Release Notes - Wicket - Version 6.2.0

** Sub-task
    * [WICKET-4752] - Revert Wicket-4715 -Read multipart request params in WebApplication

** Bug
    * [WICKET-4587] - URLRenderer renderFullUrl
    * [WICKET-4589] - Closing </wicket:container> tag is incorrectly setup as autocomponent
    * [WICKET-4756] - AtmosphereWebRequest should return true on calls to #isAjax()
    * [WICKET-4759] - FilterForm/FilterToolbar don't work when there's more than one IColumn to be filtered
    * [WICKET-4769] - Clicking on Label of Radio doesn't update component with Ajax update
    * [WICKET-4771] - Submitting value filled in DropDownChoice fails when tinymce textarea is on the page
    * [WICKET-4776] - Problems with switching between HTTP/HTTPS
    * [WICKET-4777] - JavaScriptReference escapes given URL
    * [WICKET-4786] - AjaxTabbedPanel doesn't include constructor with model
    * [WICKET-4787] - Registering resource bundles with duplicate resource references gives an NPE
    * [WICKET-4788] - FilteringHeaderResponse does not unwrap PriorityHeaderItems for filtering
    * [WICKET-4789] - URL rendering regression
    * [WICKET-4791] - UploadProgressBar hides immediately after being shown.
    * [WICKET-4792] - wickettester#startcomponent(component) doesn't call oninitialize
    * [WICKET-4794] - RfcCompliantEmailAddressValidator error message not defined
    * [WICKET-4796] - DatePickerTest fails because of java inconsistensy on localizing March german shortname Mrz vs Mär
    * [WICKET-4797] - CheckBoxMultipleChoice loses state
    * [WICKET-4801] - BaseWicketTester.executeAjaxEvent only fires one of multiple behaviors bound to a given event
    * [WICKET-4806] - AjaxEditableChoiceLabel won't close select onblur
    * [WICKET-4810] - CLONE - BaseWicketTester.clickLink() does not work with a ResourceLink with ResourceReference
    * [WICKET-4816] - Handling of semicolons in form action URLs
    * [WICKET-4818] - NullPointerException while reading the POST parameters
    * [WICKET-4820] - Race condition in ResourceSettings: getResourceWatcher() is not thread safe
    * [WICKET-4822] - Wicket.Event.add requires Wicket.$, but wicket-ajax-jquery is not listed as a dependency
    * [WICKET-4824] - Redirect to HTTPS is using wrong port 80 if HttpsConfig with default ports 80/443 is used

** Improvement
    * [WICKET-4160] - Make AbstractAutoCompleteRenderer.renderHeader() and .renderFooter() non-final
    * [WICKET-4772] - DataTable API and handling of AbstractToolbar
    * [WICKET-4778] - Add factory methods to JavaScriptHeaderItem to create a deferred JavaScript header item.
    * [WICKET-4798] - Make IteratorFilter.onFilter protected
    * [WICKET-4804] - Add #setStatus(int) in AbstractResource.ResourceResponse
    * [WICKET-4808] - WebClientInfo.getRemoteAddr() handling "Forwarded-For" value tokens not being ip addresses
    * [WICKET-4812] - Make SerializationChecker easier for extending so custom checks can be added to it

** New Feature
    * [WICKET-4793] - Support Jetty 9.x websocket implementation
    * [WICKET-4802] - Add functionality to be able to export DataTable content
    * [WICKET-4815] - Interface to mark components with type safe models

=============================================================================

Release Notes - Wicket - Version 6.1.1

** Bug
    * [WICKET-4759] - FilterForm/FilterToolbar don't work when there's more than one IColumn to be filtered
    * [WICKET-4789] - URL rendering regression

=============================================================================

Release Notes - Wicket - Version 6.1.0

** Bug
    * [WICKET-4645] - encodeURL broken on Tomcat 7.0.28
    * [WICKET-4656] - Atmosphere example doesn't work due to missing 'page' when creating AjaxRequestTarget
    * [WICKET-4729] - atmosphere example will stop working if opening the same url in a different tab in the same browser
    * [WICKET-4732] - CssPackageResource doesn't work if the extension isn't css
    * [WICKET-4734] - Button value is double escaped
    * [WICKET-4735] - KittenCaptchaPanel is broken
    * [WICKET-4737] - IllegalStateException on WicketFilter.init() after calling setFilterPath()
    * [WICKET-4738] - DownloadLink doesn't wrap the String model used for file name nor does it detach
    * [WICKET-4741] - Only complete handler (no success or failure handler) is called when Ajax link produces an exception
    * [WICKET-4742] - Wicket 6 and Atmospher Integration:  AtmosphereServlet does not delegate WicketFilter for Error Pages .
    * [WICKET-4743] - SerializingObjectSizeOfStrategy do not use framework serializer
    * [WICKET-4749] - TabbedPanel - IModel<?> initModel()
    * [WICKET-4750] - AbstractDefaultAjaxBehavior.getCallbackFunction should not add the event attribute
    * [WICKET-4751] - UploadProgressBar regression
    * [WICKET-4753] - Resource bundles are not resolved on PriorityHeaderItems
    * [WICKET-4755] - "'NEW VALUE' is not a valid Serializable" error during ajax form submission
    * [WICKET-4757] - FormComponents remain invalid forever if there is no feedback panel
    * [WICKET-4758] - DOM ids change of the input fields of FilterToolbar
    * [WICKET-4760] - JavaScriptStripper fails with single line comments
    * [WICKET-4761] - ModalWindow.closeCurrent Javascript error
    * [WICKET-4763] - Page's stateless hint is initially set to 'false'
    * [WICKET-4766] - multiple <style> tags in header are rendered incorrectly
    * [WICKET-4768] - Whitespace in TabbedPanel markup causes layout issues
    * [WICKET-4770] - Wicket 6: modal windows in Safari are always in drag or resize mode
    * [WICKET-4773] - ComponentFeedbackPanel broken under Wicket 6.0.0 when RenderStrategy.REDIRECT_TO_RENDER
    * [WICKET-4775] - PageParameters#mergeWith may loose values of the 'other' PP
    * [WICKET-4780] - Using both MountedMapper and CryptoMapper causes warning

** Improvement
    * [WICKET-4730] -  Filter component does not clear filter fields
    * [WICKET-4731] - TimeField not able to work with a java.sql.Time
    * [WICKET-4736] - JavaScriptFilteredIntoFooterHeaderResponse should reverse filter logic
    * [WICKET-4745] - Allow to set initial state of DebugBar to expanded / collapsed
    * [WICKET-4746] - Wizard component translation for pt_BR
    * [WICKET-4748] - Improve WicketTester to be able to find AjaxBehaviors on second/third/... event name

** New Feature
    * [WICKET-3969] - Add CDI integration

** Task
    * [WICKET-4781] - Downgrade the warning that a rendering falls back to redirect_to_buffer to a DEBUG

=============================================================================

Release Notes - Wicket - Version 6.0.0

** Sub-task
    * [WICKET-4672] - Do not render pageId for the action links for stateless pages

** Bug
    * [WICKET-3753] - PropertyVariableInterpolator add support for IConverterLocator (patch included)
    * [WICKET-4626] - WicketFilter unify the filterPath
    * [WICKET-4641] - AjaxFallbackLink and log a warning when there are several ajax event behaviors on the same event
    * [WICKET-4646] - atomicity violation bugs of using concurrent collections
    * [WICKET-4651] - Null BroadCaster in EventBus
    * [WICKET-4652] - AbstractAjaxTimerBehavior throws ComponentNotFoundException when its component is replaced in PageMap.
    * [WICKET-4659] - The default exception mapper is replying cacheable exceptional responses
    * [WICKET-4661] - Ajax channel busy flag not properly cleared upon SUCCESSFUL callback executions
    * [WICKET-4662] - StringResourceModel interpolation in resource key is broken
    * [WICKET-4663] - LazyInitProxyFactory uses wrong ClassLoader in OSGi environment
    * [WICKET-4665] - Add a new AjaxChannel that discards any Ajax requests if there is a running request in the same channel
    * [WICKET-4668] - Ajax responses for QUEUE and DROP type channels are not guaranteed to be processed in the order of the requests
    * [WICKET-4669] - Resources for Fragment in ModalWindow are not added to the page head
    * [WICKET-4673] - JavaScript error when submitting nested multipart form
    * [WICKET-4679] - XmlPullParser doesn't parse correctly attributes with complex namespace
    * [WICKET-4682] - html forms always submitted with "GET" method (by querystring)
    * [WICKET-4683] - <script> tag broken in certain situations when <script src="..."></script> reference added in page header section
    * [WICKET-4684] - NotSerializableException of the field "pageMarkup" in BaseWicketTester$StartComponentInPage
    * [WICKET-4685] - ValidationError addKey fails with error abort
    * [WICKET-4687] - ConcurrentModificationException with IFeedback instances that contain other IFeedback instances
    * [WICKET-4689] - Javascript timers not removed when a Component is replaced by ajax
    * [WICKET-4690] - DataTable - use <div> for the content
    * [WICKET-4691] - Unescaped html in autocomplete
    * [WICKET-4694] - ClassCastException in SqlDateConverter
    * [WICKET-4695] - Javascript error when closing a ModalWindow with IE 8
    * [WICKET-4696] - NumberTextField doesn't accept values <=0 for Double and Float
    * [WICKET-4701] - 6.0.0-beta3: Ajax upload file is not working in IE
    * [WICKET-4702] - TypeError: Wicket.Event is undefined
    * [WICKET-4706] - SerializableChecker.close throws NPE
    * [WICKET-4710] - DataTable - Headers with OrderByBorder have invalid HTML markup
    * [WICKET-4715] - WebApplication doesn't recognize if an incoming request is multipart.
    * [WICKET-4717] - StringValidator.exactLength has wrong variable in ErrorMessage
    * [WICKET-4718] - ResourceStreamResource#getResourceStream() is called multiple times
    * [WICKET-4725] - DatePicker doesn't send 'change' event to the input field
    * [WICKET-4727] - ThreadContext should be detached before each subscribed page gets notified

** Improvement
    * [WICKET-4065] - Improve behavior#getStatelessHint() by accounting for the common cases when behaviors are not stateless
    * [WICKET-4254] - IE allows only 31 stylesheet objects on a page
    * [WICKET-4471] - Generic registry of javascript/css resource references
    * [WICKET-4593] - TabbedPanel bi-directional model
    * [WICKET-4648] - Upgrade Atmosphere to 1.0-beta
    * [WICKET-4653] - Subscribing behaviors for push events
    * [WICKET-4666] - ResourceModel once assigned should not re-assign
    * [WICKET-4674] - Add support for Ajax call listsners for multipart form submittion
    * [WICKET-4675] - Process Ajax responses in one go
    * [WICKET-4676] - Relax restrictions on url of ExternalUrlResourceReference and rename it to UrlResourceReference
    * [WICKET-4677] - Improve AjaxRequestAttributes' ExtraParameters to accept more than one value for a key 
    * [WICKET-4688] - Make use of the generic type passed to IPageFactory#newPage() and return the typed Page
    * [WICKET-4704] - Allow using custom CSS classes for the selected and last TabbedPanel tabs
    * [WICKET-4719] - Allow a list of a subclass of IColumn in DataTable constructor
    * [WICKET-4720] - WebSession#authenticate() is superfluous
    * [WICKET-4721] - ConcatBundleResource is unnecessarily limited to accepting PackageResourceReference
    * [WICKET-4722] - Don't set ADDED_AT and CONSTRUCTED_AT keys if components use checking is not enabled
    * [WICKET-4726] - Do not try to show the date picker if it is already shown
    * [WICKET-4728] - Use IModel for the bread crumb participant's title

** New Feature
    * [WICKET-4244] - Add EnhancedPageView to ease debugging with the InspectorPage
    * [WICKET-4699] - Add NonResettingRestartException

** Task
    * [WICKET-4613] - Add Apache licence check tests for Atmoshpere module

=============================================================================

Release Notes - Wicket - Version 6.0.0-beta3

** Bug
    * [WICKET-4358] - BufferedWebResponse fails to add/clear cookie in redirect
    * [WICKET-4445] - ArrayIndexOutOfBoundsException in Url.resolveRelative by using Check and alias for Page
    * [WICKET-4529] - AjaxEditableLabel not selecting text or moving cursor on Firefox 11.0 or Safari 5.0.5
    * [WICKET-4536] - FeedbackPanel does not show messages on stateless pages
    * [WICKET-4550] - jsessionid is not added to resources if cookies are disabled by the server
    * [WICKET-4558] - WicketTester fails with a StringIndexOutOfBounds exception when using an external redirect
    * [WICKET-4559] - Component#getMarkupId() generates id that gets hidden by AdBlock
    * [WICKET-4561] - Wicket 1.5.6 duplicates segments in absolute URLs
    * [WICKET-4563] - Possible NPE in FragmentMarkupSourcingStrategy.getMarkup
    * [WICKET-4569] - AutoComplete text field does not work: Channel busy - postponing..
    * [WICKET-4570] - Shared Behavior's renderHead method called only for one component, not for all of them
    * [WICKET-4572] - DiskDataStore returns the wrong page when the page disk space is full 
    * [WICKET-4574] - ThreadContext does not properly remove the ThreadLocal from the current thread
    * [WICKET-4575] - AjaxButton / AjaxFormSubmitBehavior behaviour in wicket 1.5
    * [WICKET-4578] - Link always causes Page to become stateful, regardless of visibility
    * [WICKET-4581] - AJAX autocomplete of wicket-examples not working
    * [WICKET-4584] - NumberTextField does not have default minimum and maximum
    * [WICKET-4588] - non-relative redirects do not work properly
    * [WICKET-4590] - Palette does not display a single unselected item
    * [WICKET-4592] - WicketAjaxDebug.logError used in Wicket.Head.addJavascript - JS error when wicket-ajax-debug.js not loaded
    * [WICKET-4594] - Do not use the parsed PageParameters when re-creating an expired page
    * [WICKET-4596] - DataTable toolbars do not handle dynamically changing columns in the table
    * [WICKET-4597] - bug in Duration.toString(Locale locale)
    * [WICKET-4598] - Duplicate INFO logs due to twice registered RequestInterfaceListeners
    * [WICKET-4599] - Ajax request attributes should be passed to the dynamic parameters
    * [WICKET-4610] - WicketTester.assertRedirectUrl always fails because it always thinks the redirect was null
    * [WICKET-4616] - onError call order doesn't match onSubmit
    * [WICKET-4617] - ResourceStreamLocator vs ResourceFinder
    * [WICKET-4632] - ResourceStreamResource should use #getResourceStream() instead of directly using the 'stream' field
    * [WICKET-4633] - MultiFileUploadField after selecting a file does not hide properly input field 
    * [WICKET-4634] - UrlRenderer / Problem with rendering of relative URLs on error page
    * [WICKET-4642] - Atmosphere Modul not working on first page after Jetty restart
    * [WICKET-4643] - AjaxFormChoiceComponentUpdatingBehavior not set model object
    * [WICKET-4644] - AjaxFallbackLink still renders inline.javascript

** Improvement
    * [WICKET-4554] - WicketTester tries to create a directory called "tester" every time the tests run and thus fails when run under the security manager
    * [WICKET-4556] - Remove IResourceSettings#getUseDefaultResourceAggregator
    * [WICKET-4560] - Remove methods marked deprecated in 1.5.x from wicket 6 
    * [WICKET-4564] - Use JsonFunction to deliver JSON with function literals
    * [WICKET-4565] - Unify the order of the parameters for the JavaScript handlers
    * [WICKET-4582] - wicket-auth-roles cannot be extended/customized without copy/pasting MetaDataRoleAuthorizationStrategy and ActionPermissions
    * [WICKET-4586] - add getter for renderer in autocompletetextfield
    * [WICKET-4593] - TabbedPanel bi-directional model
    * [WICKET-4601] - IResourceStreamWriter to take OutputStream instead of Response
    * [WICKET-4606] - dynamic extra parameter gets used/executed even when ajaxcallistener is preconditioned to false
    * [WICKET-4608] - Vars in ValidationErrors should be properly converted
    * [WICKET-4614] - improve warning message in addDefaultResourceReference
    * [WICKET-4615] - checkRendering message should also mention XHTML tags as a possible cause
    * [WICKET-4621] - Add constructor that accepts IModel<String> for AbstractColumn's header
    * [WICKET-4622] - Optimize PageExpiredException to not load its stacktrace
    * [WICKET-4624] - requestcyclelistener which registers and retrieves pagerequesthandler in a request
    * [WICKET-4627] - Change Url#parse() methods to accept CharSequence instead of String
    * [WICKET-4629] - AbstractLink does not call getBody to get its body
    * [WICKET-4635] - Improve JavaScript files by applying common JSHint rules 
    * [WICKET-4636] - Using setResponsePage() before page rendering should skip the rendering of the page
    * [WICKET-4638] - Make it possible to use JsonFunction as a AjaxCallListener's handler
    * [WICKET-4649] - Add an additional Ajax call listener point - before the call
    * [WICKET-4650] - Do not publish '/dom/node/added' is there is no added node

** Task
    * [WICKET-4612] - Add Apache licence headers test for native websocket modules

** Test
    * [WICKET-4619] - Windows JRE6 StoredResponsesMapTest#getExpiredValue() falures

** Wish
    * [WICKET-4611] - Provide native integration with web containers' websocket support

=============================================================================

Release Notes - Wicket - Version 6.0.0-beta2

** Sub-task
    * [WICKET-4517] - Wicket-core don't export "internal" packages in OSGi manifest.
    * [WICKET-4521] - Improve all IComponentResolvers which are also IMarkupFilters to set tag ids with common prefix

** Bug
    * [WICKET-4260] - UrlRenderer renders invalid relative URLs if first segment contains colon
    * [WICKET-4286] - ListView causes page ID increment
    * [WICKET-4458] - wicket-core-1.5.5.jar not closed when Application is undeployed from directory
    * [WICKET-4475] - Inline Enclosure needs to check isVisibleInHierarchy, not only isVisible
    * [WICKET-4477] - SmartLinkLabel failing to process email with -
    * [WICKET-4480] - newResourceResponse() always process IResourceStream
    * [WICKET-4483] - Component#setDefaultModel() should call #modelChanging()
    * [WICKET-4484] - wicket:link component ids get too long
    * [WICKET-4485] - TagUtils uses wrong separator in its #copyAttributes()
    * [WICKET-4487] - TextTemplate in RenderHead() on component doesn't Re-Render for every page
    * [WICKET-4488] - URL with a previous page version ignores requested page based on mount path
    * [WICKET-4489] - HttpSessionStore.onUnbind is never invoked
    * [WICKET-4494] - HtmlHandler wrongly handles tags not requiring closed tags if the markup does not have "top" level tag
    * [WICKET-4500] - InterceptData never cleared from session after continueToOriginalDestination is called
    * [WICKET-4501] - NumberTextField<BigDecimal> renders its value in unsupported number format 
    * [WICKET-4502] - Make it easier to produce a page with links with absolute urls
    * [WICKET-4504] - AjaxLazyLoadPanel not replaced within AjaxTabbedPanel
    * [WICKET-4505] - AbstractTextComponent not escaping html data by default therefore user text is not redisplayed correctly
    * [WICKET-4506] - Fix missing in 1.4.19, was fixed in 1.3.3:  Discrepancy between Button implementation of getForm and the code in Form.findSubmittingButton()
    * [WICKET-4507] - wicktTester.getLastResponseAsString() returns wrong result after starting a Component
    * [WICKET-4509] - Spaces in path cause ModifcationWatcher to fail
    * [WICKET-4511] - Stack overflow when render malformed html.
    * [WICKET-4514] - UrlRenderer produces wrong full urls when the passed parameter is not absolute (as Url understands 'absolute')
    * [WICKET-4516] - WebApplication#renderXmlDecl() uses wrong name of the 'Accept' request header
    * [WICKET-4518] - Wicket example 'forminput' is broken due to bad url for IOnChangeListener
    * [WICKET-4519] - discrepancy between JavaDoc and code in MarkupContainer#visitChildren()
    * [WICKET-4520] - Inline enclosure doesn't work if wicket:message attribute is used on the same tag
    * [WICKET-4530] - Final methods on ResourceNameIterator prevent full customization of IResourceStreamLocator
    * [WICKET-4535] - Inconsistent use of generics in sorting APIs
    * [WICKET-4543] - AbstractDefaultAjaxBehavior.getCallbackFunctionBody uses jQuery.extend
    * [WICKET-4546] - Unencoded ampersands in CSS-/Javascript-Reference
    * [WICKET-4548] - NullPointerException in org.apache.wicket.markup.html.form.ValidationErrorFeedback
    * [WICKET-4566] - Resource filtering breaks the calculation of rendered resources

** Improvement
    * [WICKET-598] - Support jetty continuations in wicket
    * [WICKET-2128] - StringValidator error messages erroneously mention input instead of label
    * [WICKET-2674] - AbstractChoice Option Style Hook
    * [WICKET-4219] - Enable markup escaping of WizardStep's labels by default due to security aspects
    * [WICKET-4466] - Use an external host for the static javascript files (specially JQuery)
    * [WICKET-4468] - Stateful components which are invisible force page to be stateful
    * [WICKET-4474] - Disallow PackageResources in the bundles
    * [WICKET-4478] - DiskDataStore to use multi-level directory structure to avoid slowness when thousands of sessions are active.
    * [WICKET-4479] - Маке ModificationWatcher easier for extending
    * [WICKET-4481] - Add setHeader() to MockHttpServletRequest to be able to override default headers
    * [WICKET-4486] - Give StringValue toEnum methods
    * [WICKET-4503] - org.apache.wicket.datetime.markup.html.form.DateTextField#getConverter() is final, should not be
    * [WICKET-4523] - Use new maven compiler plugin to speed up build time
    * [WICKET-4524] - ListMultipleChoice has a performance issue with very large lists, patch included
    * [WICKET-4526] - OpenCloseTagExpander should include i tag
    * [WICKET-4527] - Recorder#getSelectedChoices() can be very slow under certain circumstances
    * [WICKET-4528] - make recorder component of wicket-extensions palette more efficient for large number of items and easier to extend
    * [WICKET-4533] - Resource bundle api should have convenience methods for resource replacements
    * [WICKET-4540] - Allow AJAX callback function generation to also generate parameters
    * [WICKET-4541] - Warn if wicket:container has an attribute
    * [WICKET-4542] - Session.java javadoc needs to be corrected
    * [WICKET-4552] - WicketFilter.processRequest() should check that given FilterChain is not null

** New Feature
    * [WICKET-363] - Push behavior to handle server side events
    * [WICKET-4532] - Disable caching for particular resources.

** Task
    * [WICKET-4482] - Regression in OnChangeAjaxBehavior

** Wish
    * [WICKET-4308] - onNewBrowserWindow wanted
    * [WICKET-4498] - Ignore Netbeans XML configuration files in Apache License Header Test Case

=============================================================================

Release Notes - Wicket - Version 6.0.0-beta1

** Sub-task
    * [WICKET-3026] - Add onStart callback for ajax calls
    * [WICKET-3367] - Rewrite all JavaScript inline event handlers to be proper attached event handlers
    * [WICKET-4179] - Add IDataStore#canBeAsynchronous() so that DefaultPageManagerProvider can determine whether to wrap it in AsynchronousDataStore
    * [WICKET-4236] - Use JQuery as a backing library for Wicket's JavaScript code
    * [WICKET-4348] - Add a method to IHeaderResponse to create IE conditional commented link to JavaScript resource
    * [WICKET-4364] - Think of a way to reintroduce show|hideIncrementally with the new Ajax impl
    * [WICKET-4417] - CombinedRequestParametersAdapter ignores duplicate values for the same parameter name
    * [WICKET-4422] - Minimize Wicket's Ajax JavaScript files at build time
    * [WICKET-4439] - Move classes around so that there are no two packages with the same name in different modules
    * [WICKET-4447] - Upgrade maven-bundle-plugin to 2.3.7

** Bug
    * [WICKET-2705] - Feedback messages get cleaned up in AJAX request, thus never rendered and never visible to user
    * [WICKET-2949] - Select does not work properly in functional flows e.g. Wizards
    * [WICKET-3669] - <script> element in Ajax requests is executed twice in Opera 11
    * [WICKET-3974] - Wicket-auth annotation always denies access if the deny list is empty
    * [WICKET-4000] - Header contributions order is not stable
    * [WICKET-4076] - INamedParameters.getAllNamed() returns a list of NamedPairs defined as inner class of PageParameters
    * [WICKET-4204] - AutoCompleteTextField+OnChangeAjaxBehavior doesn't fire updates on item selection
    * [WICKET-4213] - DatePicker fails to display when rendered via ajax response
    * [WICKET-4222] - CryptoMapper - Error decoding text, exception thrown for links in markup
    * [WICKET-4226] - TreeColumn not displayed when there is only one "Alignment.MIDDLE" column
    * [WICKET-4227] - dropping ajax request causes componentnotfoundexception
    * [WICKET-4235] - Parent / Child HeaderRenderStrategy does not work with <wicket:head> tag
    * [WICKET-4241] - Modal Window respond 404 with Internet Explorer.
    * [WICKET-4243] - Fix PageInspector styling
    * [WICKET-4245] - AbstractResourceAggregatingHeaderResponse forgets some information about references
    * [WICKET-4250] - Checks in IRequestMapper.mapHandler should be on page instance, if available
    * [WICKET-4251] - Multipart Form and AjaxSubmitLink will result in invalid redirect after user session expires
    * [WICKET-4255] - bug in org.apache.wicket.validation.validator.UrlValidator
    * [WICKET-4256] - onBeforeRender() is called on components that are not allowed to render
    * [WICKET-4258] - DebugBar displays stacktrace with FeedbackPanels
    * [WICKET-4259] - Using an IValidator on an AjaxEditableLabel causes ClassCastException
    * [WICKET-4260] - UrlRenderer renders invalid relative URLs if first segment contains colon
    * [WICKET-4261] - Wicket autocomplete keeps working on the replaced element when somehow the element is replaced by another ajax request when it is shown.
    * [WICKET-4264] - JavaSerializer Does Not Call SerializableChecker
    * [WICKET-4265] - Thread Test Uses Sun Proprietary API
    * [WICKET-4266] - AjaxEditableLabel does not work when using CryptoMapper
    * [WICKET-4267] - Descendants of DynamicImageResource don't pass PageParameters to rendering code
    * [WICKET-4276] - Select component loses it's value
    * [WICKET-4278] - Performance regression in Component.configure() in 1.5 
    * [WICKET-4279] - CheckGroupSelector "Select all" behavior broken when the CheckGroup contains a single check box.
    * [WICKET-4289] - Improve WicketTester handling of cookies 
    * [WICKET-4290] - Confusion between a form component's wicket:id and a PageParameter in Wicket 1.5.x
    * [WICKET-4292] - MockHttpServletResponse.addCookie(Cookie) adds duplicate cookies
    * [WICKET-4293] - UrlResourceStream closes incorrect InputStream causing stacktraces on undeploy on GlassFish
    * [WICKET-4298] - FormTester doesn't handle nested forms correctly
    * [WICKET-4300] - JavaScript error "Uncaught TypeError: Cannot read property 'length' of undefined" in case of CheckGroupSelector and nested forms
    * [WICKET-4301] - ByteArrayResource throws error if data is null
    * [WICKET-4303] - mounting a home page with package mapper results in invalid url's
    * [WICKET-4305] - Problems with AbstractAutoCompleteBehavior on BookmarkablePages
    * [WICKET-4306] - Content type overriding in PackageResource
    * [WICKET-4309] - StringValueConversionException for correct situation
    * [WICKET-4311] - ModalWindow does not close cleanly
    * [WICKET-4312] - Do not strip the jsessionid from the url for resources
    * [WICKET-4317] - Logic or Exception Message is incorrect in ModalWindow.setCookieName
    * [WICKET-4318] - BaseWicketTester#startComponentInPage swallows useful exceptions
    * [WICKET-4320] - wicketTester#executeBehavior doesn't ever attach request-parameter 
    * [WICKET-4323] - StringResourceModels doesn't seem to detach properly
    * [WICKET-4330] - Non standard ("wicket") namespace causes incorrect relative URL in certain cases
    * [WICKET-4332] - FileUpload: Using IE9 in IE8 compatibility mode, ajax-response cannot be parsed
    * [WICKET-4333] - BreadCrumbPanelLink should accept subclasses of BreadBrumbPanel
    * [WICKET-4336] - One of the stateless examples makes the page stateful 
    * [WICKET-4340] - Rerendering a page with WicketTester after you loose permission does not give access-denied
    * [WICKET-4345] - CryptoMapper does not work for applications having a home page that needs query parameters
    * [WICKET-4346] - getAjaxRegionMarkupId called for not enabled behaviors
    * [WICKET-4357] - Calling WebResponse#enableCaching() is insufficient to enable caching on WebPage (which by default is non-cacheable)
    * [WICKET-4359] - Package resources leak file handles
    * [WICKET-4361] - Markup Inheritance & getVariation(): markup loading (and merging) order depends on whether the super constructor adds components or not.
    * [WICKET-4363] - Duplicate html ID for generated divs in modal.js
    * [WICKET-4365] - Form components' name/value are encoded in stateless form's action url
    * [WICKET-4367] - WicketTester.assertFeedback broken
    * [WICKET-4370] - HttpSession getSession() in MockHttpServletRequest is not compliant with the j2ee servlet spec
    * [WICKET-4378] - datatime extension's calendar-min.js appears to be corrupted / out of sync
    * [WICKET-4379] - org.apache.wicket.validation.ValidatorAdapter class causes problem with validator properties to be loaded
    * [WICKET-4384] - improve wicket's handling of empty / null page parameters
    * [WICKET-4387] - StringIndexOutOfBoundsException when forwarding requests
    * [WICKET-4390] - MarkupParser#add(IMarkupFilter filter,Class beforeFilter) doesn't add the filter into the correct place.
    * [WICKET-4391] - XsltOutputTransformerContainer incorrectly claims markup type "xsl"
    * [WICKET-4392] - autocomplete: show list on empty input is ignored if show list on focus gain is false
    * [WICKET-4398] - Any empty url-parameter will make wicket 1.5 crash
    * [WICKET-4401] - Redirect after sign in with SignInPanel does not always reach homepage
    * [WICKET-4405] - Multi byte characters are not decoded in AutoCompleteTextField
    * [WICKET-4406] - ApplicationListenerCollection onBeforeDestroyed calls onAfterInitialized for each of the listeners
    * [WICKET-4409] - Form#getJsForInterfaceUrl(..) works not correct if cookies are disabled
    * [WICKET-4410] - The datepicker components stops popup in Chrome 17. 
    * [WICKET-4416] - CheckGroup model in AjaxFormChoiceComponentUpdatingBehavior onUpdate contains only last checked item
    * [WICKET-4420] - Unversioned pages don't get touched when created and cannot be found by ID later
    * [WICKET-4424] - getComponentFromLastRenderedPage appends componentInPage id when it shouldn't
    * [WICKET-4425] - Wicket 1.5 rewrites template content where it should not
    * [WICKET-4429] - executeAllTimerBehaviors does not find all timer behaviours
    * [WICKET-4435] - UploadProgressBar won't update while uploading if CryptoMapper is used as RootRequestMapper
    * [WICKET-4437] - BaseWicketTester.clickLink() does not work with a ResourceLink
    * [WICKET-4443] - AbstractClassResolver recreates URL incorrectly
    * [WICKET-4446] - processScript in Wicket.Head.Contributor doesn't remove CDATA - eval fails in IE
    * [WICKET-4451] - Resource decoration fail on wicket examples
    * [WICKET-4454] - Repaint the initial state of the page if the page is expired, a new one is freshly created and the requested component is not available 
    * [WICKET-4460] - Wrong string encoding in JavaScriptPackageResource
    * [WICKET-4465] - Autocomplete IE javascript error: 'target' is null or not an object

** Improvement
    * [WICKET-499] - Investigate whether we can use component meta data for the storage of feedback messages
    * [WICKET-1033] - Allow Grace Period for AJAX Busy Indication
    * [WICKET-1175] - IDataProvider-Overflow with size()
    * [WICKET-1221] - setRequired(true) will force a Checkbox to be checked
    * [WICKET-1310] - StringValidator.maximumLength should automatically add maxlength html attribute
    * [WICKET-1525] - Ability to enable/disable AbstractAjaxTimerBehavior
    * [WICKET-1557] - Handling lost connection from AJAX button
    * [WICKET-1654] - Update validators to accept IModel parameter for easy override of error messages
    * [WICKET-2249] - Modal Window: add overridable wantUnloadConfirmation() method
    * [WICKET-2498] - IChainingModel implementation
    * [WICKET-2745] - Add info about SubmittingButton to RequestLogger
    * [WICKET-2747] - AjaxPagingNavigator Link factories should return AbstractLink
    * [WICKET-3227] - Complete documentation of Component Lifecycle
    * [WICKET-3554] - Constructor of org.apache.wicket.PageReference should be public
    * [WICKET-3805] - Change Component#visitParents to enable visitors of any type
    * [WICKET-3879] - Support FormValidator and package level resource bundles
    * [WICKET-3911] - Hungarian localisation for upload component and examples
    * [WICKET-3990] - Eliminate all protected static methods for better Scala integration
    * [WICKET-4035] - Allow to set the property type in ISortState
    * [WICKET-4074] - RequestLogger needs a clear separation of concerns
    * [WICKET-4088] - Make Application#init() run after IInitializers to allow the application to override any settings configured by initializers
    * [WICKET-4091] - Automate output of markup ids
    * [WICKET-4114] - Mark getRequestCycleProvider in Application final
    * [WICKET-4145] - Improve AutoLinkResolver
    * [WICKET-4224] - DataTable markup : change SPAN to DIV
    * [WICKET-4231] - allow palette's choicesModel to take current selection into account
    * [WICKET-4233] - Allow writing response through an OutputStream
    * [WICKET-4234] - Remove AbstractValidator because its mostly noise
    * [WICKET-4239] - add wicket-jar_es.properties for spanish localization
    * [WICKET-4240] - Replace wicket's tree with a better implementation - http://code.google.com/p/wicket-tree/
    * [WICKET-4246] - WicketTester.assertFeedback(java.lang.String path, java.lang.String[] messages) should not require feedback messages to be in the specified order.
    * [WICKET-4248] - Upgrade pom reference for joda-time from 1.6.2 to 2.0
    * [WICKET-4249] - Use better namespacing for Wicket.DateTime initialization scripts
    * [WICKET-4252] - Ajax refactoring
    * [WICKET-4253] - LoadableDetachableModel's getObject is not final
    * [WICKET-4262] - Wicket autocomplete should try to select the real selected value in the list if preselect property is true instead of just always 0
    * [WICKET-4269] - Component#continueToOriginalDestination() should not return a value
    * [WICKET-4270] - Remove superfluous call to ResourceReference#getResource() in ResourceMapper when trying to map the resource URL
    * [WICKET-4271] - Remove 'final' keyword from method AbstractResource#respond
    * [WICKET-4273] - Rendering of resources in the header with dependency resolving and support for bundles
    * [WICKET-4284] - make getkey() in resourereference public
    * [WICKET-4285] - PageSavingThread.stop() blocks forever
    * [WICKET-4302] - Allow set Ajax settings for AjaxCheckBox 
    * [WICKET-4304] - Rename interface DataStoreEvictionStrategy to IDataStoreEvictionStrategy  
    * [WICKET-4313] - Possibility to move certain HeaderItems to the front
    * [WICKET-4326] - Make AjaxRequestTarget an interface and move the impl to AjaxRequestHandler
    * [WICKET-4328] - JavaScriptFilteredIntoFooterHeaderResponse + CssHeaderItem Enhancement
    * [WICKET-4329] - Optimize consequent usages of MarkupContainer# hasAssociatedMarkup() and #getAssociatedMapkup()
    * [WICKET-4334] - Prevent Wicket from causing redundant download of stateless resources (.js, .css etc.,) on browser with cookies enabled
    * [WICKET-4337] - deprecate start panel in 1.5.x
    * [WICKET-4342] - Allow resources disable the flushing of the response after setting the headers
    * [WICKET-4349] - Extract the code that creates the XML for Ajax responses out of AjaxRequestHandler
    * [WICKET-4350] - Add more programmatic support for web app construction via servlet 3.0 
    * [WICKET-4354] - org/apache/wicket/util/io/FullyBufferedReader getLineAndColumnText not used
    * [WICKET-4356] - StringValueConversionException should not be thrown when requesting a conversion on StringValue with a default value
    * [WICKET-4368] - Useless concatenation with empty string in DefaultAuthenticationStrategy.save 
    * [WICKET-4369] - Allow user supplied behaviors to override automatically created ones
    * [WICKET-4372] - Two WicketSerializableException classes
    * [WICKET-4380] - Null url argument on missing static resource
    * [WICKET-4386] - Add maxlength to  wicket-datetime hours, minutes input fields
    * [WICKET-4388] - o.a.w.util.file.WebApplicationPath duplicates the same logic as o.a.w.util.file.Path
    * [WICKET-4393] - ResourceNameIterator should not produce resource names with trailing dot
    * [WICKET-4412] - ChoiceFilteredPropertyColumn with setNullValid to true
    * [WICKET-4434] - Replace all String.split(char) with Wicket's Strings.split(String, char) for better performance
    * [WICKET-4448] - Update russian localization 
    * [WICKET-4449] - Change IValidationError API to work with java.io.Serializable as other methods (info, error, success, ...) in Component and Session
    * [WICKET-4453] - [StyleAndScriptIdentifier] Better matching of CDATA-comments
    * [WICKET-4455] - modify the http status code in PageExpiredErrorPage

** New Feature
    * [WICKET-1384] - TreeTable should support MultilineLabel for NodeLink
    * [WICKET-2302] - Add Folder.freeDiskSpace
    * [WICKET-4064] - Guice 3.0 integration does not support JSR-330 @javax.inject.Inject annotations
    * [WICKET-4162] - Add new StringResourceLoader to allow Wicket extensions to provide localization resource bundles
    * [WICKET-4212] - Add ISessionStore BindListener
    * [WICKET-4307] - Support javax.inject.Inject annotation in Guice and Spring integration
    * [WICKET-4419] - Option to deflate serialized pages on the fly

** Task
    * [WICKET-3068] - remove application settings which are no longer needed
    * [WICKET-4211] - package.properties should be prefixed with "wicket-" namespace
    * [WICKET-4280] - Remove IComponentSource and related code
    * [WICKET-4281] - Simplify Maven build for development
    * [WICKET-4377] - Deprecate Component#onMarkupAttached()
    * [WICKET-4389] - Is there any use for EmptyAjaxRequestHandler?
    * [WICKET-4399] - Remove IRequestCycleSettings#(s|g)etUnexpectedExceptionDisplay in favour of IExceptionSettings
    * [WICKET-4461] - Make all Session methods which deal with attributes public

** Wish
    * [WICKET-757] - FormComponent.rawInput needs a better name
