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

package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;

import org.nightlabs.jfire.transfer.TransferData;

/**
 * Subclass in order to hold specific data for your payment process.
 * This additional data can be defined by the client payment processor (gathered by
 * wizard pages or other input forms) and is
 * passed to the {@link org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor}. 
 * <p>
 * Instances of this class are only stored temporarily and might be removed
 * from the datastore, afer a payment has been completed.
 * See {@link #clearSensitiveInformation()}
 *
 * @see org.nightlabs.jfire.accounting.pay.PaymentDataCreditCard
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.PaymentDataID"
 *		detachable="true"
 *		table="JFireTrade_PaymentData"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, paymentID"
 *		include-body="id/PaymentDataID.body.inc"
 */
public class PaymentData
implements Serializable, TransferData
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true" 
	 */
	private long paymentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Payment payment;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Payment paymentBackupForUpload = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PaymentData()
	{
	}

	public PaymentData(Payment payment)
	{
		this.payment = payment;
		this.organisationID = payment.getOrganisationID();
		this.paymentID = payment.getPaymentID();
	}

	/**
	 * This method is called multiple times, for initialization and after data
	 * has been written.
	 * It allows to set <tt>null</tt> members to empty strings or other
	 * "healthy" data. It is called, too, before the payment is performed to
	 * prevent <tt>NullPointerException</tt>s and similar.
	 * <p>
	 * If you don't overwrite it, this method is a no-op.
	 */
	public void init()
	{
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the paymentID.
	 */
	public long getPaymentID()
	{
		return paymentID;
	}
	/**
	 * @return Returns the payment.
	 */
	public Payment getPayment()
	{
		return payment;
	}

	/**
	 * This method is called a certain time after payment (e.g. a few weeks). Overwrite
	 * it to remove sensitive information from your fields (e.g. set the credit card number
	 * to an empty string or keep only the last 4 digits). If the instance shall be removed
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
	 * This method backups {@link #payment} by copying it to
	 * the transient non-persistent field {@link #paymentBackupForUpload}.
	 * {@link #payment} is set to the result of {@link Payment#cloneForUpload()}
	 * in order to minimize traffic.
	 *
	 * @see #restoreAfterUpload()
	 */
	public void prepareUpload()
	{
		paymentBackupForUpload = payment;
		payment = payment.cloneForUpload();
	}

	/**
	 * This method is called after upload to undo the changes done by
	 * {@link #prepareUpload()}.
	 */
	public void restoreAfterUpload()
	{
		if (paymentBackupForUpload == null)
			throw new IllegalStateException("paymentBackupForUpload == null! It seems as if prepareForUpload() was not called before!");

		payment = paymentBackupForUpload;
		paymentBackupForUpload = null;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = PRIME * result + (int) (paymentID ^ (paymentID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PaymentData other = (PaymentData) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (paymentID != other.paymentID)
			return false;
		return true;
	}
}
