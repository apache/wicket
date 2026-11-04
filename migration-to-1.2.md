<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
# Migration to Wicket 1.2

1.2 is the previous stable release.

## List of major changes from 1.1 to 1.2

See the changelist for minor changes and bug fixes.

- ServletContext init parameter changed from deployment:true/false to configuration:deployment/development

- SignInPage setting in IApplicationSettings and ISecuritySettings that was present during pre-alpha does not exist in the final code. Instead see the Library example and especially the wicket-auth-roles-examples project to see how to do authentication neatly in Wicket.

- Authorization now includes IUnauthorizedComponentInstantiationListener which can be registered via Application.getSecuritySettings().setUnauthorizedComponentInstantiationListener() (oddly enough), and will be called anytime an unauthorized attempt is made to instantiate a component. The default implementation in settings now just throws and exception (during some pre-alpha code this was fancier, but we thought better of that by final).

- The wicket.request.target package has been reorganized somewhat to reveal its structure.

- The urlFor() methods available in pre-alpha in Page have been moved up to Component where they are generally more convenient and useful.

- Moved the Page decoded from the request into Request so it can be accessed in static contexts like RestartResponseAtInterceptPageException like this: RequestCycle.get().getRequest().getPage().getPageMap().redirectToInterceptPage(...).

- The methods redirectToInterceptPage() and continueToOriginalDestination() methods have been removed from Session, as has any notion of "current page map" (which was temporarily available during pre-alpha). These methods are now available in PageMap (which is the implementation site) and Component (for convenience)

- The new RequestListenerInterface class encapsulates what it means to add a request listener interface in Wicket. Instances of this class are instantiated as public static final members of each interface (see IRedirectListener.INTERFACE, for example) and it is no longer necessary to register such interfaces with RequestCycle (registration happens automatically in the RequestListenerInterface constructor and this code now detects conflicts should one arise). A bunch of gory, hardwired code goes away as a result of this first-class representation and Wicket becomes just a little more efficient at request processing to boot. All API methods that took a Method argument pointing to a listener interface method now take this first class object. In addition, the urlFor() methods which took Class arguments now take an instance of this class instead (which you can reference via the public static final INTERFACE member of whatever interface you are working with).

- PageMap is now a public API and PageMap objects are consistently used throughout the API instead of pageMapName Strings. Page maps are no longer created automatically based on the pageMapName query parameter. The behavior is now more explicit and typesafe. You can get a PageMap with PageMap.forName(String pageMapName). If the named page map does not exist, it will be created. A PageMap property has been added to the BookmarkablePageLink class and this property may eventually be supported on Links in general. The pageMapName query parameter is now an implementation detail that should not be relied on. In the future, it will be removed in favor of encoding the page map name into the url path like this: "pageMapName:pageId:path", where the default pagemap will be the empty string. This detail does not concern you, however, unless you are explicitly setting the pageMapName query string parameter, in which case your code will break in the future. To ensure that your code does not break in the future, you should convert your code to explicitly create page maps with PageMap.forName().

- BookmarkablePageLink now supports setPageMap(), allowing you to specify a PageMap target.

- Internals of RequestCycle are refactored. The processing is more formalized now, as defined in interface IRequestCycleProcessor. This interface is used by RequestCycle to delegate processing. The target of a request, like Page or e.g. shared resource, is abstracted in IRequestTarget, of which the current is set in RequestCycle.

- AttributeModifier is now backed by IBehavior. Behaviors are classes that can coorperate with Components. You attach them to components (Component.add) and they decide how to cooperate. Besides AttributeModifiers, AjaxHandlers extend IBehavior. Let your behaviors implement IBehaviorListener to be able to receive callbacks such as AjaxHandler does.

- Replaced OGNL with our own implementation. Most simple property and method calls (with no parameters) are supported. see wicket.util.object.Objects. Replacing OGNL resulted in a significant performance increase.

- Properties files, like markup files already, are now automatically reloaded if modified.

- IOnChangeListeners, like DropDownChoice, now use the form submit to call the interface method.

- Introduced mounting/ nice urls. Provided convenience methods for mounting bookmarkable pages and packages

- Session.get/setAttribute are now final to support attaching attribute observers. Use doGet/SetAttribute instead.

- Introduced wicket.protocol.http.servlet.WicketSessionFilter to intercepts the current session and set it in a thread local, so that non-wicket applications can reuse the session by just calling Session.get().

- moved WebApplication.onRuntimeException(Page page, RuntimeException e) to RequestCycle, which is a much more logical place for it, and has a more natural grouping with onBeginRequest and onEndRequest

- Spelling of some class names has been changed from British to American. ie Behaviour==\>Behavior

- onRender() has been deprecated in favour of onRender(MarkupStream)

