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
package wicket.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupCache;
import wicket.markup.MarkupParser;
import wicket.markup.MarkupParserFactory;
import wicket.markup.MarkupResourceStream;
import wicket.markup.loader.DefaultMarkupLoader;
import wicket.markup.parser.filter.EnclosureHandler;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.markup.resolver.EnclosureResolver;
import wicket.protocol.http.WebApplication;
import wicket.resource.DummyApplication;
import wicket.util.file.Folder;
import wicket.util.io.Streams;
import wicket.util.resource.StringResourceStream;
import wicket.util.tester.WicketTester;

/**
 * Gather the names of all *.html files, load the content into memory, and than
 * randomly select any and load it. You may repeat this as often as you want to
 * for regretion tests, to find memory leaks, or for performance tests. All
 * tests are CPU bound.
 * 
 * @author Juergen Donnerstag
 */
public class MarkupParserPerformanceTest
{
	private static final Log log = LogFactory.getLog(MarkupParserPerformanceTest.class);

	/**
	 * Some markup causes deliberately an exception and hence must be exluded
	 * from the test
	 */
	private static List<String> exclude;

	/** List of all HTML files */
	private List<File> files;

	/** List of all MarkupResourceStream; in the very same order as 'files' */
	private List<MarkupResourceStream> resources;

	private WebApplication application = new DummyApplication() 
	{
		@Override
		protected void init()
		{
			super.init();
			getPageSettings().addComponentResolver(new EnclosureResolver());
		}
	};

	/** Wicket tester object */
	private WicketTester tester = new WicketTester(this.application);

	/** Time to execute a single test */
	private double duration[];

	static
	{
		// Register wicket tags which are usually registered by the components
		WicketTagIdentifier.registerWellKnownTagName("border");
		WicketTagIdentifier.registerWellKnownTagName("body");
		WicketTagIdentifier.registerWellKnownTagName("panel");
		WicketTagIdentifier.registerWellKnownTagName("param");

		// HTML files which must be excluded as they are deliberately causing
		// exceptions
		exclude = new ArrayList<String>();
		exclude.add("basic\\SimplePage_4.html");
		exclude.add("basic\\SimplePanel_6.html");
		exclude.add("panel\\SimplePanel_1.html");
		exclude.add("outputTransformer\\PageExpectedResult_1.html");
		exclude.add("outputTransformer\\PageExpectedResult_2.html");
		exclude.add("util\\io\\test_3.html");
		exclude.add("util\\io\\test_4.html");
		exclude.add("util\\io\\test_5.html");
		exclude.add("util\\io\\test_6.html");
		exclude.add("util\\io\\test_7.html");
	}

	/**
	 * Get a list of all html files
	 * 
	 * @param startDir
	 * @return List
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	private List<File> getFileListing(final File startDir) throws FileNotFoundException
	{
		final List<File> result = new ArrayList<File>();
		final File[] filesAndDirs = startDir.listFiles();
		for (File file : filesAndDirs)
		{
			if (file.isFile() == false)
			{
				// Directory: recursive call
				final List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			}
			else
			{
				// Oly html files arre relevant
				if (file.getName().endsWith(".html"))
				{
					// Exclude all file known to be excluded
					boolean hit = false;
					for (String name : exclude)
					{
						if (file.getPath().endsWith(name))
						{
							hit = true;
							break;
						}
					}
					if (hit == false)
					{
						result.add(file);

						// Try to load the corresponding class
						String name = file.getPath();
					}
				}
			}
		}
		return result;
	}

	/**
	 * Load the markup files into memory to avoid IO bottlenecks during the
	 * tests
	 * 
	 * @param files
	 * @return List
	 * @throws IOException
	 */
	private List<MarkupResourceStream> loadFiles(final List<File> files) throws IOException
	{
		List<MarkupResourceStream> resources = new ArrayList<MarkupResourceStream>(files.size());

		int i = 0;
		for (File file : files)
		{
			String buf = Streams.readString(new FileInputStream(file));
			StringResourceStream stream = new StringResourceStream(buf);
			resources.add(new MarkupResourceStream(stream, null, null));

			if ((++i % 100) == 0)
			{
				log.info("... " + i);
			}
		}

		log.info("Finished: " + i);
		return resources;
	}

