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

package org.nightlabs.jfire.accounting;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.accounting.state.InvoiceState;
import org.nightlabs.jfire.accounting.state.InvoiceStateDefinition;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.InvoiceLocalID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceIDPrefix, invoiceID"
 *
 * @jdo.fetch-group name="Invoice.invoiceLocal" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.invoice" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.bookUser" fields="bookUser"
 * @jdo.fetch-group name="InvoiceLocal.this" fields="invoice, bookUser"
 */
public class InvoiceLocal
implements Serializable
{
	public static final String FETCH_GROUP_INVOICE = "InvoiceLocal.invoice";
	public static final String FETCH_GROUP_BOOK_USER = "InvoiceLocal.bookUser";
	public static final String FETCH_GROUP_THIS_INVOICE_LOCAL = "InvoiceLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String invoiceIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long invoiceID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Invoice invoice;

	/**
	 * This member represents the amount already paid.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amountPaid;

	/**
	 * This flag is true as long as the vendor still hopes to get the money. This means,
	 * it is set false automatically when paid becomes true or when the vendor gives up
	 * - e.g. because the customer is bankrupt.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean outstanding = true;

	/**
	 * This member stores the user who booked this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User bookUser = null;

	/**
	 * This member stores when this Invoice was booked.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date bookDT  = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private InvoiceState invoiceState;

	/**
	 * This is the history of <b>all</b> {@link InvoiceState}s with the newest last and the oldest first.
	 * Of course, only the states known to the current organisation are linked here.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="InvoiceState"
	 *		dependent-element="true"
	 *		table="JFireTrade_InvoiceLocal_invoiceStates"
	 *
	 * @jdo.join
	 */
	private List<InvoiceState> invoiceStates;

	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceLocal() { }

	public InvoiceLocal(Invoice invoice)
	{
		this.organisationID = invoice.getOrganisationID();
		this.invoiceIDPrefix = invoice.getInvoiceIDPrefix();
		this.invoiceID = invoice.getInvoiceID();
		this.invoice = invoice;
		this.invoiceActionHandlers = new HashSet<InvoiceActionHandler>();

		invoice.setInvoiceLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getInvoiceIDPrefix()
	{
		return invoiceIDPrefix;
	}
	public long getInvoiceID()
	{
		return invoiceID;
	}
	public Invoice getInvoice()
	{
		return invoice;
	}

	protected void incAmountPaid(long diffAmount)
	{
		this.amountPaid = amountPaid + diffAmount;
	}

	public long getAmountPaid() {
		return amountPaid;
	}
	public long getAmountToPay() {
		return getInvoice().getPrice().getAmount() - getAmountPaid();
	}

	public boolean isOutstanding() {
		return outstanding;
	}
	public void setOutstanding(boolean outstanding)
	{
		if (this.outstanding != outstanding)
			this.outstanding = outstanding;
	}

	protected void setBooked(User bookUser) {
		if (this.bookDT != null)
			return;

		this.bookUser = bookUser;
		this.bookDT = new Date();
	}
	public User getBookUser() {
		return bookUser;
	}
	public Date getBookDT() {
		return bookDT;
	}
	/**
	 * This member is set to true as soon as the money is booked on the various
	 * accounts.
	 */
	public boolean isBooked() {
		return bookDT != null;
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="InvoiceActionHandler"
	 *		table="JFireTrade_InvoiceLocal_invoiceActionHandlers"
	 *
	 * @jdo.join
	 */
	private Set<InvoiceActionHandler> invoiceActionHandlers;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<InvoiceActionHandler> _invoiceActionHandlers = null;

	/**
	 * @return Instances of {@link InvoiceActionHandler}.
	 */
	public Set<InvoiceActionHandler> getInvoiceActionHandlers()
	{
		if (_invoiceActionHandlers == null)
			_invoiceActionHandlers = Collections.unmodifiableSet(invoiceActionHandlers);

		return _invoiceActionHandlers;
	}

	public void addInvoiceActionHandler(InvoiceActionHandler invoiceActionHandler)
	{
		if (!invoiceActionHandlers.contains(invoiceActionHandler))
			invoiceActionHandlers.add(invoiceActionHandler);
	}

	public boolean removeInvoiceActionHandler(InvoiceActionHandler invoiceActionHandler)
	{
		return invoiceActionHandlers.remove(invoiceActionHandler);
	}

	/**
	 * This method is <b>not</b> intended to be called directly.
	 * Call {@link InvoiceStateDefinition#createInvoiceState(User, Invoice)} instead!
	 */
	public void setInvoiceState(InvoiceState currentInvoiceState)
	{
		if (currentInvoiceState == null)
			throw new IllegalArgumentException("invoiceState must not be null!");

		if (!currentInvoiceState.getInvoiceStateDefinition().isPublicState())
			throw new IllegalArgumentException("invoiceState.invoiceStateDefinition.publicState is false!");

		this.invoiceState = currentInvoiceState;
		this.invoiceStates.add(currentInvoiceState);
	}

	public InvoiceState getInvoiceState()
	{
		return invoiceState;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<InvoiceState> _invoiceStates = null;

	public List<InvoiceState> getInvoiceStates()
	{
		if (_invoiceStates == null)
			_invoiceStates = Collections.unmodifiableList(invoiceStates);

		return _invoiceStates;
	}
}
