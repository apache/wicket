package wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.servlet.MultipartServletWebRequest;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.util.lang.Bytes;
import wicket.util.upload.FileUploadException;

/**
 * @author Igor Vaynberg (ivaynberg)
 */
public class UploadWebRequest extends ServletWebRequest
{


	final HttpServletRequest req;

	/**
	 * Constructor
	 * 
	 * @param req
	 */
	public UploadWebRequest(final HttpServletRequest req)
	{
		super(req);
		this.req = req;
	}

	/**
	 * @see wicket.protocol.http.WebRequest#newMultipartWebRequest(wicket.util.lang.Bytes)
	 */
	public WebRequest newMultipartWebRequest(Bytes maxsize)
	{
		try
		{
			return new MultipartRequest(req, maxsize);
		}
		catch (FileUploadException e)
		{
			throw new WicketRuntimeException(e);
		}
	}


	private static class MultipartRequest extends MultipartServletWebRequest
	{
		/**
		 * @param req
		 * @param maxSize
		 * @throws FileUploadException
		 */
		public MultipartRequest(HttpServletRequest req, Bytes maxSize) throws FileUploadException
		{
			super(req, maxSize);
			if (req == null)
			{
				throw new IllegalStateException("req cannot be null");
			}
		}

		protected boolean wantUploadProgressUpdates()
		{
			return true;
		}

		protected void onUploadStarted(int totalBytes)
		{
			UploadInfo info = new UploadInfo(totalBytes);

			HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest())
					.getHttpServletRequest();
			setUploadInfo(request, info);
		}

		protected void onUploadUpdate(int bytesUploaded, int total)
		{
			HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest())
					.getHttpServletRequest();
			UploadInfo info = getUploadInfo(request);
			if (info == null)
			{
				throw new IllegalStateException(
						"could not find UploadInfo object in session which should have been set when uploaded started");
			}
			info.setBytesUploaded(bytesUploaded);

			setUploadInfo(request, info);
		}

		protected void onUploadCompleted()
		{
			HttpServletRequest request = ((WebRequest)RequestCycle.get().getRequest())
					.getHttpServletRequest();
			clearUploadInfo(request);
		}
	}

	private static final String SESSION_KEY = UploadWebRequest.class.getName();


	static UploadInfo getUploadInfo(HttpServletRequest req)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		return (UploadInfo)req.getSession().getAttribute(SESSION_KEY);
	}

	static void setUploadInfo(HttpServletRequest req, UploadInfo uploadInfo)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		if (uploadInfo == null)
		{
			throw new IllegalArgumentException("uploadInfo cannot be null");
		}
		req.getSession().setAttribute(SESSION_KEY, uploadInfo);
	}

	static void clearUploadInfo(HttpServletRequest req)
	{
		if (req == null)
		{
			throw new IllegalArgumentException("req cannot be null");
		}
		req.getSession().removeAttribute(SESSION_KEY);
	}

}