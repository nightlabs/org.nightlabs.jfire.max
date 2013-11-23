package org.nightlabs.jfire.reporting.engine.birt.classloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ReportingEngineBirtClassLoader
extends URLClassLoader
{
	private static final Logger logger = Logger.getLogger(ReportingEngineBirtClassLoader.class);

	/**
	 * We do not pass the parent CL to the super constructor in order to keep control of when to delegate to the parent.
	 */
	private ClassLoader parent;
//	private volatile ClassLoader osgiClassLoader;

	private static ReportingEngineBirtClassLoader sharedInstance = null;

	private static void populateURLs(List<URL> urls, File dir)
	{
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				populateURLs(urls, f);
				continue;
			}

			try {
				urls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public synchronized static ReportingEngineBirtClassLoader createSharedInstance(URL[] additionalUrls)
	{
		if (sharedInstance != null)
			throw new IllegalStateException("Already created!");

		List<URL> urls = new ArrayList<URL>();
		for (URL url : additionalUrls) {
			urls.add(url);
		}

		// determine the BIRT plugins directory
		File birtPlatformDir = new File(JFireServerDataDirectory.getJFireServerDataDirFile(), "birt");
		File birtLibDir = new File(birtPlatformDir, "lib");
//		File birtPluginsDir = new File(birtPlatformDir, "plugins");

		populateURLs(urls, birtLibDir);
//		// add some plugins to our ClassLoader's URLs:
//		// * BIRT-core JAR
//		// * our glue-project
//		for (File f : birtPluginsDir.listFiles()) {
//			String fileName = f.getName();
//			if (
//					fileName.startsWith("org.eclipse.birt.core_") ||
//					fileName.startsWith("org.nightlabs.jfire.reporting.birt_") ||
//					fileName.startsWith("org.eclipse.birt.report.engine_") ||
//					fileName.startsWith("org.eclipse.birt.report.model_")
//			)
//			{
//				try {
//					urls.add(f.toURI().toURL());
//				} catch (MalformedURLException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}

		sharedInstance = new ReportingEngineBirtClassLoader(
				urls.toArray(new URL[urls.size()]),
				ReportingEngineBirtClassLoader.class.getClassLoader()
		);

		return sharedInstance;
	}

	public synchronized static ReportingEngineBirtClassLoader sharedInstance() {
		if (sharedInstance == null)
			throw new IllegalStateException("Method createSharedInstance(...) was not called before!");

		return sharedInstance;
	}
	
	public synchronized static boolean isSharedInstanceCreated() {
		return sharedInstance != null;
	}

	private ReportingEngineBirtClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, null);
		this.parent = parent;
	}

//	public void setOsgiClassLoader(ClassLoader cl) {
//		this.osgiClassLoader = cl;
//	}

//	/* (non-Javadoc)
//	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
//	 */
//	@Override
//	public Class<?> loadClass(String name) throws ClassNotFoundException
//	{
//		if (logger.isDebugEnabled()) {
//			logger.debug("loadClass("+name+")");
//		}
//
////		if (isOsgiClass(name)) {
//////			throw new ClassNotFoundException("Filtering enabled for this class: " + name);
////			return loadOsgiClass(name);
////		}
////		return super.loadClass(name);
//
//		try {
//			return loadOsgiClass(name);
//		}
//		catch (ClassNotFoundException e) {
//			return super.loadClass(name);
//		}
//	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		return super.findResources(name);
	}

	@Override
	public URL findResource(String name) {
		return super.findResource(name);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
	 */
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
	throws ClassNotFoundException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("loadClass(\""+name+"\", "+resolve+")");
		}

		Class<?> result = null;

		if (isReverseLoadingStrategy(name)) {
			// reverse (ask child first instead of parent)

//			// first OSGI
//			try {
//				if (result == null)
//					result = loadOsgiClass(name);
//			} catch (ClassNotFoundException e) {
//				// ignore
//			}

			// 2nd our reporting-libs
			try {
				if (result == null)
					result = super.loadClass(name, resolve);
			} catch (ClassNotFoundException e) {
				// ignore
			}

			// 3rd parent
			try {
				if (result == null)
					result = parent.loadClass(name);
			} catch (ClassNotFoundException e) {
				// ignore
			}
		}
		else {
			// normal (ask parent first)

			// 1st parent
			try {
				if (result == null)
					result = parent.loadClass(name);
			} catch (ClassNotFoundException e) {
				// ignore
			}

			// 2nd our reporting-libs
			try {
				if (result == null)
					result = super.loadClass(name, resolve);
			} catch (ClassNotFoundException e) {
				// ignore
			}

//			// 3rd OSGI
//			try {
//				if (result == null)
//					result = loadOsgiClass(name);
//			} catch (ClassNotFoundException e) {
//				// ignore
//			}
		}

		if (result == null)
			throw new ClassNotFoundException(name);

		return result;
	}

	private boolean isReverseLoadingStrategy(String name) {
		return name.startsWith("org.mozilla");
	}

//	private Class<?> loadOsgiClass(String name) throws ClassNotFoundException
//	{
////		ClassLoader osgiClassLoader = this.osgiClassLoader;
////		if (osgiClassLoader == null)
////			throw new ClassNotFoundException("OsgiClassLoader not yet initialised, but this class should be loaded by OSGI: " + name);
////
////		return osgiClassLoader.loadClass(name);
//		return null;
//	}

//	/* (non-Javadoc)
//	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
//	 */
//	@Override
//	public Class<?> loadClass(String name) throws ClassNotFoundException
//	{
//		if (logger.isDebugEnabled()) {
//			logger.debug("loadClass("+name+")");
//		}
//		try {
//			return loadClassInternal(name);
//		} catch (ClassNotFoundException e) {
//			return super.loadClass(name);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
//	 */
//	@Override
//	protected synchronized Class<?> loadClass(String name, boolean resolve)
//	throws ClassNotFoundException
//	{
//		if (logger.isDebugEnabled()) {
//			logger.debug("loadClass("+name+", "+resolve+")");
//		}
//		try {
//			return loadClassInternal(name);
//		} catch (ClassNotFoundException e) {
//			return super.loadClass(name, resolve);
//		}
//	}

//	private Class<?> loadClassInternal(String name) throws ClassNotFoundException {
////		IBundle bundle = Platform.getBundle("org.mozilla.rhino");
////		if (bundle != null)
////			return bundle.loadClass(name);
////		else
////			throw new ClassNotFoundException("Classes starting with org.mozilla are ignored");
//		if (osgiClassLoader != null) {
//			Class<?> clazz = osgiClassLoader.loadClass(name);
//			if (logger.isDebugEnabled()) {
//				logger.debug("class = "+clazz.getName());
//				logger.debug(clazz.getName()+", classLoader="+clazz.getClassLoader());
//			}
//			return clazz;
//		}
//		else
//			throw new ClassNotFoundException("Classes starting with org.mozilla are ignored");
//	}

}
