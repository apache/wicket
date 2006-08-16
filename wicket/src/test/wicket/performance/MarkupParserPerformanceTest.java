/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision$ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.MarkupContainer;
import wicket.markup.MarkupResourceStream;
import wicket.markup.loader.DefaultMarkupLoader;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.resource.DummyPage;
import wicket.util.file.Folder;
import wicket.util.io.Streams;
import wicket.util.resource.StringResourceStream;
import wicket.util.tester.WicketTester;

/**
 * Gather the names of all *.html files, load the content into memory, and than
 * randomly select any and load it. You may repeat this as often as you want to
 * for regretion tests, to find memory leaks, or for performance tests.
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

	/** List of all Wicket container classes; in the very same order as 'files' */
	private List<Class<? extends MarkupContainer>> containers;
	
	/** Wicket application object */
	private WicketTester application = new WicketTester(null);

	/** Time to execute a single test */
	private long duration[];
	
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
						int pos = name.indexOf("\\wicket");
						name = name.substring(pos + 1);
						name = name.substring(0, name.length() - 5);
						name = name.replace('\\', '.');
						name = name.replace("..", ".");
						
						// make sure the class loader loaded the class already
						try
						{
							Class clazz = this.getClass().getClassLoader().loadClass(name);
							
							Constructor constructor = null;
							try
							{
								constructor = clazz.getConstructor(new Class[] {});
							}
							catch (Exception ex)
							{
								try
								{
									constructor = clazz.getConstructor(new Class[] { MarkupContainer.class, String.class });
								}
								catch (Exception ex2)
								{
									log.error("Failed to load constructor for: " + clazz.toString());
									throw ex2;
								}
							}
//							Constructor constructor = containerClass.getConstructor(new Class[] { MarkupContainer.class, String.class, IModel.class }); 
							this.containers.add((Class<? extends MarkupContainer>)clazz);
						}
						catch (Exception ex)
						{
							// ignore
						}
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
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private void init() throws IOException
	{
		this.containers = new ArrayList<Class<? extends MarkupContainer>>();
		
		log.info("Scanning for HTML files");
		List<File> files = getFileListing(new Folder("src/test"));
		files.addAll(getFileListing(new Folder("src/java")));
		this.files = Collections.unmodifiableList(files);
		log.info("Found overall " + files.size() + " files");

		List<MarkupResourceStream> resources = loadFiles(files);
		log.info("All Files loaded into memory");

		this.resources = Collections.unmodifiableList(resources);
	}

	/**
	 * Parse the markup in sequentiell order. Usufull to identify hotspots.
	 * 
	 * @throws Exception
	 */
	private void parseAllSequentially() throws Exception
	{
		log.info("Sequentially parse all markups already loaded");

		int i = 0;
		MarkupResourceStream resourceStream = null;

		try
		{
			for (MarkupResourceStream stream : this.resources)
			{
				resourceStream = stream;
				DefaultMarkupLoader loader = new DefaultMarkupLoader(this.application);
				loader.loadMarkup(null, stream);

				if ((++i % 100) == 0)
				{
					log.info("... " + i);
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to load markup: " + this.files.get(i).toString());
			throw ex;
		}

		log.info("Finished: " + i);
	}

	/**
	 * Parse the markup in sequentiell order. Usufull to identify hotspots.
	 * 
	 * @throws Exception
	 */
	private void parseAllSequentiallyWithMarkupCache() throws Exception
	{
		log.info("Sequentially parse all markups (with MarkupCache)");

		DummyPage page = new DummyPage();
		
		int i = 0;
		Class<? extends MarkupContainer> markupContainerClass = null;

		try
		{
			for (Class<? extends MarkupContainer>containerClass : this.containers)
			{
				markupContainerClass = containerClass;
				
				Constructor constructor = null;
				try
				{
					constructor = containerClass.getConstructor(new Class[] {});
				}
				catch (Exception ex)
				{
					try
					{
						constructor = containerClass.getConstructor(new Class[] { MarkupContainer.class, String.class });
					}
					catch (Exception ex2)
					{
						log.error("Failed to load constructor for: " + markupContainerClass.toString());
					}
				}
//				Constructor constructor = containerClass.getConstructor(new Class[] { MarkupContainer.class, String.class, IModel.class }); 
//				this.application.getMarkupCache().getMarkupStream(container, true);

				if ((++i % 100) == 0)
				{
					log.info("... " + i);
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to load markup: " + markupContainerClass.toString());
			throw ex;
		}

		log.info("Finished: " + i);
	}

	/**
	 * Parse the markup in random order: regression and stress test
	 * 
	 * @param count
	 *            No if iterations
	 * @throws Exception
	 */
	private void parseRandomly(final int count) throws Exception
	{
		log.info("Randomly parse all markups already loaded");

		this.duration = new long[count];
		
		int index = 0;
		MarkupResourceStream resourceStream = null;

		try
		{
			Random random = new Random();
			int size = this.resources.size();

			for (int i = 0; i < count; i++)
			{
				index = random.nextInt(size);
				MarkupResourceStream stream = this.resources.get(index);
				resourceStream = stream;

				long time = System.currentTimeMillis();
				
				DefaultMarkupLoader loader = new DefaultMarkupLoader(this.application);
				loader.loadMarkup(null, stream);
				
				this.duration[i] = System.currentTimeMillis() - time;
				
				if ((i % 100) == 0)
				{
					log.info("... " + i);
				}
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to load markup: " + this.files.get(index).toString());
			throw ex;
		}

		log.info("Finished: " + count);
	}

	/**
	 * Some statistics
	 */
	private void stats()
	{
		int min = 0;
		int max = 0;
		int mean = 0;
		int stdDeviation = 0;
		int within15percent = 0;  
		int within30percent = 0;  
		int within45percent = 0;  
		int within90percent = 0;  
	}
	
	/**
	 * Execure the tests
	 */
	public void run()
	{
		try
		{
			// Initialize
			init();

			// ------------------------------------------------------------------
			// Sequential tests
			// ------------------------------------------------------------------
			{
//				int count = 200;
//				long time = System.currentTimeMillis();
//				for (int i = 0; i < count; i++)
//				{
//					parseAllSequentially();
//				}
//				long diff = System.currentTimeMillis() - time;
//				long avgDuration = diff / count;
//				long avgDurPerMarkup = avgDuration * 1000 / this.files.size();
//				log.info("Time: " + diff + "ms; average per run: " + avgDuration
//						+ "ms; avg per markup: 0." + avgDurPerMarkup + "ms");
			}

			// ------------------------------------------------------------------
			// Regression tests
			// ------------------------------------------------------------------
			{
//				long time = System.currentTimeMillis();
//				int count = 4000;
//				parseRandomly(count);
//				long diff = System.currentTimeMillis() - time;
//				long avgDurPerMarkup = diff * 1000 / count;
//				log.info("Time: " + diff + "ms; avg per markup: 0." + avgDurPerMarkup + "ms");
			}

			// ------------------------------------------------------------------
			// Sequential with MarkupCache
			// ------------------------------------------------------------------
			{
				int count = 200;
				long time = System.currentTimeMillis();
				for (int i = 0; i < count; i++)
				{
					parseAllSequentiallyWithMarkupCache();
				}
				long diff = System.currentTimeMillis() - time;
				long avgDuration = diff / count;
				long avgDurPerMarkup = avgDuration * 1000 / this.files.size();
				log.info("Time: " + diff + "ms; average per run: " + avgDuration
						+ "ms; avg per markup: 0." + avgDurPerMarkup + "ms");
			}
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
