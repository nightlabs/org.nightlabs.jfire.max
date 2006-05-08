/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.File;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.nightlabs.jfire.reporting.platform.RAPlatformContext;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class JFireReportingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String birtHome = Utils.addFinalSlash(
				"/home/alex/Java/jboss-marco2/server/default/deploy/JFire.last")+
				"JFireReporting.ear"+File.separator+"birt"+File.separator;

		System.setProperty(Platform.PROPERTY_BIRT_HOME, birtHome);
		PlatformConfig config = new PlatformConfig();
		config.setProperty(Platform.PROPERTY_BIRT_HOME, birtHome);
		
		RAPlatformContext platformContext = new RAPlatformContext(birtHome);
		config.setPlatformContext(platformContext);
		try {
			Platform.startup(config);
		} catch (BirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		EclipseStarter
		System.out.println(Platform.getPlatformType());
		System.out.println(Platform.getExtensionRegistry());
	}

}
