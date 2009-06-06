/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.IOUtil;

/**
 * Helper class for handling {@link RenderedReportLayout}s.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class RenderedReportLayoutUtil {

	/**
	 * This method takes a {@link RenderedReportLayout} and stores its data to the disk
	 * under the given folder. This method takes into account that {@link RenderedReportLayout}s
	 * might have multiple files as result and might be zipped.
	 *
	 * @param folder The folder to place the result files from the {@link RenderedReportLayout}.
	 * @param layout The {@link RenderedReportLayout} to prepare.
	 * @param monitor A monitor to report progress to.
	 * @return The entry file of the prepared layout.
	 */
	public static File prepareRenderedReportLayout(File folder, RenderedReportLayout layout, ProgressMonitor monitor) {
		File zip = new File(folder, "renderedLayout.zip"); //$NON-NLS-1$
		File file = new File(folder, layout.getHeader().getEntryFileName());

		if (!layout.getHeader().isZipped()) {
			// redirect writing to entry file
			zip = file;
		}

		try {
			monitor.setTaskName("Storing rendered report layout");
			if (zip.exists())
				zip.delete();

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(zip));
			try {
				out.write((byte[])layout.getData());
			} finally {
				out.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (layout.getHeader().isZipped()) {
			monitor.setTaskName("Unzipping rendered report layout");
			try {
				IOUtil.unzipArchive(zip, folder);
			} catch (IOException e) {
				throw new IllegalStateException("Could not unzip rendered report layout", e); //$NON-NLS-1$
			}
		}

		return file;
	}
}
