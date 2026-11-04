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
# Migration to Wicket 1.3

1.3 is the current stable release.

## Migrating to Wicket 1.3

> [!NOTE]
> **Getting it**
>
> <http://wicket.apache.org/getting-wicket.html> explains how to obtain Wicket 1.3.x.

#### Package rename

Due to Wicket incubating at Apache, we applied a package rename for all core projects. Instead of wicket.\* we now use org.apache.wicket.\*. The <http://wicket.sourceforge.net> namescape declarations should be changed to <http://wicket.apache.org>.

#### Filter instead of a Servlet

The recommended set-up for Wicket now uses a servlet-api Filter, not a servlet-api Servlet (although a Servlet is still provided for use in environments that do not support a Filter).

Replace code like this (in subclasses of WebApplication):

```java
ServletContext sc = getWicketServlet().getServletContext();
```

with

```java
ServletContext sc = getServletContext();
```

and

```java
wicket.protocol.http.IWebApplicationFactory#createApplication(wicket.protocol.http.WicketServlet)
```

is replaced by

```java
wicket.protocol.http.IWebApplicationFactory#createApplication(wicket.protocol.http.WicketFilter)
```

You can get the servlet context from a filter like this:

```java
filter.getFilterConfig().getServletContext()
```

The main advantage of working with a filter instead of a servlet is that it is easier to pass through resources, and map your application to the root.

Here's an example of how to configure your application now with a filter in web.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE web-app
      PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
      "http://java.sun.com/dtd/web-app_2_3.dtd">

