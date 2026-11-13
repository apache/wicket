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
# Reviewing a change to Apache Wicket

What a review of a pull request checks, whoever does it: a committer, a contributor or an agent.
The conventions for writing a change — format, commits, build, API compatibility, Javadoc,
comments, language and security — are in `AGENTS.md`. A review checks the change against those as
well; this file adds what is easy to miss when a change otherwise looks right.

Each section opens with when it applies. A review is done when every section the change
triggers is accounted for: confirmed, or a finding naming the file and line. A finding is
resolved in the change itself, in the same pull request. A finding that may be a vulnerability
is the exception: it never goes into a review comment, but to security@apache.org, as
`SECURITY.md` describes.

## License compliance

Applies when the change adds a file, adds or upgrades a dependency, or copies third-party material
into the source tree.

- A file that can't carry the ASF license header is listed in `.rat-excludes`, with a reason;
  RAT fails the build on any other file without one.
- A new dependency, or a new version of one, is under a license the ASF allows: category A, or
  category B, which may only be included in binary form, never as source copied into the tree.
  Category X licenses (GPL, AGPL, LGPL and their like) are never allowed. The categories are
  listed at https://www.apache.org/legal/resolved.html.
- Third-party code, scripts, styles, fonts or images copied into the source tree are listed in
  `LICENSE`, with the license text in `licenses/`. When they end up in a published jar, they are
  listed in `LICENSE-binary` as well. `NOTICE` changes only when the third party's own license or
  NOTICE file requires an attribution there.
- A Maven dependency is not bundled, so it needs no entry in these files, unless it is shaded or
  copied into a Wicket artifact.

## Security

Applies when the change writes a value into markup, JavaScript, a URL or a header; reads request
input; adds or changes a listener, a resource or a request mapper; or touches encryption, the page
store or authorization. `SECURITY.md` states what Wicket trusts: a change keeps to that model, or
changes `SECURITY.md` in the same pull request.

- A component escapes the model data it renders while `escapeModelStrings` is on. A bundle value
  is escaped for the context it is written into: `Strings#escapeMarkup` for markup,
  `JavaScriptUtils#escapeQuotes` for a JavaScript literal. `escapeMarkup` covers element text and
  quoted attribute values only; a value inside `<script>` or `<style>`, in an unquoted attribute
  or in a URL needs an encoding for that context.
- Request parameters, headers, cookies and uploaded file names are untrusted. The host, port and
  scheme come from the container, and `X-Forwarded-*` headers count only through
  `XForwardedRequestWrapperFactory`.
- A new listener is invoked through the request cycle, so `ResourceIsolationRequestCycleListener`
  and `Component#canCallListener` apply to it, and it runs only on an enabled, visible component.
- A resource served from the classpath passes `IPackageResourceGuard`, and a path built from
  request input stays inside the directory it was meant for.
- Encryption uses an authenticated scheme and a `SecureRandom`; `CryptoMapper` is the only place
  that encrypts deterministically. Keys, session ids and stored values stay out of logs and out of
  the error pages rendered in `DEPLOYMENT` mode.
- Nothing deserializes data a client can supply.
- A suspected vulnerability, in the change or in code it touches, is reported to
  security@apache.org and not mentioned in the pull request. A fix for an unreleased
  vulnerability is not prepared in a public pull request; see `SECURITY.md`.

## OSGi and JPMS

Applies when the change adds, moves, renames or removes a package, adds a dependency, or touches
`module-info.java` or an `osgi.*` property in a pom. Every published module is both an OSGi
bundle, built by `maven-bundle-plugin` from the `osgi.export.package` and `osgi.import.package`
properties in its pom, and a JPMS module, described by its `module-info.java`.

- A package that is added, moved, renamed or removed is exported or dropped in both places.
  `wicket-core` and `wicket-extensions` list their exported packages one by one, so a new package
  there isn't exported until it is added to the list; the other modules export by wildcard.
- A new dependency is a `requires` in `module-info.java`. An optional one is
  `<optional>true</optional>` in the pom and `requires static` in `module-info.java`; the bundle
  plugin then imports its packages with `resolution:=optional`. Check the `Import-Package`
  header of the built jar's manifest when in doubt.
- The import version ranges stay usable: a range that excludes a version applications commonly
  run breaks them in an OSGi container even when the build passes.

## Performance

Applies when the change touches a hot path: `Component` and `ComponentState`, `MarkupContainer`
children, rendering, markup parsing and loading, request mapping and handling, models, and page
serialization. Wicket walks the component tree for every page it renders and stores pages in the
session and the page store, so small costs in these classes multiply.

- A hot path keeps its time complexity and its memory footprint per component.
- A change that makes a hot path slower or larger says so in the commit message and justifies it,
  with numbers from `wicket-benchmarks` where it can be measured.
- A replacement for a library class — a collection, a cache, a parser — keeps the complexity of
  every operation Wicket relies on, not only the common ones.

## Serialization

Applies when a component, behavior or model gains or changes a field, or stores a lambda or
anonymous class. Pages are serialized into the page store, and with them every component, behavior
and model they hold.

- A field added to a component, behavior or model adds to the size of every stored page that
  holds one. A field that can be computed or loaded again belongs in a transient field or a
  detachable model.
- A change to the serialized form — a new field type, a different collection class, a changed
  `serialVersionUID` — means pages stored by the previous version can't be read back. Such a
  change lands in a major release on `master` and is described in the migration guide; a
  maintenance line keeps the serialized form it was released with.
- A lambda or anonymous class stored in a component has to be serializable; Wicket's lambda-based
  API takes the `Serializable*` interfaces for that reason.

## Accessibility

Applies when the change touches markup, a component or JavaScript that renders UI, examples and
test pages included. Components produce the HTML of every application that uses them, so their
accessibility is the applications' accessibility.

- New markup, components and JavaScript that render UI meet WCAG 2.2 at level AA. A change to
  existing UI leaves it at least as accessible as it was.
- Check in particular: form controls have labels, images have text alternatives, roles and ARIA
  attributes are correct and kept in sync with the state they describe, everything that works with
  a mouse works with a keyboard, focus is not lost or moved unexpectedly by an Ajax update, and
  changes announced only visually are also announced to assistive technology where that matters.
- Examples and test pages meet the same bar: they are what applications copy.

## Documentation

Applies when users notice the change — a new feature, a changed default, a removed or renamed
API, a change in behavior or in dependencies — or when the change makes existing documentation
wrong.

- A change users notice updates the Javadoc and the user guide in `wicket-user-guide`.
- A change that breaks or changes an existing application comes with its migration path, as
  `AGENTS.md` describes.
- Documentation that the change makes wrong is fixed, wherever it is: Javadoc, the user guide,
  `README.md`, the examples.

## Tests

Applies to every behavior change and every bug fix.

- A behavior change or a bug fix comes with a test that fails without it.
- The test is named and placed as `AGENTS.md` says under "Build and test" and "Where code goes".
