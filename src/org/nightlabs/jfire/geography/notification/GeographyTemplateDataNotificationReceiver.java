package org.nightlabs.jfire.geography.notification;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.geography.CSV;
import org.nightlabs.jfire.geography.Geography;
import org.nightlabs.jfire.geography.GeographyTemplateDataManager;
import org.nightlabs.jfire.geography.GeographyTemplateDataManagerUtil;
import org.nightlabs.jfire.geography.id.CSVID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class GeographyTemplateDataNotificationReceiver
extends NotificationReceiver
{
	private static final Logger logger = Logger.getLogger(GeographyTemplateDataNotificationReceiver.class);
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected GeographyTemplateDataNotificationReceiver() { }

	public GeographyTemplateDataNotificationReceiver(NotificationFilter notificationFilter) {
		super(notificationFilter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle) throws Exception {
		try {
			Set<CSVID> csvIDs = new HashSet<CSVID>();
			for (Iterator<DirtyObjectID> it = notificationBundle.getDirtyObjectIDs().iterator(); it.hasNext();) {
				DirtyObjectID dirtyObjectID = it.next();
				if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState()))
					throw new IllegalStateException("Why the hell is the lifecycleState == DELETED?!?!?");

				CSVID csvID = (CSVID)dirtyObjectID.getObjectID();
				csvIDs.add(csvID);
			}

			PersistenceManager pm = getPersistenceManager();
			pm.getExtent(CSV.class);

			GeographyTemplateDataManager gm = GeographyTemplateDataManagerUtil.getHome(
					Lookup.getInitialContextProperties(pm, getOrganisationID())).create();
			Set<CSV> csvSet = gm.getCSVs(csvIDs, FETCH_GROUPS_CSV, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			for (Iterator<CSV> it = csvSet.iterator(); it.hasNext();) {
				CSV csv = it.next();
				// JDO optimizes write access and therefore does not persist the CSV instance if it already
				// exists, because it has not been modified (this is tracked by the JDO object). Hence,
				// we need to somehow make it dirty.
				//
				// IMHO this should work, but probably there is a JPOX bug :-(
//				JDOHelper.makeDirty(csv, "data");
				csv.setData(csv.getData()); // TODO JPOX WORKAROUND remove when fixed

				if (logger.isDebugEnabled())
					logger.debug("onReceiveNotificationBundle: persisting CSV: csv.data.length=" + csv.getData().length + " oid=" + JDOHelper.getObjectId(csv));

				pm.makePersistent(csv);
			}
			Geography.sharedInstance().clearCache();
		} catch (Exception x) {
			logger.error(
					"Synchronising the modified CSVs failed! user=" + SecurityReflector.getUserDescriptor().getCompleteUserID(), x);
			throw x;
		}
	}

	private static final String[] FETCH_GROUPS_CSV = { FetchPlan.DEFAULT, CSV.FETCH_GROUP_DATA };
}