<web-app>
  <filter>
    <filter-name>MyApplication</filter-name>
    <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>
    <init-param>
      <param-name>applicationClassName</param-name>
      <param-value>com.myapp.MyApplication</param-value>
    </init-param>
  </filter>

  <filter-mapping>
    <filter-name>MyApplication</filter-name>
    <url-pattern>/app/*</url-pattern>
  </filter-mapping>
</web-app>
```

#### API changes

##### IModel change

IModel#getObject(Component) is replaced by IModel#getObject() and\
IModel#setObject(Component, Object) is replaced by IModel#setObject(Object)

Here are some regex replacement that are helpful:

```java
s/public\s+Object\s+getObject\s*\(\s*(final)*\s*(wicket.)?Component\s+\w+\s*\)/public Object getObject()
```

and

```java
s/public\s+void\s+setObject\s*\(\s*(final)*\s*(wicket.)?Component\s+\w+,\s*(final)*\s*Object\s+(\w+)\s*\)/public void setObject(Object \2)
```

You're probably best off doing the calls to get/setObject you did on models yourself manually.

Some miscellaneous abstract model classes have been removed as they did not provide much value. Here is a simple migration mapping if you extended one of the removed classes in your code:

```java
AbstractModel->Model
AbstractReadOnlyDetachableModel->LoadableDetachableModel (or other detachable models that don't need to have a setObject method)
```

if you do need the Component in the model (see below the IAssignementAwareModel) then there is a quick model

```java
ComponentModel
```

that is pretty\
much a drop in replacement for the current model implementation you have. If that model was a DetacheableModel then you can use the

```java
ComponentDetachableModel
```

as a replacement. Then onAttach -\> attach, onDetach -\> detach, onSetObject is just setObject and onGetObject is just getObject\
wicket.extensions.model.AbstractCheckBoxModel now has select/ unselect instead of setSelected(Component, boolean), and isSelected doesn't have the Component parameter anymore.

IWrapModel must be used as a return type of the 2 new Model interface markers IAssignementAwareModel and IInheritableModel both of these have a wrapOnXXXX method\
that gives you the component as a parameter. What those method should return is a new model that wraps the outer model (this) and the component it gets in an IWrapModel\
class instance so that it that combination can be used later on to resolve the object in get and set object. See below for an explanation of the 2 special interfaces.

IAssignementAwareModel is used now when the Model implementation needs an Component to resolve things in set/getObject() calls. Because now we don't get the\
component parameter in those 2 methods anymore the component is is "assigned" to that model when we see such a model being assigned to the component an example is ResourceModel\
It wants to do this in the getObject

```java
public Object getObject()
{
   return Application.get().getResourceSettings().getLocalizer().getString(resourceKey,component, defaultValue);
}
```

So ResourceModel implements now IComponentAssignedModel\
and then implements

```java
public IWrapModel wrapOnAssignment(final Component component)
{
   return new AssignmentWrapper(resourceKey, defaultValue, component);
}
```

That can be an innerclass that has reference to the component and the outer model (which is returned in the getWrappedModel() call of IWrapModel)\
So the above getObject that was in ResourceModel is now moved to the AssignmentWrapper.getObject() method and the getObject() of ResourceModel\
shouldn't be called anymore, so you could throw an exception in that getObject() method.

InheritableModel is the replacement of the ICompoundModel interface it works the same as the ICompoundModel so child components\
do inherit that model from a parent just like the ICompoundModel was inherited. The difference is now that instead of directly assigning the\
ICompoundModel (CompoundPropertyModel) to the component the wrapOnInheritance method is called on thos models, where you should do the same thing\
as with the IComponentAssignedModel so you wrap the component you get and the IInheritableModel in another model so that model has the component\
to get the property expression from (the id of the component)\
The IInheritableModel differs from the IAssignementAwareModel in the way that the get/setObject calls on the main model itself also should just work.\
Because there you set the main domain/pojo object where all the childs get one property value from.

Looking at the CompoundPropertyModel:

```java
public void setObject(Object object)
{
  this.target = object;
}

public IWrapModel wrapOnInheritance(Component component)
{
  return new AttachedCompoundPropertyModel(component);
}
```

as you can see the setObject just sets the target == main domain object, so you can do this:

```java
CompoundPropertyModel model = new CompoundPropertyModel();
Form form = new Form("form", model);
form.add(new TextField("name"));
// later on
model.setObject(new Person());
```

Also it wraps the component and the 'this' in an innerclass AttachedCompoundPropertyModel

```java
private class AttachedCompoundPropertyModel extends AbstractPropertyModel implements IWrapModel,
{
  private final Component owner;

  public AttachedCompoundPropertyModel(Component owner)
  {
    super(CompoundPropertyModel.this);
    this.owner = owner;
  }

  protected String propertyExpression()
  {
    return CompoundPropertyModel.this.propertyExpression(owner);
  }

  public IModel getWrappedModel()
  {
    return CompoundPropertyModel.this;
  }
```

That innerclass extends AbstractPropertyModel in this case and implements IWrapModel\
it overrides the propertyExpression method for the AbstractPropertyModel where it extracts\
the property from the component.\
It also implements getWrappedModel() from IWrapModel to return the outer class instance.

So to summarize if you have implemented your own ICompoundModel that is now the IInheritableModel and you need\
to implement the wrapOnInheritance method.\
If you have an own model that did use the Component parameter of the get/setObject calls you now should implement\
the IComponentAssignedModel and implement the wrapOnAssignment method and move what was in your current\
set/getObject code to that model you return there.

Also the BoundCompoundPropertyModel doesn't have to be used anymore now where you did this before:

```java
BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(new Person());
Form form = new Form("form", model);
TextField textField = new TextField("xxx");
form.add(textField);
model.bind("name",textfield);
```

can be replaced by

```java
CompoundPropertyModel model = new CompoundPropertyModel(new Person());
Form form = new Form("form", model);
form.add(new TextField("xxx", model.bind("name"));
```

##### PageParameters.

If storing strings in PageParameters, it was previously possible to refer to one by calling:

(String) pageParameters.get(keyValue)

Now, one must call:

pageParameters.getString(keyValue)

##### Static Images.

When using static images as resources, one could use

\<img wicket:id = "picture" alt="Picture" src="BagsPosition.png"/\>

in a page's HTML, and set the image dynamically using AttributeModifier in a WebComponent as follows:

WebComponent wmc = new WebComponent("picture");\
wmc.add( new AttributeModifier( "src", true, new Model(pictureFileName) ) );\
add( wmc );

Because of the change from a Wicket Servlet to a Wicket Filter, this will no longer work consistently. Instead, one should use ContextImage as follows:

ContextImage ci = new ContextImage("picture", new Model(pictureFileName) );

##### Component.onAttach/onBeforeRender/onAfterRender changes.

Before we had this behaviour:

new Page:

Page constructed\
Page.onAttach() -\> all the components are visited\
Page.onBeforeRender()\
Page.render -\>\
childComponent.onBeforeRender()\
childComponent.render()\
childComponent.onAfterRender()\
Page.onAfterRender()\
Page.onDetach() -\> all the components are visited

Listener request which sets a new page:

Page1.componentCalled() \>- Page2 is set to response.\
Page2.onAttach() -\> all the components are visited\
Page2.onBeforeRender()\
Page2.render -\>\
childComponent.onBeforeRender()\
childComponent.render()\
childComponent.onAfterRender()\
Page2.onAfterRender()\
Page2.onDetach() -\> all the components are visited\
Page1.onDetach() -\> all the components are visited

So the page1 ondetach is called but not the on attach\
in 1.3 on attach is changed so that when a page is used, for an interface call or rendering, onPageAttached is called.\
onBeforeRender did become onAttach because they where pretty much the same already.\
so now the behavior is this:

Page1.onPageAttached() -\> this is just called on page. Users can visit components if they need to\
Page1.componentCalled() \>- Page2 is set to response.\
Page2.onBeforeRender() -\> all the components are visited\
Page2.render()\
Page2.onAfterRender() -\> all the components are visited\
Page2.onDetach() -\> all the components are visited\
Page1.onDetach() -\> all the components are visited

The onAttach method on components is not available any more.\
So everything that was done before this change in onAttach should be done in onBeforeRender\
also in all the 3 call backs we have now (onBeforeRender,onAfterRender and onDetach()) you have\
to make a super.xxx call else you will get an exception.

##### Converters

IConverter is now more specific to what we're doing in a web environment. The signature is

```java
public interface IConverter extends IClusterable
{
	/**
	 * Converts the given string value to class c.
	 *
	 * @param value
	 *            The string value to convert
	 * @param locale
	 *            The locale used to convert the value
	 * @return The converted value
	 */
	Object convertToObject(String value, Locale locale);

	/**
	 * Converts the given value to a string.
	 *
	 * @param value
	 *            The value to convert
	 * @param locale
	 *            The locale used to convert the value
	 *
	 * @return The converted string value
	 */
	String convertToString(Object value, Locale locale);
}
```

The new converters make it much easier to write custom ones. SimpleConverterAdaptor and ITypeConverter are deleted as they are not needed anymore.

Wicket 1.2 had a default Converter implementation that actually served as a registry for converter instances. This is renamed to ConverterLocator and backed by interface IConverterLocator. The application's instance of the converter locator can be configured by overriding Application#newConverterLocator, which is called during startup.

Note that in 1.2.x, the validator was called first, then the converter; this was considered a bug. In 1.3 it's the other way around, so you may need to adjust your code if you do both conversion and validation on the same field.

##### Validation Changes

Form component level validation has been decoupled from FormComponent so that validators can be reused outside wicket. The new API can be found in wicket.validation package, with the validator implementations in wicket.validation.validator. From the point of view of validator development not much has changed if you extended the AbstractValidator; if you however implemented the IValidator interface directly you will need to use the new API, namely error reporting via ValidationError instead of FormComponent.error(List,Map). Errors with messages fully constructed inside the validator can still be reported using FormComponent.error(String).

EmailAddressPatternValidator has changed name to EmailAddressValidator. Make sure you update your resource keys appropriately.

The resource key "RequiredValidator", used to lookup the message by any component on which isRequired returns true, has changed to simply "Required". Mak sure you update your resource keys appropriately.

##### WicketTester and MockWebApplication

MockWebApplication and WicketTester are no longer derived from WebApplication which allows to use\
"MyApplication" for testing as well instead of copy & paste the MyApps code.

MockWebApplication and therefore WicketTester both consistently use factory methods to create requests and responses now. This allows for custom request and response subclasses to be used in testing.

Also, it is now possible to use WicketTester with a test framework other than JUnit, by extending BaseWicketTester where all most methods have been moved to.

##### Mounts

The path of a mount is now part of the IRequestTargetUrlCodingStrategy (String getMountPath()). See WICKET-410.

##### RequestCycle, IRequestCycleFactory, Session, ISessionFactory, IPageMap and PageMap changes

IRequestCycleFactory and ISessionFactory were removed from the code base. You now simply override

```java
public RequestCycle newRequestCycle(final Request request, final Response response)
```

and

```java
public Session newSession(Request request, Response response)
```

in your application to provide a custom request cycle or custom session respectively

RequestCycle's constructor now takes Application as its first parameter instead of Session.

##### Custom Sessions

Session's constructor signature is now:

```java
protected Session(Application application, Request request)
```

The locale is not set right after construction in WebApplication, but rather in the constructor using the passed in request. You can now "fix" the session's locale by setting it in its constructor.

Session.get() now lazily creates Sessions using the Application session factory and RequestCycle methods that work with Sessions now call Session.exists() and/or Session.get() directly. Finally, IPageMap and PageMap work in the same way and no longer hold a session reference.

##### Repeaters

The repeaters package has moved from wicket-extensions into core. The package name has been changed from wicket.extensions.markup.html.repeater to wicket.markup.repeater. Notice that only DataView and backing classes have moved, the higher level components such as the DataTable are still in wicket-extensions. Also notice that the names of classes have not changed so it should be a simple matter of ctrl-shift-o in your eclipse project to relink to the new classes.

##### TextTemplate

The TextTemplate package has moved from wicket-extensions into core. The package name has been changed from wicket.extensions.util.resource to org.apache.wicket.util.template. Notice that the names of classes have not changed so it should be a simple matter of ctrl-shift-o in your eclipse project to relink to the new classes.

##### DatePicker

The DatePicker component has been removed from the wicket-extensions package, as it conflicts with the Apache license.

There are two options for migration:

- A [drop-in replacement](http://wicketstuff.org/confluence/display/STUFFWIKI/wicket-contrib-datepicker) (same component) provided by Wicket Stuff.
- A new date picker based on the Yahoo UI library, which is available in the wicket-datetime package. Find it under wicket.extensions.yui.calendar.DateField (see also DateTimeField and CalendarPopup for other useful components).

##### Portlets JSR-168

The portlet support has been moved to wicket-stuff, as we couldn't support it inside the Wicket core. If you need portlet support please checkout the code from wicket-stuff and include that instead. The portlet examples also have been moved to Wicket stuff.

- [wicket-portlets](http://wicket-stuff.svn.sf.net/svnroot/wicket-stuff/branches/wicket-1.3/wicket-portlets)
- [wicket-portlet-examples](http://wicket-stuff.svn.sf.net/svnroot/wicket-stuff/branches/wicket-1.3/wicket-portlet-examples/)

Portlet support has recently been brought back into wicket core and will be available with release 1.3.0-beta4 and supersedes the old Wicket 1.2.x portlet support.\
Detailed information how to use the new portlet support will be provided shortly (stay tuned) but for now check out the umbrella [WICKET-647](http://issues.apache.org/jira/browse/WICKET-647) and [WICKET-983](http://issues.apache.org/jira/browse/WICKET-983) issues.

##### ISessionStore

ISessionStore had the following changes:\
String getSessionId(Request request); -\> String getSessionId(Request request, boolean create);\
\+ void onBeginRequest(Request request);\
\+ void onEndRequest(Request request);\
\+ PageMap createPageMap(String name, Session session);\
By default, the creation of lasting sessions is deferred until actually needed. As long no lasting session is yet created and users are accessing stateless pages, a temporary session object is used for the current request.

HttpSessionStore and all subclasses now require the application which they are created for to be passed in in the constructor.

##### Button

AjaxSubmitButton and AjaxSubmitLink now extend Button and Button extends IFormSubmittingComponent. As a result of this, method onSubmit changed from protected to public

The Button component name is a little bit confusing, because it should be used only when the INPUT tag is contained inside a form, otherwise you should use Link:

- when using

  ```xml
  <input type="submit"/>
  ```

  you should use Button.
- when using

  ```xml
  <input type="button"/>
  ```

  or

  ```xml
  <button>MyButton</button>
  ```

  which are not contained in a form - you should use Link.

  > There has been some initiative to rename Button to SubmitButton to\
  > better reflect what it does. but we didn't want to break the API. We can also\
  > consider renaming Link to Callback, but that is too abstract.

##### IHeaderContributor

void IHeaderContributor.renderHead(final Response response); -\> void IHeaderContributor.renderHead(final IHeaderResponse response);\
This resulted in a couple of cascading changes, like methods onRenderHeadContribution and onRenderHeadInitContribution not being used anymore. Note that the filtering of duplicate contributions is now part of IHeaderResponse.\
A common fix is this:

```java
protected void onRenderHeadInitContribution(Response response) {
    writeJsReference(response, AUTOCOMPLETE_JS);
  }
