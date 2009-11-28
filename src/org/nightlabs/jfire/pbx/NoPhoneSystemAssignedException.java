package org.nightlabs.jfire.pbx;

import org.nightlabs.jfire.pbx.resource.Messages;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class NoPhoneSystemAssignedException extends PhoneSystemException
{
	private static final long serialVersionUID = 1L;

	private WorkstationID workstationID;

	public NoPhoneSystemAssignedException(WorkstationID workstationID) {
		super("There is no PhoneSystem assigned in the current workstation's configuration."); //$NON-NLS-1$
		this.workstationID = workstationID;
	}

	public WorkstationID getWorkstationID() {
		return workstationID;
	}
}
