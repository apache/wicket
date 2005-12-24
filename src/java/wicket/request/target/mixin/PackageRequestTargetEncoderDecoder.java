/*
 * $Id: BookmarkablePageRequestTargetEncoderDecoder.java,v 1.1 2005/12/10 21:28:56 eelco12
 * Exp $ $Revision$ $Date$
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
package wicket.request.target.mixin;

import wicket.IRequestTarget;
import wicket.PageParameters;
import wicket.Session;
import wicket.request.IBookmarkablePageRequestTarget;
import wicket.request.target.BookmarkablePageRequestTarget;
import wicket.util.lang.Classes;

/**
 * Encodes and decodes mounts for a whole package.
 * 
 * @author Eelco Hillenius
 */
public class PackageRequestTargetEncoderDecoder extends AbstractRequestTargetEncoderDecoder
{
	/** package for this mount. */
	private final String mountedPackageName;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 * @param classOfPackageToMount
	 *            class from which the package name must be extracted for this mount
	 */
	public PackageRequestTargetEncoderDecoder(final String mountPath, Class classOfPackageToMount)
	{
		super(mountPath);
		mountedPackageName = getPackageName(classOfPackageToMount);
	}

	/**
	 * @see wicket.request.target.mixin.IRequestTargetEncoderDecoder#decode(java.lang.String)
	 */
	public IRequestTarget decode(String urlFragment)
	{
		String remainder = urlFragment.substring(getMountPath().length());
		final String parametersFragment;
		int ix = remainder.indexOf('/', 1);
		if (ix == -1)
		{
			ix = remainder.length();
			parametersFragment = "";
		}
		else
		{
			parametersFragment = remainder.substring(ix);
		}
		final String bookmarkablePageClassName = mountedPackageName + remainder.substring(1, ix);
		Class bookmarkablePageClass = Session.get().getClassResolver().resolveClass(
				bookmarkablePageClassName);
		PageParameters parameters = decodePageParameters(parametersFragment);
		BookmarkablePageRequestTarget target = new BookmarkablePageRequestTarget(
				bookmarkablePageClass, parameters);
		return target;
	}

	/**
	 * @see wicket.request.target.mixin.IRequestTargetEncoderDecoder#encode(wicket.IRequestTarget)
	 */
	public final String encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("this encoder can only be used with instances of "
					+ IBookmarkablePageRequestTarget.class.getName());
		}
		StringBuffer url = new StringBuffer();
		url.append(getMountPath());
		IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
		url.append("/").append(Classes.name(target.getPageClass()));
		appendPageParameters(url, target.getPageParameters());
		return url.toString();
	}

	/**
	 * @see wicket.request.target.mixin.IRequestTargetEncoderDecoder#matches(wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
			if ( mountedPackageName.equals(getPackageName(target.getPageClass())) )
			{
				return true;
			}
		}
		return false;
	}

	private String getPackageName(Class classOfPackageToMount)
	{
		String className = classOfPackageToMount.getName();
		int index = className.lastIndexOf(".");
		if(index != -1)
		{
			return className.substring(0,index+1); // including '.';
		}
		return "";
	}
}
