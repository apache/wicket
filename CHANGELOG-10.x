This file contains all changes done in releases for Apache Wicket 10.x.

=======================================================================

Release Notes - Wicket - Version 10.0.0-M1

** Bug

    * [WICKET-6895] - Links to examples in documentation points to old version of 8x
    * [WICKET-6896] - AutoCompleteTextField re-opens dropdown item list after item has been selected
    * [WICKET-6897] - Javadoc build fails on fresh checkout of master or rel/wicket-9.3.0
    * [WICKET-6902] - Change of PartialPageUpdate order of onAfterResponse and writePriorityEvaluations makes IListener.onAfterResponde ignore prepended javascripts
    * [WICKET-6908] - Possible bug / edge case where page is not detached
    * [WICKET-6913] - Java 17 compatibility with cglib
    * [WICKET-6914] - Visibility change of "File Upload" via ajax causes "missing" form-data
    * [WICKET-6921] - MultipartFormComponentListener breaks on hidden components
    * [WICKET-6936] - FilePageStore fails on windows 
    * [WICKET-6944] - Memory leak in WicketEndpoint
    * [WICKET-6945] - MultipartFormComponentListener modifies enctype on invisible forms, leading to javascript errors
    * [WICKET-6947] - IndicatingAjaxButton does not work with Bootstrap 4
    * [WICKET-6953] - JavaScriptDeferHeaderResponse not working correctly for AJAX requests
    * [WICKET-6955] - Wicket uses unstable slf4j version
    * [WICKET-6965] - Memory leak in WicketEndpoint
    * [WICKET-6966] - IndexOutOfBounds in InSessionPageStore
    * [WICKET-6970] - Unnecessary string building in AssociatedMarkupSourcingStrategy
    * [WICKET-6971] - NullPointerException in ModificationWatcher
    * [WICKET-6974] - JavaxUpgradeHttpRequest returns an empty contextPath
    * [WICKET-6975] - Behavior.renderHead may be called multiple times
    * [WICKET-6981] - InSessionPageStore does not trigger flushSession
    * [WICKET-6988] - String.format used in JS generation leads to errors
    * [WICKET-6990] - DiskPageStore loses pages when container re-binds attributes
    * [WICKET-6996] - NotSerializableException near KeyInSessionSunJceCryptFactory
    * [WICKET-6999] - Missing Export-Package of packages with "internal" in name
    * [WICKET-7005] - ByteBuddy IllegalStateException: Cannot inject already loaded type
    * [WICKET-7007] - Code snippets for CSRF documentation fixing
    * [WICKET-7013] - IndexOutOfBoundsException in InSessionPageStore
    * [WICKET-7022] - JavaScriptStripper fails to detect regular expression correctly
    * [WICKET-7028] - CSP header not rendered when using RedirectPolicy.NEVER_REDIRECT
    * [WICKET-7034] - WebSocket.Closed event not fired when error occurred
    * [WICKET-7037] - [Ajax Download] cookie used to track download complete misses the SameSite attribute
    * [WICKET-7040] - find a different way to add CSP headers
    * [WICKET-7044] - Images in the Wicket 9.x reference guide are not displayed.
    * [WICKET-7052] - Interrupting a task should not be logged as an error
    * [WICKET-7054] - Tag <script> mus be wrapped in CDATA for 'type' values 'module' and 'importmap' 
    * [WICKET-7055] - AjaxEventBehavior onload event unstable

** New Feature

    * [WICKET-6886] - LambdaChoiceRenderer
    * [WICKET-7029] - Add migration recipes to Wicket 10
    * [WICKET-7033] - add support to uploading to a resource

