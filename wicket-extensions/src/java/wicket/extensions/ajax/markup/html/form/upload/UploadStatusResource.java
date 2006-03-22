package wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import wicket.RequestCycle;
import wicket.protocol.http.WebRequest;
import wicket.resource.DynamicByteArrayResource;

/**
 * UploadResourceStatus 
 * 
 * @author Andrew Lombardi
 * @author Igor Vaynberg (ivaynberg)
 */
public class UploadStatusResource extends DynamicByteArrayResource
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
		private String status;

		public UploadResourceState()
		{

			RequestCycle rc = RequestCycle.get();
			HttpServletRequest req = ((WebRequest)rc.getRequest()).getHttpServletRequest();
			UploadInfo info = UploadWebRequest.getUploadInfo(req);

			if (info == null)
			{

				// status = "70.1 KB of 1.9 MB at 61.0 KB/s; half a minute
				// remaining<script>if($('UploadProgressBar1')){$('UploadProgressBar1').firstChild.firstChild.style.width='3%'}</script>";
				status = "0|0|0|0";
			}
			else
			{
				if (info.getTotalBytes() > 0)
				{

					// status = "70.1 KB of 1.9 MB at 61.0 KB/s; half a minute
					// remaining<script>if($('UploadProgressBar1')){$('UploadProgressBar1').firstChild.firstChild.style.width='"+percentageComplete+"%'}</script>";
					status = "" + info.getPercentageComplete() + "|"
							+ info.getBytesUploadedString() + "|"
							+ info.getTotalBytesString() + "|"
							+ info.getTransferRateString() + "|"
							+ info.getRemainingTimeString();
				}
				else
				{
					status = "0|0|0|0";
				}
			}
		}

		public String getContentType()
		{
			return "text/plain";
		}

		public int getLength()
		{
			return status.length();
		}

		public byte[] getData()
		{
			return status.getBytes();
		}
	}
}
