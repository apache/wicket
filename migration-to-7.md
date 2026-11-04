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
# Migration to Wicket 7

## Migrating to Wicket 7.0

#### Environment

##### Wicket 7.0 requires at least Java 7

##### Wicket 7.0 requires Servlet 3.0

###### The HTTP response is not flushed automatically when the request is started in asynchronous mode. [WICKET-5152](https://issues.apache.org/jira/browse/WICKET-5152)

This is an improvement in Wicket 7.0 which should not affect any application migrating from Wicket 6.x. But if you use Servlet 3.0's AsyncContext in IResource in Wicket 7 then make sure that your code flushes the http response after completing the request.

#### API changes

###### Changes to org.apache.wicket.ajax.json.\* [WICKET-6287](https://issues.apache.org/jira/browse/WICKET-6287) - Switch from [json.org](http://json.org) to open-json RESOLVED

Because of license issues all classes in the mentioned package have been replaced by classes of open-json (<https://github.com/tdunning/open-json>) which means that the basic functionality is nearly the same. Some classes however (example: org.json.HTTP) are throwing WicketRuntimeExceptions because there is no corresponding implementation in open-json, yet. json.org can be used in this case (can be found in the central maven repository) if the license conditions are accepted.

###### DecimalConverters' number format [WICKET-5853](https://issues.apache.org/jira/browse/WICKET-5853) [WICKET-5861](https://issues.apache.org/jira/browse/WICKET-5861)

org.apache.wicket.util.convert.converter.AbstractDecimalConverter's #getNumberFormat() and #setNumberFormat() has been removed in favour of #newNumberFormat(). Applications using the old methods should override the new one, where they configure specifics or preserve a reference to the created NumberFormat.

###### org/apache/wicket/core/util/io/SerializableChecker is replaced with org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream [WICKET-4817](https://issues.apache.org/jira/browse/WICKET-4817)

CheckingObjectOutputStream accepts a list of org.apache.wicket.core.util.objects.checker.IObjectChecker objects which are used to check for different kind of problems during object serialization. Commit diff: [d0441059e0](https://git-wip-us.apache.org/repos/asf/wicket/repo?p=wicket.git;a=commitdiff;h=d0441059e0bba325f3f68a5db00282cc3901dc5a)

Use org.apache.wicket.core.util.objects.checker.ObjectSerializationChecker as a replacement.

###### org/apache/wicket/markup/html/border/BoxBorder is removed with no replacement [WICKET-4966](https://issues.apache.org/jira/browse/WICKET-4966).

BoxBorder class has been deprecated in Wicket 6.x series

###### org.apache.wicket.Component#renderHead(HtmlHeaderContainer) is renamed to Component#internalRenderHead(HtmlHeaderContainer) [WICKET-4964](https://issues.apache.org/jira/browse/WICKET-4964)

Component#renderHead(HtmlHeaderContainer) was very similar to the usually used Component#renderHead(IHeaderResponse). So it has been renamed to avoid any confusions.

###### org.apache.wicket.request.resource.ResourceReference#getDependencies() now returns a mutable List\<HeaderItem\> [WICKET-5124](https://issues.apache.org/jira/browse/WICKET-5124)

The old API returned Iterable\<? extends HeaderItem\> and was inconvenient to override and add additional dependencies.\
With the new API it is as easy as:

*MyResourceReference.java*

```java
@Override
public List<HeaderItem> getDependencies() {
  List<HeaderItem> dependencies = super.getDependencies();
  dependencies.add(dep1);
  dependencies.add(dep2);
  return dependencies;
}
```

The same is done for org.apache.wicket.markup.head.HeaderItem#getDependencies() too.

###### org.apache.wicket.model.StringResourceModel interpolation of null in resourceKey [WICKET-5820](https://issues.apache.org/jira/browse/WICKET-5820)

If a null value is interpolated into the resourceKey, it will now be represented as 'null' instead of the variableName. Thus if you had "weather.${status}=..." in your resource file, you now should use "weather.null=..." instead.

###### org.apache.wicket.model.StringResourceModel constructor accepts IModel\<String\> for its default value [WICKET-4972](https://issues.apache.org/jira/browse/WICKET-4972)

There were two problems with the old way:

- since the *parameters* argument type is Object..., i.e. varargs, it was hard for the compiler and runtime to differentiate the "defaultValue" from the "parameters"
- it wasn't possible to use lazy evaluation of the default value
- the amount of parameters for StringResourceModel resulted in rather cryptic code indecipherable for mere mortals.

So we opted to remove most of the constructors of StringResourceModel and use a fluent API instead. For ease of migration you can use the StringResourceModelMigration class to alleviate the pain of figuring out the right way to construct your instance using the new fluent API.

Change the `new StringResourceModel(...)` constructor call to an invocation of `StringResourceModelMigration.of(...)`, and then perform the *Inline Method* refactoring of your IDE.

###### org.apache.wicket.extensions.validation.validator.+Xyz+Validator#decorate() now works with *IValidationError* [WICKET-5174](https://issues.apache.org/jira/browse/WICKET-5174)

This method now accepts IValidationError as an argument and returns IValidationError. This way it can be used with other IValidationError implementations like RawValidationError for example.

###### org.apache.wicket.ajax.attributes.AjaxRequestAttributes#setAllowDefault() is deprecated in favour of #setPreventDefault() [WICKET-5197](https://issues.apache.org/jira/browse/WICKET-5197)

This is done to make it more consistent with JavaScript API.

Same for #isAllowDefault() - use #isPreventDefault() instead.

###### org.apache.wicket.ajax.attributes.AjaxRequestAttributes#eventPropagation is now BUBBLE by default [WICKET-5198](https://issues.apache.org/jira/browse/WICKET-5198)

This is done to make it more consistent with JavaScript API.

###### org.apache.wicket.ajax.form.AjaxFormValidatingBehavior doesn't use static methods anymore [WICKET-5196](https://issues.apache.org/jira/browse/WICKET-5196)

To use AjaxFormValidatingBehavior in Wicket 6.x the application code should do something like:

*MyComponent.java*

```java
AjaxFormValidatingBehavior.addToAllFormComponents(form, "keydown", Duration.ONE_SECOND);
```

Due to the usage of static method it wasn't possible to extend this behavior and override for example #updateAjaxAttributes(AjaxRequestAttributes).

The behavior has been reworked a bit to allow this. The new usage is:

*MyComponent.java*

```java
form.add(new AjaxFormValidatingBehavior("keydown", Duration.ONE_SECOND);
```

or

*MyComponent.java*

```java
formComponent.add(new AjaxFormValidatingBehavior("keydown", Duration.ONE_SECOND);
```

in this case the formComponent's owning Form will be used.

###### org.apache.wicket.ajax.AjaxRequestTarget.IListener#updateAjaxAttributes() is introduced [WICKET-5212](https://issues.apache.org/jira/browse/WICKET-5212)

With [WICKET-4958](https://issues.apache.org/jira/browse/WICKET-4958) has been introduced org.apache.wicket.ajax.AjaxRequestTarget.AbstractListener which is an empty implementation of AjaxRequestTarget.IListener to be able to introduce AbstractListener#updateAjaxAttributes() method - a place where an application can configure globally the Ajax attributes. With [WICKET-5212](https://issues.apache.org/jira/browse/WICKET-5212) this new method is moved to the interface as well.\
From now on it is recommended all implementations of AjaxRequestTarget.IListener to extend from AjaxRequestTarget.AbstractListener. This way they won't need to a implement another method when a new one is added in a major release.

###### org.apache.wicket.ISessionListener#onUnbound(String) is added [WICKET-5213](https://issues.apache.org/jira/browse/WICKET-5213)

This way all registered ISessionListeners will be notified when a Session is unbound.

###### (Base)WicketTester uses org.junit.Assert instead of junit.framework.Assert

junit.framework.Assert is deprecated. Due to this change org.junit.Assert#fail() throws now java.lang.AssertionError instead of junit.framework.AseertionFailedError

###### (Base)WicketTester can be reused after thrown exception

If an exception is thrown during processing in BaseWicketTester or the request was not processed by Wicket, you have to use #getLastRequest() to access this request (was #getRequest() previously).\
[WICKET-5389](https://issues.apache.org/jira/browse/WICKET-5389).

###### WicketTester assert feedback message methods work with java.io.Serializable [WICKET-5128](https://issues.apache.org/jira/browse/WICKET-5128)

org.apache.wicket.util.tester.WicketTester#assertErrorMessages and org.apache.wicket.util.tester.WicketTester#assertInfoMessages now accept a list/varargs of java.io.Serializable as argument, instead of list of Strings.

This makes these methods consistent with org.apache.wicket.Component#error and other feedback message related methods.

Whatever type of message you pass to Component#error() the same message type can be asserted on.

###### org.apache.wicket.Component#setMetaData() value could be only a Serializable [WICKET-5227](https://issues.apache.org/jira/browse/WICKET-5227)

Component and Session's meta data must be serializable.

###### org.apache.wicket.Application#getHeaderContributorListenerCollection() is renamed to #getHeaderContributorListeners()

This way it is consistent with the other #getXyzListeners() methods in Application class

###### org.apache.wicket.authorization.IAuthorizationStrategy#isResourceAuthorized(IResource, PageParameters) is introduced [WICKET-5012](https://issues.apache.org/jira/browse/WICKET-5012)

Wicket 7.x supports authorization for requests to resources (IResource) additionally to authorization for components.

###### org.apache.wicket.Component#setResponsePage() now accepts org.apache.wicket.request.component.IRequestablePage instead of org.apache.wicket.Page

org.apache.wicket.Component#setResponsePage(page) just delegates to org.apache.wicket.request.cycle.RequestCycle#setResponsePage() that required just IRequestablePage.

###### AutoCompleteTextField doesn't support *useHideShowCoveredIEFix* setting [WICKET-5130](https://issues.apache.org/jira/browse/WICKET-5130)

*useHideShowCoveredIEFix* setting was needed for IE7 support. This version of IE is not supported officially anymore and this setting has been removed.

###### org.apache.wicket.markup.html.form.TextField#getInputType() returns String array [WICKET-5286](https://issues.apache.org/jira/browse/WICKET-5286)

org.apache.wicket.extensions.markup.html.form.DateTextField can be used with several date-like types: date, datetime, datetime-local, month, time, week.\
To be able to support this the return type of TextField#getInputType() has been changed from String to String\[\], and the method itself has been renamed to #getInputTypes().

To migrate it you should do something like:

*OldMyTextField.java*

```java
...
@Override
protected String getInputType()
{
	return "sometype";
}
...
```

to

*NewMyTextField.java*

```java
...
@Override
protected String[] getInputTypes()
{
	return new String[] { "sometype" };
}
...
```

###### AuthenticatedWebSession#authenticate is now protected instead of public. [WICKET-5308](https://issues.apache.org/jira/browse/WICKET-5308)

No change in user code is required, but the authenticate method can now be made protected to avoid confusion with the signIn method.

###### AbstractResource.ResourceResponse API became more fluent. [WICKET-5330](https://issues.apache.org/jira/browse/WICKET-5330)

All setter methods in AbstractResource.ResourceResponse class now will return *this* instance for method chaining.

###### org.apache.wicket.markup.head.IHeaderItemWrapper is merged to IWrappedHeaderItem. [WICKET-5366](https://issues.apache.org/jira/browse/WICKET-5366)

IHeaderItemWrapper was a temporary class used to add a new method (#wrap(HeaderItem)) without breaking the APIs in Wicket 6.x.\
The new method is moved its proper class - IWrappedHeaderItem.

###### All 'QuirkXyz' properties has been removed from org.apache.wicket.protocol.http.ClientProperties [WICKET-5370](https://issues.apache.org/jira/browse/WICKET-5370)

The 'quirk' properties were mostly related to IE6/7 and old versions of Firefox browsers.

###### org.apache.wicket.request.resource.AbstractResource#flushResponseAfterHeaders() is removed [WICKET-5392](https://issues.apache.org/jira/browse/WICKET-5392)

This method was needed as a workaround for some old version of Firefox that had an issue with rendering images.The flush of the headers caused problems with proper buffering of the response, e.g. when the response needs to be compressed.

###### org.apache.wicket.markup.html.IPackageResourceGuard#acceptAbsolutePath(Class, String) now without scope parameter

The scope parameter was no longer used already, so it is now removed from the method signature. The method is renamed to IPackageResourceGuard#accept(String)

###### All IXyzSettings are removed [WICKET-5410](https://issues.apache.org/jira/browse/WICKET-5410)

Keywords: IApplicationSettings, IDebugSettings, IExceptionSettings, IFrameworkSettings, IJavaScriptLibrarySettings, IMarkupSettings, IPageSettings, IRequestCycleSettings, IRequestLoggerSettings, IResourceSettings, ISecuritySettings, IStoreSettings, IWebSocketSettings

All interfaces related to configuration settings have been removed because the user application usually call setter methods like *application.getXyzSettings().setAbc(abc)* and don't provide their own implementations of these interfaces.\
Additionally by using interfaces it is not possible to add new settings in a minor release of Wicket because it is considered as an API break.

To fix any compilation issues just remove the leading **I** from the class name, e.g. IMarkupSettings -\> MarkupSettings, and optimize the imports in your IDE.

###### org.apache.wicket.request.resource.caching.version.IResourceVersion#getVersionPattern() is added [WICKET-5446](https://issues.apache.org/jira/browse/WICKET-5446)

This method makes the API more flexible when extracting the version from the url.

The pattern is used to check whether a candidate String could be a version.

To allow any pattern this method can return null or Pattern.compile(".\*" )

###### add org.apache.wicket.markup.html.form.IChoiceRenderer#getObject(String idValue, IModel\<? extends List\<? extends T\>\> choices) method [WICKET-663](https://issues.apache.org/jira/browse/WICKET-663)

This method should return the object that produces *idValue* when org.apache.wicket.markup.html.form.IChoiceRenderer#getIdValue(T object, int index) is called.

###### remove org.apache.wicket.request.mapper.mount.MountMapper [WICKET-4686](https://issues.apache.org/jira/browse/WICKET-4686)

MountMapper and its related classes (IMountedRequestMapper, Mount, MountParameters, UnmountedMapperAdapter and UnmountedRequestHandlerAdapter) have been introduced with Wicket 1.5.0.

Its main task was to deal with the mounting of a delegate IRequestMapper at a given path. Unfortunately it has been used only for wrapping PackageMapper and at some point the most used mapper - MountedMapper, used to mount page class - has been improved to deal with mandatory and optional parameters. To fix WICKET-4686 (support mandatory and optional parameters in PackageMapper) we needed to extract the related logic to a place where it can be reused. We decided this place to be AbstractBookmarkableMapper - the parent class for all bookmarkable mappers.

If you have used MountMapper for your custom mappers in Wicket 1.5/6.x then now your custom mapper has to extend from AbstractBookmarkableMapper.

**Note**: MountMapper and its related classes are still available in 7.x but deprecated and not used by Wicket itself. They will be completely removed in Wicket 8.0.0

**Renamed all \*AjaxResponse classes to \*PartialPageUpdate WICKET-5929**

XmlAjaxResponse was renamed to XmlPartialPageUpdate (see git commit 7c40e4dafa75973bf99b34567d977f310a5f02d0)

###### AuthenticatedWebApplication#onUnauthorizedPage [WICKET-5490](https://issues.apache.org/jira/browse/WICKET-5490)

The type of the parameter passed to AuthenticatedWebApplication#onUnauthorizedPage() has changed from org.apache.wicket.Page to org.apache.wicket.Component because this is an instance of a page that is partially constructed. Only the constructor of org.apache.wicket.Component is fully executed. All super constructors are not executed and the instance is not really a Page.

###### PropertyColumn#createLabelModel has been renamed to getDataModel [WICKET-4802](https://issues.apache.org/jira/browse/WICKET-4802) [WICKET-5201](https://issues.apache.org/jira/browse/WICKET-5201)

The property column from wicket-extensions has some methods renamed to better reflect their meaning.

###### #onSelectionChanged() use generic type as argument

RadioGroup and RadioChoice use their generic type for the argument of #onSelectionChanged() now.

###### Palette and Wizard use \<div\>s instead of \<table\> markup [WICKET-5349](https://issues.apache.org/jira/browse/WICKET-5349)

For these components markup is changed from \<table\> to nested \<div\>s. Wizard's previous "default" CSS styling is no longer supported, Palette offers an optional styling via org.apache.wicket.extensions.markup.html.form.palette.theme.DefaultTheme.

###### Use i18n property files for CSS class names [WICKET-5257](https://issues.apache.org/jira/browse/WICKET-5257)

Until Wicket 7.0.0 some components have been setting hardcoded CSS class names (e.g. FeedbackPanel), other were using a overridable Behavior to set the needed class names (e.g. OrderByLink's ICssProvider).

Since Wicket 7.0.0 this is simplified and now the applications can provide their own CSS class names when needed by overriding some keys in their i18n properties files (e.g. in MyApplication.properties).

Here is a list of the default keys and their values:

FeedbackMessage.CSS.undefined=feedbackPanelUNDEFINED\
FeedbackMessage.CSS.debug=feedbackPanelDEBUG\
FeedbackMessage.CSS.info=feedbackPanelINFO\
FeedbackMessage.CSS.success=feedbackPanelSUCCESS\
FeedbackMessage.CSS.warning=feedbackPanelWARNING\
FeedbackMessage.CSS.error=feedbackPanelERROR\
FeedbackMessage.CSS.fatal=feedbackPanelFATAL

FormComponentLabel.CSS.required=required\
FormComponentLabel.CSS.invalid=error\
FormComponentLabel.CSS.disabled=disabled

OddEvenItem.CSS.odd=odd\
OddEvenItem.CSS.even=even

OddEvenListItem.CSS.odd=odd\
OddEvenListItem.CSS.even=even

AutoLabel.CSS.required=required\
AutoLabel.CSS.invalid=error\
AutoLabel.CSS.disabled=disabled

OrderByLink.CSS.ascending=wicket_orderUp\
OrderByLink.CSS.descending=wicket_orderDown\
OrderByLink.CSS.none=wicket_orderNone

TabbedPanel.CSS.container=tab-row\
TabbedPanel.CSS.selected=selected\
TabbedPanel.CSS.last=last

Node.CSS.expanded=tree-junction-expanded\
Node.CSS.collapsed=tree-junction-collapsed\
Node.CSS.other=tree-junction

Folder.CSS.other=tree-folder-other\
Folder.CSS.closed=tree-folder-closed\
Folder.CSS.open=tree-folder-open\
Folder.CSS.selected=selected

###### Generics related improvements in the signature of methods and constructors with collections [WICKET-5350](https://issues.apache.org/jira/browse/WICKET-5350)

Components' methods and constructors which use collections have been updated to follow best practices. List of updated components:

AbstractChoice, CheckGroup, FileUploadField, ListView, PageableListView, PropertyListView, AbstractPageableView, RefreshingView, ListDataProvider, AjaxEditableChoiceLabel, Palette, SelectOption, SelectOptions, ChoiceFilter, ChoiceFilteredPropertyColumn

Model#of(Collection), Model#ofList(List) and Model#ofSet(Set) have been adjusted to return collections without wildcards.

###### Make FormComponent#convertInput() public [WICKET-5708](https://issues.apache.org/jira/browse/WICKET-5708)

The visibility of org.apache.wicket.markup.html.form.FormComponent#convertInput() method has been changed from \<em\>protected\</em\> to \<em\>public\</em\> to allow application components and behaviors to convert the input of another form component(s) during custom form (component) submit processing.

###### Use commons-fileupload as Maven dependency instead of keeping copies of its classes [WICKET-5503](https://issues.apache.org/jira/browse/WICKET-5503)

All classes from package org.apache.wicket.util.upload.\*\* are now in org.apache.commons.fileupload.\*\*.

For example FileItem and FileUploadException.

###### Rename PageSettings#recreateMountedPagesAfterExpiry to #recreateBookmarkablePagesAfterExpiry [WICKET-5829](https://issues.apache.org/jira/browse/WICKET-5829)

###### Rename IStaticCacheableResource#getCacheableResourceStream() to #getResourceStream() [WICKET-5909](https://issues.apache.org/jira/browse/WICKET-5909)

#### Behavior changes

###### org.apache.wicket.request.Url#getQueryString [WICKET-4664](https://issues.apache.org/jira/browse/WICKET-4664)

Url#getQueryString() now behaves as HttpServletRequest behaves :

- returns the query string without the leading "?"
- returns null when there is no query string

RequestUtils#decodeParameters() now strips the "?" from the output value.

###### org.apache.wicket.request.http.WebResponse encodes the value of the "filename" in Content-Disposition header [WICKET-4934](https://issues.apache.org/jira/browse/WICKET-4934)

The value of the file name used in "Content-Disposition" response header can contain characters which should be [encoded](http://greenbytes.de/tech/tc2231/)

When serving file with name 'fileN**ä**me.txt' with content disposition *attachment* (or *inline*) Wicket will put a response header like:

Content-Disposition", "attachment; fileN%C3%A4me.txt"; filename\*=UTF-8''fileN%C3%A4me.txt

The value is not very human readable but this is the best way to show the file name in cross browser compatible way.

###### org.apache.wicket.markup.html.form.FormComponentLabel outputs "required", "disabled" and "error" classes when its form component is either required, disabled or invalid. [WICKET-5177](https://issues.apache.org/jira/browse/WICKET-5177)

This way it is in sync with AutoLabel (the auto component that is used for "wicket:for" attribute).

###### org.apache.wicket.markup.html.panel.FeedbackPanel Do not set CSS class on the li \> span element for a feedback message [WICKET-4831](https://issues.apache.org/jira/browse/WICKET-4831)

The css class is now only applied to the li element and not to the span inside. Additionally a FeedbackPanel#newMessageItem() method has been added to allow customization of each feedback message item (similar to DataTable#newRowItem()).

###### AjaxEventBehavior doesn't prevent the default behavior of the JavaScript event [WICKET-5197](https://issues.apache.org/jira/browse/WICKET-5197)

From now on only AjaxFallback\*\* components prevent the default JavaScript event behavior so only the Ajax call is made when JavaScript is enabled in the browser.\
If the default behavior should be prevented in any use case then use:

```java
attributes.setPreventDefault(true);
```

###### Ajax behaviors let JavaScript events bubble by default [WICKET-5198](https://issues.apache.org/jira/browse/WICKET-5198)

If JavaScript events should not bubble then use:

```java
attributes.setEventPropagation(EventPropagation.STOP);
```

or:

```java
attributes.setEventPropagation(EventPropagation.STOP_IMMEDIATE);
```

###### PackageTextTemplate loads its content lazily [WICKET-4579](https://issues.apache.org/jira/browse/WICKET-4579)

PackageTextTemplate will load its content at first usage of PackageTextTemplate#toString() or PackageTextTemplate#interpolate(Map) methods.\
If its style/locale/variation/encoding are changed then it will reload its content.

###### AbstractLink no longer alters its markup to render itself as disabled [WICKET-4904](https://issues.apache.org/jira/browse/WICKET-4904)

The href and disabled attributes are still added/removed by AbstractLink as required. But no further mangling of markup is performed. org.apache.wicket.markup.html.link.DisabledLinkBehavior can be used to restore the pre-Wicket-7.x representation of disabled links with \<em\>\<span\>\</span\>\</em\> (note that \<em\> and \<span\> swap their position though). If the old behavior is need it can be easily re-applied by using org.apache.wicket.markup.html.link.DisabledLinkBehavior.LinkInstantiationListener.

Hence "setAfterDisabledLink(String)" and "setBeforeDisabledLink(String)" have been removed from AbstractLink.

"isLinkEnabled()" has been removed as well. Use "isEnabledInHierarchy()" as replacement.

###### RadioChoice and CheckBoxMultipleChoice no longer append \<br/\> as suffix [WICKET-5640](https://issues.apache.org/jira/browse/WICKET-5640)

org.apache.wicket.markup.html.form.RadioChoice and org.apache.wicket.markup.html.form.CheckBoxMultipleChoice use empty prefix and suffix by default from now on.

It is recommended to use CSS for styling them.

To revert the old behavior for easier migration the application could use org.apache.wicket.markup.html.form.RadioChoiceWicket6Listener and org.apache.wicket.markup.html.form.CheckBoxMultipleChoiceWicket6Listener.

These component instantiation listeners are marked as deprecated and will be removed with Wicket 8.0.

###### Button no longer silently ignores exceptions [WICKET-5235](https://issues.apache.org/jira/browse/WICKET-5235)

... when it renders its model into the value attribute.

###### Improved AbstractAjaxTimerBehavior [WICKET-5439](https://issues.apache.org/jira/browse/WICKET-5439)

AbstractAjaxTimerBehavior no longer updates its hosting component when restarted. Stopping and restarting is now possible on non-Ajax requests too.

###### Check and Radio no longer render CSS class [WICKET-5476](https://issues.apache.org/jira/browse/WICKET-5476)

Check and Radio no longer generade a CSS class attribute, encoding their group's markup id. Wicket no longer needs this - if you do, you can easily write a suitable attribute value by yourself.

###### FileParts must be parsed explicitly before trying to read them [WICKET-5839](https://issues.apache.org/jira/browse/WICKET-5839)

If a MultipartWebRequest is created manually then FileParts must be parsed explicitly with #parseFileParts(). See <http://wicketinaction.com/2012/11/uploading-files-to-wicket-iresource/> for example.

#### Deprecated classes/methods/fields are removed [WICKET-5201](https://issues.apache.org/jira/browse/WICKET-5201)

###### AbstractDefaultAjaxBehavior's #getSuccessHandler(), #getFailureHandler(), #getChannel() and #getPrecondition() are removed.

They were deprecated since 6.0.0 and were there only for backward compatibility. Use #updateAjaxAttributes() to configure the same functionalities.

###### The old Tree component in wicket-extensions is removed.

It was based on Swing APIs and many people complained about this. Use the new Tree component introduced in Wicket 6.0.0 instead.

###### Wicket.Window.unloadConfirmation has been removed [WICKET-5246](https://issues.apache.org/jira/browse/WICKET-5246)

In Wicket 6 a new setting has been added to ModalWindow - org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow#showUnloadConfirmation(). In Wicket 7 the new Java API should be used instead of dealing with the JavaScript property *Wicket.Window.unloadConfirmation*.

###### AllVisitFilter has been removed.

Use IVisitFilter.ANY instead.

###### Label constructor with String argument has been removed [WICKET-5201](https://issues.apache.org/jira/browse/WICKET-5201)

Use Serializable argument instead.

###### RangeValidator deprecated keys removed

Use RangeValidator.\* instead of MinimumValidator and MaximumValidator.

###### InlineFrame uses IPageProvider [WICKET-5347](https://issues.apache.org/jira/browse/WICKET-5347)

IPageLink has been removed because it was superseded by IPageProvider. Its last usage has been removed from InlineFrame

###### org.apache.wicket.markup.html.form.EnumChoiceRenderer#postprocess() is removed [WICKET-5470](https://issues.apache.org/jira/browse/WICKET-5470)

WICKET-5470 has shown that org.apache.wicket.markup.html.form.EnumChoiceRenderer#postprocess() is not really needed and the same can be achieved by overriding #getDisplayValue() method and doing the processing after calling super.getDisplayValue().

Additionally now the specializations of EnumChoiceRenderer can provide completely custom implementation of #getDisplayValue().

#### Using keyboard UP and DOWN keys will loop thru the AutoCompleter's options [WICKET-5275](https://issues.apache.org/jira/browse/WICKET-5275)

If the currently selected item is the first one and the user uses KEY_UP then the selection will move to the last option. Similarly for KEY_DOWN - if the selected option is the last one then using DOWN key will move the selection to the first one.

#### Form#findSubmittingComponent() doesn't throw exceptions for invisible/disabled submitters [WICKET-5672](https://issues.apache.org/jira/browse/WICKET-5672)

The check whether the form submitter is invisible or disabled is moved to Form#onFormSubmitted(). Now the applications can change the visible/enabled state of the submitter in #onSubmit() implementations and later use Form#findSubmittingComponent() in the application code. It is now responsibility of the application to check whether the submitting component is visible/enabled or not and react on this appropriately.

#### Dependency updates

All libraries on which Wicket modules depend are updated to their latest stable versions.\
The most notable ones are:

- Spring Framework 4.1.x
- Guice 4.0
- ASM 5.x
- CGLIB 3.1
- SLF4J 1.7.x
- Jetty (in the quickstart) 9.2.x
