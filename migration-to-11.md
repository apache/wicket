# Migrating to Wicket 11

This file describes changes in Wicket 11 that break existing code or change behaviour, and what
an application has to do about them. The official guide is going to be the wiki page
[Migration to Wicket 11.0](https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+11.0),
which does not exist yet.

The OpenRewrite recipe `org.apache.wicket.MigrateToWicket11`
(`wicket-migration/src/main/resources/META-INF/rewrite/wicket.yml`) handles the mechanical part
of the migration. Its comment lists the other Wicket 11 changes that need manual migration.

## Uploads to a resource (`FileUploadToResourceField`)

Pull request [#1671](https://github.com/apache/wicket/pull/1671), issue
[#1670](https://github.com/apache/wicket/issues/1670). All classes are in
`org.apache.wicket.markup.html.form.upload.resource`. None of these changes is mechanical, so
there is no OpenRewrite recipe for them.

### Who has to change something

You have nothing to do if you use `FileUploadToResourceField` with the default
`FileUploadResourceReference`, and reach the uploaded files only through `UploadInfo#getFile()`.

You do have to act if you:

- implement `IUploadsFileManager`;
- find files stored by `FolderUploadsFileManager` by their client file name;
- extend `AbstractFileUploadResource` or `FileUploadResourceReference`;
- rely on `UploadInfo#equals()`, on the error texts of the field, or on what the browser-side
  callbacks receive.

### Why the API changed

`FolderUploadsFileManager` stored each file under its client file name, so two files of the same
name overwrote each other, in one upload or in two, while the field was told of both. Keeping
them apart needs a name for each stored file that the file manager chooses, and that name has to
travel from the file manager through the browser back to the field. That is why the file manager,
the resource and `UploadInfo` all gained a file name.

### `IUploadsFileManager`

Before:

```java
void save(FileUpload fileItem, String uploadFieldId);

File getFile(String uploadFieldId, String clientFileName);
```

After:

```java
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

- Store the file under a name of your own choice and return that name. It must be unique among
  the files of the upload field.
- `getFile()` then receives that name.
- The name goes to the browser and comes back from it, so it must not reveal anything the user
  is not supposed to know.
- Since the browser can change the name, `getFile()` must never resolve it to a file of another
  upload field.

An existing implementation of `save()` still compiles, with a removal warning, and keeps working
unchanged. The default `store()` calls it and returns the client file name, which means files of
the same name still overwrite each other until you implement `store()`.

Watch out:

- `save()` used to be abstract, and the compiler made you implement it. Now it has a default, so
  an implementation that overrides neither method compiles. At its first upload it fails with an
  `UnsupportedOperationException` that says to implement `store()`.
- Wicket no longer calls `save()`. If your own code calls it, call `store()` instead. On a manager
  that implements only `store()`, the deprecated `save()` throws an
  `UnsupportedOperationException`. `FolderUploadsFileManager` still implements both.

### `FolderUploadsFileManager`

| | Before | After |
|---|---|---|
| Stores with | `save()` | `store()`; the deprecated `save()` still works as before |
| File on disk | `<folder>/<uploadFieldId>/<client file name>` | `<folder>/<uploadFieldId>/<random UUID>` |
| `getFile(uploadFieldId, name)` | `name` is the client file name | `name` is the stored name |

To migrate, find the files with `UploadInfo#getFile()` or `UploadInfo#getFileName()`, not by
their client file name. Files stored before the upgrade keep their client file name on disk, and
`getFile(uploadFieldId, clientFileName)` still finds them. The checks against path traversal are
unchanged.

### `AbstractFileUploadResource`

Before:

```java
protected void saveFiles(List<FileUpload> fileItems, String uploadId)

protected void prepareResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest,
	List<FileUpload> fileItems)

protected abstract String generateJsonResponse(ResourceResponse resourceResponse,
	ServletWebRequest webRequest, List<FileUpload> files);
```

After:

```java
protected List<String> saveFiles(List<FileUpload> fileItems, String uploadId)

protected void prepareResponse(ResourceResponse resourceResponse, ServletWebRequest webRequest,
	List<FileUpload> fileItems, List<String> fileNames)

protected abstract String generateJsonResponse(ResourceResponse resourceResponse,
	ServletWebRequest webRequest, List<FileUpload> files, List<String> fileNames);
```

`fileNames` holds the stored name of each file, in the order of the files. There are no
deprecated overloads with the old signatures, on purpose: these methods are hooks that the
resource calls, so an override of an old signature would compile and then never be called. With
`@Override` it now fails to compile, which tells you to migrate.

To migrate:

- Add the `List<String> fileNames` parameter to your overrides.
- In `generateJsonResponse()`, write each name into the JSON of its file as `fileName`.
- An override of `saveFiles()` returns the names that `IUploadsFileManager#store()` returned.

How the resource answers errors has changed as well:

| | Before | After |
|---|---|---|
| Answer to a failure | `{"error": true, "errorMessage": "<key>"}` | Unchanged |
| Exception other than a `FileUploadException` (the file manager failing, for example) | Not logged. With debug logging on, the exception message was sent as `errorMessage` | Logged at ERROR. The key `uploadFailed` is sent, never the exception message |

### `FileUploadResourceReference`

Before:

```java
protected void generateFileInfo(JSONObject fileJson, FileUpload fileItem)
```

After:

```java
protected void generateFileInfo(JSONObject fileJson, FileUpload fileItem, String fileName)
```

The JSON of each file gets `fileName` next to `clientFileName`, `size` and `contentType`. To
migrate, add the parameter to your override, and keep calling `super` or put `fileName` in
yourself.

### `FileUploadToResourceField.UploadInfo`

| | Before | After |
|---|---|---|
| Constructor | `UploadInfo(String clientFileName, long size, String contentType)` | `UploadInfo(String fileName, String clientFileName, long size, String contentType)`. The old one is deprecated and uses the client file name as the stored name |
| Stored name | None | `getFileName()` |
| `equals()` and `hashCode()` | By client file name | By stored name: two files of the same client name are no longer equal |
| `fromJson()` | Reads `clientFileName`, `size`, `contentType` | Also reads `fileName`, and falls back to `clientFileName` when it is missing, as in the answer of a resource written for Wicket 10 |

### `FileUploadToResourceField`: errors

| | Before | After |
|---|---|---|
| Failure other than a limit (`uploadFailed`) | The text interpolates `${exception.localizedMessage}`, where the field had put a String, so the Ajax request failed and the user landed on the error page | `onUploadFailure()` receives "The upload failed." (key `uploadToResourceFailed`) |
| HTTP error | The status text was looked up as a resource key: `MissingResourceException`, and the error page | Read as a failed upload |
| Empty key (HTTP/2, network failure) | "File must be less than …" (`uploadTooLarge`) | Network failure: "The upload failed: the connection to the server was lost." (key `uploadConnectionLost`). HTTP/2 error: read as a failed upload |
| Unknown key | `MissingResourceException`, and the error page | Read as a failed upload |
| Choosing the text for a key | Private | `protected String getUploadErrorMessage(String errorKey)` |

To migrate:

- If you changed the text of `uploadFailed` to work around the error page, change
  `uploadToResourceFailed` instead.
- To word errors that your own script reports, override `getUploadErrorMessage()`. Answer the
  keys you know and leave the others to `super`. Never look up a key you were given as it is: it
  comes from the browser.

New public constants: `UPLOAD_FAILED_RESOURCE_KEY` (`"uploadToResourceFailed"`) and
`CONNECTION_LOST_RESOURCE_KEY` (`"uploadConnectionLost"`).

New resource keys, in `FileUploadToResourceField.properties`:

| Key | Text |
|---|---|
| `uploadToResourceFailed` | The upload failed. |
| `uploadConnectionLost` | The upload failed: the connection to the server was lost. |

### `FileUploadToResourceField`: browser side

| | Before | After |
|---|---|---|
| `getClientBeforeSendCallBack()` default | `function (xhr, settings) { return true; }`. The return value was ignored and `settings` never passed | `function (xhr) { return true; }`. Returning `false` keeps the upload from being sent and calls `onUploadCanceled()` |
| `errorMessage` given to `getClientSideUploadErrorCallBack()` | A resource key, an HTTP status text, or an exception message | Only keys: `uploadTooLarge`, `uploadSingleFileTooLarge`, `uploadTooManyFiles`, `wicket.no.files.selected`, `uploadFailed`, `uploadConnectionLost` |
| Upload started while one runs | A second request was sent, and the two broke each other | Refused: `upload()` returns `false` and logs it |
| Files beyond a limit of the field | Sent, then refused by the server, often with a reset connection | Not sent. The error callback gets the key of the limit, `onUploadFailure()` its text |
| Telling the field which files arrived | `GET` with the files in the URL. Beyond about 65 files the server answered 414, and `onUploadSuccess()` was never called | `POST` |
| `cancel()` after the upload ended | Called the cancel callback and logged "canceled" | Only logs that there is nothing to cancel |
| Answer that is not JSON | The field waited forever | The upload fails with `uploadFailed` |
| Timeout, set on the request by the before-send callback | Neither the error callback nor `onUploadFailure()` was called | The upload fails with `uploadFailed` |

To migrate, check a before-send callback that returns `false`: it used to be ignored, and now
cancels the upload. Also check an error callback that expects HTTP status texts.
