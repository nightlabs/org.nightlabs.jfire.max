package org.nightlabs.jfire.store;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.util.Util;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryNoteActionHandlerID"
 */
public class DeliveryNoteActionHandler implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryNoteActionHandlerID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteActionHandler() { }

	public DeliveryNoteActionHandler(String organisationID, String deliveryNoteActionHandlerID)
	{
		this.organisationID = organisationID;
		this.deliveryNoteActionHandlerID = deliveryNoteActionHandlerID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getDeliveryNoteActionHandlerID()
	{
		return deliveryNoteActionHandlerID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * This method is called by {@link Store#onBookDeliveryNote(User, DeliveryNote)} after
	 * all work is done.
	 *
	 * @param user The responsible user.
	 * @param deliveryNote The deliveryNote that is booked.
	 */
	public void onBook(User user, DeliveryNote deliveryNote)
	{
	}

	/**
	 * This method is called by {@link Store#deliverBegin(User, DeliveryData)} after the {@link ServerDeliveryProcessor} has
	 * been triggered.
	 *
	 * @param user The responsible user.
	 * @param deliveryData The delivery-data which allows to access the {@link Delivery}.
	 * @param deliveryNote The deliveryNote that is paid. Note that multiple <code>DeliveryNoteActionHandler</code>s might be called for multiple <code>DeliveryNote</code>s
	 * 		for <strong>the same</strong> {@link DeliveryData}, because one delivery can comprise many deliveryNotes.
	 * @throws Exception If sth. goes wrong. It will be wrapped inside a DeliveryException
	 * @throws DeliveryException If you throw a DeliveryException directly, it won't be wrapped.
	 */
	public void onDeliverBegin(User user, DeliveryData deliveryData, DeliveryNote deliveryNote)
	throws Exception, DeliveryException
	{
	}

	/**
	 * This method is called by {@link Store#deliverDoWork(User, DeliveryData)} after the {@link ServerDeliveryProcessor} has
	 * been triggered.
	 *
	 * @param user The responsible user.
	 * @param deliveryData The delivery-data which allows to access the {@link Delivery}.
	 * @param deliveryNote The deliveryNote that is paid. Note that multiple <code>DeliveryNoteActionHandler</code>s might be called for multiple <code>DeliveryNote</code>s
	 * 		for <strong>the same</strong> {@link DeliveryData}, because one delivery can comprise many deliveryNotes.
	 * @throws Exception If sth. goes wrong. It will be wrapped inside a DeliveryException
	 * @throws DeliveryException If you throw a DeliveryException directly, it won't be wrapped.
	 */
	public void onDeliverDoWork(User user, DeliveryData deliveryData, DeliveryNote deliveryNote)
	throws Exception, DeliveryException
	{
	}

	/**
	 * This method is called by {@link Store#deliverEnd(User, DeliveryData)} after all has been done successfully.
	 * <p>
	 * <b>Note that this method is called, too, if the delivery has been postponed.</b> Thus, you should check
	 * {@link Delivery#isPostponed()}!
	 * </p>
	 * <p>
	 * You should try to avoid throwing an Exception here, because it is too late for a roll-back in an external delivery system!
	 * If you do risky things that might fail, you should better override {@link #onDeliverDoWork(User, DeliveryData, DeliveryNote)} and do them
	 * there! The best solution, is to ensure already in {@link #onDeliverBegin(User, DeliveryData, DeliveryNote)} that a delivery will succeed.
	 * </p>
	 * <p>
	 * An exception at this stage (i.e. thrown by this method) will require manual clean-up by an operator!
	 * </p>
	 *
	 * @param user The responsible user.
	 * @param deliveryData The delivery-data which allows to access the {@link Delivery}.
	 * @param deliveryNote The deliveryNote that is paid. Note that multiple <code>DeliveryNoteActionHandler</code>s might be called for multiple <code>DeliveryNote</code>s
	 * 		for <strong>the same</strong> {@link DeliveryData}, because one delivery can comprise many deliveryNotes.
	 */
	public void onDeliverEnd(User user, DeliveryData deliveryData, DeliveryNote deliveryNote)
	{
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof DeliveryNoteActionHandler))
			return false;

		DeliveryNoteActionHandler o = (DeliveryNoteActionHandler) obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.deliveryNoteActionHandlerID, o.deliveryNoteActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(deliveryNoteActionHandlerID);
	}
}
