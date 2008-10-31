/**
 *
 */
package org.nightlabs.jfire.voucher.editor2d.iofilter;

import org.nightlabs.editor2d.iofilter.ManifestWriter;
import org.nightlabs.editor2d.iofilter.XStreamFilter;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class VoucherXStreamFilter
extends XStreamFilter
{
	public static final String FILE_EXTENSION = "v2d"; //$NON-NLS-1$
	public static final String VOUCHER_CONTENT_TYPE = "application/x-nightlabs-jfire-voucher"; //$NON-NLS-1$

	@Override
	protected String initDescription() {
		return "JFire Voucher Design File Format";
	}

	@Override
	protected String[] initFileExtensions() {
		return new String[] { FILE_EXTENSION };
	}

	@Override
	protected String initName() {
		return "JFire Voucher Design";
	}

	@Override
	protected ManifestWriter createManifestWriter() {
		ManifestWriter manifestWriter = super.createManifestWriter();
		manifestWriter.setContentType(VOUCHER_CONTENT_TYPE);
		return manifestWriter;
	}

}
