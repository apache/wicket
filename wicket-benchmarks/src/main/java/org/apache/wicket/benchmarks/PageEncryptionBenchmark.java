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
package org.apache.wicket.benchmarks;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.apache.wicket.core.util.crypt.AesGcmCryptScheme;
import org.apache.wicket.core.util.crypt.ICryptScheme;
import org.apache.wicket.core.util.crypt.SchemeCrypt;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures encrypting a serialized page, which {@code CryptingPageStore} does for every page when
 * {@code StoreSettings#isEncrypted()} is on.
 * <p>
 * Sized like real pages rather than like a token: the payload is what gets copied, so anything the
 * path does per byte shows up here and not in a benchmark over short strings.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(3)
@Threads(1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class PageEncryptionBenchmark
{
	@Param({"5000", "40000"})
	public int payloadSize;

	private SchemeCrypt crypt;

	private byte[] plaintext;

	private byte[] encrypted;

	@Setup(Level.Trial)
	public void setUp()
	{
		SecureRandom random = new SecureRandom();
		ICryptScheme scheme = new AesGcmCryptScheme();
		SecretKey key = scheme.generateKey(random);
		crypt = new SchemeCrypt(key, random, scheme, List.of(scheme));

		plaintext = new byte[payloadSize];
		random.nextBytes(plaintext);
		encrypted = crypt.encrypt(plaintext);
	}

	@Benchmark
	public byte[] encrypt()
	{
		return crypt.encrypt(plaintext);
	}

	@Benchmark
	public byte[] decrypt()
	{
		return crypt.decrypt(encrypted);
	}
}
