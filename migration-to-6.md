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
# Migration to Wicket 6

## Migrating to Wicket 6.0

#### Environment

- Wicket 6.0 requires at least Java 6

#### Repeaters

- \`IDataProvider\` was converted to using \`long\` instead of \`int\` to better line up with JPA and other persistence frameworks. This has caused a rather large cascade of \`int\` to \`long\` changes all over the repeater packages ([WICKET-1175](https://issues.apache.org/jira/browse/WICKET-1175)).
- All classes in wicket-extensions which are related to repeaters with sorting behavior have improved generics\
  related to the sorting property ([WICKET-4535](https://issues.apache.org/jira/browse/WICKET-4535)). In most cases you just have to add '**, String**' to the previous parameterized type. For example: *IColumn\<Entity\>* becomes *IColumn\<Entity, String\>*. But now it is possible to use other type for the sort property than java.lang.String too.

#### Form Processing

##### Validation

- \`StringValidator\` will add the \`maxlength\` attribute if added to a component attached to an input tag
- `MinimumValidator` and `MaximumValidator` are replaced by `RangeValidator.minimum` and `RangeValidator.maximum` respectively
- \`AbstractValidator\` has been removed
- \`ValidationError\` now makes creating standard error keys (classname and classname.subtype) easier. Before one had to do: new ValidationError().addMessageKey(validator.getClass().getSimpleName()) to construct the default key, now you can simply say new ValidationError(validator) and it will do the above for you. Also, for more specific keys one had to do new ValidationError().addMessageKey(validator.getClass().getSimpleName()+".variant"), now you can do new ValidationError(validator, "variant").
- Most validators provide a \`decorate(ValidationError, Validatable)\` method for overriding how they report errors. For example, to add an extra resource key for the error message to StringValidator one can do

  ```java
  class MyStringValidator extends StringValidator {
      ValidationError decorate(ValidationError error, IValidatable validatable) {
         error.addKey("mystringerror");
         return error;
      }
  }
  ```
- org.apache.wicket.validation.RawValidationError is introduced. It can be used to bring a raw Serializable object which is registered as FeedbackMessage with org.apache.wicket.Component#error(Serializable).
- IFormValidator can have its own resource bundle as other IValidator implementations. [WICKET-3879](https://issues.apache.org/jira/browse/WICKET-3879)
- CheckBox never fails required check: [WICKET-1221](https://issues.apache.org/jira/browse/WICKET-1221)

##### Feedback Storage Refactoring

- Feedback messages reported against components are now stored in component's metadata rather then in session. This will allow them to survive across requests, see WICKET-2705.
- Previously the cleanup of messages was controlled by RequestCycle#cleanupFeedbackMessages(), now it is controlled by an IFeedbackMessageFilter set via IApplicationSettings#setFeedbackMessageCleanupFilter(). When session and components are detached they will run this filter on any stored messages, all messages accepted by the filter are removed. The default filter will accept any rendered message, so by default feedback messages are removed once rendered.
- Form components will clear any stored messages when validate() is called. This will remove any unredered, but now stale, messages.
- Since messages are now stored in session and in component tree retrieving them is not as simple as Session.getFeedbackMessages(). To assist with this common usecase please see FeedbackCollector.
- In test land cleanup of feedback messages was controlled by RequestCycle#set/isCleanupFeedbackMessagesOnDetach(), but this method is now deprecated. Instead, in testland you can now call WicketTester#cleanupFeedbackMessages() to clean up message just like it would be done at the end of a request. Alternatively, WicketTester#clearFeedbackMessages() can be used to clear all feedback messages. By default WicketTester will not clean any feedback messages.

##### Feedback messages now use ${label} instead of ${input}

You should use form component.setLabel(...) to provide a good (internationalized) label for your form components such as text fields. If no label is set on your form fields, Wicket will use the component identifier as the label. This will change the feedback messages from:

- '12345a' is not a valid integer

To:

- 'Number' is not a valid integer

Provided that the field has a 'Number' label.

##### added onAfterSubmit to Buttons and SubmitLinks

The call order of onSubmit in Buttons vs Forms was inconsistent in 1.4, and fixed at "Button always comes first" in 1.5. Sometimes it is desirable to have the Button's onSubmit running after the Form's, however. In 6.0, `IFormSubmitter.onSubmit` is still fixed at "button first". In addition to that, there is now `.onAfterSubmit`. Your submitting component can implement either one of them or even both. Both will be called after the validation and form model update, just like before. The only difference is that you can now pick whether something should happen before or after `Form.onSubmit`.

#### Ajax

##### Use JQuery as a backing library for Wicket Ajax functionality

Apache Wicket needed to improve the implementation of its JavaScript libraries used for Ajax functionality (wicket-ajax.js and wicket-event.js) by using any of the bigger JavaScript libraries and delegate to it the handling of the differences in the browsers (DOM, events, Ajax, ...). After a discussion in the mailing lists the Wicket team decided to use JQuery for that.\
For more information read [Wicket Ajax](https://cwiki.apache.org/confluence/x/NK2oAQ)

##### AjaxRequestTarget is an interface

o.a.w.ajax.AjaxRequestTarget is an interface now with a default implementation o.a.w.ajax.AjaxRequestHandler. This way it will be possible to replace it with a different implementation, or mock/spy it in tests.\
This change required to refactor AjaxRequestTarget.get() too. The replacement code is:

```java
  AjaxRequestTarget target = requestCycle.find(AjaxRequestTarget.class);