```

should be converted to:

```java
public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(AUTOCOMPLETE_JS);
  }
```

or for instance code like

```java
protected String getImplementationId() {
  return "ArchiveActions";
}

protected void onRenderHeadContribution(Response response) {
  if (!isComplete()) {
    response.write("<script>");
    response.write(getCallbackScript().toString());
    response.write("</script>");
  }
}
```

would be rewritten like

```java
public void renderHead(IHeaderResponse response) {
  if (!isComplete()) {
    response.renderJavascript(getCallbackScript(), "ArchiveActions");
  }
}
```

##### IBehavior

IBehavior.rendered(Component) -\> IBehavior.afterRender(Component)

IBehavior.detachModel(Component) -\> IBehavior.detach(Component)

##### Replacement for getBodyContainer

If you had code like this:

```java
public void renderHead(HtmlHeaderContainer container) {
  ((WebPage) getPage()).getBodyContainer().addOnLoadModifier("foo();", null);
  super.renderHead(container);
}
```

you should instead let your component implement IHeaderContributor, and then in the interface method, do:

```java
public void renderHead(IHeaderResponse response) {
  response.renderOnLoadJavascript("foo();");
}
```

##### FormComponent.IVisitor

Things have been refactored into interfaces here. If you previously implemented this directly, extend FormComponent.AbstractVisitor instead, and rename your formComponent() implementation to onFormComponent().

##### ClientProperties

wicket.protocol.http.ClientProperties was taken from the Echo2 project that uses a license which is incompatible with ASL2. It has therefore been rewritten and the usage of it has also changed.

Instead of:

```java
WebClientInfo clientInfo = (WebClientInfo) Session.get().getClientInfo();
ClientProperties properties = clientInfo.getProperties();

