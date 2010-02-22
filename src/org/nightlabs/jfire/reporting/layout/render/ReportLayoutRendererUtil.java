/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.reporting.ReportingEngine;
import org.nightlabs.jfire.reporting.RoleConstants;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.NLLocale;

/**
 * Helper class for {@link ReportLayoutRenderer} to unify their behaviour.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportLayoutRendererUtil {

	private static final String PROTECTIONFILE_NAME = "protectionfile";

	/**
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(ReportLayoutRendererUtil.class);

	/**
	 * Renders the {@link ReportLayout} referenced by the given {@link RenderReportRequest}
	 * and returns the resulting {@link RenderedReportLayout}.
	 * <p>
	 * This method checks if the current user has the RoleID {@link RoleConstants#renderReport}
	 * </p>
	 *
	 * @param pm The {@link PersistenceManager} to use to obtain the ReportEngine.
	 * @param renderReportRequest The request defining which report to render, to which format it
	 * 		should be rendered and which Locale should be applied etc.
	 * @return The resulting {@link RenderedReportLayout} that can be used to transfer the report.
	 * @throws RenderReportException When rendering the report fails.
	 */
	public static RenderedReportLayout renderReport(PersistenceManager pm, RenderReportRequest renderReportRequest)
			throws RenderReportException {

		// check if user is allowed to render
		ReportRegistryItem registryItem = (ReportRegistryItem) pm.getObjectById(renderReportRequest.getReportRegistryItemID());
		Authority.resolveSecuringAuthority(pm, registryItem, ResolveSecuringAuthorityStrategy.organisation).assertContainsRoleRef(
				SecurityReflector.getUserDescriptor().getUserObjectID(), RoleConstants.renderReport);

		Locale locale = renderReportRequest.getLocale();
		try {
			NLLocale.setOverrideLocale(locale);

			ReportingEngine reportingEngine = ReportingEngine.getReportingEngine(pm, null);
			if (reportingEngine != null) {
				IRenderManager rm = reportingEngine.createRenderManager(renderReportRequest);
				return rm.renderReport(pm, renderReportRequest);
			} else {
				throw new RenderReportException("Could not obtain ReportingEngine from datastore.", new NullPointerException());
			}

		} finally {
			NLLocale.setOverrideLocale(null);
		}
	}

	/**
	 * @return the root folder for all temporary rendered report folders.
	 * 		This is (JFireReporting#earDir/birt/rendered).
	 */
	public static File getRenderedLayoutOutputRootFolder() {
//		File earDir;
//		try {
//			earDir = JFireReportingEAR.getEARDir();
//		} catch (Exception e) {
//			throw new IllegalStateException("Could not obtain archive directory!",e);
//		}
		File renderedRoot = IOUtil.getUserTempDir("jfire_birt.rendered.", null);
//		File renderedRoot = new File(earDir, "birt"+File.separator+"rendered");
		if (!renderedRoot.exists()) {
			renderedRoot.mkdirs();
			CacheDirTag cacheDirTag = new CacheDirTag(renderedRoot.getParentFile());
			try {
				cacheDirTag.tag(ReportLayoutRendererUtil.class.getName(), true, false);
			} catch (IOException e) {
				logger.warn("Could not tag rendered report tmp dir.", e);
			}
		}
		return renderedRoot;
	}

	/**
	 * @return A folder where the a report for the calling session can safely be placed in.
	 */
	public static File getRenderedLayoutOutputFolder() {
		File layoutRoot = getRenderedLayoutOutputRootFolder();
		layoutRoot = new File(layoutRoot, SecurityReflector.getUserDescriptor().getSessionID()+"-"+Long.toHexString(Thread.currentThread().getId()));
		return layoutRoot;
	}

	/**
	 * Retruns after asuring that a folder exists, that can be uniquely addressed
	 * using the sessionID of the actual user.
	 *
	 * @return The root folder for renderedReportLayouts of the actual session.
	 */
	public static File prepareRenderedLayoutOutputFolder() {
		File layoutRoot = getRenderedLayoutOutputFolder();
		if (layoutRoot.exists()) {
			if (!IOUtil.deleteDirectoryRecursively(layoutRoot))
				throw new IllegalStateException("Could not delete rendered report tmp folder "+layoutRoot);
		}
		if (!layoutRoot.exists()) {
			if (!layoutRoot.mkdirs())
				throw new IllegalStateException("Could not create rendered report tmp folder "+layoutRoot);
		}
		getLayoutOutputFolderProtectionFile(layoutRoot, true);
		logger.debug("Returning rendered layout outputfolder: "+layoutRoot);
		return layoutRoot;
	}

	/**
	 * Returns and creates if necessary a file that tags the given directory as used.
	 * @param layoutRoot The folder to be tagged.
	 * @param createIfNotExist Whether the file should be created if it does not exist.
	 * @return The protection file object.
	 */
	public static File getLayoutOutputFolderProtectionFile(File layoutRoot, boolean createIfNotExist) {
		File protectionFile = new File(layoutRoot, PROTECTIONFILE_NAME);
		if (!protectionFile.exists() && createIfNotExist) {
			try {
				if (!protectionFile.createNewFile()) {
					throw new IOException("Could not create protecitonFile " + protectionFile.getAbsolutePath());
				}
				protectionFile.deleteOnExit();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return protectionFile;
	}

	/**
	 * Takes the root Folder of a rendered BIRT Layout and populates the given
	 * {@link RenderedReportLayout}'s data with its contents.
	 * <p>
	 * If doZip is true, the whole folder will be zipped and set as data. If it is false this method
	 * tries to locate [entryFileName].[OUTPUTFORMAT] and set this as data (not zipped of course).
	 * </p>
	 * <p>
	 * Additionally note, that regardless of the value of doZip, if this method finds the given
	 * rendered report layout to consist of multiple files the zipping of the whole folder is
	 * forced.
	 * </p>
	 *
	 * @param layoutRoot The root folder for the rendered layout (e.g. containig one html file and
	 *            several images, or a single pdf).
	 * @param reportLayout The {@link RenderedReportLayout} with a valid header. The data member of
	 *            this instance will be manipulated (populated with the file data).
	 * @param doZip Whether to zip contents or not. (Note, that if the contents of the report are
	 *            found to consist of multiple files zipped is forced to be <code>true</code>).
	 */
	public static void prepareRenderedLayoutForTransfer(File layoutRoot, RenderedReportLayout reportLayout, String entryFileName, boolean doZip) {

		// remove all localisation data needed for the rendering first...
		ReportLayoutLocalisationData.cleanFolderFromLocalisationData(layoutRoot);

		File[] layoutFiles = layoutRoot.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().startsWith(PROTECTIONFILE_NAME);
			}
		});

		boolean multipleFiles = layoutFiles != null && layoutFiles.length > 1;
		reportLayout.getHeader().setMultipleFiles(multipleFiles);
		if (multipleFiles) {
			// if we find that the layout consists of multiple files, we force zipping
			doZip = true;
		}

		// zip the complete folder
		File transferFile = null;

		if (doZip) {
			reportLayout.getHeader().setZipped(true);
			transferFile = new File(layoutRoot, entryFileName+".zip");
			try {
				IOUtil.zipFolder(transferFile, layoutRoot);
			} catch (IOException e) {
				throw new IllegalStateException("Could not zip the rendered layout.", e);
			}
		}
		else {
			reportLayout.getHeader().setZipped(false);
			transferFile = new File(layoutRoot, entryFileName+"."+reportLayout.getHeader().getOutputFormat().toString());
		}

		// set the header information
		reportLayout.getHeader().setEntryFileName(entryFileName+"."+reportLayout.getHeader().getOutputFormat().toString());

		// read the data into the result RenderedReportLayout
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(transferFile));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not find zip file "+transferFile, e);
		}
		try {
			DataBuffer dataBuffer = null;
			try {
				dataBuffer = new DataBuffer(buf);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create DataBuffer!", e);
			}
			reportLayout.setData(dataBuffer.createByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Could not create the rendered report data", e);
		} finally {
			try {
				buf.close();
			} catch (IOException e) {
				throw new IllegalStateException("Could not close FileInputStream", e);
			}
		}
		// delete the protection file for this folder,
		// so it can be deleted later
		File protectionFile = getLayoutOutputFolderProtectionFile(layoutRoot, false);
		if (protectionFile.exists()) {
			try {
				if (!protectionFile.delete()) {
					logger.warn("Could not delete render folder protection file " + protectionFile);
				}
			} catch (Throwable t) {
				logger.warn("Could not delete render folder protection file " + protectionFile, t);
			}
		}
	}

	/**
	 * Cleans up the folder with the rendered report layouts.
	 * (Deletes all output directories, that are not protected by a protection file any more).
	 */
	public static void cleanupRenderedReportLayoutFolders() {
		logger.debug("#cleanUpRenderedReportLayoutFolders: started.");
		File outputFolder = getRenderedLayoutOutputRootFolder();
		logger.debug("#cleanUpRenderedReportLayoutFolders: listing files.");
		File[] files = outputFolder.listFiles();
		if (files == null) {
			// nothing to do
			logger.debug("#cleanUpRenderedReportLayoutFolders: nothing to do, no sub-folders found.");
			return;
		}
		for (File layoutRoot : files) {
			if (!layoutRoot.isDirectory())
				continue;
			File protectionFile = getLayoutOutputFolderProtectionFile(layoutRoot, false);
			if (!protectionFile.exists()) {
				logger.debug("#cleanUpRenderedReportLayoutFolders: Found unlocked directory, will delete it: " + layoutRoot);
				if (!IOUtil.deleteDirectoryRecursively(layoutRoot)) {
					logger.warn("Could not delete report render folder " + layoutRoot);
				}
			}
		}
		logger.debug("Cleanup of rendered report layout folders finished.");
	}
}