- Session now uses ISessionStore for the actual storing of session attributes (including it's own object). The session store can be configured through the ISessionStoreFactory in session settings.

- Components and the Application now have a strongly typed meta data facility.

- for tunable authorization, you can now use IAuthorizationStrategy which is configured in security settings.

- project wicket-auth-roles is a supported default implementation of IAuthorizationStrategy based on simple roles. It supports defining authorization based on annotations and/ or on the new wicket meta data facility. See wicket-auth-roles-examples for examples. Even if this implemention would be too simplistic for some users, it is a good starting point to see how you can implement authorization.

- There must now be a 1:1 relationsship between markup and components. E.g. you can not have the same wicket:id at the same level multiple times in the markup and just one Component added to the hierarchy.

- Page.beforeCallComponent(Component component, Method m) is changed in Page.beforeCallComponent(Component component, RequestListenerInterface listener)

- URL parameters were refactored to decrease the chance of conflicting parameter names, and to encode the page map more directly. Users mostly won't have a problem with this - it's mostly an implementation detail - but in case you were manually coding urls, bookmarkable page might be an issue.

Formerly, you had to address a bookmarkable page like this:

myapp?bookmarkablePage=com.my.Page

in Wicket 1.2 and up, this should be changed in this:

myapp?wicket:bookmarkablePage=:com.my.Page

where wicket:bookmarkablePage is the parameter name, com.my.Page is the page name, and : is the seperator between the page map and the page. In this case, the page map is null.

- PageMaps are refactored. Any page can be constructed to be in a page map (previously, you could only do this with bookmarkable pages), and page maps have to be constructed beforehand (previously, pagemaps were created on the fly)

- PasswordTextField is now required by default, which is the most common usecase

### settings refactor

\*Your MyApplication initialization code should be moved from MyApplication constructor to MyApplication.init() to make sure that the application gets initialized properly. Settings provided in the constructor are in danger of being replaced by Wicket later on in the initialization phase by default values.

\*Application now expects the home page class provided by implementing abstract Application.getHomePage() instead of pushing it into the settings object

\*Application.getPages() and ApplicationPages have been removed, its properties are now located in Application.getApplicationSettings()

\*getPages().putClassAlias() has been replaced by a more flexible mounting strategy. See WebApplication.mount() for details.

\*Application.getAuthorizationStrategy() is no longer supported. instead use\
Application.getSecuritySettings().setAuthoriazationStrategy(...)

\*Application.getLocalizer() is no longer supported. instead use Application.getMarkupSettings().setLocalizer(...)

\*Crypt setup has changed. Application.newCrypt(), ApplicationSettings.setEncryptionKey(), and ApplicationSettings.setCryptClass() are gone. Instead use Application.getSecuritySettings().setCryptFactory(...). By default a CachingSunJceCryptFactory will be used with ISecuritySettings.DEFAULT_ENCRYPTION_KEY.

\*Application.getAdditionalMarkupHandler() is gone. instead use Application.getMarkupSettings().setMarkupParserFactory(new MarkupParserFactory(IMarkupFilter\[\] additionalFilters))

\*ApplicationSettings.configure() methods moved to Application

\*You used to be able to get the HttpSession by doing ((WebSession)getSession())getHttpSession(). Due to some horrible misuse we saw, and due to the fact that WebSession doesn't have to be backed by HttpSession at all, we decided to remove the getHttpSession method. If you /really/ need the HttpSession (for instance for integration with non-Wicket programs), you can get it as follows: ((WebRequest)getRequest).getHttpServletRequest().getSession().

## Bordered Pages

In order to implement bordered pages you were asked to do something like

```java
	/**
	 * Constructor.
	 */
	public BorderedPage()
	{
		border = new PageBorder("border");
		super.add(border);
	}

	/**
	 * @see wicket.MarkupContainer#add(wicket.Component)
	 */
	public MarkupContainer add(final Component child)
	{
		// Add children of the page to the page's border component
		border.add(child);
		return this;
	}

	/**
	 * @see wicket.MarkupContainer#removeAll()
	 */
	public void removeAll()
	{
		border.removeAll();
	}

	/**
	 * @see wicket.MarkupContainer#replace(wicket.Component)
	 */
	public MarkupContainer replace(Component child)
	{
		return border.replace(child);
	}

	/**
	 * @see wicket.MarkupContainer#autoAdd(Component)
	 */
	public boolean autoAdd(final Component component)
	{
	    return border.autoAdd(component);
	}
```

Now it is much simpler. Just do

```java
	public BorderedPage()
	{
		border = new PageBorder("border");
		border.setTransparentResolver(true);
		super.add(border);
	}
```

As a consequence, add(), remove(), removeAll() and autoAdd() are now final and not be replaced any more.
