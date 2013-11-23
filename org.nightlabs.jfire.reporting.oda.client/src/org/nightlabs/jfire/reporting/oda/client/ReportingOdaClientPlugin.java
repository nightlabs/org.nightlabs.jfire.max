package org.nightlabs.jfire.reporting.oda.client;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.ui.Login;
import org.nightlabs.jfire.reporting.ReportManagerRemote;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ReportingOdaClientPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.nightlabs.jfire.reporting";

	// The shared instance
	private static ReportingOdaClientPlugin plugin;

	/**
	 * The constructor
	 */
	public ReportingOdaClientPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ReportingOdaClientPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a new ReportManager bean.
	 */
	public static ReportManagerRemote getReportManager() {
		try {
			return JFireEjb3Factory.getRemoteBean(ReportManagerRemote.class,
					Login.getLogin().getInitialContextProperties()
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
