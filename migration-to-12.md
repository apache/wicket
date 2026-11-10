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
