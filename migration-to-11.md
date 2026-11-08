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
# Migration to Wicket 11

This guide lists the changes in Wicket 11.0.0 that break code written for Wicket 10, or change what a running application does, and what you have to do about them. It compares against Wicket 10.12.0.

Most of an upgrade from Wicket 10 is in two steps: Java 21 and Jakarta Servlet 6.1, and the libraries that come with them. Wicket's own API changed in a few places, and an OpenRewrite recipe makes the mechanical changes for you.

To come from Wicket 8 or 9, migrate to Wicket 10 first, with the [migration guide to Wicket 10](https://github.com/apache/wicket/blob/wicket-10.x/migration-to-10.md).

## Requirements

|  | Wicket 10 | Wicket 11 |
|---|---|---|
| Java | 17 | 21 |
| Jakarta Servlet | 6.0 | 6.1, for example Jetty 12.1 or Tomcat 11 |
| CDI (`wicket-cdi`) | 4.0, Weld 5 | 5.0, Weld 7 |
| Spring (`wicket-spring`) | 6.2 | 7.0 |
| Hibernate Validator (`wicket-bean-validation`) | 8.0 | 9.1 |
| Jackson (`JsonRequestLogger` in `wicket-extensions`) | 2 (`com.fasterxml.jackson`) | 3 (`tools.jackson`) |
| JUnit (through `wicket-tester`) | 5 | 6 |

Wicket 11 is built against these versions. Upgrade the container and the libraries first, then Wicket.

## Automatic migration

The OpenRewrite recipe `org.apache.wicket.MigrateToWicket11`, in the `wicket-migration` module, makes the mechanical changes. Run it on your project:

```
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=org.apache.wicket:wicket-migration:11.0.0 \
  -Drewrite.activeRecipes=org.apache.wicket.MigrateToWicket11
```

The recipe:

- moves the project to Java 21, with the `UpgradeToJava21` recipe of OpenRewrite;
- changes the version of the `org.apache.wicket` dependencies to 11;
- moves `ICrypt`, `ICryptFactory`, `NoCrypt` and `NoCryptFactory` from `org.apache.wicket.util.crypt` to `org.apache.wicket.core.util.crypt`;
- renames `SignInPanel` to `UsernamePasswordPanel` and `SignInPage` to `UsernamePasswordPage`;
- moves `wicket-metrics` to the groupId `org.apache.wicket.experimental.wicket11`.

Everything else in this guide needs a decision or a change by hand.

## Removed without a replacement

### `Include`

`Include` fetched a URL and wrote the response into the page as it was. There is nothing to migrate to: compose pages with panels, borders and markup inheritance instead. The component was deprecated in 8.19.0, 9.24.0 and 10.11.0.

### Remember me: `IAuthenticationStrategy`

Removed:

- `IAuthenticationStrategy`, `DefaultAuthenticationStrategy` and `NoOpAuthenticationStrategy`, with their packages `org.apache.wicket.authentication` and `org.apache.wicket.authentication.strategy`;
- `SecuritySettings#getAuthenticationStrategy()` and `#setAuthenticationStrategy()`;
- in `wicket-auth-roles`, the remember-me support of the sign-in panel.

The default strategy stored the user name and the password in a cookie. If your application needs a login that survives the session, implement it yourself with a random, revocable token per device. All of this API was deprecated in 8.19.0, 9.24.0 and 10.11.0.

`SignInPanel` and `SignInPage` are now `UsernamePasswordPanel` and `UsernamePasswordPage`, and the recipe renames them. The package, the member names, the component id `signInForm`, the resource key `signInFailed` and the translations stay the same. `getRememberMe()`, `setRememberMe()`, `onSignInRemembered()` and the constructor `SignInPanel(String, boolean)` have no equivalent: remove the calls. `UsernamePasswordPage(PageParameters)` passes the parameters on to `WebPage`, which `SignInPage` did not.

Wicket 11 does not clear the `LoggedIn` cookie a Wicket 10 application may have set. It expires within thirty days.

## Encryption

Wicket 10 had two unrelated encryption APIs: `ICrypt` for URLs and the remember-me cookie, and `ICrypter` for the page store. Wicket 11 has one, in `org.apache.wicket.core.util.crypt`, and all of its encryption is authenticated.

| Wicket 10 | Wicket 11 |
|---|---|
| `org.apache.wicket.util.crypt.ICrypt`, `ICryptFactory`, `NoCrypt`, `NoCryptFactory` | The same names in `org.apache.wicket.core.util.crypt`. The recipe moves them |
| `SunJceCrypt`, `AbstractCrypt`, `CryptFactoryCachingDecorator`, `AESCrypt`, `AbstractJceCrypt` | A crypt scheme: `AesGcmCryptScheme` (AES-256-GCM, the default) or `AesGcmSivCryptScheme` (AES-256-GCM-SIV, needs Bouncy Castle), set with `SecuritySettings#setCryptScheme()` |
| `KeyInSessionSunJceCryptFactory`, `AbstractKeyInSessionCryptFactory` | `KeyInSessionCryptFactory`, a key per session, the default. `ApplicationKeyCryptFactory` uses one key for the whole application |
| `ICrypter`, `DefaultCrypter`, `GCMSIVCrypter`, `StoreSettings#setCrypter()`, `CryptingPageStore#newCrypter()` | The page store encrypts with the crypt scheme of `SecuritySettings` |
| `SecuritySettings#setCryptFactory(org.apache.wicket.util.crypt.ICryptFactory)` | `SecuritySettings#setCryptFactory(org.apache.wicket.core.util.crypt.ICryptFactory)` |
| `CryptoMapper#getCrypt()` returns `org.apache.wicket.util.crypt.ICrypt` | Returns `org.apache.wicket.core.util.crypt.ICrypt` |

To migrate:

- Remove the configuration of the removed classes. The defaults need no configuration.
- To choose a scheme, call `getSecuritySettings().setCryptScheme(...)` in `init()`.
- An implementation of your own of `ICrypt` implements `encrypt(byte[], byte[])`, `decrypt(byte[], byte[])` and `encryptDeterministic(byte[], byte[])`. The second argument is associated data, authenticated but not encrypted, and may be `null`. `decrypt` returns `null` for anything it cannot decrypt and verify. `encryptUrlSafe()` and `decryptUrlSafe()` are default methods now.
- An implementation of your own of `ICrypter` becomes an `ICryptScheme`, or better, goes: the page store uses the configured scheme.

Wicket 11 cannot decrypt what Wicket 10 encrypted:

- **The encrypted page store** (`StoreSettings#setEncrypted(true)`): pages stored by Wicket 10 are not found after the upgrade. A user with such a page open gets the page-expired page, as after a restart.
- **Encrypted URLs** (`CryptoMapper`): a URL encrypted by Wicket 10 no longer resolves. With the default key per session, such a URL did not outlive its session anyway.
- **Your own data**, encrypted with Wicket's crypt classes and stored: decrypt it with Wicket 10 and encrypt it again with Wicket 11.

Every ciphertext now starts with a byte that names its scheme. To move to another scheme later without losing data, accept the old scheme next to the new one with `SecuritySettings#setWhitelistedCryptSchemes()` until the data is rewritten.

## `LoadableDetachableModel#detach()` is final

A subclass of `LoadableDetachableModel` that overrides `detach()` no longer compiles. Move the body of the override:

- to `onDetachAlways()`, and drop the call to `super.detach()`. This keeps the behaviour exactly: it runs on every detach, also when the model was not attached;
- or to `onDetach()`, if it only has to run when the model was attached.

The new `onDetach(T object)` receives the loaded object before it is discarded, so a subclass no longer has to keep its own reference to it. The default `onDetach()` calls it, so an override of `onDetach()` that does not call `super` skips it. Compiled subclasses keep running: the change is binary compatible.

## Content Security Policy

`CSPRequestCycleListener` is gone. A `WebPage` writes the CSP headers itself, from `configureResponse()`.

| Wicket 10 | Wicket 11 |
|---|---|
| `setProtectedFilter(Predicate<IRequestHandler>)`, default: every `RenderPageRequestHandler` | `setProtectedFilter(Predicate<WebPage>)`,default: every `WebPage` |
| `protected boolean mustProtectRequest(IRequestHandler)` | `public boolean mustProtect(WebPage)` |
| `CSPHeaderWriter#write(WebResponse, IRequestHandler)` | `CSPHeaderWriter#write(WebResponse, RequestCycle)` |

To migrate:

- Rewrite a protected filter to test the page instead of the request handler. A lambda taking an `IRequestHandler` no longer compiles.
- Check overrides of `WebPage#configureResponse()`: one that does not call `super` now leaves out the CSP headers.

## `IRequestHandlerDelegate.unwrap()`

Wicket 10.10.0 added the static `IRequestHandlerDelegate.unwrap(IRequestHandler)`. Wicket 11.0.0 does not have it. If you call it, use a loop of your own until it returns:

```
while (handler instanceof IRequestHandlerDelegate delegate)
{
	handler = delegate.getDelegateHandler();
}
```

## Text fields write `required`, `placeholder` and `pattern`

In Wicket 10 only the `HTML5Attributes` behavior wrote these attributes. In Wicket 11 `AbstractTextComponent` (`TextField`, `TextArea` and their subclasses) always writes:

- `required="required"` when the component is required;
- `placeholder` with the label, when the component has a label. This replaces a `placeholder` in your markup;
- `pattern` with the pattern of a `PatternValidator`.

A `Button` whose `getDefaultFormProcessing()` returns `false` gets `formnovalidate`.

When a form is submitted without Ajax, by a `Button`, a `SubmitLink` or the Enter key, the browser now checks required fields and patterns first. An empty required field or a value that does not match stops the submit in the browser, with the browser's own message, so the request never reaches Wicket and your feedback panel shows nothing. Ajax submits (`AjaxButton`, `AjaxSubmitLink`) are not affected: the browser does not validate them, and Wicket's validation and messages work as before. To keep Wicket's messages for a form submitted without Ajax, put `novalidate` on the `<form>` tag. To keep a placeholder from your markup, or to leave out an attribute, remove it in an override of `onComponentTag()` after calling `super`, or set it again there.

`HTML5Attributes#onButton()` is removed.

## `AjaxFormComponentUpdatingBehavior` on a `FormComponentPanel`

The behavior now also processes the form components inside a `FormComponentPanel`, so the model of the panel is up to date when `onUpdate()` runs. A `Palette` or a `DateTimeField` takes the behavior directly now: you no longer have to attach it to an inner component and call `processInput()` yourself. Processing the inner components makes the data of those components serialized with the page. To go back to the behaviour of Wicket 10, override `updateInnerComponents()` to return `false`.

## Component ids and markup ids

A component id and a markup id may only contain letters, digits, `_`, `-` and `.`. A component with another id throws an exception in its constructor, `setMarkupId()` throws for such a markup id, and so does reading markup with such an `id` attribute.

- A component id used as a property expression by a `CompoundPropertyModel`, such as `address.lines[0]` or `getName()`, needs `(`, `)`, `[` or `]`. Allow these with`getMarkupSettings().setPropertyExpressionIdsAllowed(true)`, or bind the component with a`PropertyModel` instead.
- A markup id never allows those four characters. The default markup id generator replaces them with `_`, and a custom `IMarkupIdGenerator` has to do the same.

## Resources

- **Authorization**: `IAuthorizationStrategy#isResourceAuthorized()` is now also asked about a resource that a component serves from an `IResource` instance: `ResourceLink`, `Image`, `ImageButton`, `AjaxDownloadBehavior` and the export links of `ExportToolbar`. If your strategy refuses a resource, these components now get that refusal, and `ResourceLink#onClick()` does not run. On these routes `Attributes#getParameters()` is an empty `PageParameters` rather than `null`.
- **Package resources**: the `IPackageResourceGuard` is now asked about the file that is actually served, after the style, the variation and the locale are added to its name, as well as about the requested path. A guard that allows a name only in its plain form may now refuse a localized or styled variant of it.
- **Resource URLs**: a locale, style or variation that contains `/` or `\` is dropped and logged, and the resource is looked up without it.

## Role annotations (`wicket-auth-roles`)

The five annotations now follow one rule: an annotation on a class replaces the annotation of the same kind on its package, and rules on the same level are combined with AND.

- `@AuthorizeAction` and `@AuthorizeActions` now take the package annotation into account, per action. `@AuthorizeActions` can be put on a package.
- `@AuthorizeResource` on a class replaces the one on its package, instead of being combined with it.
- `@AuthorizeInstantiations` takes part in the same rule, and can be put on a package.

The annotations are `@Inherited`: an annotation on a superclass in another package counts as an annotation on the class, and so overrides the annotation of the class's own package. Check the pages and components in an annotated package whose superclass is annotated.

## `JsonRequestLogger` and Jackson 3

`JsonRequestLogger` uses Jackson 3. `getMapper()` returns a `tools.jackson.databind.ObjectMapper`. Jackson is an optional dependency of `wicket-extensions`, as before: if you use the logger, depend on `tools.jackson.core:jackson-databind` instead of `com.fasterxml.jackson.core:jackson-databind`.

## `wicket-metrics`

`wicket-metrics` is published under the groupId `org.apache.wicket.experimental.wicket11`. Change the groupId from `org.apache.wicket.experimental.wicket10`, or let the recipe do it.

## `XsltTransformer`

`XsltTransformer` now transforms with `FEATURE_SECURE_PROCESSING`, as `XSLTResourceStream` already did. External entities and external DTDs in the transformed output are no longer resolved.

## Uploads to a resource (`FileUploadToResourceField`)

Pull request [#1671](https://github.com/apache/wicket/pull/1671), issues [#1670](https://github.com/apache/wicket/issues/1670) and [#1669](https://github.com/apache/wicket/issues/1669). All classes are in`org.apache.wicket.markup.html.form.upload.resource`. None of these changes is mechanical, so there is no OpenRewrite recipe for them.

### Who has to change something

You have nothing to do if you use `FileUploadToResourceField` with the default `FileUploadResourceReference`, and reach the uploaded files only through `UploadInfo#getFile()`.

You do have to act if you:

- implement `IUploadsFileManager`;
- find files stored by `FolderUploadsFileManager` by their client file name;
- extend `AbstractFileUploadResource` or `FileUploadResourceReference`;
- rely on `UploadInfo#equals()`, on the error texts of the field, or on what the browser-side callbacks receive.

### Why the API changed

`FolderUploadsFileManager` stored each file under its client file name, so two files of the same name overwrote each other, in one upload or in two, while the field was told of both. Keeping them apart needs a name for each stored file that the file manager chooses, and that name has to travel from the file manager through the browser back to the field. That is why the file manager, the resource and `UploadInfo` all gained a file name.

### `IUploadsFileManager`

Before:

```
void save(FileUpload fileItem, String uploadFieldId);

File getFile(String uploadFieldId, String clientFileName);
```

After:

```
// new, @since 11.0.0
default String store(FileUpload fileItem, String uploadFieldId)
{
	save(fileItem, uploadFieldId);
	return fileItem.getClientFileName();
}

@Deprecated(since = "11.0.0", forRemoval = true)
default void save(FileUpload fileItem, String uploadFieldId)
{
	throw new UnsupportedOperationException(...);
}

File getFile(String uploadFieldId, String fileName);
```

To migrate, implement `store()` instead of `save()`:

- Store the file under a name of your own choice and return that name. It must be unique among the files of the upload field.
- `getFile()` then receives that name.
- The name goes to the browser and comes back from it, so it must not reveal anything the user is not supposed to know.
- Since the browser can change the name, `getFile()` must never resolve it to a file of another upload field.

An existing implementation of `save()` still compiles, with a removal warning, and keeps working unchanged. The default `store()` calls it and returns the client file name, which means files of the same name still overwrite each other until you implement `store()`.

Watch out:

- `save()` used to be abstract, and the compiler made you implement it. Now it has a default, so an implementation that overrides neither method compiles. At its first upload it fails with an `UnsupportedOperationException` that says to implement `store()`.
- Wicket no longer calls `save()`. If your own code calls it, call `store()` instead. On a manager that implements only `store()`, the deprecated `save()` throws an `UnsupportedOperationException`.`FolderUploadsFileManager` still implements both.

### `FolderUploadsFileManager`

|  | Before | After |
|---|---|---|
| Stores with | `save()` | `store()`; the deprecated `save()` still works as before |
| File on disk | `<folder>/<uploadFieldId>/<client file name>` | `<folder>/<uploadFieldId>/<random UUID>` |
| `getFile(uploadFieldId, name)` | `name` is the client file name | `name` is the stored name |

To migrate, find the files with `UploadInfo#getFile()` or `UploadInfo#getFileName()`, not by their client file name. Files stored before the upgrade keep their client file name on disk, and `getFile(uploadFieldId, clientFileName)` still finds them. The checks against path traversal are unchanged.

### `AbstractFileUploadResource`

Before:

```
protected void saveFiles(List<FileUpload> fileItems, String uploadId)

protected void prepareResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest,
	List<FileUpload> fileItems)

protected abstract String generateJsonResponse(ResourceResponse resourceResponse,
	ServletWebRequest webRequest, List<FileUpload> files);
```

After:

```
protected List<String> saveFiles(List<FileUpload> fileItems, String uploadId)

protected void prepareResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest,
	List<FileUpload> fileItems, List<String> fileNames)

protected abstract String generateJsonResponse(ResourceResponse resourceResponse,
	ServletWebRequest webRequest, List<FileUpload> files, List<String> fileNames);
```

`fileNames` holds the stored name of each file, in the order of the files. There are no deprecated overloads with the old signatures, on purpose: these methods are hooks that the resource calls, so an override of an old signature would compile and then never be called. With `@Override` it now fails to compile, which tells you to migrate.

To migrate:

- Add the `List<String> fileNames` parameter to your overrides.
- In `generateJsonResponse()`, write each name into the JSON of its file as `fileName`.
- An override of `saveFiles()` returns the names that `IUploadsFileManager#store()` returned.

How the resource answers errors has changed as well:

|  | Before | After |
|---|---|---|
| Answer to a failure | `{"error": true, "errorMessage": "<key>"}` | Unchanged |
| Exception other than a `FileUploadException` (the file manager failing, for example) | Not logged. With debug logging on, the exception message was sent as `errorMessage` | Logged at ERROR. The key `uploadFailed` is sent, never the exception message |

### `FileUploadResourceReference`

Before:

```
protected void generateFileInfo(JSONObject fileJson, FileUpload fileItem)
```

After:

```
protected void generateFileInfo(JSONObject fileJson, FileUpload fileItem, String fileName)
```

The JSON of each file gets `fileName` next to `clientFileName`, `size` and `contentType`. To migrate, add the parameter to your override, and keep calling `super` or put `fileName` in yourself.

### `FileUploadToResourceField.UploadInfo`

|  | Before | After |
|---|---|---|
| Constructor | `UploadInfo(String clientFileName, long size, String contentType)` | `UploadInfo(String fileName, String clientFileName, String contentType)`. The old one is deprecated, uses the client file name as the stored name and ignores the size |
| `getSize()` and `get()` | The size the browser reported | The length and the bytes of the stored file; 0 and `null` when no stored file is attached |
| Stored name | None | `getFileName()` |
| `equals()` and `hashCode()` | By client file name | By stored name: two files of the same client name are no longer equal |
| `fromJson()` | Reads `clientFileName`, `size`, `contentType` | Also reads `fileName`, and falls back to `clientFileName` when it is missing, as in the answer of a resource written for Wicket 10 |

### `FileUploadToResourceField`: errors

|  | Before | After |
|---|---|---|
| Failure other than a limit (`uploadFailed`) | The text interpolates `${exception.localizedMessage}`, where the field had put a String, so the Ajax request failed and the user landed on the error page | `onUploadFailure()` receives "The upload failed." (key `uploadToResourceFailed`) |
| HTTP error | The status text was looked up as a resource key: `MissingResourceException`, and the error page | Read as a failed upload |
| Empty key (HTTP/2, network failure) | "File must be less than …" (`uploadTooLarge`) | Network failure: "The upload failed: the connection to the server was lost." (key `uploadConnectionLost`). HTTP/2 error: read as a failed upload |
| Unknown key | `MissingResourceException`, and the error page | Read as a failed upload |
| Choosing the text for a key | Private | `protected String getUploadErrorMessage(String errorKey)` |

To migrate:

- If you changed the text of `uploadFailed` to work around the error page, change `uploadToResourceFailed` instead.
- To word errors that your own script reports, override `getUploadErrorMessage()`. Answer the keys you know and leave the others to `super`. Never look up a key you were given as it is: it comes from the browser.

New public constants: `UPLOAD_FAILED_RESOURCE_KEY` (`"uploadToResourceFailed"`) and`CONNECTION_LOST_RESOURCE_KEY` (`"uploadConnectionLost"`).

New resource keys, in `FileUploadToResourceField.properties`:

| Key | Text |
|---|---|
| `uploadToResourceFailed` | The upload failed. |
| `uploadConnectionLost` | The upload failed: the connection to the server was lost. |
| `uploadToResourceRemove` | Remove |

### `FileUploadToResourceField`: browser side

|  | Before | After |
|---|---|---|
| `getClientBeforeSendCallBack()` default | `function (xhr, settings) { return true; }`. The return value was ignored and `settings` never passed | `function (xhr) { return true; }`.Returning `false` keeps the upload from being sent and calls `onUploadCanceled()` |
| `errorMessage` given to `getClientSideUploadErrorCallBack()` | A resource key, an HTTP status text, or an exception message | Only keys: `uploadTooLarge`, `uploadSingleFileTooLarge`, `uploadTooManyFiles`, `wicket.no.files.selected`, `uploadFailed`, `uploadConnectionLost` |
| Upload started while one runs | A second request was sent, and the two broke each other | Refused: `upload()` returns `false` and logs it. This holds as well for a field rendered anew by Ajax during the upload, which takes the upload over |
| `cancelUpload()` after an Ajax rendering of the field during an upload | Did nothing: the upload went on | Cancels the upload |
| Files beyond a limit of the field | Sent, then refused by the server, often with a reset connection | Not sent. The error callback gets the key of the limit, `onUploadFailure()` its text |
| Telling the field which files arrived | `GET` with the files in the URL. Beyond about 65 files the server answered 414, and `onUploadSuccess()` was never called | `POST` |
| `cancel()` after the upload ended | Called the cancel callback and logged "canceled" | Only logs that there is nothing to cancel |
| Answer that is not JSON | The field waited forever | The upload fails with `uploadFailed` |
| Timeout, set on the request by the before-send callback | Neither the error callback nor `onUploadFailure()` was called | The upload fails with `uploadFailed` |

To migrate, check a before-send callback that returns `false`: it used to be ignored, and now cancels the upload. Also check an error callback that expects HTTP status texts.

### Drop zone and file list

- `FileDropZoneBehavior` makes any component a drop zone for the files of a field.
- `FileListBehavior`, on a `<ul>` or `<ol>`, lists the files chosen for the next upload, each with a remove button. With it, a `multiple` input collects files over several choices.

Without these behaviors, the field works as before: a new choice replaces the files of the input.

## Changes in Wicket 11 releases after 11.0.0

### 11.1.0: `PageParameters` in hash-based collections

`PageParameters.hashCode()` now agrees with `equals()`, which ignores the order of the named parameters ([#1866](https://github.com/apache/wicket/issues/1866)). The fix is backported from Wicket 12. Before, two `PageParameters` that differed only in that order were equal but had different hashes, so a `HashMap` or `HashSet` kept them as two keys.

This shows in rendered pages. When a page renders a CSS or JavaScript reference header item twice for the same resource, with the same page parameters added in a different order, it now gets one `<link>` or `<script>` tag, with the URL of the first item. Before, it got two. An application that keeps `PageParameters` in a hash-based collection of its own now finds reordered parameters under the same entry.

## New in Wicket 11, without migration

These are additions. An application that does not use them sees no difference.

- **Ajax without jQuery**: `WicketAjaxResourceReference` is an implementation of Wicket's Ajax library without jQuery, with the same JavaScript API. jQuery stays the default. Switch with`getJavaScriptLibrarySettings().setWicketAjaxReference(WicketAjaxResourceReference.get())`.
- **Ajax veils**: `PageVeilBehavior` and `LocalVeilBehavior` in `wicket-extensions` block the page or one component while an Ajax request runs.
- **Uploads**: `FileDropZoneBehavior` and `FileListBehavior`, described in the uploads section above.
