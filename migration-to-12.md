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
