This file contains all changes done in releases for Apache Wicket 11.x.

=======================================================================

Release Notes - Wicket - Version 11.0.0

** Bug

    * [WICKET-7107] - CSP Header not rendered when using RedirectPolicy.AUTO_REDIRECT
    * [WICKET-7182] - Wicket Autocomplete calls event on a non jquery object
    * [WICKET-7186] - wicket 10.9.0 / 10.9.1 leaks File Reaper threads
    * [WICKET-7189] - make partial page update creation pluggable.
    * [WICKET-7199] - Vulnerable dependencies need to be updated
    * [WICKET-7203] - wicket-extensions: Dependency on jackson-databind should be optional in module-info.java
    * [WICKET-7205] - CSP-Headers not set if RenderPageRequestHandler wrapped in more than one IRequestHandlerDelegates
    * [GH-1576] - BUG since 10.11.0: Wrong escaping in Button label,while getEscapeModelStrings=false
    * [GH-1582] - build fails on SignOut
    * [GH-1593] - Typo in WARN message
    * [GH-1616] - AsynchronousPageStoreTest.storeAsynchronousContextClosed never observes its own assertions
    * [GH-1620] - TrapFocusBehavior: Tab/Shift+Tab at trap boundary throws TypeError on wicket-10.x
    * [GH-1646] - Wicket.Event.remove does not remove a listener added for several event types
    * [GH-1647] - The WebSocket charts example calls jQuery.parseJSON, which jQuery 4 removed
    * [GH-1650] - AjaxButton triggering the submit event also submits the form in Firefox
    * [GH-1658] - The quickstart archetype's Start.java does not compile against Jetty 12
    * [GH-1659] - CrossOrigin.USE_CREDENTIALS renders user-credentials instead of use-credentials
    * [GH-1670] - problems with upload to resource
    * [GH-1678] - Quickstart archetype: give slf4j-simple test scope
    * [GH-1688] - WebSocket: Request wrappers break the Servlet header contract (case-sensitive lookup, single-use enumerations)
    * [GH-1721] - Fix the failures in release.sh found while releasing 9.25.0

** New Feature

    * [WICKET-7169] - Make partHeaderSizeMax in AbstractFileUpload configurable
    * [WICKET-7190] - Unified cryptography API
    * [WICKET-7196] - Allow unescaped wicket:label tags
    * [GH-1492] - AjaxRequestHandler generation of PartialPageUpdate is not extensible
    * [GH-1583] - create a version of wicket ajax and all the other standard components that is jQuery independent.
    * [GH-1614] - Make LoadableDetachableModel#detach() final and add detach hooks that receive the attached object
    * [GH-1631] - Add Ajax veil behaviors that block the page, or a component, while a request runs
    * [GH-1669] - multiple file upload to resource + drag and drop files and upload to resource.

** Improvement

    * [WICKET-7135] - Work on deprecation warnings
    * [WICKET-7144] - Improve Ajax updates of form component panels
    * [WICKET-7172] - Support new CSP style, script directives
    * [WICKET-7176] - Bump up CDI API and Weld versions
    * [WICKET-7179] - add support for jQuery 4.0.0
    * [WICKET-7184] - Make <wicket:header-items/> HTML spec compliant
    * [WICKET-7187] - JavaScript header items: `module` for content, and import maps
    * [WICKET-7188] - Sign file upload resource constraints
    * [WICKET-7191] - Deprecate HTML5Attributes and move its logic to FormComponet hierarchy
    * [WICKET-7192] - restrict SourcesPage to resources of the page's own package
    * [WICKET-7195] - Add tag type="search" to those supported by TextField
    * [WICKET-7200] - Align class-vs-package resolution across the wicket-auth-roles annotations
    * [WICKET-7204] - Don't use INJECTION for ByteBuddy proxy creation

** Task

    * [WICKET-7193] - Deprecate and remove IAuthenticationStrategy
    * [WICKET-7194] - Deprecate and remove Include
    * [GH-1584] - Redundant japicmp exclusions need to removed
    * [GH-1699] - Include GitHub issues in the release notes built by build-changelog.sh
    * [GH-1708] - build-versions.py fails on Python 3 and misses the experimental modules

=======================================================================


