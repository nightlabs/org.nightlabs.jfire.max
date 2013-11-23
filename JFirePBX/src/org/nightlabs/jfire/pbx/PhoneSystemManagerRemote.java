package org.nightlabs.jfire.pbx;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.pbx.id.PhoneSystemID;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Remote
public interface PhoneSystemManagerRemote
{
	void initialise() throws Exception;
	void call(Call call) throws PhoneSystemException;

	PhoneSystem storePhoneSystem(PhoneSystem phoneSystem, boolean get, String[] fetchGroups, int maxFetchDepth);
	void deletePhoneSystem(PhoneSystemID projectID);
	List<PhoneSystem> getPhoneSystems(Collection<PhoneSystemID> phoneSystemIDs,	String[] fetchGroups, int maxFetchDepth);
	Set<PhoneSystemID> getPhoneSystemIDs();
	Set<PhoneSystemID> getPhoneSystemIDs(Class<? extends PhoneSystem> phoneSystemClass, boolean includeSubclasses);
	/**
	 * @param workstationID the identifier of the workstation or <code>null</code> for the current user's workstation.
	 */
	PhoneSystemID getPhoneSystemID(WorkstationID workstationID);

	PhoneSystemID getPhoneSystemID(ConfigID configID);
}