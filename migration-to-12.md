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