// Before constants where used to get properties
System.out.println(properties.get(ClientProperties.BROWSER_INTERNET_EXPLORER));
```

You now say:

```java
WebClientInfo clientInfo = (WebClientInfo) Session.get().getClientInfo();
ClientProperties properties = clientInfo.getProperties();

// Now you use a property with that name instead
System.out.println(properties.isBrowserInternetExplorer());
```

##### Application settings

Application#getSettings is now private (so you can't call nor override that anymore), and the getXxxSettings are now overridable in case you want to do fancy stuff like creating a session-dependent settings object. Methods getApplicationPages and getApplicationSettings from component are now removed in favor of the getXxxSettings methods in Application.

Setting get/setDefaultLocale is removed and is does not have a replacement.

##### Custom resource loading

In Wicket 1.2 you could override method newMarkupResourceStream from MarkupContainer to provide a custom resource stream for loading the component's markup. The new way of letting markup containers provide custom markup is to let them implement interface IMarkupResourceStreamProvider and implement it's method getMarkupResourceStream. Additionally, a new feature is that you can provide your own markup cache key, which is used in the MarkupCache class. The only real use case for that is to let a markup container return a cache key that is null, in which case the resource won't be cached, causing Wicket to get call getMarkupResourceStream everytime the component's markup is requested. A use case for that is when you have dynamic markup (e.g. from a database) that is request/ session dependent. To achieve this, let your markup container also implement IMarkupCacheKeyProvider and let method getCacheKey return null.

##### Tree components

The tree components from wicket and wicket-extensions are switched. The one previously in extensions (ajax enabled etc) is now in core and is the recommended component to work with, and the old one is moved to the extensions project. The package names got switched as well.

##### Test serialization setting removed

The setting IDebugSettings#SerializeSessionAttributes is removed. Instead you can use the SecondLevelCacheSessionStore, which serialized (old) pages to a second level cache (user.tmp by default). SecondLevelCacheSessionStore will (probably) be the default configured session store, but you can configure it yourself by doing in your application:

```java
/**
 * @see wicket.Application#newSessionStore()
 */
