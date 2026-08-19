# Security Policy

Apache Wicket follows the [Apache Software Foundation security process](https://www.apache.org/security/).

## Reporting a Vulnerability

**Please do not report security vulnerabilities through GitHub issues, GitHub
discussions, pull requests, JIRA, or the public mailing lists.** Doing so
discloses the issue publicly before a fix is available.

Report suspected vulnerabilities privately to:

- **security@apache.org** — the ASF Security Team, who will forward the report
  to the Wicket PMC.

A useful report includes:

- the affected Wicket version(s) and the module (e.g. `wicket-core`),
- the affected class and method, ideally with a source reference,
- a concrete description of how an attacker reaches the code, including what
  the attacker is assumed to control (see [Security Model](#security-model)),
- the impact you believe follows from that, and
- a reproducer where possible — a failing test is ideal.

Please state clearly whether you have published anything about the issue, and
whether you are requesting a CVE.

We ask reporters to keep the issue confidential until a fixed release is
published and the PMC has announced it. In return, we will keep you informed of
our assessment and of the release timeline, and credit you in the announcement
unless you ask us not to.

Note that reports are assessed against the scope and the security model below.
A report that depends on the framework distrusting something this model treats
as trusted may be closed as a deployment or configuration issue rather than a
framework vulnerability. If you believe the model itself is wrong, that is a
legitimate and useful thing to report — please say so explicitly, so we discuss
the model rather than the individual code path.

Conversely, a demonstrated bypass of a boundary this model does claim — for
example the package resource guard, or an authorization strategy — is a
vulnerability, and we want to hear about it. The boundaries below describe what
Wicket intends to enforce; where the code falls short of them, the code is what
needs fixing.

## Supported Versions

Security fixes are applied to the actively maintained release lines. Refer to
the [download page](https://wicket.apache.org/start/download.html) for the
current status and the latest release of each line.

| Version | Status                                          |
| ------- | ----------------------------------------------- |
| 11.x    | In development (`master`) — not yet released     |
| 10.x    | Current, supported                              |
| 9.x     | Supported                                       |
| 8.x     | Security fixes only — upgrade to 9.x or 10.x    |
| ≤ 7.x   | Discontinued — no security fixes                |

If you are running a discontinued version, the fix is to upgrade. See the
[Migration to Wicket 10.0](https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+10.0)
guide on our wiki, which links the guides for the earlier lines.

## Scope

The table above says which release lines receive security fixes. Two categories
of code inside those lines sit outside this process.

### Deprecated code is out of scope

`@Deprecated` is our statement that code has no future and that an application
should stop relying on it. Where an application has to reach that code
deliberately — calling a deprecated method, extending a deprecated class,
setting a deprecated setting — the remedy for a problem in it is to stop using
it, not to harden something we intend to remove. Such a report is closed with a
pointer to whatever the migration is; it receives no CVE, and the deprecated
code is not fixed.

Two limits on that, both of which cut in the reporter's favour:

- **Deprecation is per release line.** A member deprecated on `master` may still
  be current in 10.x, 9.x or 8.x. A report is judged against the line it
  targets, not against `master`. This matters most for 8.x, which carries by far
  the largest body of deprecated code of the supported lines.
- **Deprecating a member does not deprecate the behaviour behind it.** Where a
  deprecated accessor merely fronts a feature that is still current and still
  reachable without the application opting in, the feature is in scope and the
  deprecated accessor is beside the point. What this section excludes is
  functionality an application chooses to use, not behaviour it gets whether it
  asks for it or not.

Where something is deprecated *because* it is insecure, the javadoc says so.
Usually it also names what to use instead. Sometimes it cannot: where the design
rather than the implementation is the problem, a feature may be one that cannot
be made safe, and we will deprecate it with no replacement offered — the secure
course is to stop doing the thing at all rather than to do it differently, so
there is nothing to migrate to. Deprecation is the fix in that case, and the code
is out of scope on the same footing as any other deprecated code. The javadoc
says which of the two applies, so it is clear before reporting.

### `wicket-examples` is sample code, not production code

`wicket-examples` exists to demonstrate framework features in as few lines as
possible. It is not written to production standards, and some of it is
deliberately insecure so that the examples run anywhere out of the box.
`WicketExampleApplication`, the base class of every example, installs `NoCrypt`
as the crypt factory — a no-op cipher, so that nothing depends on the local JCE
setup — and enables the development utilities; the source says in as many words
not to do either in a real application. Individual examples go further:
`authentication1` hardcodes its one credential pair in the source. Do not read
the examples as a security reference, and do not copy them into an application
unchanged. This applies with particular force on 8.x, where the examples
demonstrate a release line that receives security fixes only.

We do want to hear about problems in them, because example code gets copied and
a misleading pattern propagates from there into real applications. But fixing
one is a correction to teaching material rather than a fix to a vulnerability in
the framework, so:

- the PMC will not request a CVE for it;
- it is fixed on `master` only. `wicket-examples` ships as a WAR in every
  release, and we knowingly leave the released examples as they are;
- once we have confirmed the problem is example-only, it is tracked in public
  [JIRA](https://issues.apache.org/jira/projects/WICKET), since there is nothing
  to embargo.

The same reasoning covers `wicket-devutils`, a development aid rather than a
production module (see
[Deployment configuration](#deployment-configuration-is-the-operators-responsibility)),
and the test-only modules under `testing/` that are never published. It does
**not** cover `org.apache.wicket.util.tester`, which on 8.x ships inside
`wicket-core` and is depended on by application test suites, and it does not
cover the quickstart archetype: applications are started from the archetype, so
it is expected to be secure by default and is in scope like any other module.

The examples are also hosted publicly by the ASF. Those deployments are ASF
infrastructure, not a Wicket release. If you find something that affects the
hosting rather than the example application itself, it is still worth reporting
to **security@apache.org** — say that it concerns the hosted site, so that it can
be routed to ASF Infrastructure as well as to the PMC.

## Security Model

Wicket is a framework, not a deployed application. It runs inside a servlet
container, usually behind a reverse proxy, and it inherits its view of the
outside world from that container. This section documents which of those inputs
Wicket treats as trusted, so that operators know what they are responsible for
and reporters know what the framework does and does not claim to defend.

**This copy describes Wicket 8.x.** The boundaries below are not the same as on
9.x and 10.x, and in two places 8.x defends less: its cross-origin protection is
the older `Origin`/`Referer` check rather than resource isolation, and it has no
option to encrypt the page store. Both are noted where they arise. 8.x receives
security fixes only; the current model is documented on the 9.x and 10.x
branches, and upgrading is the way to reach it.

### Wicket trusts the container-reported host, port and scheme

Wicket derives its own public identity — the scheme, host and port it believes
it is being served on — from the servlet container, via
`HttpServletRequest#getScheme()`, `#getServerName()` and `#getServerPort()`.
There is no hostname allowlist in the framework and no attempt to verify the
`Host` header, in any of the places this identity is used:

- `ServletWebRequest#setParameters` sets the host, port and protocol on the
  client URL from these three values. That URL backs `UrlRenderer`, and so
  every absolute URL Wicket renders.
- `HttpsMapper#createRedirectUrl` builds the scheme-switch redirect for
  `@RequireHttps` pages from the same values.
- `CsrfPreventionRequestCycleListener#getTargetUriFromRequest` builds the
  **trusted** target URI that incoming `Origin` and `Referer` headers are
  compared against.

This is a deliberate design decision, not an oversight. Only the deployment
knows its own canonical hostnames; the framework cannot infer them. Note in
particular that the third item means the container-reported host is a trusted
input to Wicket's own request-forgery defences — a deployment that lets
arbitrary `Host` values through weakens more than URL rendering.

**Therefore the deployment is responsible for ensuring that only expected
`Host` values reach the application.** Concretely:

1. Configure the container or virtual host to reject requests carrying an
   unrecognised `Host` — return a 400 or 404 rather than routing them to the
   application. Tomcat, Jetty and the common reverse proxies all support this.
2. If TLS is terminated at a proxy, have the proxy set or overwrite `Host` to
   the canonical name rather than forwarding whatever the client sent.
3. Do not expose a Wicket application through a catch-all or default virtual
   host that accepts any `Host`.
4. Serve the application over HTTPS and enable HSTS, so that plaintext requests
   — including the ones `HttpsMapper` exists to upgrade — are not part of the
   normal flow.

A consequence worth stating plainly: on a deployment that accepts arbitrary
`Host` values, absolute URLs and redirects generated by Wicket will contain the
host the client supplied. That is the documented behaviour of trusting the
container. It is not treated as a framework vulnerability, because the host in
such a response is always the same authority the client had already connected
to — it grants an attacker no origin they did not already control. The fix
belongs at the container or proxy, per the points above.

### `X-Forwarded-*` headers are not trusted by default

Wicket ignores `X-Forwarded-For` and `X-Forwarded-Proto` unless you explicitly
enable `XForwardedRequestWrapperFactory`. When enabled, it overrides
`getRemoteAddr()`, `getRemoteHost()`, `getScheme()` and `getServerPort()` from
those headers, subject to its `allowedInternalProxies` and `trustedProxies`
configuration.

Only enable it when a trusted proxy in front of the application appends to
these headers and strips any client-supplied copies; otherwise the headers are
attacker-controlled. Wicket does not implement `X-Forwarded-Host` at all, and
`XForwardedRequestWrapper` does not override `getServerName()` — the host always
comes from the container as described above.

### Client-supplied URLs are not trusted for authority

For Ajax requests Wicket reads a client-supplied base URL — the
`Wicket-Ajax-BaseURL` header, falling back to the `wicket-ajax-baseurl` request
parameter — in order to resolve relative URLs against the page the client is
actually on. The host, port and protocol of that URL are always overwritten with
the container-reported values before use. The client can influence the path
Wicket renders relative to, never the authority.

### Deployment configuration is the operator's responsibility

`RuntimeConfigurationType.DEVELOPMENT` enables debugging aids, verbose error
reporting and development-only components, and disables some caching. It is not
intended for production and is not hardened. Always run production deployments
with the configuration type set to `RuntimeConfigurationType.DEPLOYMENT`. Issues
only reachable in `DEVELOPMENT` mode are treated as configuration errors rather
than vulnerabilities.

Likewise, `wicket-devutils` is a development aid. Do not deploy it in
production; it is out of scope for the same reason the examples are (see
[Scope](#scope)).

### Serialized data is trusted

Wicket serializes page instances and session data to its page store. Java
deserialization is not a safe operation on untrusted input, and by default
Wicket's page store does not defend against it. Treat the page store and the
session store as trusted, private storage: do not point them at storage that
untrusted parties can write to, and do not accept externally supplied
serialized page or session data.

There is no exception to that on 8.x, and no way to soften it through
configuration: 8.x has no encrypted page store. `StoreSettings` offers no
encryption option, and the `IDataStore` implementations that ship with it —
`DiskDataStore`, `HttpSessionDataStore` and the `AsynchronousDataStore` wrapper
around them — write serialized bytes as they are. Whatever the store is pointed
at must therefore be storage only the application itself can read and write.
The encrypted page store, and the trust statement that goes with it, exist on
9.x and 10.x only.

### Model data is escaped; markup and message bundles are trusted

Wicket escapes the text a component renders from its model. `Component`'s
`escapeModelStrings` flag is **on by default**, and a component renders
model-derived text either through `Component#getDefaultModelObjectAsString()` or
by applying `Strings#escapeMarkup` when that flag is set. A component that writes
application model data into the markup unescaped in the default configuration is
a bug in the framework and an opening for cross-site scripting (XSS). We want to
hear about it.

`setEscapeModelStrings(false)` is the application saying the content is markup
and taking responsibility for it. Reports that depend on an application having
cleared the flag are configuration issues rather than framework vulnerabilities.
Note that a few components clear it themselves because their value is written
into an attribute, which is escaped when the tag is written and would otherwise
be encoded twice; that is an implementation detail of those components and not an
invitation to render untrusted markup through them.

Two inputs on the other side of the boundary are trusted, because both are
authored by the developer and neither is data the application received at
runtime:

- **Markup files are trusted.** A `.html` file on the classpath is a template,
  exactly like a JSP or a Thymeleaf template, and Wicket renders it as markup. An
  application that serves markup from somewhere an untrusted party can write —
  through a custom `IMarkupResourceStreamProvider`, for instance — has taken
  that trust on itself.
- **Message bundles are trusted.** `<wicket:message key="…"/>` renders its
  property value as markup by default, and `escape="true"` opts in to escaping.
  Markup in a bundle is therefore a supported way to format a message.

The value a bundle string interpolates is a different matter. `${name}` in a
message resolves first to a child component with `wicket:id="name"`, whose
rendered markup carries that component's own escaping. Only when there is no
such child does Wicket fall back to reading `name` from the surrounding
component's model, and that value is written as it came — so a static bundle can
still place model data in the markup unescaped. Prefer the child component.
Where the fallback is unavoidable and the data is not trusted, the message needs
`escape="true"`, which escapes the whole message and therefore any markup the
bundle itself contains.

Finally, `Strings#escapeMarkup` escapes `<`, `>`, `&`, `"` and `'`. That is
enough for element text and for a quoted attribute value, and it is not enough
for anything else: it does not make a value safe inside `<script>` or `<style>`,
in an unquoted attribute, or in a URL where the scheme itself is the payload.
Wicket does not escape for a JavaScript context anywhere, so a value the
application places in one — through `TextTemplate` variable substitution, for
example — has to be encoded by the application.

### Another origin may not invoke a listener

Where `CsrfPreventionRequestCycleListener` is registered, a request originating
from another origin must not be able to invoke a listener on a page — a
`Link.onClick()`, a `Form.onSubmit()`, or an AJAX behaviour. A demonstrated way
for another origin to reach one is a vulnerability. Applications using native
WebSockets should register `WebSocketAwareCsrfPreventionRequestCycleListener`
instead, which extends the same check to the WebSocket handshake.

Three things sit deliberately outside that boundary:

- **Rendering a page is allowed.** The listener checks action handlers, not
  render handlers, so a page may be reached from anywhere and remains linkable
  from other sites. Only the invocation of a listener is refused. Which handlers
  are checked can be widened by overriding `isChecked(IRequestHandler)`, and
  individual pages can be exempted or required through
  `isChecked(IRequestablePage)`.
- **Origins may be trusted explicitly.** `addAcceptedOrigin` allowlists origins
  the deployment chooses to trust. Actions from an allowlisted origin are then
  that deployment's decision rather than a framework vulnerability.
- **The absent-header case is configurable.** A request carrying neither
  `Origin` nor `Referer` is rejected by default, as is `Origin: null` from a
  privacy-sensitive context. A deployment may relax either through
  `setNoOriginAction`, and may soften a mismatch through
  `setConflictingOriginAction`. Having chosen to allow such requests, it owns
  the consequences.

Note what this check is, because it is weaker than the mechanism on 9.x and
10.x. It compares the `Origin` or `Referer` header against the
container-reported target URI, and that is all it has to work with: 8.x reads no
`Sec-Fetch-*` headers, so it cannot tell a top-level navigation from a
subresource load, a `fetch`, or an `<object>` embed. It also depends on the
headers being present and on the container-reported host being trustworthy
(see [above](#wicket-trusts-the-container-reported-host-port-and-scheme)).
Resource isolation on 9.x and 10.x does not have these limitations; they are a
reason to upgrade rather than a framework vulnerability in 8.x.

This listener is opt-in and is not registered by default. Without it Wicket
enforces no cross-origin boundary on listener invocation at all. `CryptoMapper`
raises the cost of forging a URL but is not a substitute for it, for the reason
below.

### `CryptoMapper` is not an authorization mechanism

`CryptoMapper` encrypts URLs so that page and component identifiers are not
guessable. It raises the cost of forging a URL, but it is not an access-control
mechanism. Authorization must be enforced with `IAuthorizationStrategy` (or
equivalent) so that it holds regardless of whether a URL was guessed,
replayed, leaked through a referrer, or found in a log.

### The crypt Wicket ships is unauthenticated, and weaker on 8.x

`SecuritySettings#getCryptFactory()` defaults to
`KeyInSessionSunJceCryptFactory`, which produces a `SunJceCrypt` using
`PBEWithMD5AndDES` — DES in CBC mode. That is what encrypts `CryptoMapper` URLs
and, where `DefaultAuthenticationStrategy` is configured, the remember-me
cookie. It gives confidentiality only: the ciphertext is malleable and its
integrity is not checked on decryption, so a value that decrypts without error
has not thereby been shown to be unmodified. Never read "it was encrypted" as
"it was not tampered with". The javadoc of `SunJceCrypt` says the same.

Two things about this are specific to 8.x:

- `SunJceCrypt` derives its key with a compiled-in salt and an iteration count
  of 17. The salt is a public constant, `SunJceCrypt.SALT`, so anyone can read
  it out of the jar. From 9.x the factory generates a fresh random salt per
  session and uses 1000 iterations.
- 8.x has no AES implementation at all. `AESCrypt`, and with it the option of
  configuring an AES-based `ICrypt`, arrived in 9.x. On 8.x the cipher is DES,
  with the 56-bit key that implies.

The encryption key itself is still per-session and random on 8.x —
`KeyInSessionSunJceCryptFactory` builds it from the session id and a fresh
`UUID` — so the fixed salt matters less here than it would for a key derived
from a shared passphrase. None of this is treated as a framework vulnerability,
because the framework does not claim the crypt as a boundary; `CryptoMapper`
above says why. It is documented because an operator weighing their exposure
needs to know it, and because it is a concrete reason to move off 8.x.

Where it matters most is the remember-me cookie, which holds credentials that
the application can recover. A deployment that wants a durable sign-in should
prefer a revocable token of its own over
`DefaultAuthenticationStrategy`, on any release line.

## Reporting Something That Is Not a Vulnerability

Findings that are real but not vulnerabilities are still welcome — please raise
them publicly in [JIRA](https://issues.apache.org/jira/projects/WICKET) or as a
pull request rather than through the private security channel, so they can be
discussed and fixed in the open. Hardening suggestions, defence-in-depth
improvements, and clarifications to this document all fall into that category.

If you are unsure which channel applies, use the private one — we would rather
receive a non-issue privately than a real issue publicly.
