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
# Migration to Wicket 1.5

## Migrating to Wicket 1.5

Note: at the bottom of the page there is a Wiki label **wicket15**. Clicking on it will show you all pages with information about Wicket 1.5.

#### Environment

- Wicket 1.5 requires at least Java 5

#### (RC1) Upgraded to Servlet API 2.5

In order to stay with the times, we have upgraded the minimal requirements for servlet specifications to servlet API 2.5.

#### Maven users are recommended to depend on org.apache.wicket:wicket:pom from now on

org.apache.wicket:wicket:pom is an aggregation project that has dependencies to all mandatory jars to run basic Wicket application (org.apache.wicket:wicket-core:jar, org.apache.wicket:wicket-util:jar and org.apache.wicket:wicket-request:jar). Notice that this requires \<type\>pom\</type\> in your dependency configuration.\
Users which don't use dependency management tools (like Maven, Ivy, Gradle, ...) should download these three jars and put them in the application classpath.

OSGi users can use [wicket-bundle](http://repo1.maven.org/maven2/org/wicketstuff/wicket-bundle/) which combines these three jars into one with the proper OSGi Manifest entries.

#### (RC2) All standard (form) validators now extend Behavior.

This allows them to contribute to the component's markup, e.g. provide client-side validation.

#### Upgraded to JUNIT4

We changed the root pom.xml to use junit4 (4.7)

#### Removed deprecated method, class and interface definitions

In order to keep the code clean, we removed all methods, classes and interfaces which were marked deprecated in Wicket 1.4. Before migrating to Wicket 1.5 you should replace all deprecated invocations.

#### Removed FormComponent.setPersistence()

[WICKET-2213](http://issues.apache.org/jira/browse/WICKET-2213): At the very beginning of Wicket we introduced FormComponent.setPersistence() with the idea to make creating Login panels very easy. Basically Wicket would deal with the Cookie handling for the login details. Now some years later it is evident that this feature is not very useful:

- It basically is used for login panels only
- Even in our examples Password.setPersistence is false which means that "rememberMe" doesn't really work. Only the user name is stored in a Cookie
- Forcing formComponent.getPageRelativePath() as the Cookie name is too restrictive
- You can not combine username and password into a single Cookie and e.g. decrypt it for safety reasons

So we removed that feature and introduced IAuthenticationStrategy. A default implementation has been provided and the examples have been updated. As usual it gets registered with ISecuritySettings.

In case you want to implement your own IAuthenticationStrategy, a utility class `org.apache.wicket.util.cookies.CookieUtils` has been added.

#### Removed CompressedPackageResource

Since all modern java and web servers support compression of resources we dropped that feature from wicket. Usually servers not only support that feature but also are more flexible so there's no need for a redundant functionality that does not belong to a web framework but to the server itself.

#### StringResourceModel changes

StringResourceModel has changed some of its constructors and modified the order of the arguments. The constructors with default values might introduce errors because the java compiler matches them, but their semantics have changed.

*Wicket 1.4 StringResourceModel*

```java
public StringResourceModel(final String resourceKey, final Component component,
       final IModel<?> model, final Object[] parameters, final String defaultValue)
```

Became:

*Wicket 1.5 StringResourceModel*

```java
public StringResourceModel(final String resourceKey, final Component component,
       final IModel<?> model, final String defaultValue, final Object... parameters)
```

If you used the 1.4 constructor, the compiler would now match the following (Wicket 1.5) constructor:

```java
public StringResourceModel(final String resourceKey, final Component component,
	final IModel<?> model, final Object... parameters)
```

Where the Object\[\] array is matched to the first parameter of the ellipsis and the defaultValue as the second parameter.

StringResourceModel#setLocalizer() was removed, if needed you can override #getLocalizer() instead.

#### Removed IComponentBorder

The interface has been removed since IBehavior can do exactly the same. MarkupComponentBorder has been migrated, which means you can add associated markup to the behavior. Markup which will surround the behavior's component.

#### Component.getStyle() implementation changed

getStyle() used to return Component.getVariation() + "\_" + style. That eventually caused issue such as mentioned in [WICKET-2298](https://issues.apache.org/jira/browse/WICKET-2298).

We changed that to getStyle() now returning style only which as a consequence required us to change all resource related APIs to add variation. To migrate your code you might either provide component.getVariation() or null where not relevant.

#### Component.getMarkupId() implementation changed

If an "id" attribute is specified in a component's markup, it is now used verbatim instead of being replaced with a unique, generated value. During your migration, verify that your setOutputMarkupId(true) components don't have "id" attributes. Otherwise multiple component instances will share the same markup ID, breaking AJAX updates.

#### Removed magic from Border Component

We had several issues with Border such as [WICKET-2494](https://issues.apache.org/jira/browse/WICKET-2494) and [WICKET-3702](https://issues.apache.org/jira/browse/WICKET-3702) the fact that all over core you could find code such as `"if (comp instanceof Border) {}"` . That is now all fixed, though at a (low) price. Because the magic is now gone, we think it is even clearer than it was before. The difference is you have to tell Border where to add the child component. Whether it will be added to `<wicket:border>..</wicket:border>` which is called the "border", or `<span wicket:id="myBorder">..</span>` which called the "border body". And because the body can be wrapped by a container such as a Form, you need to add (move) the body to the wrapper component via `wrapper.add(getBodyContainer())` if needed. By doing so you create a clean component hierarchy with no more magic and ambiguities and transparent resolvers.

`border.addToBorder()` will add to the border, while `border.add()` is a convenient and explicit alias for `border.getBodyContainer().add()`

see Border javadoc as well

#### (RC6) AbstractSingleSelectChoice uses empty String as value for default choice

AbstractSingleSelectChoice now uses an empty String instead of NO_SELECTION_VALUE ("-1"), allowing negative values for choices. See [WICKET-933](https://issues.apache.org/jira/browse/WICKET-933).

AbstractChoice.setType() now throws an UnsupportedOperationException as type-conversion is performed by the assigned IChoiceRenderer. See [WICKET-3745](https://issues.apache.org/jira/browse/WICKET-3745).

#### Component rendering

We used to have different entry points to render a Page, a Component for a web request and a Component for an AJAX request. That is history. Whenever you want to render a component, simply call component.render().

You may recall that Component.render(MarkupStream) already existed and thus in most cases (e.g. IComponentResolvers implementations) you simply need to remove the markupStream parameter.

The markup fragment which is still needed to render a Component is retrieved via Component.getMarkup(). Most components will ask their parent container to find it via MarkupContainer.getMarkup(Component child).

IMarkupFragment is a new concept introduced. A markup fragment is what is says: a fragment of markup taken from a larger resource (file). Please see the javadoc on when it is better to subclass getMarkup() and when getMarkup(Component).

Component.getMarkup() successfully returns a result as soon as it is connected (add) to a parent with an associated markup resource (file). Note that this usually is not in the component constructor, since the component itself will not yet been added to its parent.

You may recall that until now you very carefully had to forward the markup stream to the next suitable position. That has been simplified as well. Every component will use its own markup stream and the parent container is responsible to position it while rendering its child components.

On the same topic: onRender(MarkupStream) has been changed to onRender(); no more MarkupStream. You can get for every component once you called render() via getMarkupStream().

#### UTF-8 encoded property files

If you are using Java 6 or newer, than Wicket will be configured to use `java.util.Properties#load(Reader(in, "utf-8"))` for files with "\*.utf8.properties" extension.

#### XML based property files

The file extension for xml based property files is changed from ***.xml*** to ***.properties.xml***.\
This way it is possible to use pages with [XML markup type](http://wicket.apache.org/apidocs/1.4/org/apache/wicket/Page.html#getMarkupType()) and xml based property files. Part of [WICKET-2035](https://issues.apache.org/jira/browse/WICKET-2035).

#### TabbedPanel

- ITab.getPanel() now returns WebMarkupContainerWithAssociatedMarkup instead of Panel so it is now possible to create tabs using Fragments or Panels rather then just Panels.
- TabbedPanel.setSelectedTab() is now chainable
- TabbedPanel now takes a List\<? extends ITab\> rather then List\<ITab\>

#### Wizard

- Wizard no longer invokes IWizardStep#init(IWizardModel), this is now the responsibility of the IWizardModel itself. WizardModel and DynamicWizardModel do it already, but you have to take care of this for yourself if you have a custom IWizardModel implementation.

#### MarkupContainer.isTransparentResolver() removed

We removed `MarkupContainer.isTransparentResolver()` in favor of the more general `IComponentResolver`. To mitigate the transition, we provide a `TransparentWebMarkupContainer`. But please note it does not solve all use cases `isTransparentResolver()` did.

There are several solutions for you. Your component may implement `IComponentResolver` itself and resolve the child component. The `TransparentWebMarkupContainer` implementation may serve as an example.

The main reason why we removed `MarkupContainer.isTransparentResolver()` has been the magic necessary in Wicket's core and the number of issues/questions, where we identified `isTransparentResolver()` as the root cause. It all boils down to that with `isTransparentResolver()` the markup and component hierarchies get out of sync.

Noteable difference: different than `MarkupContainer.isTransparentResolver()` where the component id wasn't needed in the path (transparent), with IComponentResolver it must be included. Imagine you used to have a transparent container such as `<body wicket:id="myBody">`, where you were able to access a label from the Page just by "myLabel". In 1.5. you must use "myBody:myLabel".

#### IComponentResolver.resolve API has changed

Until now, with `IComponentResolver.resolve()` it was your responsibility to render the component. You returned true, if the component was found and rendered. That was because we had different means to render the component and wicket itself was not able to judge which one to use. That has changed and thus we were able to change the `IComponentResolver.resolve` API. With the revised API, you simply return the resolved component and wicket core knows what to do with it. It made Wicket's internal code simpler and hopefully yours as well.

#### Wicket-IOC changes

InjectorHolder.getInjector().inject(Object object) is replaced with org.apache.wicket.injection.Injector.get().inject(Object object).\
This is valid for both Wicket-Spring and Wicket-Guice.

Wicket-Guice's InjectionFlagCachingGuiceComponentInjector is removed. The replacement is GuiceComponentInjector. The caching mechanism that InjectionFlagCachingGuiceComponentInjector provided is moved to org.apache.wicket.injection.Injector so that the caching is re-used for both Wicket-Spring and Wicket-Guice.\
Wicket-Guice no more supports method injection. The same could be achieved with field injection. This way it is the same as Wicket-Spring and the involved code is highly reused between the IOC projects.

###### Spring Example

In Wicket 1.4, you might have done this to configure your application to use `@SpringBean`:

```java
    @Override
    protected void init()
    {
        // initialize Spring
        addComponentInstantiationListener(new SpringComponentInjector(this, applicationContext));
    }
```

In Wicket 1.5, you would do this:

```java
    @Override
    protected void init()
    {
        // initialize Spring
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, applicationContext))
    }
```

#### Visitors

Visitors have been cleaned up. Dependency on magic return values defined in Component#IVisitor have been replaced with a Component#IVisit object that allows the visitor to control the flow of the traversal.

```java
formComponent.visitChildren(Radio.class, new IVisitor<Component>()
{
	public /*[1]*/ Object component(Component component /*[2]*/)
	{
		if (value.equals(component.getDefaultModelObject()))
		{
			addFormComponentValue(formComponent,((Radio<?>)component).getValue());
			return STOP_TRAVERSAL; /*[3]*/
		}
		return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER; /*[4]*/
	}
});
```

```java
formComponent.visitChildren(Radio.class, new IVisitor<Component, Void>()
{
	public void /*[1]*/ component(final Component component, final IVisit<Void> visit /*[2]*/)
	{
		if (value.equals(component.getDefaultModelObject()))
		{
			addFormComponentValue(formComponent,((Radio<?>)component).getValue());
			visit.stop(); /*[3]*/
		}
		else
		{
			visit.dontGoDeeper(); /*[4]*/
		}
	}
});
```

\[1\] The new style visitor no longer returns a value, instead the value can be returned using visit.stop(value)\
\[2\] The new visit object is introduced to control the visitor traversal\
\[3\] Instead of relying on magic return values the traversal is stopped by using the new visit object\
\[4\] Same as \[3\] but a different method on IVisit is used

#### Component Initialization - Component#onInitialize

A new callback method was added to Component class - onInitialize(). This method is intended to provide an alternate place to initialize the component in addition to standard constructors. The advantage of using this method over a constructor is that when it is invoked the Page and the markup of the component are both available and so a more complex initialization can take place.

Before this method was introduced the developer had to rely on using onBeforeRender() method with some kind of "has-already-been-called-or-not" check to make sure the initialization code has only been run a single time. onInitialize() has this guarantee baked in - it will only be called once during the lifetime of the component.

#### Request parameters

To get the current request parameters (for example in Ajax behavior processing) in Apache Wicket 1.4:

```java
    String parameterValue = RequestCycle.get().getRequest().getParameter(parameterName);
```

In 1.5 you have a better control where the parameter comes from:

```java
   // GET request
   StringValue parameterValue = RequestCycle.get().getRequest().getQueryParameters().getParameterValue(parameterName);

   // POST request
   StringValue parameterValue = RequestCycle.get().getRequest().getPostParameters().getParameterValue(parameterName);
```

Or if you don't care about the method:

```java
   StringValue parameterValue = RequestCycle.get().getRequest().getRequestParameters().getParameterValue(parameterName);
```

#### Page mounting

In 1.4.x the parameters of mounted pages where encoded in the path of the URL by default:

```java
   http://server/mount/path/parameter1/value1/parameter2/value2
```

But now pages are mounted with MountedMapper instead, which leads to the following URLs:

```java
   http://server/mount/path?parameter1=value1&parameter2=value2
```

To get the previous 1.4.x URLs you have to configure the mapper with a UrlPathPageParametersEncoder:

```java
   mount(new MountedMapper("/mount/path", MyPage.class,
			new UrlPathPageParametersEncoder()));
```

See [Request mapping](https://cwiki.apache.org/confluence/display/WICKET/Request+mapping#MountedMapper) for further mount options.

#### Switching to/from secured communication over https

In 1.4 a custom IRequestCycleProcessor is needed:

```java
public class MyApplication extends WebApplication
{
 	@Override
 	protected IRequestCycleProcessor newRequestCycleProcessor()
  	{
  		return new HttpsRequestCycleProcessor(new HttpsConfig());
  	}
}
```

In 1.5 this is re-implemented with the new HttpsMapper:

```java
public class MyApplication extends WebApplication
{
        @Override
 	public void init()
 	{
 		super.init();

 		setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig()));
 	}
}
```

Registering HttpsMapper as a root mapper will check all pages before rendering for annotation @RequireHttps.

#### HeaderContribution

In 1.4 a custom HeaderContributor needed:

```java
new HeaderContributor(new IHeaderContributor() {

            private static final long serialVersionUID = 1L;

            @Override
            public void renderHead(IHeaderResponse response) {
                 //your logic goes here
            }
        });
```

This could be substitute with:

```java
new AbstractBehavior() {

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                //your logic goes here
            }
        };
```

#### Component implements IHeaderContributor and Behavior has #renderHead(Component, IHeaderResponse)

Prior to 1.5 IHeaderContributor was used as a mixin for components and behaviors that wanted to write to the header, in 1.5 it is no longer necessary to implement the interface directly because Component implements it and Behavior has #renderHead(Component, IHeaderResponse).

#### Removed HeaderContributor and friends.

HeaderContributor was a convenience that did not add much and actually made things worst by increasing memory footprint in a lot of cases. Instead we can simply override IHeaderContributor#renderHead(IHeaderResponse) and achieve the same functionality in a simpler and consistent fashion.

Wicket 1.4:

```java
public class MyPage extends WebPage {
  public MyPage() {
    add(HeaderContributor.forJavaScript(YuiLib.class, "yahoo-dom-event/yahoo-dom-event.js"));
    add(HeaderContributor.forCss(AbstractCalendar.class, "assets/skins/sam/calendar.css"));
  }
}
```

Becomes the following in 1.5:

```java
public class MyPage extends WebPage {
  public MyPage() {
  }
  public void renderHead(IHeaderResponse response) {
    response.renderJavaScriptReference(new PackageResourceReference(YuiLib.class,
      "yahoo-dom-event/yahoo-dom-event.js"));
    response.renderCSSReference(new PackageResourceReference(AbstractCalendar.class,
      "assets/skins/sam/calendar.css"));
  }
}
```

Likewise,

```java
public class MyPage extends WebPage {
  public MyPage() {
    WebMarkupContainer c=new WebMarkupContainer();
    c.add(HeaderContributor.forJavaScript(YuiLib.class, "yahoo-dom-event/yahoo-dom-event.js"));
    add(c);
  }
}
```

becomes

```java
public class MyPage extends WebPage {
  public MyPage() {
    WebMarkupContainer c=new WebMarkupContainer();
    c.add(new Behavior() {
      public void renderHead(Component component, IHeaderResponse response) {
        response.renderJavaScriptReference(new PackageResourceReference(YuiLib.class,
          "yahoo-dom-event/yahoo-dom-event.js"));
      }
    });
    add(c);
  }
}
```

#### Removed TextTemplateHeaderContributor / StringHeaderContributor

Wicket 1.4:

```java
public class DynamicCssBehavior extends TextTemplateHeaderContributor
    public DynamicCssBehavior() {
        super(new CssTemplate(new MyTextTemplate()), new VariablesModel());
    }

    private static class VariablesModel extends AbstractReadOnlyModel<Map<String, Object>> {
        public Map<String, Object> getObject() {
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("background", "#ff0000");
            return variables;
        }
    }
}
```

Wicket 1.5:

```java
public class DynamicCssBehavior extends Behavior {
    private TextTemplate template = CssTemplate(new MyTextTemplate());

    public void renderHead(Component component, IHeaderResponse response) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("background", "#ff0000");
        response.renderString(template.asString(variables));
}
```

#### (M4) Resource decoration

With org.apache.wicket.Application.setHeaderResponseDecorator(IHeaderResponseDecorator) it is possible to intercept the contribution of resources. This way you may specify which resources will be contributed to the \<head\> part of the page and which will be contributed to the end (after \</body\>). Or you may merge several contributions into a bigger one, i.e. serve them all in one http request. For more information consult with [Resource decoration](https://cwiki.apache.org/confluence/display/WICKET/Resource+decoration) or see the new wicket-[example](http://wicketstuff.org/wicket/resourceaggregation).

#### RequestCycle

RequestCycle has changed in 1.5-M1 and WebRequestCycle has been removed.

In 1.4 you would have overridden onBeginRequest, onEndRequest, and onRuntimeException, like so:

*RequestCycle*

```java
public MyRequestCycle extends WebRequestCycle {
    @Override
    public void onBeginRequest() {
      //...
    }

    @Override
    public void onEndRequest() {
        //...
    }

   @Override
    public Page onRuntimeException(Page page, RuntimeException e) {
        // ...
    }
}
```

In 1.5, onRuntimeException is gone (see next section). You can still override onBeginRequest and onEndRequest. However, if you are creating a framework that needs to be able to "plug in" functionality into those callbacks, it is recommended that you no longer create a subclass of RequestCycle that your users must also subclass. This limits them from using two such frameworks. Now there is RequestCycle#IRequestCycleListener that you can plug in, either to the cycle itself or to the application, and you will be notified of all of these method callbacks. For example:

*IRequestCycleListener*

```java
public class SomeWebApplication extends WebApplication
{

	@Override
	protected void init()
	{
                getRequestCycleListeners().add(new IRequestCycleListener() {
		{
			public void onBeginRequest()
			{
				// optionally do something at the beginning of the request
			}

			public void onEndRequest()
			{
				// optionally do something at the end of the request
			}

			public IRequestHandler onException(RequestCycle cycle, Exception ex)
			{
				// optionally do something here when there's an exception

				// then, return the appropriate IRequestHandler, or "null"
				// to let another listener handle the exception
			}

			// ... more methods for various events as per IRequestCycleListener
		});
	}

	@Override
	public Class<? extends Page> getHomePage()
	{
		return SomePage.class;
	}
}
```

Note also that instead of overriding org.apache.wicket.Application.newRequestCycle(...), you will now call org.apache.wicket.Application.setRequestCycleProvider(IRequestCycleProvider) in your Application#init method to provide a factory that can return any custom RequestCycle that you do want to create, although with the plugins, the need to create a custom request cycle is much lower now.

#### Exception handling

In Wicket 1.4 it was needed to extend org.apache.wicket.RequestCycle.onRuntimeException(Page, RuntimeException).\
Wicket 1.5 gives even better control:

- add your own org.apache.wicket.request.cycle.IRequestCycleListener (AbstractRequestCycleListener) with org.apache.wicket.Application.getRequestCycleListeners().add() and implement its #onException(RequestCycle, Exception)
- or override org.apache.wicket.Application.getExceptionMapperProvider() - the IExceptionMapper is used if none of the configured IRequestCycleListeners doesn't know how to handle the exception.\
  For information on how the request cycle handles exceptions see [RequestCycle in Wicket 1.5](https://cwiki.apache.org/confluence/display/WICKET/RequestCycle+in+Wicket+1.5) for more information

#### ImageMap removed

ImageMap was deprecated in 1.4.11 and in 1.5 it was replaced with ClientSideImageMap component

#### in IResourceSettings method setAddLastModifiedTimeToResourceReferenceUrl() has been replaced by IResourceCachingStrategy

Wicket 1.5 has improved support for caching. It gives you fine-grained control over caching of your package resources.

There are several out-of-the-box implementations of the interface

`org.apache.wicket.request.resource.caching.IResourceCachingStrategy`

which provide different ways of adding caching related information your package resources (CSS, javascript, images, etc.). Also it's pretty easy to roll out your own strategy.

Basically the caching strategy will decorate the package resource with a version-dependendant fingerprint.

The default caching strategy in development and production mode is

`org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy`

*(taken from javadoc of* `FilenameWithVersionResourceCachingStrategy`*)*

> - resource caching strategy that adds a version for the
> - requested resource to the filename.
> -
> - versioned_filename := \[basename\]\[version-prefix\]\[version\](.extension)
> -

In development mode the \[version\] string is be last modified timestamp of the resource in UNIX milliseconds. If the resource resides in a jar the timestamp of the jar file will be taken instead.

In production mode the MD5 hash of the file content will be calculated and taken as \[version\]. The calculation will be done once and then be cached for the lifetime of the application. We decided against choosing the last modified timestamp in deployment mode since it seems to be inconsistent in some cluster setups.

Example:

| basename | styles.css |
|---|---|
| last-modified | 1303741587000 |
| md5-checksum | F1336A3F160F1399ECF87B6196AB1C2A |
| decorated-filename-in-development-mode | styles-ver-1303741587000.css |
| decorated-filename-in-deployment-mode | styles-ver-F1336A3F160F1399ECF87B6196AB1C2A.css |

So whenever the file changes the changed filename will cause a cache miss and the updated file will be requested from the server.

For additional information on this style of caching see chapter

` Use fingerprinting to dynamically enable caching`

in

`http://code.google.com/intl/de-DE/speed/page-speed/docs/caching.html`

You can set your a non-default caching strategy with

`getResourceSettings().setCachingStrategy(...)`

To disable caching altogether (e.g. for performance comparisons) use

`getResourceSettings().setCachingStrategy(NoOpResourceCachingStrategy.INSTANCE)`

#### Inter-component events

Wicket 1.5 offers a simple, yet flexible, way for component to communicate with each other in a decoupled manner. The two major interfaces that facilitate this are:

```java
/**
 * Objects that can send events
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IEventSource
{
	/**
	 * Sends an event
	 *
	 * @param <T>
	 *            type of payload
	 *
	 * @param sink
	 *            object that will receive the event
	 * @param broadcast
	 *            if the object that receives the event needs to broadcast it to others, this is the
	 *            type of broadcast that should be used
	 * @param payload
	 *            event payload
	 */
	<T> void send(IEventSink sink, Broadcast broadcast, T payload);
}
```

and

```java
/**
 * Objects that can receive events
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IEventSink
{
	/**
	 * Called when an event is sent to this sink
	 *
	 * @param event
	 */
	void onEvent(IEvent<?> event);
}
```

.

The classes that implement these interfaces, and can thus participate in the event mechanism are: Component, RequestCycle, Session, and Application.

The mechanism allows for different event broadcast methods defined here:

```java
/**
 * Defines the event broadcast type.
 *
 * @author igor
 */
public enum Broadcast {
	/**
	 * Breadth first traversal. Supported sinks in order of traversal:
	 *
	 * <ol>
	 * <li>{@link Application}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Page}</li>
	 * <li>{@link Component}s</li>
	 * </ol>
	 *
	 * Any sink along the path can be specified and traversal will start with the specified sink as
	 * root, eg:
	 *
	 * <ul>
	 * <li>If a component inside the page is specified then only the component and all its children
	 * will receive the event</li>
	 * <li>If Session is specified then the session, the request cycle, the page and all its
	 * components will receive the event</li>
	 * </ul>
	 */
	BREADTH,
	/**
	 * Depth first traversal. Supported sinks in order of traversal:
	 *
	 * <ol>
	 * <li>{@link Component}s</li>
	 * <li>{@link Page}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link Application}</li>
	 * </ol>
	 *
	 * Any sink along the path can be specified and traversal will start with the specified sink as
	 * root, eg:
	 *
	 * <ul>
	 * <li>If a component inside the page is specified then only the component and all its children
	 * will receive the event</li>
	 * <li>If Session is specified then the session, the request cycle, the page and all its
	 * components will receive the event</li>
	 * </ul>
	 *
	 */
	DEPTH,
	/**
	 * A bubble-up traversal. In a bubble-up traversal only the sink and its parents are notified.
	 *
	 * Supported sinks in order of traversal are:
	 * <ol>
	 * <li>{@link Component}s</li>
	 * <li>{@link Page}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link Application}</li>
	 * </ol>
	 *
	 * Any sink along the path can be specified and traversal will start at the specified sink and
	 * work its way up to the {@link Application}, eg:
	 *
	 * <ul>
	 * <li>If a component inside the page is specified then only the component, its parents, the
	 * request cycle, the session, and the application will be notified.
	 * <li>If Session is specified then the session, the application will be notified</li>
	 * </ul>
	 */
	BUBBLE,
	/**
	 * Only the specified sink receives the event
	 */
	EXACT;
}
```

There is an example in wicket-examples which demonstrates the usage of this.

##### IEventDispatcher

M4 also introduces IEventDispatcher. Applications can register custom event dispatchers in frameworksettings; the dispatchers can be used to build custom event delivery mechanisms. For example a custom IEventDispatcher mechanism can route events to annotated methods, eg

```java
public class MyComponent extends Component {
    @OnEvent
    private void onUserAdded(UserAddedEvent event) {...}
}
```

where UserAddedEvent is the event payload object.

The default Component#onEvent() method will be called even if custom dispatchers are registered.

##### Default ajax event

M4 also introduces a default event raised whenever wicket begins to create an ajax response. The payload of the event is the AjaxRequestTarget used for event. Sample implementation:

```java
// component that always adds itself to the ajax response
public class MyComponent extends Component {
    public void onEvent(IEvent event) {
        if (event.getPayload() instanceof AjaxRequestTarget) {
            ((AjaxRequestTarget)event.getPayload()).add(this);
         }
    }
}
```

#### (M4) Header render sequence inverted

In Wicket 1.4 header contributions were rendered parent first. Since 1.5 we render the deepest child first. Now it's possible for containers, which manage their children, to supersede a childs contribution. See [WICKET-2693](http://issues.apache.org/jira/browse/WICKET-2693)

#### (M4) WicketTester.startPanel() and WicketTester.assertLabel()

WicketTester always requires a Page, no exception. For startPanel() to work, we use DummyPanelPage and add the panel. Until now assertLabel() required a path relative to page. That wasn't very intuitive, since it required you to prepend the panel-id to your label path. That has changed. Now the path relative to the Panel is sufficient.

#### (M4) IBehavior interface refactored to abstract class Behavior

IBehavior interface has been refactored into an abstract Behavior class. AbstractBehavior has been deprecated, Behavior should be extended instead. All classes directly implementing IBehavior should now instead extend Behavior.

###### Javascript Libraries

In the past you might have used `org.wicketstuff`:`prototype` for Javascript integration and done something like this to add the `prototype.js` to your header.

```java
   /**
     * Add prototype to header
     */
    @Override
    public void renderHead(IHeaderResponse response)
    {
        response.renderJavascriptReference(PrototypeResourceReference.INSTANCE);
    }
```

In 1.5, you use

```xml
     <dependency>
	<groupId>org.wicketstuff</groupId>
	<artifactId>wicketstuff-jslibraries</artifactId>
	<version>1.5.8</version>
     </dependency>
```

And do something like this:

```java
  /**
   * Add prototype to header
   */
  @Override
  public void renderHead(Component component, IHeaderResponse response)
  {
    val header = JSLib.getHeaderContribution(VersionDescriptor.alwaysLatest(Library.PROTOTYPE))
    header.renderHead(response)
  }
```

Note that this also applies to multiple Javascript libraries. From `org.wicketstuff.jslibraries.Library`:

```java
DOJO, EXT_CORE, JQUERY, JQUERY_UI, MOOTOOLS_CORE, MOOTOOLS_MORE, PROTOTYPE, SCRIPTACULOUS, SWFOBJECT, YUI
```

#### (M4) Added support for 'x-forwarded-for' headers and more

Please see <http://code.google.com/p/xebia-france/wiki/XForwardedFilter> which has been used as blueprint, though we are not using a ServletFilter. It's more tightly integrated to benefit from Wicket's core functionalities. See `XForwardedRequestWrapperFactory` and `XForwardedRequestWrapper`. The most convinient place to add them is via subclassing `WebApplication.newWebRequest()` in your own Application object.

#### (RC5) Clarified if and when a xml decl gets added to the response

In case the markup file contains an xml decl such as `<?xml version="1.0" encoding="utf-8" ?>`, it'll only be used for properly reading the markup. It'll never be used for the response and it's presence or absence will no longer determine whether an xml decl gets added to the response or not.

The response encoding is determined via `Application.getRequestSettings().getResponseRequestEncoding()` and applies for all your pages.

WebPage.renderXmlDecl(response) and WebApplication.renderXmlDecl(response) have been introduced to control the xml decl contribution. The behavior can be changed per Page or for your whole application. It can generally be enabled, disabled or determined via a rule. The rule currently is simple: if the page mime-type is `application/xhtml+xml` and the HTTP_ACCEPT header allows for the same, then insert the xml decl.

#### Redirect to non wicket or external page.

Now when you need to redirect to a non Wicket managed URL or external link you can use RedirectRequestHandler instead of

Old code

```java
  getRequestCycle().setRequestTarget(new RedirectRequestTarget("/usage.html"));

  // or
  page.getRequestCycle().setRequestTarget(new RedirectRequestTarget("http://www.facebook.com/"));
```

And post 1.5 version:

```java
  getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler("/usage.html"));

  // or
  page.getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler("http://www.facebook.com/"));
```

#### (RC5) Pluggable serialization

`org.apache.wicket.util.io.IObjectStreamFactory` is replaced with `org.apache.wicket.serialize.ISerializer`.\
Now via org.apache.wicket.settings.IFrameworkSettings.setSerializer(ISerializer) it is possible to use different than Java Serialization way to serialize the pages in the IPageStore/IDataStore.

By default Wicket still uses Java Serialization (ObjectOutputStream + ObjectInputStream) to do this job bacause it is tested and stable.\
Additionally there is an implementation based on [Kryo](http://code.google.com/p/kryo/) at [WicketStuff](https://github.com/wicketstuff/core/tree/master/jdk-1.5-parent/serializer-kryo). Kryo is faster than Java Serialization and produces smaller byte array for the same object but the implementation at WicketStuff is still young and not so stable.

#### List of renamed classes and methods

Following the renames in subclasses is easier if the @Override annotations are present. IDE-s, like Eclipse, can be used to add missing @Override annotations before moving to wicket 1.5.

| 1.4 | 1.5 |
|---|---|
| org.apache.wicket.ResourceReference | org.apache.wicket.request.resource.PackageResourceReference |
| org.apache.wicket.RedirectToUrlException | org.apache.wicket.request.flow.RedirectToUrlException |
| org.apache.wicket.Request | org.apache.wicket.request.Request |
| org.apache.wicket.RequestCycle | org.apache.wicket.request.cycle.RequestCycle |
| org.apache.wicket.RequestCycle.urlFor | org.apache.wicket.request.cycle.RequestCycle.urlFor |
| org.apache.wicket.util.lang.Objects.cloneObject(Object) | org.apache.wicket.util.lang.WicketObjects.cloneObject(Object) |
| org.apache.wicket.protocol.http.WebRequest | org.apache.wicket.request.http.WebRequest |
| org.apache.wicket.protocol.http.WebResponse | org.apache.wicket.request.http.WebResponse |
| org.apache.wicket.request.target.basic.RedirectRequestTarget | RedirectRequestHandler |
| org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy | use PageParameters.getNamedParameter(String) with any IRequestHandler |
| org.apache.wicket.request.target.resource.ResourceStreamRequestTarget | import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler |
| org.apache.wicket.request.http.WebRequest.getHttpServletRequest() | org.apache.wicket.protocol.http.servlet.ServletWebRequest.getContainerRequest() |
| org.apache.wicket.protocol.http.WebApplication.newWebResponse(final HttpServletResponse) | (missing) |
| org.apache.wicket.Application.set(Application) | org.apache.wicket.ThreadContext.setApplication(Application) |
| org.apache.wicket.protocol.http.BufferedWebResponse.BufferedWebResponse(HttpServletResponse) | org.apache.wicket.protocol.http.BufferedWebResponse.BufferedWebResponse(WebResponse) |
| org.apache.wicket.PageParameters | org.apache.wicket.request.mapper.parameter.PageParameters |
| org.apache.wicket.PageParameters.put(String, String) | org.apache.wicket.request.mapper.parameter.PageParameters.set(String, Object) |
| org.apache.wicket.PageParameters.getInt(String) | org.apache.wicket.request.mapper.parameter.PageParameters.get(String).toInt() |
| org.apache.wicket.protocol.http.HttpSessionStore | org.apache.wicket.session.HttpSessionStore |
| org.apache.wicket.protocol.http.WebApplication.newSessionStore() | (missing, HttpSessionStore is the only supported) |
| org.apache.wicket.markup.html.resources.CompressedResourceReference | Removed (see above) |
| org.apache.wicket.markup.html.resources.JavascriptResourceReference | org.apache.wicket.request.resource.JavascriptResourceReference |
| org.apache.wicket.markup.html.image.Image.Image(String) | (this constructor is protected in 1.5, as replacement can be used NonCachingImage(String)) |
| org.apache.wicket.markup.html.image.resource.DynamicImageResource | org.apache.wicket.request.resource.DynamicImageResource |
| org.apache.wicket.util.convert.ConverterLocator | org.apache.wicket.ConverterLocator |
| org.apache.wicket.util.lang.Objects | org.apache.wicket.util.lang.WicketObjects |
| org.apache.wicket.protocol.http.WebApplication.sessionDestroyed(String) | org.apache.wicket.protocol.http.WebApplication.sessionUnbound(String) |
| org.apache.wicket.protocol.https.HttpsRequestCycleProcessor | org.apache.wicket.protocol.https.HttpsMapper |
| org.apache.wicket.protocol.http.WebRequestCycle.newBrowserInfoPage(String) | org.apache.wicket.protocol.http.WebSession.newBrowserInfoPage(String) |
| org.apache.wicket.protocol.http.WebRequestCycle.getClientInfo() | org.apache.wicket.protocol.http.WebSession.getClientInfo() |
| org.apache.wicket.protocol.http.WebApplication.newWebRequest(HttpServletRequest) | org.apache.wicket.protocol.http.WebApplication.newWebRequest(HttpServletRequest, String) |
| org.apache.wicket.protocol.http.WebApplication.newWebResponse(HttpServletResponse) | org.apache.wicket.protocol.http.WebApplication.newWebResponse(HttpServletRequest, HttpServletResponse) |
| org.apache.wicket.protocol.http.WebApplication.mount(String, Class) | org.apache.wicket.protocol.http.WebApplication.mountPage(String, Class) |
| org.apache.wicket.protocol.http.WebApplication.mountBookmarkablePage(String, Class) | org.apache.wicket.protocol.http.WebApplication.mountPage(String, Class) |
| org.apache.wicket.resource.loader.IStringResourceLoader.loadStringResource(Component, String) | org.apache.wicket.resource.loader.IStringResourceLoader.loadStringResource(Component, String, Locale, String, String) |
| org.apache.wicket.ajax.IAjaxCallDecorator.decorateScript(CharSequence) | org.apache.wicket.ajax.IAjaxCallDecorator.decorateScript(Component, CharSequence) |
| org.apache.wicket.IResponseFilter (and all default implementations) | org.apache.wicket.response.filter.IResponseFilter |
| org.apache.wicket.markup.html.form.Form.process() | org.apache.wicket.markup.html.form.Form.process(IFormSubmittingComponent) |
| org.apache.wicket.Application.addComponentInstantiationListener() | org.apache.wicket.Application.getComponentInstantiationListeners().add() |
| org.apache.wicket.markup.html.DynamicWebResource | org.apache.wicket.request.resource.ByteArrayResource |
| org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable.DataTable(String, IColumn\<T\>\[\], IDataProvider\<T\>, int) | org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable.DataTable(String, List\<IColumn\<T\>\>, IDataProvider\<T\>, int) |
| org.apache.wicket.validation.IValidatorAddListener | Validator.bind(Component) |
| org.apache.wicket.behavior.AbstractBehavior.renderHead(IHeaderResponse) | org.apache.wicket.behavior.Behavior.renderHead(Component, IHeaderResponse) |
| org.apache.wicket.protocol.http.request.CryptedUrlWebRequestCodingStrategy | org.apache.wicket.request.mapper.CryptoMapper |
| org.apache.wicket.util.template.PackagedTextTemplate | org.apache.wicket.util.template.PackageTextTemplate |
| org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByLink.onAjaxClick(AjaxRequestTarget) | org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByLink.onClick(AjaxRequestTarget) |
| org.apache.wicket.util.file.IFileUploadCleaner | org.apache.wicket.util.file.IFileCleaner |
| org.apache.wicket.markup.html.WebPage.configureResponse() | WebPage.configureResponse(WebResponse) |
| Page.onPageAttached() | IRequestCycleListener.onRequestTargetResolved -\> if (handler instanceof IPageRequestHandler) |
