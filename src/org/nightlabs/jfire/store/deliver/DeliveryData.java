/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;

/**
 * Subclass in order to hold specific data for your delivery process.
 * This additional data can be defined by the client delivery processor (gathered by
 * wizard pages or other input forms) and is
 * passed to the {@link org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor}. 
 * <p>
 * Instances of this class are only stored temporarily and might be removed
 * from the datastore, afer a delivery has been completed.
 * See {@link #clearSensitiveInformation()}
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryDataID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryData"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryID"
 *		include-body="id/DeliveryDataID.body.inc"
 *
 */
public class DeliveryData
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @!jdo.column length="100"
	 */
	private long deliveryID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Delivery delivery;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Delivery deliveryBackupForUpload = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryData()
	{
	}

	public DeliveryData(Delivery delivery)
	{
		this.delivery = delivery;
		this.organisationID = delivery.getOrganisationID();
		this.deliveryID = delivery.getDeliveryID();
	}

	/**
	 * This method is called multiple times, for initialization and after data
	 * has been written.
	 * It allows to set <tt>null</tt> members to empty strings or other
	 * "healthy" data. It is called, too, before the delivery is performed to
	 * prevent <tt>NullPointerException</tt>s and similar.
	 * <p>
	 * If you don't overwrite it, this method is a no-op.
	 */
	public void init()
	{
	}
	/**
	 * @return Returns the deliveryID.
	 */
	public long getDeliveryID()
	{
		return deliveryID;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the delivery.
	 */
	public Delivery getDelivery()
	{
		return delivery;
	}

	/**
	 * This method is called a certain time after delivery (e.g. a few weeks). Overwrite
	 * it to remove sensitive information from your fields (e.g. set the address
	 * to an empty string). If the instance shall be removed
	 * from the datastore completely, you don't need to overwrite this method, because the
	 * default implementation returns <tt>true</tt>.
	 *
	 * @return Whether to delete the instance from datastore (<tt>true</tt>) or to keep it
	 *		(<tt>false</tt>).
	 */
	public boolean clearSensitiveInformation()
	{
		return true;
	}


	/**
	 * This method backups {@link #delivery} by copying it to
	 * the transient non-persistent field {@link #deliveryBackupForUpload}.
	 * {@link #delivery} is set to the result of {@link Delivery#cloneForUpload()}
	 * in order to minimize traffic.
	 *
	 * @see #restoreAfterUpload()
	 */
	public void prepareUpload()
	{
		deliveryBackupForUpload = delivery;
		delivery = delivery.cloneForUpload();
	}

	/**
	 * This method is called after upload to undo the changes done by
	 * {@link #prepareUpload()}.
	 */
	public void restoreAfterUpload()
	{
		if (deliveryBackupForUpload == null)
			throw new IllegalStateException("deliveryBackupForUpload == null! It seems as if prepareForUpload() was not called before!");

		delivery = deliveryBackupForUpload;
		deliveryBackupForUpload = null;
	}

}
