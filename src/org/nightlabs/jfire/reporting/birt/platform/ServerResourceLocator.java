/**
 *
 */
package org.nightlabs.jfire.reporting.birt.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.model.api.DefaultResourceLocator;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.nightlabs.jfire.reporting.layout.LocalisationFileName;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.id.ReportLayoutLocalisationDataID;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.util.IOUtil;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ServerResourceLocator extends DefaultResourceLocator implements IResourceLocator
{
	private static Logger logger = Logger.getLogger(ServerResourceLocator.class);
	private static ThreadLocal<ReportLayout> currentReportLayout = new ThreadLocal<ReportLayout>();

	public static void setCurrentReportLayout(ReportLayout reportLayout)
	{
		currentReportLayout.set(reportLayout);
	}

	public static ReportLayout getCurrentReportLayout()
	{
		return currentReportLayout.get();
	}

	/**
	 *
	 */
	public ServerResourceLocator() {
	}

	/**
	 * Tries to find a resource (normally for localisation).
	 * <p>
	 * {@inheritDoc}
	 * @see org.eclipse.birt.report.model.api.IResourceLocator#findResource(org.eclipse.birt.report.model.api.ModuleHandle, java.lang.String, int)
	 */
	@Override
	public URL findResource(ModuleHandle handle, String fileName, int type) {
		LocalisationFileName localisationFileName = new LocalisationFileName(fileName);
		System.out.println("findResource called...");

		String locale = localisationFileName.getLocale();
		if (locale == null)
			locale = "";

		ReportLayout layout = getCurrentReportLayout();
		if (layout != null) {
			URL result = findResource(handle, fileName, type, layout, locale);

			// If the resource exists, we make sure that the fallback files exist, too, because BIRT
			// will search for them without calling this method again.
			// For example, if the fileName is "messages_de_CH.properties", we make sure
			// that "messages_de.properties" and "messages.properties" exist, too
			// (if the data exists in our database, only, of course).
			//
			// If the resource does not exist (i.e. result == null), we do nothing, because
			// BIRT will then call this method again for the next fallback file name.
			//
			// Marco.
			if (result != null) {
				while (!locale.isEmpty()) {
					int i = locale.lastIndexOf('_');
					if (i < 0)
						locale = "";
					else
						locale = locale.substring(0, i);

					String fn = localisationFileName.createFullName(locale);
					findResource(handle, fn, type, layout, locale);
				}

				return result;
			}
		}
		return super.findResource(handle, fileName, type);
	}

	private URL findResource(ModuleHandle handle, String fileName, int type, ReportLayout layout, String locale)
	{
		try {
			File layoutRoot = ReportLayoutRendererUtil.getRenderedLayoutOutputFolder();
			File outputFile = new File(layoutRoot, fileName);
			// TODO We cannot ensure that the directory is new (and thus empty), because the current API is not clean and an existing directory might be used.
			// Therefore, we temporarily force overwriting of the resource files. Freddy+Marco.
			if (false && outputFile.exists()) { // remove the 'false' here, when the cleanup is done!
				// This code is ALWAYS executed on the server-side with a unique directory for each report-rendering. TODO currently not true :-( Marco.
				if (logger.isDebugEnabled())
					logger.debug("findResource: The file already exists, will skip and thus NOT overwrite it: " + outputFile.getAbsolutePath());
			}
			else {
				ReportLayoutLocalisationDataID localisationID = ReportLayoutLocalisationDataID.create(
						layout.getOrganisationID(),
						layout.getReportRegistryItemType(),
						layout.getReportRegistryItemID(),
						locale
				);
				PersistenceManager pm = JDOHelper.getPersistenceManager(layout);
				if (pm == null)
					throw new IllegalStateException("Could not obtain PersistenceManager from ReportLayout "+JDOHelper.getObjectId(layout));

				ReportLayoutLocalisationData localisationData = null;
				try {
					localisationData = (ReportLayoutLocalisationData) pm.getObjectById(localisationID);
					localisationData.getLocale(); // WORKAROUND: Need to access
				} catch (JDOObjectNotFoundException e) {
					return null;
				}

				InputStream in = localisationData.createLocalisationDataInputStream();
				try {
					FileOutputStream out = new FileOutputStream(outputFile);
					try {
						IOUtil.transferStreamData(in, out);
					} finally {
						out.close();
					}
				} finally {
					in.close();
				}

				if (logger.isDebugEnabled())
					logger.debug("findResource: Created localisation file: " + outputFile.getAbsolutePath());
			}
			// TODO Shouldn't the following be outputFile.toURI().toURL() instead of outputFile.toURL()? The difference is AFAIK the
			// handling of spaces. The current code keeps spaces and simply adds 'file:' at the beginning, while the correct URL-form
			// would have %20 instead of spaces. What does BIRT expect?
			// Note, that this must be tested on Windows, because on Linux, there usually are NO spaces (the temp folder is /tmp).
			// On Windows, however, the temp folder often is located at "C:\Documents and Settings\....".
			// After testing both versions on Windows, it should be documented here! If both work, the non-deprecated version (toURI().toURL()) should be used instead.
			// Freddy+Marco.
			return outputFile.toURL();
		} catch (IOException e) {
			logger.error("findResource: " + e, e);
			return null;
		}
	}
}
