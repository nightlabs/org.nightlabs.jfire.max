package org.nightlabs.jfire.voucher.store;

import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.math.Base62Coder;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.voucher.store.id.VoucherStoreID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.voucher.store.id.VoucherStoreID"
 *		detachable="true"
 *		table="JFireVoucher_VoucherStore"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	objectIdClass=VoucherStoreID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherStore")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherStore
{
	public static VoucherStore getVoucherStore(PersistenceManager pm)
	{
		Iterator<VoucherStore> it = pm.getExtent(VoucherStore.class).iterator();
		VoucherStore voucherStore = (it.hasNext() ? it.next() : null);

		if (voucherStore == null) {
			voucherStore = new VoucherStore();
			voucherStore.organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
			voucherStore = pm.makePersistent(voucherStore);
		}

		return voucherStore;
	}

	protected VoucherStore() { }

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * This ({@value #MAX_VOUCHER_ORGANISATION_ID }) is the highest 4 digit number in the base-62-system,
	 * i.e. "zzzz" => {@link Base62Coder}) and defines the highest possible value for a numeric voucher
	 * organisation ID.
	 *
	 * @see #voucherOrganisationID
	 */
	public static final int MAX_VOUCHER_ORGANISATION_ID = 14776335;

	/**
	 * This is the unique numeric ID assigned to this organisation (i.e. {@link #organisationID }). To guarantee
	 * that it is unique, it is obtained from the root-organisation. As long as the root-organisation didn't
	 * generate a <code>voucherOrganisationID</code>, this value will be -1.
	 * <p>
	 * Range: 0..14 776 335 (see {@link #MAX_VOUCHER_ORGANISATION_ID })
	 * </p>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int voucherOrganisationID = -1;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of TicketingStore is currently not attached to a datastore! Cannot obtain PersistenceManager!");
		return pm;
	}

	public int getVoucherOrganisationID()
	{
		return getVoucherOrganisationID(true);
	}

	/**
	 * @param checkRange Whether or not to throw an {@link IllegalStateException}, if
	 *		the voucherOrganisationID has not yet been assigned (and thus is -1) or is
	 *		greater than {@link #MAX_VOUCHER_ORGANISATION_ID }.
	 *
	 * @return Returns the numeric voucher-related organisationID.
	 *
	 * @see #getVoucherOrganisationID()
	 */
	public int getVoucherOrganisationID(boolean checkRange)
	{
		if (checkRange) {
			int res = voucherOrganisationID; // we store this in res, because JDO-enhancement might make this read access a little bit expensive

			if (res < 0)
				throw new IllegalStateException("The voucherOrganisationID has not yet been assigned!");

			if (res > MAX_VOUCHER_ORGANISATION_ID)
				throw new IllegalStateException("The voucherOrganisationID is too large! How the hell can this happen?!");

			return res;
		}
		return voucherOrganisationID;
	}

	/**
	 * This method can only be called once to assign a numeric organisationID used in voucher.
	 *
	 * @param voucherOrganisationID The numeric id as generated by the root-organisation.
	 */
	public void setVoucherOrganisationID(int voucherOrganisationID)
	{
		if (voucherOrganisationID == this.voucherOrganisationID)
			return;

		if (voucherOrganisationID < 0 || voucherOrganisationID > MAX_VOUCHER_ORGANISATION_ID)
			throw new IllegalArgumentException("voucherOrganisationID is out of range!");

		if (this.voucherOrganisationID >= 0)
			throw new IllegalStateException("This VoucherStore (for organisation " + organisationID + ") has already a numeric voucherOrganisationID assigned! Cannot modify it!");

		this.voucherOrganisationID = voucherOrganisationID;
	}
}
