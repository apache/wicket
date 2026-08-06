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

Note that reports are assessed against the security model below. A report that
depends on the framework distrusting something this model treats as trusted may
be closed as a deployment or configuration issue rather than a framework
vulnerability. If you believe the model itself is wrong, that is a legitimate
and useful thing to report — please say so explicitly, so we discuss the model
rather than the individual code path.

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

## Security Model

Wicket is a framework, not a deployed application. It runs inside a servlet
container, usually behind a reverse proxy, and it inherits its view of the
outside world from that container. This section documents which of those inputs
Wicket treats as trusted, so that operators know what they are responsible for
and reporters know what the framework does and does not claim to defend.

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
- `OriginResourceIsolationPolicy#getTargetUriFromRequest` builds the **trusted**
  target URI that incoming `Origin` and `Referer` headers are compared against.

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
those headers, subject to its `internalProxies` and `trustedProxies`
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
production.

### Serialized data is trusted

Wicket serializes page instances and session data to its page store. Java
deserialization is not a safe operation on untrusted input, and by default
Wicket's page store does not defend against it. Treat the page store and the
session store as trusted, private storage: do not point them at storage that
untrusted parties can write to, and do not accept externally supplied
serialized page or session data.

The partial exception is a page store configured with encryption
(`StoreSettings#setEncrypted(true)`). Every `ICryptScheme` Wicket ships is
authenticated (AEAD), so encrypted pages are tamper-evident as well as
confidential: modified or substituted bytes fail to decrypt and the page is
treated as absent instead of being handed to the deserializer. Each page is
additionally bound to the page id it was stored under, so a stored page cannot
be replayed as a different one. The scheme marker prefixing each ciphertext is
authenticated too, and is refused unless it is one of the schemes accepted by
`SecuritySettings#setWhitelistedCryptSchemes`, so an attacker cannot force
decryption with a weaker scheme.

Three limits on that exception are worth stating. The key lives in the user's
session, so this protects the stored pages against a party who can read or
write the store, not against one who already controls the session. It covers
the page store only — the container's session store, and anything else holding
serialized Wicket data, remains trusted storage. And a custom `ICryptScheme`
inherits the guarantee only if it honours the contract: `decrypt` must return
`null` on authentication failure rather than returning unverified plaintext.

### Another origin may not invoke a listener

Where `ResourceIsolationRequestCycleListener` is registered, a request originating
from another origin must not be able to invoke a listener on a page — a
`Link.onClick()`, a `Form.onSubmit()`, or an AJAX behaviour. A demonstrated way
for another origin to reach one is a vulnerability.

Two things sit deliberately outside that boundary:

- **Rendering a page is allowed.** A page may be reached by a simple top-level
  navigation from anywhere, so that pages remain linkable from other sites. Only
  the invocation of a listener is refused. Requests that are not top-level
  navigations — subresource loads, `fetch`, `<object>` and `<embed>` — are
  refused for renders too.
- **Sibling origins may be trusted explicitly.** `Sec-Fetch-Site: same-site`
  means a different origin on the same registrable domain and scheme, such as
  another subdomain, and is refused by default. A deployment that trusts every
  origin on its own site can allow it; sibling-origin actions are then that
  deployment's decision rather than a framework vulnerability.

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

### Encrypted URLs are deterministic by design

`CryptoMapper` encrypts a URL to the same text every time, for as long as the
key lives. It has to: a URL regenerated during rendering must match the one the
client requested, and a resource URL must stay identical across requests or the
browser re-downloads the resource on every page view. The consequence is that
equal URLs are recognisable as equal, and that anyone holding the key can
confirm a guessed URL by encrypting it themselves. With the default
`KeyInSessionCryptFactory` the key is per session, so this is confined to a
single user; with an application-wide key it is not. Encrypted URLs are
therefore an obfuscation and a per-session CSRF token, never a secret in their
own right — which is the same reason they are not an authorization mechanism.
Everything Wicket encrypts elsewhere (the page store, the "remember me" cookie)
uses the randomized path and does not have this property.

## Reporting Something That Is Not a Vulnerability

Findings that are real but not vulnerabilities are still welcome — please raise
them publicly in [JIRA](https://issues.apache.org/jira/projects/WICKET) or as a
pull request rather than through the private security channel, so they can be
discussed and fixed in the open. Hardening suggestions, defence-in-depth
improvements, and clarifications to this document all fall into that category.

If you are unsure which channel applies, use the private one — we would rather
receive a non-issue privately than a real issue publicly.
