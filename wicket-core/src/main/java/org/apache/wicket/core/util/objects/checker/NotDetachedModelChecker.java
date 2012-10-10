package org.apache.wicket.core.util.objects.checker;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * An implementation of {@link IObjectChecker} that returns a failure
 * result when the checked object is a {@link LoadableDetachableModel}
 * and it is model object is still attached.
 */
public class NotDetachedModelChecker implements IObjectChecker
{
	@Override
	public Result check(Object obj)
	{
		Result result = Result.SUCCESS;

		if (obj instanceof LoadableDetachableModel<?>)
		{
			LoadableDetachableModel<?> model = (LoadableDetachableModel<?>) obj;
			if (model.isAttached())
			{
				result = new Result(Result.Status.FAILURE, "Not detached model found!");
			}
		}

		return result;
	}
}