protected ISessionStore newSessionStore() {
  return new SecondLevelCacheSessionStore(new FilePageStore());
}
```

Also, you can use

```java
wicket.util.Objects#checkSerializable(Object)
```

to check for non-serializable objects yourself. You could do this for instance in a custom session store implementation.

##### Palette component in wicket-extensions now has default CSS.

You can revert this back to 1.2.x behaviour by overriding getCSS() and returning null.

##### IRequestCycleProcessor got simplified

The interface itself didn't change, but we've pulled out a redundant layer of indirection that was confusing more than it helped matters. There is now a simply class hierarchy, and WebRequestCycleProcessor is the class you'll most likely need. See for more info the JIRA issue at <http://issues.apache.org/jira/browse/WICKET-287>

##### Removed IPageSettings#maxPageVersions

The IPageSettings#maxPageVersions is removed from 1.3. The setting is not relevant for SecondLevelCacheSessionStore (the new default session store implementation), which uses SecondLevelCachePageVersionManager. If you use HttpSessionManager or a custom one together with UndoPageVersionManager, you can provide max page versions number as a UndoPageVersionManager's constructor argument. By default this is now 20 (opposed to max int which it was earlier). See also <http://issues.apache.org/jira/browse/WICKET-266>

##### Simplified HeaderContributor

Removed instance methods of HeaderContributor and now just keep one IHeaderContributor instance for clarity and efficiency.

##### Removed feedback messages from page

All feedback messages are now maintained by the session. See <https://issues.apache.org/jira/browse/WICKET-442>

##### Slight change in the handling of multivalued widgets

ListMultipleChoice and Palette now call model.setObject() after changing the list elements in updateModel()

##### Made it easier to add IMarkupFilter to MarkupParser

The MarkupParser can no longer be re-used for multiple markup files. You have to get a new instance from IMarkupParserFactory for every markup file to be parsed (which Wicket-core did already anyway). The MarkupParser API has been changed to make that clear as well.\
This has the advantage that initializing the default set of markup filters now happens in the MarkupParsers constructor and that adding your own filter is now just a matter of invoking appendMarkupFilter() once you created a new MarkupParser. Creating a new MarkupParser happens in MarkupParserFactory (no change) which is registered with Settings (IMarkupSettings). But MarkupParserFactory has changed to reflect the change in MarkupParser. MarkupParserFactory.newMarkupParser(resource) gets you a new MarkupParser. In order add your own IMarkupFilter whenever Wicket requests a parser, you must subclass MarkupParserFactory, substitute newMarkupParser and register your own MarkupParserFactory with Settings.

```java
public class MyMarkupParserFactory
{
  ...
  public MarkupParser newMarkupParser(MarkupResouceStream resource)
  {
     MarkupParser parser = new MarkupParser(resource);
     parser.appendMarkupFilter(new MyFilter());
     return parser;
  }
}
```

In order to allow better control over where in the chain of IMarkupFilter to place your own one, appendMarkupFilter has got a second parameter "markupFilterClass". Your own IMarkupFilter will inserted before the one identified by the parameter.

##### Logging API Change

The Logging API changed from Commons Logging to SLF4J, so you need to update your POM in order to use it:\
e.g:\
\*if you are using commons-logging:

```xml
<dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-jcl</artifactId>
     <version>1.1.0</version>
