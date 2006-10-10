/*
 * $Id: AbstractTime.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision: 5874 $ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.ajax.markup.html.form.upload;

import javax.servlet.http.HttpServletRequest;

import wicket.RequestCycle;
import wicket.markup.html.DynamicWebResource;
import wicket.protocol.http.WebRequest;

/**
 * A resource that prints out basic statistics about the current upload. This
 * resource is used to feed the progress bar information by the progress bar
 * javascript which requests this resource through ajax.
 * 
 * @author Andrew Lombardi
 * @author Igor Vaynberg (ivaynberg)
 */
class UploadStatusResource extends DynamicWebResource
{
	/**
	 * Dynamic resource for upload state.
	 */
	private static class UploadResourceState extends DynamicWebResource.ResourceState
	{
		/**
		 * status string that will be returned to javascript to be parsed
		 * 
		 * uploaded count|total count|transfer rate|time remaining
		 */
		private String status;

		/**
		 * Construct.
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
			status = "<html>|" + status + "|</html>";
		}

		/**
		 * @see wicket.markup.html.DynamicWebResource.ResourceState#getContentType()
		 */
		@Override
		public String getContentType()
		{
			return "text/plain";
		}

		/**
		 * @see wicket.markup.html.DynamicWebResource.ResourceState#getData()
		 */
		@Override
		public byte[] getData()
		{
			return status.getBytes();
		}

		/**
		 * @see wicket.markup.html.DynamicWebResource.ResourceState#getLength()
		 */
		@Override
		public int getLength()
		{
			return status.length();
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.markup.html.DynamicWebResource#getResourceState()
	 */
	@Override
	protected ResourceState getResourceState()
	{
		return new UploadResourceState();
	}
}
