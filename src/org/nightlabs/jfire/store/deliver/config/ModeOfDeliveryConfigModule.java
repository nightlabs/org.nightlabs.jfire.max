/**
 * 
 */
package org.nightlabs.jfire.store.deliver.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * ConfigModule for a set of {@link ModeOfDeliveryFlavour}s.
 * Use the API for {@link ModeOfDeliveryFlavourID}s on detached instances.
 * <p>
 * This is registered for Users and Workstations and the {@link ModeOfDeliveryFlavour}s
 * available for a payment can be filtered by the intersection of the entries
 * configured for the current user and the workstation he is currently logged on
 * (see {@link ModeOfDeliveryFlavour#getModeOfDeliveryFlavourProductTypeGroupCarrier(PersistenceManager, java.util.Collection, java.util.Collection, byte, boolean)}) 
 * 
 * </p>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_ModeOfDeliveryConfigModule"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="ModeOfDeliveryConfigModule.ModeOfDeliveryFlavours" fields="ModeOfDeliveryFlavours"
 * @jdo.fetch-group name="ConfigModule.this" fields="personStructFields"
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @version $Revision$, $Date$
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ModeOfDeliveryConfigModule")
@FetchGroups({
	@FetchGroup(
		name="ModeOfDeliveryConfigModule.ModeOfDeliveryFlavours",
		members=@Persistent(name="ModeOfDeliveryFlavours")),
	@FetchGroup(
		name="ConfigModule.this",
		members=@Persistent(name="personStructFields"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ModeOfDeliveryConfigModule extends ConfigModule implements DetachCallback, AttachCallback {

	private static final long serialVersionUID = 20080112L;

	// No access to the flavours directly
//	public static final String FETCH_GROUP_MODE_OF_PAYMENT_FLAVOURS = "ModeOfDeliveryConfigModule.modeOfDeliveryFlavours";
	public static final String FETCH_GROUP_MODE_OF_DELIVERY_FLAVOURIDS = "ModeOfDeliveryConfigModule.modeOfDeliveryFlavourIDs";
	
	public static final class FieldName {
		public static final String modeOfDeliveryFlavours = "modeOfDeliveryFlavours";
		public static final String modeOfDeliveryFlavourIDs = "modeOfDeliveryFlavourIDs";
		public static final String modeOfDeliveryFlavourIDsDetached = "modeOfDeliveryFlavourIDsDetached";
	}
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour"
	 *		table="JFireTrade_ModeOfDeliveryConfigModule_modeOfDeliveryFlavours"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_ModeOfDeliveryConfigModule_modeOfDeliveryFlavours",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<ModeOfDeliveryFlavour> modeOfDeliveryFlavours;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean modeOfDeliveryFlavourIDsDetached;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Set<ModeOfDeliveryFlavourID> modeOfDeliveryFlavourIDs;
	
	/**
	 */
	public ModeOfDeliveryConfigModule() {
		modeOfDeliveryFlavours = new HashSet<ModeOfDeliveryFlavour>();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModule#init()
	 */
	@Override
	public void init() {
		modeOfDeliveryFlavours.addAll(ModeOfDeliveryFlavour.getAllModeOfDeliveryFlavours(JDOHelper.getPersistenceManager(this)));
	}
	
	public Set<ModeOfDeliveryFlavour> getModeOfDeliveryFlavours() {
		return Collections.unmodifiableSet(modeOfDeliveryFlavours);
	}

	public Set<ModeOfDeliveryFlavourID> getModeOfDeliveryFlavourIDs() {
		if (!modeOfDeliveryFlavourIDsDetached)
			throw new JDODetachedFieldAccessException("Field " + FieldName.modeOfDeliveryFlavourIDs + " was not detached.");
		return modeOfDeliveryFlavourIDs;
	}
	
	public void setModeOfDeliveryFlavourIDs(Set<ModeOfDeliveryFlavourID> ModeOfDeliveryFlavourIDs) {
		if (!modeOfDeliveryFlavourIDsDetached)
			throw new JDODetachedFieldAccessException("Field " + FieldName.modeOfDeliveryFlavourIDs + " was not detached.");
		this.modeOfDeliveryFlavourIDs = ModeOfDeliveryFlavourIDs;
	}

	@Override
	public void jdoPostDetach(Object attached) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(attached);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_MODE_OF_DELIVERY_FLAVOURIDS)) {
			this.modeOfDeliveryFlavourIDs = new HashSet<ModeOfDeliveryFlavourID>();
			for (ModeOfDeliveryFlavour flavour : ((ModeOfDeliveryConfigModule) attached).getModeOfDeliveryFlavours()) {
				modeOfDeliveryFlavourIDs.add((ModeOfDeliveryFlavourID) JDOHelper.getObjectId(flavour));
			}
			this.modeOfDeliveryFlavourIDsDetached = true;
		}
	}

	@Override
	public void jdoPreDetach() {
	}

	@Override
	public void jdoPostAttach(Object detached) {
		ModeOfDeliveryConfigModule detachedModule = (ModeOfDeliveryConfigModule) detached;
		if (!detachedModule.modeOfDeliveryFlavourIDsDetached)
			return;
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("PersistenceManager is null on attached instance?");
		Set<ModeOfDeliveryFlavourID> containedIDs = new HashSet<ModeOfDeliveryFlavourID>();
		for (Iterator<ModeOfDeliveryFlavour> it = modeOfDeliveryFlavours.iterator(); it.hasNext(); ) {
			ModeOfDeliveryFlavourID flavourID = (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(it.next());
			if (detachedModule.modeOfDeliveryFlavourIDs == null || !detachedModule.modeOfDeliveryFlavourIDs.contains(flavourID)) {
				it.remove();
			} else {
				containedIDs.add(flavourID);
			}
		}
		for (ModeOfDeliveryFlavourID flavourID : detachedModule.modeOfDeliveryFlavourIDs) {
			if (!containedIDs.contains(flavourID)) {
				this.modeOfDeliveryFlavours.add((ModeOfDeliveryFlavour) pm.getObjectById(flavourID));
			}
		}
	}

	@Override
	public void jdoPreAttach() {
	}
	
	@Override
	public FieldMetaData getFieldMetaData(String fieldName) {
		if (FieldName.modeOfDeliveryFlavourIDs.equals(fieldName) || FieldName.modeOfDeliveryFlavourIDsDetached.equals(fieldName)) {
			return null;
		}
		return super.getFieldMetaData(fieldName);
	}
}
