package org.nightlabs.jfire.reporting;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.jfire.base.ui.login.Login;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ReportingPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.nightlabs.jfire.reporting";

	// The shared instance
	private static ReportingPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ReportingPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ReportingPlugin getDefault() {
		return plugin;
	}

	
	/**
	 * Returns a new ReportManager bean.
	 */
	public static ReportManager getReportManager() {
		try {
			return ReportManagerUtil.getHome(
					Login.getLogin().getInitialContextProperties()
				).create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
