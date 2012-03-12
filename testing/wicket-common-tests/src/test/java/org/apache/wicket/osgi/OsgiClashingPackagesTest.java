package org.apache.wicket.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.wicket.util.string.Strings;
import org.junit.Assert;
import org.junit.Test;

/**
 * A test that verifies that there are no non-empty packages with the same name in
 * two or more wicket modules.
 *
 * Based on https://gist.github.com/1977817, contributed by Andreas Pieber
 */
public class OsgiClashingPackagesTest extends Assert
{

	@Test
	public void collectProjectPackages() throws IOException
	{
		char pathSeparator = System.getProperty("path.separator").charAt(0);
		String classpath = System.getProperty("java.class.path");
		String[] dependencies = Strings.split(classpath, pathSeparator);

		// packageName -> projects containing a package with this name
		Map<String, List<Project>> projectBuckets = new HashMap<String, List<Project>>();

		for (String dependency : dependencies)
		{
			// process only wicket-xyz.jar
			if (dependency.contains("wicket-") && dependency.endsWith(".jar"))
			{
				JarFile jarFile = new JarFile(dependency);
				try
				{
					String projectName = Strings.afterLast(dependency, '/');
					Project project = new Project(projectName, jarFile);
					project.addTo(projectBuckets);
				} finally {
					jarFile.close();
				}
			}
		}

		Set<Entry<String, List<Project>>> entrySet = projectBuckets.entrySet();
		for (Entry<String, List<Project>> entry : entrySet) {
			List<Project> projects = entry.getValue();
			if (projects.size() > 1) {
				fail(entry);
			}
		}
	}

	private void fail(Entry<String, List<Project>> entry) {
		StringBuilder builder = new StringBuilder();
		String packageName = entry.getKey();
		builder.append("Package '").append(packageName).append("' has files in two or more modules: ");
		for (Project conflict : entry.getValue()) {
			builder.append(conflict.getName()).append(", ");
		}
		try
		{
			builder.append("\nResources:\n");
			Enumeration<URL> resources = getClass().getClassLoader().getResources(packageName);
			while (resources.hasMoreElements())
			{
				URL resource = resources.nextElement();
				builder.append("\n\t").append(resource.toExternalForm());
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		fail(builder.toString());
	}

	private static class Project {
		// a set with all package names in a dependency
		private final Set<String> packagesWithContent = new TreeSet<String>();

		// the name of the dependency
		private final String name;

		public Project(String name, JarFile jarFile) {
			this.name = name;
			collectPackageNames(jarFile);
		}

		/**
		 * Adds this project to as a value in the global map that contains 'packageName -> List[Project]'
		 * @param projectBuckets
		 *      the global map
		 */
		public void addTo(Map<String, List<Project>> projectBuckets) {
			for (String packageWithContent : packagesWithContent) {
				if (!projectBuckets.containsKey(packageWithContent)) {
					projectBuckets.put(packageWithContent, new ArrayList<OsgiClashingPackagesTest.Project>());
				}
				projectBuckets.get(packageWithContent).add(this);
			}
		}

		/**
		 * Collects the names of all packages in this JarFile
		 * @param jarFile
		 *      the jar file to analyze
		 */
		private void collectPackageNames(final JarFile jarFile)
		{
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				JarEntry jarEntry = entries.nextElement();
				String entryName = jarEntry.getName();
				if (shouldCollect(entryName))
				{
					String packageName = Strings.beforeLast(entryName, '/');
					packagesWithContent.add(packageName);
				}
			}
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString()
		{
			return "Project{" +
					"name='" + name + '\'' +
					'}';
		}

		private boolean shouldCollect(final String entryName)
		{
			if (
				// ignore folder names. count just files/resources
				entryName.endsWith("/") ||

				// all modules have META-INF {MANIFEST.MF, Maven stuff, ..}
				entryName.startsWith("META-INF/") ||

				// ignore Wicket's IInitializer conf files
				(entryName.startsWith("wicket") && entryName.endsWith(".properties"))
			)
			{
				return false;
			}

			return true;
		}
	}

}