</dependency>
```

\*if you are using log4j

```xml
<dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.1.0</version>
</dependency>
```

##### Application#configure() DEPLOYMENT/DEVELOPMENT changes

In 1.2, you should have configured your app for DEPLOYMENT/DEVELOPMENT mode using any of a System property ("wicket.configuration"), a servlet-specific init-param or a servlet context init-param. This will still work exactly as before. You may have previously called Application#configure(String configurationType) to provide your own way of setting this. This previously resulted in #configure() being called twice (one with the system-derived param, and once with your own). This is no longer the case. You can override Application#getConfigurationType() to provide the configuration string, and configure() will be called correctly and only once internally by Wicket. This provides greater flexibility for configuring the default application type settings via Spring or any other mechanism you choose.

##### HtmlBodyContainer \<body\> gone

Wicket no longer automatically creates a WebMarkupContainer for \<body\>, thus \<body\> is no longer treated any different than any other HTML tag.

##### PasswordTextField no longer supports cookies by default

Calling `setPersistent(true)` on a PasswordTextField will now yield an exception (`FormComponent PasswordTextField does not support cookies`) due to security issues. Overload the method `supportsPersistence` to get the old behavior:

```java
add(password = new PasswordTextField("password", new PropertyModel(properties, "password")) {
    protected boolean supportsPersistence() {
        return true;
    }
});
```

#### New Features

##### wicket:enclosure tag

see <http://www.nabble.com/two-small-feature-ideas-tf2107229.html#a5835972>

##### Reloading class loader

see ReloadingClassLoader

##### Relative URLs

Wicket now generates all its URLs as relative paths, which means it works with zero-config behind a proxy server.

ApplicationSettings.get/setContextPath() is therefore no longer required or available.

The contextPath servlet init parameter that configured this in 1.2.x is therefore also no longer required/honoured.

If you were previously using PrependContextPathHandler, this has been removed and is no longer required (relative path prepending is included by default for all your markup).
