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
	 * Tries to find
	 * <p>
	 * {@inheritDoc}
	 * @see org.eclipse.birt.report.model.api.IResourceLocator#findResource(org.eclipse.birt.report.model.api.ModuleHandle, java.lang.String, int)
	 */
	@Override
	public URL findResource(ModuleHandle handle, String fileName, int type) {
		String locale = ReportLayoutLocalisationData.extractLocale(fileName);
		if (locale == null)
			locale = "";

		ReportLayout layout = getCurrentReportLayout();
		if (layout != null) {
			ReportLayoutLocalisationDataID localisationID = ReportLayoutLocalisationDataID.create(
					layout.getOrganisationID(),
					layout.getReportRegistryItemType(),
					layout.getReportRegistryItemID(),
					locale
			);
			PersistenceManager pm = JDOHelper.getPersistenceManager(layout);
			if (pm == null)
				throw new IllegalStateException("Could not obtain PersistenceManager for ReportLayout "+JDOHelper.getObjectId(layout));
			ReportLayoutLocalisationData localisationData = null;
			try {
				localisationData = (ReportLayoutLocalisationData) pm.getObjectById(localisationID);
				localisationData.getLocale(); // WORKAROUND: Need to access
			} catch (JDOObjectNotFoundException e) {
				return null;
			}
			File layoutRoot = ReportLayoutRendererUtil.getRenderedLayoutOutputFolder();
			File outputFile = new File(layoutRoot, fileName);
			InputStream in = localisationData.createLocalisationDataInputStream();
			try {
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
				return outputFile.toURL();
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
		}
		return super.findResource(handle, fileName, type);
	}
}
