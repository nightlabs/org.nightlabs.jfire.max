package org.nightlabs.jfire.accounting.state;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.state.id.InvoiceStateID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceState"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceStateID"
 *
 * @jdo.fetch-group name="InvoiceState.user" fields="user"
 * @jdo.fetch-group name="InvoiceState.invoice" fields="invoice"
 * @jdo.fetch-group name="InvoiceState.invoiceStateDefinition" fields="invoiceStateDefinition"
 */
public class InvoiceState
implements Serializable
{
	private static final long serialVersionUID = 1L;

	// zweifelhafte forderung	
	// uneinbringliche forderungsa

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long invoiceStateID;


	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Invoice invoice;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private InvoiceStateDefinition invoiceStateDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceState() { }

	public InvoiceState(String organisationID, long invoiceStateID, User user, Invoice invoice, InvoiceStateDefinition invoiceStateDefinition)
	{
		this.organisationID = organisationID;
		this.invoiceStateID = invoiceStateID;
		this.user = user;
		this.invoice = invoice;
		this.invoiceStateDefinition = invoiceStateDefinition;
		this.createDT = new Date();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getInvoiceStateID()
	{
		return invoiceStateID;
	}
	public User getUser()
	{
		return user;
	}
	public Invoice getInvoice()
	{
		return invoice;
	}
	public InvoiceStateDefinition getInvoiceStateDefinition()
	{
		return invoiceStateDefinition;
	}
	public Date getCreateDT()
	{
		return createDT;
	}

}
