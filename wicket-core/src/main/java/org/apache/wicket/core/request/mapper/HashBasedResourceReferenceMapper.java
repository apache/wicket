/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.core.request.mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.mapper.ParentPathReferenceRewriter;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.util.lang.Args;

/**
 * <p>
 * Resource reference {@link org.apache.wicket.request.IRequestMapper} that encodes class names as hash codes.
 * This allows hiding the class name from resource references. i.e., instead of <em>/wicket/resource/org.xxx.yyy.ZPanel/a.js</em> the
 * URL will display  <em>/wicket/resource/ddd/a.js</em>, where ddd = hash(org.xxx.yyy.ZPanel). This allows globally hiding
 * class structure of your application (not displaying it via URLs).
 * </p>
 *
 * <p>
 *     Caveat: we don't take into account hash collisions. I.e. two different class names having the same hash code.
 * </p>
 * <p>
 *     Note: if you want to hide the "wicket" part of URL for "xxx" you can do:
 *	   <code>
 *    protected IMapperContext newMapperContext() {
 * 		return new DefaultMapperContext(this) {
 *            public String getNamespace() {
 * 				return "xxx";
 *            }
 *        };
 *    }
 *    </code>
 *    on your Application class.
 * </p>
 * @author Ernesto Reinaldo Barreiro (reienr70 at gmail.com)
 */
public class HashBasedResourceReferenceMapper extends ParentPathReferenceRewriter
{
	/**
	 * Knows how to compute the hash of a class name.
	 */
	public interface IHasher {

		long computeHash(String name);
	}

	public static class HashBasedBasicResourceReferenceMapper extends BasicResourceReferenceMapper
	{

		private final Map<Long, String> hashMap = new ConcurrentHashMap<>();

		private final boolean checkHashCollision;

		private final IHasher hasher;

		/**
		 * Construct.
		 *
		 * @param pageParametersEncoder {@link IPageParametersEncoder}
		 * @param cachingStrategy Supplier for {@link IResourceCachingStrategy}
		 * @param checkHashCollision whether it should check for hash collisions or not.
		 * @param hasher method used to compute hash
		 */
		public HashBasedBasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
													 Supplier<? extends IResourceCachingStrategy> cachingStrategy,
													 boolean checkHashCollision,
													 IHasher hasher)
		{
			super(pageParametersEncoder, cachingStrategy);
            this.checkHashCollision = checkHashCollision;
			Args.notNull(hasher, "hasher");
            this.hasher = hasher;
        }

		@Override
		protected Class<?> resolveClass(String name)
		{
			try
			{
				long hash = Long.parseLong(name);
				String className = hashMap.get(hash);
				if (className == null)
				{
					return super.getPageClass(name);
				}
				return super.resolveClass(className);
			}
			catch (NumberFormatException e)
			{
				return super.getPageClass(name);
			}
		}

		@Override
		protected String getClassName(Class<?> scope)
		{
			String name = super.getClassName(scope);
			long hash = hasher.computeHash(name);
			if (checkHashCollision)	{
				String existing = hashMap.get(hash);
				if (existing != null && !existing.equals(name)) {
					throw new WicketRuntimeException("Class " + name + " has collision with " + existing);
				}
			}
			hashMap.putIfAbsent(hash, name);
			return Long.toString(hash);
		}
	}

	/**
	 * Construct.
	 *
	 * @param pageParametersEncoder {@link IPageParametersEncoder}
	 * @param parentPathPartEscapeSequence Supplier fpr String
	 * @param cachingStrategy Supplier fo IResourceCachingStrategy
	 * @param checkHashCollision whether it should check for hash collisions or not
	 * @param hasher method used to compute hash
	 */
	public HashBasedResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
											Supplier<String> parentPathPartEscapeSequence,
											Supplier<IResourceCachingStrategy> cachingStrategy,
											boolean checkHashCollision,
											IHasher hasher)
	{
		super(new HashBasedBasicResourceReferenceMapper(pageParametersEncoder, cachingStrategy, checkHashCollision, hasher), parentPathPartEscapeSequence);
	}

	/**
	 * Create  a HashBasedResourceReferenceMapper which uses standard java hash implementation.
	 *
	 * @param pageParametersEncoder {@link IPageParametersEncoder}
	 * @param parentPathPartEscapeSequence  Supplier for String
	 * @param cachingStrategy Supplier for {@link IResourceCachingStrategy}
	 * @param checkHashCollision whether it should check for hash collisions or not
	 * @return HashBasedResourceReferenceMapper
	 */
	public static HashBasedResourceReferenceMapper withJavaHash(IPageParametersEncoder pageParametersEncoder,
																Supplier<String> parentPathPartEscapeSequence,
																Supplier<IResourceCachingStrategy> cachingStrategy,
																boolean checkHashCollision) {
		return withOtherHash(pageParametersEncoder, parentPathPartEscapeSequence, cachingStrategy, checkHashCollision, new IHasher() {
			@Override
			public long computeHash(String name) {
				return name != null ? name.hashCode() : 0;
			}
		});
	}

	/**
	 * Creates a HashBasedResourceReferenceMapper with a custom {@link IHasher}
	 *
	 * @param pageParametersEncoder {@link IPageParametersEncoder}
	 * @param parentPathPartEscapeSequence {@link Supplier} for String
	 * @param cachingStrategy Supplier for {@link IResourceCachingStrategy}
	 * @param checkHashCollision whether it should check for hash collisions or not
	 * @param hasher method used to compute hash
	 * @return HashBasedResourceReferenceMapper
	 */
	public static HashBasedResourceReferenceMapper withOtherHash(IPageParametersEncoder pageParametersEncoder,
																Supplier<String> parentPathPartEscapeSequence,
																Supplier<IResourceCachingStrategy> cachingStrategy,
																boolean checkHashCollision, IHasher hasher) {
		return new HashBasedResourceReferenceMapper(pageParametersEncoder, parentPathPartEscapeSequence, cachingStrategy, checkHashCollision, hasher);
	}
}
