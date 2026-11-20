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

This guide lists the changes in Wicket 12.0.0 that break code written for Wicket 11, or change what a running application does, and what you have to do about them. Wicket 12 is in development, and the guide grows with every change that lands on master.

To come from Wicket 10, migrate to Wicket 11 first, with the [migration guide to Wicket 11](https://github.com/apache/wicket/blob/wicket-11.x/migration-to-11.md).

## `PageParameters` in hash-based collections

`PageParameters.hashCode()` now agrees with `equals()`, which ignores the order of the named parameters ([#1866](https://github.com/apache/wicket/issues/1866)). The fix is backported to Wicket 11.1.0 and 10.13.0, so an application already on one of those sees no change. Before, two `PageParameters` that differed only in that order were equal but had different hashes, so a `HashMap` or `HashSet` kept them as two keys.

This shows in rendered pages. When a page renders a CSS or JavaScript reference header item twice for the same resource, with the same page parameters added in a different order, it now gets one `<link>` or `<script>` tag, with the URL of the first item. Before, it got two. An application that keeps `PageParameters` in a hash-based collection of its own now finds reordered parameters under the same entry.

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

## Markup encoding from `<meta charset>`

Wicket now reads the character encoding of a markup file from an HTML5 `<meta charset="...">` tag, or from `<meta http-equiv="Content-Type" content="text/html; charset=...">`, when the file has no XML declaration ([#1953](https://github.com/apache/wicket/issues/1953)). Before, it only looked at `<?xml version="1.0" encoding="..."?>` and otherwise read the file with `MarkupSettings#getDefaultMarkupEncoding()`, or the JVM default when that is not set.

Like a browser, Wicket looks for the meta tag in the first 1024 bytes of the file, outside of comments, and takes the first one that names a charset the JVM supports. An XML declaration still wins over a meta tag, and a file with neither is still read with the default encoding. The meta tag stays in the rendered page.

A markup file whose meta tag names a different encoding than the default is now read in the encoding of its meta tag. If its characters looked right before, the file is most likely not saved in the encoding its meta tag declares: save it in that encoding, or correct the tag.

A markup file with a meta charset no longer counts as having no encoding declared, so `MarkupSettings#setThrowExceptionOnMissingXmlDeclaration(true)` no longer throws for it.