** Improvement

    * [WICKET-6836] - Replace CGLib with ByteBuddy and/or Javassist
    * [WICKET-6889] - Provide specialization of SpringWebApplicationFactory that could load Spring configuration class
    * [WICKET-6890] - Render debug setting 'outputMarkupContainerClassName` as an attribute
    * [WICKET-6893] - Make ApplicationContextMock smarter by delegating to DefaultListableBeanFactory
    * [WICKET-6899] - Add setExceptionMapperProvider to Application
    * [WICKET-6901] - PropertyValidator improve configuration doc
    * [WICKET-6911] - wicket-spring throws an error when a spring bean uses ctor injection
    * [WICKET-6920] - Improve the examples to use the browser's light/dark mode
    * [WICKET-6924] - Allow image/avif in SecurePackageResourceGuard
    * [WICKET-6927] - Get rid of java.security.AccessController
    * [WICKET-6933] - Ease use of InSessionPageStore as persistent store
    * [WICKET-6943] - There should be ability to check if current session's locale is RTL
    * [WICKET-6946] - isVisibleInHierarchy/isEnabledInHierarchy broken by isVisible/isEnabled override
    * [WICKET-6949] - make AjaxDownloadBehavior more dynamic
    * [WICKET-6950] - wicket web-sockets implementation does not allow to configure/access web socket session on onOpen
    * [WICKET-6952] - Performance improvements for Strings.isEmpty
    * [WICKET-6958] - Allow to use Slf4j 1.7.x in OSGi runtime
    * [WICKET-6960] - Reduce allocations when encoding ComponentInfo
    * [WICKET-6963] - Use singletons for PanelMarkupSourcingStrategy
    * [WICKET-6964] - Do not allocate when escaping empty string
    * [WICKET-6967] - allow to send non-blocking web socket messages
    * [WICKET-6972] - Add Resource key to be logged on Warning in Localizer.java
    * [WICKET-6976] - Reduce allocations when writing synthetic closing tags
    * [WICKET-6977] - hashCode computations generate excessive garbage objects
    * [WICKET-6979] - Cut back slightly on some String instance creation in PageInfo and friends.
    * [WICKET-6982] - Unnecessary initialization of stateful pages in ListenerRequestHandler
    * [WICKET-6985] - Insufficient information logged by CheckingObjectOutputStream when delegation to Externalizable
    * [WICKET-6991] - ResourceAggregator is resizing the StringBuilder several times.
    * [WICKET-6992] - Reduce object creation and wasted memory in Url toString() methods
    * [WICKET-6994] - Make the servlet API a runtime provided dependency when using JPMS.
    * [WICKET-6998] - Bump slf4j-api to 2.0.0
    * [WICKET-7000] - ParseException ("Malformed tag") if <script> attribute 'type' is 'module' 
    * [WICKET-7002] - Application metadata access should not require synchronization
    * [WICKET-7003] -  The http RequestLogger is very expensive. #524 
    * [WICKET-7004] - Jetty config example contains security hazard
    * [WICKET-7008] -  LoadableDetachableModel.toString() should reflect the actual variable name
    * [WICKET-7009] - Upgrade Jackson dependency to 2.13.x
    * [WICKET-7011] - Improve usage of JUnit 5.x APIs
    * [WICKET-7014] - Use new API for forward compatibility with CDI 4
    * [WICKET-7016] - Support GCM-SIV for page store encryption
    * [WICKET-7017] - ListenerRequestHandler should not hide IndexOutOfBoundsException
    * [WICKET-7021] - Additional OSGi import fixes
    * [WICKET-7025] - Package private methods should be proxied by ByteBuddy
    * [WICKET-7030] - Add Convenience Methods in BaseWicketTester
    * [WICKET-7031] - Update to JQuery 3.6.4
    * [WICKET-7032] - Update the JS tests to use latest version of QUnit (2.x)
    * [WICKET-7035] - fileCountMax should be added to control the amount of files being uploaded
    * [WICKET-7038] - Add support for SameSite setting to CookieDefaults
    * [WICKET-7041] - Reduce allocations when rendering component headers
    * [WICKET-7042] - Improve sizing of StringResponse when writing scripts in PartialPageResponse
    * [WICKET-7045] - Avoid allocations in PageParameters.getNamedKeys
    * [WICKET-7046] - Avoid allocating StringResponse when no response filters are active
    * [WICKET-7047] - Improve initial buffer capacity for Strings.toMultilineMarkup
    * [WICKET-7051] - fileupload2 does not seem to have exceptions to distinguish bettween file too big and total size too big
    * [WICKET-7059] - [Serialization] make easier to avoid serialization for form request

** Task

    * [WICKET-6887] - Merge wicket-http2 into wicket-core
    * [WICKET-6903] - Replace maven-clirr-plugin with something newer
    * [WICKET-6904] - Make Apache Wicket fully supporting Java9+ module system
    * [WICKET-6906] - Wicket 10 remove deprecation
    * [WICKET-6907] - Upgrade Guice to 5.x
    * [WICKET-6915] - Update common-fileupload to 2.0
    * [WICKET-6916] - Simplify JMX with StandardMBean
    * [WICKET-6919] - Improve EnclosureContainer's javadoc to explain that it should not be used with <wicket:enclosure>
    * [WICKET-6925] - Deprecate AbstractWrapModel
    * [WICKET-6940] - Update Spring to 6.0.0
    * [WICKET-6942] - Replace usage of log4j 1.x in tests and wicket-examples with slf4j-simple
    * [WICKET-7010] - Stop bundling old JQuery versions
    * [WICKET-7027] - Remove component queueing
    * [WICKET-7053] - Update Guice to 7.x (javax -> jakarta)

=======================================================================