	/**
	 * Initialize
	 * 
	 * @param srcDirs
	 * @throws IOException
	 */
	private void init(final String[] srcDirs) throws IOException
	{
		this.application.getMarkupSettings().setMarkupParserFactory(new MarkupParserFactory()
		{
			@Override
			public MarkupParser newMarkupParser(MarkupResourceStream resource)
			{
				MarkupParser parser = super.newMarkupParser(resource);
				// register the additional EnclosureHandler
				parser.registerMarkupFilter(new EnclosureHandler());
				return parser;
			}
		});

		log.info("Scanning for HTML files");
		final List<File> files = new ArrayList<File>();
		for (final String srcDir : srcDirs)
		{
			files.addAll(getFileListing(new Folder(srcDir)));

		}
		this.files = Collections.unmodifiableList(files);
		log.info("Found overall " + files.size() + " files");

		List<MarkupResourceStream> resources = loadFiles(files);
		log.info("All Files loaded into memory");

		this.resources = Collections.unmodifiableList(resources);
	}

	/**
	 * Execute internalParseAllSequentially() N times to get better average
	 * values and for the profiler (if used) to collect more data.
	 * 
	 * @param count
	 */
	private void parseAllSequentially(final int count)
	{
		long time = System.currentTimeMillis();
		int anzFiles = 0;

		for (int i = 0; i < count; i++)
		{
			anzFiles += internalParseAllSequentially();

			if (((i % 10) == 0) && log.isDebugEnabled())
			{
				log.debug("Outer loop ... " + i + " / " + count);
			}
		}

		long diff = System.currentTimeMillis() - time;
		long avgDuration = diff / count;
		double avgDurPerMarkup = (double)diff / count / this.files.size();

		log.info(anzFiles + " files; " + count + " cycles; Time: " + diff + "ms; average per run: "
				+ avgDuration + "ms; avg per markup: " + avgDurPerMarkup + "ms");

		stats();
	}

