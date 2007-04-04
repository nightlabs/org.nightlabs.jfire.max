package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.worklock.WorklockType;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.worklock.WorklockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class WorklockTypeOrder
extends WorklockType
{
	private static final long serialVersionUID = 1L;

	public static final WorklockTypeID WORKLOCK_TYPE_ID = WorklockTypeID.create(Organisation.DEVIL_ORGANISATION_ID, WorklockTypeOrder.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	protected WorklockTypeOrder() { }

	public WorklockTypeOrder(String organisationID, String worklockTypeID)
	{
		super(organisationID, worklockTypeID);
	}

	// TODO implement logic that happens when a lock is released
}
