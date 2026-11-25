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
# Migration to Wicket 12

This guide lists the changes in Wicket 12.0.0 that break code written for Wicket 11, or deliberately change its intended behaviour, and what you have to do about them. Wicket 12 is in development, and the guide grows with every change that lands on master.

To come from Wicket 10, migrate to Wicket 11 first, with the [migration guide to Wicket 11](https://github.com/apache/wicket/blob/wicket-11.x/migration-to-11.md).

## Pages render `lang` and `dir` on `<html>`

`WebPage` now writes the language and direction of the page on its `<html>` tag, from the page locale: `lang` with the language tag of `getLocale()`, such as `nl-NL`, and `dir="rtl"` for a language written right to left, `dir="ltr"` for every other language. Screen readers need `lang` to pronounce the page (WCAG 2.2 success criterion 3.1.1), and browsers take the base direction only from `dir`, never from `lang`.

What the markup says is kept:

- A `lang` or `xml:lang` on `<html>` in your markup declares the language of the page. Wicket then writes neither `lang` nor `dir`.
- A `dir` in your markup is kept. Put `dir="ltr"` on `<html>` to keep a page left to right in every locale.
- An `<html wicket:id="...">` belongs to its component and is left alone, so a `TransparentWebMarkupContainer` that you bound to `<html>` to set `lang` keeps working. You can remove it.

Because the tag is now written from its attributes, the way component tags are, attribute values get double quotes, a valueless attribute such as `xmlns:wicket` becomes `xmlns:wicket=""`, and an `<html>` tag spread over several lines is written on one line.

If your tests compare rendered pages with expected files, update the `<html>` line of those files. `WicketTestCase` renders with `Locale.US`, which gives `<html lang="en-US" dir="ltr">`.

If your stylesheets don't support a right-to-left layout yet, an application with Arabic, Hebrew or Persian translations now shows those pages mirrored. Put `dir="ltr"` in the markup, or override `onHtmlTag` in your base page:

```java
@Override
protected void onHtmlTag(ComponentTag tag)
{
	super.onHtmlTag(tag);
	tag.remove("dir");
}
```

The same method adds other attributes that belong to the whole document, such as a theme class. To render neither `lang` nor `dir`, override it without calling `super`. The `<html>` tag is not rendered again on an Ajax request, so an attribute that has to change during one, such as a theme switched by an `AjaxLink`, has to be changed in the browser with JavaScript as well.

## Development mode markup ids separate the component id from the sequence

In development mode, `DefaultMarkupIdGenerator` now puts an underscore between the component id and the hexadecimal sequence number ([#1951](https://github.com/apache/wicket/issues/1951), WICKET-7114). A link with the id `link` now gets the markup id `link_1` where it got `link1`. Before, nothing separated the two parts, so two components could get the same id: component `1` with sequence `1a` and component `11` with sequence `a` both got `id11a`. Repeater children have numeric ids, so this happened in list views, and the duplicate id made an Ajax update replace the wrong element.

Deployment mode is unchanged: ids are still `id` followed by the sequence, such as `id1a`.

If your tests compare rendered pages with expected files, or look components up by a generated markup id written into the test, update those ids. A markup id you set yourself with `setMarkupId` is not affected.

## Markup encoding from `<meta charset>`

Wicket now reads the character encoding of a markup file from an HTML5 `<meta charset="...">` tag, or from `<meta http-equiv="Content-Type" content="text/html; charset=...">`, when the file has no XML declaration ([#1953](https://github.com/apache/wicket/issues/1953)). Before, it only looked at `<?xml version="1.0" encoding="..."?>` and otherwise read the file with `MarkupSettings#getDefaultMarkupEncoding()`, or the JVM default when that is not set.

Like a browser, Wicket looks for the meta tag in the first 1024 bytes of the file, outside of comments, and takes the first one that names a charset the JVM supports. An XML declaration still wins over a meta tag, and a file with neither is still read with the default encoding. The meta tag stays in the rendered page.

A markup file whose meta tag names a different encoding than the default is now read in the encoding of its meta tag. If its characters looked right before, the file is most likely not saved in the encoding its meta tag declares: save it in that encoding, or correct the tag.

A markup file with a meta charset no longer counts as having no encoding declared, so `MarkupSettings#setThrowExceptionOnMissingXmlDeclaration(true)` no longer throws for it.

## Markup is parsed the way HTML parses it

The markup parser now reads attributes, `<script>` and `<style>` the way the HTML tokenizer does ([#1864](https://github.com/apache/wicket/issues/1864)). Markup that parsed before can now give different attributes, and that shows in every tag Wicket writes from its parsed attributes: component tags, tags changed by a markup filter, and the `<html>` tag of a page.

Whitespace inside quotes is now part of an attribute value, including Wicket's own attributes. `wicket:id="label "` refers to a component with the id `label `, so a page whose markup has stray spaces in `wicket:id`, `wicket:message` or `wicket:for` fails with "Unable to find component with id" when it renders, and `<wicket:message key="foo ">` looks up the key `foo `. Remove the spaces from the markup. For other attributes the spaces are now rendered: `class=" a "` used to come out as `class="a"`.

The other changes:

- An unquoted value runs up to the next whitespace. `<a href=foo/bar.html>` used to give `href="foo"` and a stray attribute `bar.html`; it now gives `href="foo/bar.html"`.
- An attribute name keeps every character HTML allows, so `(click)`, `[value]`, `#ref`, `*ngIf` and `on:click|once` are rendered as written instead of stripped or split.
- Text in a tag that is not an attribute is kept the way HTML recovers it: `<tag a="1" "junk">` gets an attribute named `"junk"`. A duplicate attribute is still an error.
- A backslash no longer escapes a quote. `title="C:\dir\"` is the value `C:\dir\`; markup that used `\"` inside a quoted value now has the value end at that quote.
- The body of a `<script>` with an empty type, a JavaScript MIME type such as `application/javascript`, or a JSON type such as `application/ld+json` is raw text and passed through untouched. Before, such a script was parsed as a template, so a Wicket component inside one is no longer found. Scripts of other types, such as `text/x-handlebars-template`, are still templates.
- A custom element whose name only starts with `script` or `style`, such as `<script-loader>`, is parsed as markup. Before, its content was skipped up to the next `</script>` or `</style>`, Wicket components included.

## `ModalDialog` is accessible by default

`ModalDialog` now follows the [WAI-ARIA pattern of a modal dialog](https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/) ([#1927](https://github.com/apache/wicket/issues/1927)). Before, it was a plain `<div>` that screen readers did not announce as a dialog, and focus handling and Escape only worked after `trapFocus()` and `closeOnEscape()`.

These changes need nothing from you:

- The dialog element, the one with the class `modal-dialog`, renders `role="dialog"`, `aria-modal="true"` and a markup id. A `role` or `aria-modal` that your markup sets, or an `AttributeModifier` that you add in `newDialog`, is kept, so the workaround of setting them yourself keeps working and can go. If your tests compare rendered pages with expected files, the dialog `<div>` of an open dialog has these attributes now.
- When the dialog opens, the focus moves into it: to an element with `autofocus`, else to its first element that can be focused, else to the dialog itself. When it closes, the focus goes back to the element that had it before. A dialog's content that moves the focus itself, or a `focusComponent` on the `AjaxRequestTarget`, still decides where it goes.
- `labelledBy(Component)` names the dialog after a component in it, such as the heading of its content, and `label(IModel<String>)` names a dialog without a visible title. Without either, the dialog has no accessible name, as before.

These change what users see, and can be switched off:

- Tab and Shift+Tab cycle through the dialog. Before, Tab moved on from the last element of the dialog to the page behind the overlay, unless you called `trapFocus()`. `trapFocus(false)` gives the old behaviour back. `trapFocus()` still adds a `TrapFocusBehavior`, which in addition pulls the focus back into the dialog when it moves outside, for example into a popup that a widget renders at the end of the page's body.
- Escape closes the dialog, through `close(AjaxRequestTarget)`. Before, it did only after `closeOnEscape()`, which is now the default and can stay. A dialog that has to stay open until the user makes a choice calls `closeOnEscape(false)`. So does a dialog whose Cancel button does more than `close()`, unless you move that work into an override of `close(AjaxRequestTarget)`.
- Escape closes only the innermost open dialog, also when no element has the focus, and a dialog no longer stops the propagation of every key pressed in it. The request that Escape sends is no longer bound to a `keydown` event of the overlay: `postProcessCloseBehaviorAjaxAttributes` now gets its attributes for every open dialog that closes on Escape, and `attrs.event` is the keyboard event in the browser.
- An open dialog that closes on Escape has an Ajax behavior, so a stateless page that renders one open becomes stateful. `closeOnEscape(false)` keeps it stateless.

## Dependencies

Wicket's modules no longer bring in libraries they don't use themselves. If your application or its tests use one of these, declare it as a dependency of your own.

| Module | No longer brings in |
|---|---|
| `wicket-tester`, `wicket-extensions-tester`, `wicket-native-websocket-tester` | AssertJ (`org.assertj:assertj-core`), the JUnit Jupiter engine (`org.junit.jupiter:junit-jupiter-engine`) and JUnit Jupiter params (`org.junit.jupiter:junit-jupiter-params`). They bring in only `org.junit.jupiter:junit-jupiter-api`. |
| `wicket-ioc`, and through it `wicket-spring` and `wicket-guice` | ASM (`org.ow2.asm:asm`) |

Check that your build declares a JUnit engine in test scope, `org.junit.jupiter:junit-jupiter-engine` or `org.junit.jupiter:junit-jupiter`. Without one, Maven Surefire finds no tests to run and the build still passes. Projects created from the quickstart archetype declare the engine.

## `IPageAndComponentProvider#hasComponent()`

`IPageAndComponentProvider` has a new method without a default implementation, `hasComponent()` ([#1653](https://github.com/apache/wicket/issues/1653)). Wicket's `PageAndComponentProvider` implements it, so a provider that extends that class needs no change. A class of your own that implements the interface directly no longer compiles until it implements the method.

Return `true` only when `getComponent()` can return the component without creating the page or running any of its lifecycle callbacks: because the provider has resolved the component already, or because a page instance it holds already contains it. Return `false` otherwise. Wicket asks while it collects the log data of a listener request, after the response has been written, and only calls `getComponent()` when the answer is `true`; with `false`, the log data has no component class, behavior class or submitting component.
