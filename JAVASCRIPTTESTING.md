# JavaScript testing

Wicket's client-side Ajax/event support (`Wicket.Ajax`, `Wicket.Event`, `Wicket.DOM`,
`Wicket.Form`, `Wicket.Head`, `Wicket.Focus`, ...) has two implementations:

- `wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax-jquery.js` - the
  default, jQuery-based engine.
- `wicket-core/src/main/java/org/apache/wicket/ajax/res/js/wicket-ajax.js` - a jQuery-free,
  plain-JavaScript engine exposing the exact same public API
  (`getJavaScriptLibrarySettings().setWicketAjaxReference(WicketAjaxVanillaResourceReference.get())`).

Both are exercised by the same shared QUnit test suite under `wicket-core/src/test/js/`, run
via the `testing/wicket-js-tests` module (Grunt + Puppeteer/headless Chrome).

## Running via Maven (recommended)

The JS tests are behind the `js-test` Maven profile, which is **not active by default** -
pass `-Pjs-test` explicitly:

```bash
cd /path/to/wicket

# just this module
mvn -pl testing/wicket-js-tests -Pjs-test test

# as part of a full build
mvn -Pjs-test test
```

This uses `frontend-maven-plugin` (configured in
`testing/wicket-js-tests/pom.xml`) to:

1. Download a local Node.js v24.15.0 / npm 11.12.1 into the module (separate from any
   system Node/npm) - `install-node-and-npm` goal.
2. `npm install` - pulls in Grunt, `grunt-contrib-qunit`, `grunt-contrib-jshint`,
   `grunt-contrib-connect`, Puppeteer, etc.
3. `grunt` - runs the default Gruntfile task: `jshint` → `connect` → `qunit`.

Repeat runs are fast since Node/npm and `node_modules` are cached under
`testing/wicket-js-tests/node/` and `node_modules/`.

## Running directly with Grunt (faster iteration)

Once `npm install` has run at least once (either via the Maven profile above, or manually
with `./node/npm install` from `testing/wicket-js-tests`), you can invoke Grunt directly
without going through Maven:

```bash
cd testing/wicket-js-tests

# everything: jshint + connect + qunit (both engines)
./node/node node_modules/.bin/grunt --verbose

# lint only, no browser needed
./node/node node_modules/.bin/grunt jshint

# start the test webserver and run one engine at a time
./node/node node_modules/.bin/grunt connect qunit:all       # jQuery engine (wicket-ajax-jquery.js)
./node/node node_modules/.bin/grunt connect qunit:vanilla   # plain-JS engine (wicket-ajax.js)
```

`qunit:all` and `qunit:vanilla` launch headless Chrome via Puppeteer and serve the tests
from `grunt-contrib-connect` on `http://localhost:38887` - nothing else needs to be running
first. `all.html` also accepts other jQuery versions via a query string
(`all.html?3.7.1`, `all.html?4.0.0`, `all.html?vanilla`) if you want to check a specific
engine/jQuery version combination by hand in a real browser.

## Prerequisites

- Puppeteer downloads its own headless Chrome on first `npm install`, but that Chrome binary
  still dynamically links against your system's Chrome/Chromium shared libraries (`libatk`,
  `libatk-bridge2.0`, `libcups`, `libasound`, `libgbm`, `libpango`, `libxcomposite`,
  `libxdamage`, `libxfixes`, `libxrandr`, `libatspi`, `libgtk-3`, `libnss3`, `libnspr4`,
  `libxkbcommon`, ...). The simplest way to get all of these on Debian/Ubuntu is to install
  a real Chrome (dependency resolution pulls in the rest):

  ```bash
  sudo apt-get update
  sudo apt-get install -y google-chrome-stable
  ```

  or install the individual `lib*` packages listed above if you'd rather not install Chrome
  itself.

## What gets tested

- `wicket-core/src/test/js/{ajax,channels,dom,event,form,head,timer}.js` - the shared QUnit
  suite (`jshint:testsJs` / `qunit:all` / `qunit:vanilla` targets in
  `testing/wicket-js-tests/Gruntfile.js`), covering both Ajax engines plus `Wicket.DOM`,
  `Wicket.Event`, `Wicket.Form`, `Wicket.Head`, and the channel manager.
- `jshint:core` / `jshint:extensions` / `jshint:nativeWebSocket` - lint-only coverage
  (ES6+, no test runtime) for the rest of the framework's JS: `wicket-extensions`
  Ajax components (autocomplete, palette, upload progress bar, ajax download, trap focus),
  the dev debug bar, and native WebSocket support.
- `jshint:gymTestsJs` - the `wicket-examples` JS tests.
