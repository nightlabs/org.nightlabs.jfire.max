/**
 * 
 */
package org.nightlabs.jfire.accounting.pay.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.config.ConfigModule;

/**
 * ConfigModule for a set of {@link ModeOfPaymentFlavour}s.
 * Use the API for {@link ModeOfPaymentFlavourID}s on detached instances.
 * <p>
 * This is registred for Users and Workstations and the {@link ModeOfPaymentFlavour}s
 * available for a payment can be filtered by the intersection of the entries
 * configured for the current user and the workstation he is currently logged on
 * (see {@link ModeOfPaymentFlavour#getAvailableModeOfPaymentFlavoursForAllCustomerGroups(PersistenceManager, java.util.Collection, byte, boolean)}) 
 * 
 * </p>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_ModeOfPaymentConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="ModeOfPaymentConfigModule.modeOfPaymentFlavours" fields="modeOfPaymentFlavours"
 * @jdo.fetch-group name="ConfigModule.this" fields="personStructFields"
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @version $Revision$, $Date$
 */
public class ModeOfPaymentConfigModule extends ConfigModule implements DetachCallback, AttachCallback {

	private static final long serialVersionUID = 20080109L;

	// No access to the flavours directly
//	public static final String FETCH_GROUP_MODE_OF_PAYMENT_FLAVOURS = "ModeOfPaymentConfigModule.modeOfPaymentFlavours";
	public static final String FETCH_GROUP_MODE_OF_PAYMENT_FLAVOURIDS = "ModeOfPaymentConfigModule.modeOfPaymentFlavourIDs";
	
	public static final class FieldName {
		public static final String modeOfPaymentFlavours = "modeOfPaymentFlavours";
		public static final String modeOfPaymentFlavourIDs = "modeOfPaymentFlavourIDs";
	}
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour"
	 *		table="JFireTrade_ModeOfPaymentConfigModule_modeOfPaymentFlavours"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set<ModeOfPaymentFlavour> modeOfPaymentFlavours;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean modeOfPaymentFlavourIDsDetached;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs;
	
	/**
	 */
	public ModeOfPaymentConfigModule() {
		modeOfPaymentFlavours = new HashSet<ModeOfPaymentFlavour>();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
		modeOfPaymentFlavours.addAll(ModeOfPaymentFlavour.getAllModeOfPaymentFlavours(JDOHelper.getPersistenceManager(this)));
	}
	
	public Set<ModeOfPaymentFlavour> getModeOfPaymentFlavours() {
		return Collections.unmodifiableSet(modeOfPaymentFlavours);
	}

	public Set<ModeOfPaymentFlavourID> getModeOfPaymentFlavourIDs() {
		if (!modeOfPaymentFlavourIDsDetached)
			throw new JDODetachedFieldAccessException("Field " + FieldName.modeOfPaymentFlavourIDs + " was not detached.");
		return modeOfPaymentFlavourIDs;
	}
	
	public void setModeOfPaymentFlavourIDs(Set<ModeOfPaymentFlavourID> modeOfPaymentFlavourIDs) {
		if (!modeOfPaymentFlavourIDsDetached)
			throw new JDODetachedFieldAccessException("Field " + FieldName.modeOfPaymentFlavourIDs + " was not detached.");
		this.modeOfPaymentFlavourIDs = modeOfPaymentFlavourIDs;
	}

	@Override
	public void jdoPostDetach(Object attached) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_MODE_OF_PAYMENT_FLAVOURIDS)) {
			this.modeOfPaymentFlavourIDs = new HashSet<ModeOfPaymentFlavourID>();
			for (ModeOfPaymentFlavour flavour : ((ModeOfPaymentConfigModule) attached).getModeOfPaymentFlavours()) {
				modeOfPaymentFlavourIDs.add((ModeOfPaymentFlavourID) JDOHelper.getObjectId(flavour));
			}
			this.modeOfPaymentFlavourIDsDetached = true;
		}
	}

	@Override
	public void jdoPreDetach() {
	}

	@Override
	public void jdoPostAttach(Object detached) {
		ModeOfPaymentConfigModule detachedModule = (ModeOfPaymentConfigModule) detached;
		if (!detachedModule.modeOfPaymentFlavourIDsDetached)
			return;
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		Set<ModeOfPaymentFlavourID> containedIDs = new HashSet<ModeOfPaymentFlavourID>();
		for (Iterator<ModeOfPaymentFlavour> it = modeOfPaymentFlavours.iterator(); it.hasNext(); ) {
			ModeOfPaymentFlavourID flavourID = (ModeOfPaymentFlavourID) JDOHelper.getObjectId(it.next());
			if (detachedModule.modeOfPaymentFlavourIDs == null || !detachedModule.modeOfPaymentFlavourIDs.contains(flavourID)) {
				it.remove();
			} else {
				containedIDs.add(flavourID);
			}
		}
		for (ModeOfPaymentFlavourID flavourID : detachedModule.modeOfPaymentFlavourIDs) {
			if (!containedIDs.contains(flavourID)) {
				this.modeOfPaymentFlavours.add((ModeOfPaymentFlavour) pm.getObjectById(flavourID));
			}
		}
	}

	@Override
	public void jdoPreAttach() {
	}
}