	/**
	 * Parse the markup in sequentiell order without markup inheritance and
	 * without MarkupCache.
	 * 
	 * @return # of markup files parsed
	 * @throws Exception
	 */
	private int internalParseAllSequentially()
	{
		log.debug("Sequentially parse all markups already loaded");
		long time = System.currentTimeMillis();

		this.duration = new double[this.resources.size()];

		int i = 0;
		try
		{
			DefaultMarkupLoader loader = new DefaultMarkupLoader(this.application);
			for (MarkupResourceStream stream : this.resources)
			{
				loader.loadMarkup(null, stream);

				if (((++i % 100) == 0) && log.isDebugEnabled())
				{
					log.debug("... " + i);
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to load markup: " + this.files.get(i).toString());
		}

		long diff = System.currentTimeMillis() - time;
		log.info("Finished: " + i + " files; duration: " + diff + "ms; avg per file: "
				+ ((double)diff / i) + "ms");

		return i;
	}

	/**
	 * Execute internalParseAllSequentiallyWithMarkupCache() N times to get
	 * better average values and for the profiler (if used) to collect more
	 * data.
	 * 
	 * @param count
	 */
	private void parseAllSequentiallyWithMarkupCache(final int count)
	{
		long time = System.currentTimeMillis();
		int anzFiles = 0;
		for (int i = 0; i < count; i++)
		{
			anzFiles += internalParseAllSequentiallyWithMarkupCache();

			if ((i % 10) == 0)
			{
				log.info("Outer loop ... " + i);
			}
		}
		long diff = System.currentTimeMillis() - time;
		long avgDuration = diff / count;
		double avgDurPerMarkup = (double)avgDuration / this.files.size();
		log.info(anzFiles + " files; duration: " + diff + "ms; cycles: " + count
				+ "; average per run: " + avgDuration + "ms; avg per markup: " + avgDurPerMarkup
				+ "ms");
		stats();

	}

	/**
	 * Load the markup in sequentiell order. Though MarkupCache is used, the
	 * container is configured to not cache the markup loaded. Markup
	 * inheritance is tested and the markup is merged if needed.
	 * 
	 * @return file count
	 */
	private int internalParseAllSequentiallyWithMarkupCache()
	{
		log.debug("Sequentially parse all markups (with MarkupCache)");
		long overallTime = System.currentTimeMillis();

		this.duration = new double[this.files.size()];

		int i = 0;
		int failures = 0;
		File markupFile = null;
		MarkupCache cache = this.application.getMarkupCache();
		DummyPage page = new DummyPage();
		DummyContainer container = new DummyContainer(page);

		for (final File file : this.files)
		{
			markupFile = file;
			container.setMarkupFile(file);

			try
			{
				long time = System.currentTimeMillis();
				cache.getMarkup(container, true);
				this.duration[i] = System.currentTimeMillis() - time;
			}
			catch (Exception ex)
			{
				failures += 1;
				log.debug("Failed to load markup: " + this.files.get(i).toString());
			}

			if ((++i % 100) == 0)
			{
				log.debug("... " + i);
			}
		}

		long diff = System.currentTimeMillis() - overallTime;
		i -= failures;
		log.info(i + " files; duration: " + diff + "ms; avg per file: " + ((double)diff / i)
				+ "ms; failures: " + failures);

		return i;
	}

	/**
	 * Parse the markup in random order without markup inheritance and without
	 * MarkupCache.
	 * 
	 * @param count
	 *            No if iterations
	 * @return int
	 */
	private int parseRandomly(final int count)
	{
		log.debug("Randomly parse all markups already loaded");

		long overallTime = System.currentTimeMillis();
		this.duration = new double[count];

		int index = 0;
		try
		{
			Random random = new Random();
			int size = this.resources.size();
			DefaultMarkupLoader loader = new DefaultMarkupLoader(this.application);

			for (int i = 0; i < count; i++)
			{
				index = random.nextInt(size);
				MarkupResourceStream stream = this.resources.get(index);

				long time = System.currentTimeMillis();
				loader.loadMarkup(null, stream);
				this.duration[i] = System.currentTimeMillis() - time;

				if ((i % 500) == 0)
				{
					log.info("... " + i + " / " + count);
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to load markup: " + this.files.get(index).toString());
		}

		long diff = System.currentTimeMillis() - overallTime;
		double avgDurPerMarkup = (double)diff / count;

		log.info("Finished: " + count + " files; Time: " + diff + "ms; avg per markup: "
				+ avgDurPerMarkup + "ms");

		stats();

		return index;
	}

	/**
	 * Some statistics
	 */
	private void stats()
	{
		Univariate stats = new Univariate(this.duration);
		log.info("min: " + stats.min() + "; max: " + stats.max() + "; mean: " + stats.mean()
				+ "; median: " + stats.median() + "; stdev: " + stats.stdev() + "; variance: "
				+ stats.variance());
	}

	/**
	 * Execure the tests
	 */
	public void run()
	{
		try
		{
			// Initialize
			final String[] htmlDirs = new String[] { "src/java", "src/test" };
			init(htmlDirs);

			// ------------------------------------------------------------------
			// Sequential tests
			// ------------------------------------------------------------------
			parseAllSequentially(10);

			// ------------------------------------------------------------------
			// Random tests
			// ------------------------------------------------------------------
			parseRandomly(4000);

			// ------------------------------------------------------------------
			// Sequential with MarkupCache and inheritance
			// ------------------------------------------------------------------
			parseAllSequentiallyWithMarkupCache(10);

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new MarkupParserPerformanceTest().run();
	}
}
