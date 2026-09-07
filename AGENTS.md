# Apache Wicket

Component-based Java web framework. `master` is the 11.x development line; the maintained
release lines live on `wicket-10.x`, `wicket-9.x` and `wicket-8.x`. `SECURITY.md` says which of
them still receive fixes.

Versions — the Java release, the Servlet level, library and plugin versions — are not repeated
here, because they go stale and then mislead. Read them from `pom.xml`.

The code format (tabs, braces on their own line, 100 columns) comes from
`wicket-eclipse-settings`. No plugin in the build applies or checks it, so nothing will catch you
getting it wrong: match the file you are editing. Every source file carries the ASF license
header — copy it when you add one.

## How a change lands

- Prepare the change and stop. Whether it goes in as a direct push to master or as a pull request
  is the committer's call, and the push itself is never yours: don't push, don't open a pull
  request, don't tag unless you were told to do so.
- Fixes go on `master` first and are cherry-picked down the maintenance lines afterwards, as far
  as the fix still applies. Which lines are in scope is a decision, not an inference — ask.

## Commits

- The subject says what the change does, in plain prose. Issues are filed on GitHub now, and the
  reference belongs in the body (`GitHub issue #1576`), not the subject. Older issues still live
  in Jira; a change that resolves one keeps its `WICKET-XXXXX` key.
- The body carries the reasoning: what the behaviour was, why it was wrong, what it is now, and
  what an application that relied on the old behaviour sees after the change. This is where
  rationale goes — not into a comment.

## Build and test

- A full `mvn clean verify` has to be green before a change is done. Add `-Pjs-test` for the
  JavaScript tests; that combination is what CI runs.
- Getting there is not how to work there. While iterating, build the least that answers the
  question: `-DskipTests`, `-Pfast` (drops the `SLOW`-tagged tests, javadoc and sources),
  `-pl <module> -am`, `-o`. Pick whatever is quickest for the task and save the full build for
  the end.
- Most tests are not in the module they exercise, so a targeted run resolves the module you
  changed from the local repository rather than from your working tree, and you quietly test the
  last thing you installed. Install first, then run the test:

  ```bash
  mvn install -DskipTests -Pfast
  mvn verify -pl wicket-core-tests -Dtest=ButtonTest
  ```

  `-am` builds the dependency from source instead, but it applies `-Dtest` to every module it
  pulls in and fails on the first one with no matching test, so it needs
  `-Dsurefire.failIfNoSpecifiedTests=false` alongside.

- A test class must be named `*Test.java`. Surefire includes nothing else, so a `FooTests` or a
  `TestFoo` compiles, passes review, and never runs.

## Where code goes

- A component's markup, `.properties`, `.js` and `.css` sit next to its `.java` in
  `src/main/java` — the pom registers the source directory as a resource directory. The same goes
  for test pages: their markup belongs next to the test in `src/test/java`. `src/test/resources`
  is not registered at all, so anything put there is off the test classpath.
- Tests live in the module's `-tests` companion where one exists. `wicket-core` has none of its
  own; they are all in `wicket-core-tests`, and `wicket-core/src/test/java` — which does exist —
  holds only test resources and the JavaScript tests. Modules that have not been split,
  `wicket-extensions` among them, keep their tests in `src/test/java`. Follow the module you
  are in.
- `WicketTester`, `WicketTestCase`, `FormTester` and `TagTester` are `src/main` code in
  `wicket-tester`, shipped so applications can test against them. A fixture applications would
  want goes there, not into a test module.

## Poms

- Section, dependency and plugin order is enforced at `validate` by the pedantic pom enforcer:
  dependencies sort by scope, then groupId, then artifactId. `dependencyConvergence` is on as
  well. A pom edited in the wrong order fails the build before a single class compiles.
- A module pom carries no `<version>` for a dependency. Versions are managed in the root pom,
  each behind a `*.version` property.

## API compatibility

- japicmp fails the build on binary-incompatible changes. On a maintenance branch it compares
  against that line's first release, so nothing incompatible can land there. On `master` it
  compares against the previous `11.0.0-SNAPSHOT`, which leaves it quiet about most breaks.
- Quiet is not permission. Changing or removing public API on `master` is allowed but not free:
  it needs a justification in the commit message, and where the old member can survive next to
  the new one, deprecate it rather than remove it.
- An API change owes users a migration path. Add an OpenRewrite recipe to
  `wicket-migration/src/main/resources/META-INF/rewrite/wicket.yml` where the change is
  mechanical, and draft the migration-guide wording in the commit message or the pull request
  description — the guide itself lives on the wiki, where only a human can put it.

## Javadoc

- Javadoc is user-facing documentation, not a formality. Public API needs it, and a new public
  member gets an `@since` naming the release it first appears in.
- Say what a method does to the value it is given, not only what it returns. Anything bearing on
  escaping, trust or security — a value written into the markup as is, a template that has to be
  authored by the developer — is documented on the class or the method, because that is where
  someone deciding whether it is safe will look.

## Comments

- Only write a comment when it really matters. Most code should have none.
- Prefer a clearer name or an extracted method over a comment. Needing one to follow the code is
  the second-best fix.
- Comment the *why*, not the *what*. Never restate what the line below does or rephrase its
  intent, and don't comment the obvious.
- Keep an inline comment to a single line of intent, not a paragraph. A class-level comment may
  be a little more verbose, but keep it a general overview — no implementation detail.
- A comment that no longer matches the code is worse than none. Fix or delete it when you touch
  that code; that one is not a drive-by change.
- Don't dump your reasoning into a comment. Deeper rationale belongs in the commit message.
- Before finalizing an edit that adds a comment, re-read it against these rules. Delete it if it
  restates the code, rephrases its intent, or carries rationale that belongs in the commit
  message. When in doubt, leave it out.

## Language

- Everything you write is English: identifiers, comments, Javadoc, exception and log messages,
  bundle keys, commit messages, pull request descriptions and branch names.
- The localized bundles (`Application_xx.utf8.properties` and their like, around a hundred of
  them) are translations contributed by native speakers. Add your key to the default bundle and
  leave the rest alone.

## Security

- `SECURITY.md` is the policy, and it is long: the scope, the conditions under which a report is
  not assessed, and a security model stating what Wicket trusts. Read it before concluding that
  something is or is not a vulnerability. A finding that asks the framework to distrust something
  the model treats as trusted is a deployment issue rather than a framework one — and if the
  model itself looks wrong, say so about the model.
- A suspected vulnerability does not go into a GitHub issue, a discussion, a pull request, a
  commit message or a branch name before a fix is released. It goes to security@apache.org.
- Working notes, draft advisories and CVE records for an embargoed issue stay out of the
  repository. Exclude them locally rather than relying on remembering not to commit them.