```

RequestCycle.get().find(Class\<? extends IRequestHandler\>) can be used to find the currently running or a scheduled IRequestHandler for other types too.

#### IHeaderResponse, including decorators and filters

IHeaderResponse has been rewritten to render HeaderItems. All render\* methods have been replaced by a single render(HeaderItem) method. HeaderItems can be instantiated using the factory methods in JavaScriptHeaderItem, CssHeaderItem, OnDomReadyHeaderItem, OnLoadHeaderItem, OnEventHeaderItem and StringHeaderItem. For example, the following code in renderHead:

```java
  response.renderCSSReference(new CssResourceReference(HomePage.class, "header.css"));
  response.renderJavaScriptReference(new JavaScriptResourceReference(HomePage.class, "page.js"));
  response.renderOnDomReadyJavaScript("foobar");
```

Needs to be replaced with:

```java
  response.render(CssHeaderItem.forReference(new CssResourceReference(HomePage.class, "header.css")));
  response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(HomePage.class, "page.js")));
  response.render(OnDomReadyHeaderItem.forScript("foobar));
```

Custom HeaderResponseDecorators and IHeaderResponseFilters also need to be adjusted for the changed methods. These classes now have only one method for rendering and filtering.

#### Order of Header contributions and wicket:head

The order of header contributions (including wicket:head) has changed with Wicket 6.0. By default components are visited child-first to contribute to the header. For every component, the header contribution from the markup (wicket:head) is rendered first, followed by header contributions done in the code. This means that headers contributed by the page are rendered at the bottom of the header, where they used to be at the top with Wicket 1.5.\
This way if the user application uses a component library and a component (or Page) from the library contributes some resource the user application has the possibility to override this contribution with its own one.

#### package.properties renamed to wicket-package.properties

The special resource bundle that can contain resources for a whole package has been renamed from package.properties to wicket-package.properties. This prefix will be used for all wicket resources which may collide somehow with resources provided by other frameworks. See [WICKET-4211](https://issues.apache.org/jira/browse/WICKET-4211)

#### List of renamed classes and methods

Following the renames in subclasses is easier if the @Override annotations are present. IDE-s, like Eclipse, can be used to add missing @Override annotations before moving to wicket 6.

Here's a table of deprecated classes and methods in Wicket 1.5 and the corresponding replacement in Wicket 6.

| Wicket 1.5 (deprecated) | Wicket 6 (replacement) |
|---|---|
| org.apache.wicket.behavior.SimpleAttributeModifier | org.apache.wicket.AttributeModifier#replace(...) |
| org.apache.wicket.request.Url#toAbsoluteString() | org.apache.wicket.request.Url#toString(StringMode.FULL) |
| org.apache.wicket.markup.html.IHeaderResponse | org.apache.wicket.markup.head.IHeaderResponse |
| org.apache.wicket.markup.html.internal.HeaderResponse | org.apache.wicket.markup.head.internal.HeaderResponse |
| org.apache.wicket.resource.filtering.HeaderResponseContainerFilteringHeaderResponse | org.apache.wicket.markup.head.filter.FilteringHeaderResponse |
| org.apache.wicket.resource.filtering.HeaderResponseFilteredResponseContainer | org.apache.wicket.markup.head.filter.HeaderResponseContainer |
| org.apache.wicket.resource.filtering.\* | org.apache.wicket.markup.head.filter.\* |
| org.apache.wicket.markup.html.tree.\* | org.apache.wicket.extensions.markup.html.repeater.tree.\* |
| org.apache.wicket.extensions.markup.html.tree.\* | org.apache.wicket.extensions.markup.html.repeater.tree.\* |
| org.apache.wicket.request.mapper.\*\* | org.apache.wicket.core.request.mapper.\*\* (OSGi friendly) |
| org.apache.wicket.request.handler.\*\* | org.apache.wicket.core.request.handler.\*\* (OSGi friendly) |
| org.apache.wicket.util.io.\*\* | org.apache.wicket.core.util.io.\*\* (OSGi friendly) |
| org.apache.wicket.util.crypt.\*\* | org.apache.wicket.core.util.crypt.\*\* (OSGi friendly) |
| org.apache.wicket.util.file.\*\* | org.apache.wicket.core.util.file.\*\* (OSGi friendly) |
| org.apache.wicket.util.lang.\*\* | org.apache.wicket.core.util.lang.\*\* |
| org.apache.wicket.util.resource.\*\* | org.apache.wicket.core.util.resource.\*\* (OSGi friendly) |
| org.apache.wicket.util.string.\*\* | org.apache.wicket.core.util.string.\*\* (OSGi friendly) |
| org.apache.wicket.request.UrlEncoder | org.apache.wicket.util.encoding.UrlEncoder |
| org.apache.wicket.request.UrlDecoder | org.apache.wicket.util.encoding.UrlDecoder |
| org.apache.wicket.util.IClusterable | org.apache.wicket.util.io.IClusterable |

#### Refactorings

- HttpsMapper has been refactored to make subclassing easier

#### o.a.w.IComponentSource and o.a.w.ComponentSourceEntry are removed

These classes were used as placeholders for further optimizations in Component's size but were never finished.

#### o.a.w.Component#onMarkupAttached() has been dropped in favour of o.a.w.Component#onInitialize().

Both methods had the same semantics. #onMarkupAttached() was the one less used and thus got dropped.

#### IInitializers are initialized before Application#init()

All IInitializer classes configured in wicket.properties are initialized **before** the call to Application#init(). This way the application has the control to re-configure something that comes from external library. [WICKET-4088](https://issues.apache.org/jira/browse/WICKET-4088)

#### PackageResourceReference can load a minified version of a package resource

PackageResourceReference is improved to be able to load a minified/compressed version of a resource.\
By default in development mode the non-minimized resource is loaded (*resource.ext*) while in production mode it will try to load *resource<b>.min</b>.ext* by automatically inserting .min right before the extension. If there is no such resource with that name then it will fall back to the non-minified version.\
This behavior can be configured with org.apache.wicket.settings.IResourceSettings#setUseMinifiedResources(boolean).

#### ResourceStreamLocator and ResourceFinder have been cleaned up

The responsibilities of ResourceStreamLocator and IResourceFinder have been untangled and cleaned.

- ResourceStreamLocator now purely generates pathnames using a ResourceNameIterator and then uses a list of IResourceFinders to actually get those resources.
- ResourceStreamLocator no longer does resource loading from the classpath on its own. Instead, there is now ClassPathResourceFinder.
- IResourceSettings no longer contains just a single ResourceFinder but rather a list of them that will be tried in the given order.
- Path and WebApplicationPath no longer extend each other, and also no longer have a list of prefixes, only a single one. If you need several path prefixes, simply add more Paths, WebApplicationPaths or other IResourceFinder implementations to IResourceSettings#resourcesFinders.
- The IResourcePath interface was removed, since each ResourceFinder now only has one path.
- replace getResourceSettings().addResourceFolder("some/path") with getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), "some/path));

#### Changes to org.apache.wicket.ajax.json.\* (since Wicket 6.27.0) [WICKET-6287](https://issues.apache.org/jira/browse/WICKET-6287) - Switch from [json.org](http://json.org/) to open-json RESOLVED

Because of license issues all classes in the mentioned package have been replaced by classes of open-json (<https://github.com/tdunning/open-json>) which means that the basic functionality is nearly the same. Some classes however (example: org.json.HTTP) are throwing WicketRuntimeExceptions because there is no corresponding implementation in open-json, yet. [json.org](http://json.org) can be used in this case (can be found in the central maven repository) if the license conditions are accepted.

#### Minor changes

- Files#getLocalFileFromUrl(URL) decodes url before returning file
- WizardStep header labels encode their model values by default, override WizardStep#getHeader() if you need the old behavior (i.e. model values not encoded)
- IErrorMessageSource#substitute(Map\<String,Object\>) was merged into IErrorMessageSource#getMessage(String, Map\<String,Object\>)
- org.apache.wicket.util.resource.IResourceStreamWriter#write() now works again with java.io.OutputStream as in Wicket 1.4.x. [WICKET-4601](https://issues.apache.org/jira/browse/WICKET-4601)
- WicketTester.startPanel -\> WicketTester.startComponentInPage
- WebSession#authenticate() was moved to AuthenticatedWebSession and now abstract
- ChoiceFilteredPropertyColumn/ChoiceFilter use DropDownChoice#setNullValid(true) now
