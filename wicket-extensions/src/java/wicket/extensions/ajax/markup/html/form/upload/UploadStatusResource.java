package wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import wicket.RequestCycle;
import wicket.protocol.http.WebRequest;
import wicket.resource.DynamicByteArrayResource;

/**
 * A resource that prints out basic statistics about the current upload. This
 * resource is used to feed the progress bar information by the progress bar
 * javascript which requests this resource through ajax.
 * 
 * @author Andrew Lombardi
 * @author Igor Vaynberg (ivaynberg)
 */
class UploadStatusResource extends DynamicByteArrayResource
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ResourceState getResourceState()
	{
		return new UploadResourceState();
	}

	private static class UploadResourceState extends DynamicByteArrayResource.ResourceState
	{
		/**
		 * status string that will be returned to javascript to be parsed
		 * 
		 * uploaded count|total count|transfer rate|time remaining
		 */
		private String status;

		/**
		 * 
		 */
		public UploadResourceState()
		{

			RequestCycle rc = RequestCycle.get();
			HttpServletRequest req = ((WebRequest)rc.getRequest()).getHttpServletRequest();
			UploadInfo info = UploadWebRequest.getUploadInfo(req);

			if (info == null || info.getTotalBytes() < 1)
			{
				status = "0|0|0|0";
			}
			else
			{
				status = "" + info.getPercentageComplete() + "|" + info.getBytesUploadedString()
						+ "|" + info.getTotalBytesString() + "|" + info.getTransferRateString()
						+ "|" + info.getRemainingTimeString();
			}
		}

		/**
		 * @see wicket.resource.DynamicByteArrayResource.ResourceState#getContentType()
		 */
		public String getContentType()
		{
			return "text/plain";
		}

		/**
		 * @see wicket.resource.DynamicByteArrayResource.ResourceState#getLength()
		 */
		public int getLength()
		{
			return status.length();
		}

		/**
		 * @see wicket.resource.DynamicByteArrayResource.ResourceState#getData()
		 */
		public byte[] getData()
		{
			return status.getBytes();
		}
	}
}
