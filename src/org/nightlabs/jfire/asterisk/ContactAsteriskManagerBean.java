package org.nightlabs.jfire.asterisk;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asterisk.config.AsteriskConfigModule;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.pbx.ClientOnlyPhoneSystem;
import org.nightlabs.jfire.pbx.DefaultPhoneSystem;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class ContactAsteriskManagerBean
extends BaseSessionBeanImpl
implements ContactAsteriskManagerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ContactAsteriskManagerBean.class);

	private void initTimerTaskCleanupCallFiles(PersistenceManager pm)
	throws Exception
	{
		TaskID taskID = TaskID.create(getOrganisationID(), Task.TASK_TYPE_ID_SYSTEM, "cleanupCallFiles");
		try {
			pm.getObjectById(taskID);
			return;
		} catch (JDOObjectNotFoundException x) {
			// Do nothing.
		}

		Task task = new Task(
				taskID,
				User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
				ContactAsteriskManagerRemote.class, "cleanupCallFiles"
		);
		task = pm.makePersistent(task);
		task.getTimePatternSet().createTimePattern(
				"*", // year
				"*", // month
				"*", // day
				"*", // dayOfWeek
				"*", //  hour
				"11-59/15" // minute
		);

		task.getName().setText(Locale.ENGLISH.getLanguage(), "Clean call files");
		task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task cleans expired files that were not processed by Asterisk.");

		task.setEnabled(true);
	}

	@Override
	public void cleanupCallFiles(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			for (Iterator<AsteriskServer> itAS = pm.getExtent(AsteriskServer.class).iterator(); itAS.hasNext(); ) {
				AsteriskServer asteriskServer = itAS.next();
				if (!getOrganisationID().equals(asteriskServer.getOrganisationID()))
					continue; // Silently ignore servers from other organisations (should probably never happen, but safer is better).

				File callFileDir = new File(
						asteriskServer.getCallFileDirectory()
				);
				File tmpDir = new File(callFileDir, AsteriskServer.CALL_FILE_TEMP_SUB_DIRECTORY);
				if (tmpDir.exists())
					cleanupCallFileDirectory(asteriskServer, tmpDir);

				cleanupCallFileDirectory(asteriskServer, callFileDir);
			}
		} finally {
			pm.close();
		}
	}

	private static void cleanupCallFileDirectory(AsteriskServer asteriskServer, File directory)
	{
		long maxAgeMSec = 60L * 1000L * asteriskServer.getCallFileExpiryAgeMinutes();

		File[] callFiles = directory.listFiles();
		if (callFiles != null) {
			for (File callFile : callFiles) {
				if (callFile.isDirectory()) {
					if (logger.isDebugEnabled())
						logger.debug("cleanupCallFileDirectory: ignoring sub-directory: " + callFile.getAbsolutePath());

					continue;
				}

				if (!callFile.getName().endsWith(AsteriskServer.CALL_FILE_SUFFIX)) {
					if (logger.isDebugEnabled())
						logger.debug("cleanupCallFileDirectory: ignoring file, because it does not end on \"" + AsteriskServer.CALL_FILE_SUFFIX + "\": " + callFile.getAbsolutePath());

					continue; // ignore files that are not created by us
				}

				String fileTimeStamp = callFile.getName().split("-")[0];

				long fileTime = Long.MAX_VALUE;

				try {
					fileTime = Long.valueOf(fileTimeStamp, 36);
				} catch (NumberFormatException x) {
					logger.warn("cleanupCallFileDirectory: could not parse timestamp from file name: " + callFile.getAbsolutePath());
				}

				long ageMSec = System.currentTimeMillis() - fileTime;
				if (ageMSec > maxAgeMSec) {
					if (logger.isDebugEnabled())
						logger.debug("cleanupCallFileDirectory: deleting file: " + callFile.getAbsolutePath());

					if (!callFile.delete())
						logger.warn("cleanupCallFileDirectory: Deleting file failed: " + callFile.getAbsolutePath());

					if (logger.isInfoEnabled())
						logger.info("cleanupCallFileDirectory: deleted file: " + callFile.getAbsolutePath());
				}
				else {
					if (logger.isDebugEnabled())
						logger.debug("cleanupCallFileDirectory: kept not-yet-expired (age=" + ageMSec + ", maxAge=" + maxAgeMSec + ") file: " + callFile.getAbsolutePath());
				}
			}
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			initTimerTaskCleanupCallFiles(pm);

			AsteriskServer defaultAsteriskServer = AsteriskServer.getDefaultAsteriskServer(pm);
			defaultAsteriskServer.getName().readFromProperties(
					"org.nightlabs.jfire.asterisk.resource.messages", //$NON-NLS-1$
					AsteriskServer.class.getClassLoader(),
					"org.nightlabs.jfire.asterisk.AsteriskServer.defaultAsteriskServer.name" //$NON-NLS-1$
			);

			DefaultPhoneSystem defaultPhoneSystem = DefaultPhoneSystem.getDefaultPhoneSystem(pm);
			if (defaultPhoneSystem.getPhoneSystem() == null || ClientOnlyPhoneSystem.ID_DEFAULT_CLIENT_ONLY_PHONE_SYSTEM.equals(defaultPhoneSystem.getPhoneSystem().getPhoneSystemID()))
				defaultPhoneSystem.setPhoneSystem(defaultAsteriskServer);

			ConfigSetup workstationConfigSetup = ConfigSetup.getConfigSetup(
					pm,
					getOrganisationID(),
					WorkstationConfigSetup.CONFIG_SETUP_TYPE_WORKSTATION
			);
			Set<String> configModuleClasses = workstationConfigSetup.getConfigModuleClasses();
			if (!configModuleClasses.contains(AsteriskConfigModule.class.getName())) // DataNucleus workaround: the following add causes an exception, if it's already there. thus, we need to check.
				configModuleClasses.add(AsteriskConfigModule.class.getName());
		} finally {
			pm.close();
		}
	}
}