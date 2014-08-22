
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

