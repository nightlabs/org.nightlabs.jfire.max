package org.nightlabs.jfire.accounting.state;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.state.State;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.state.State"
 *		detachable="true"
 *		table="JFireTrade_InvoiceState"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class InvoiceState
extends State
{
	private static final long serialVersionUID = 1L;

	// zweifelhafte forderung	
	// uneinbringliche forderungsa
	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceState() { }

	public InvoiceState(String organisationID, long stateID, User user, Invoice invoice, InvoiceStateDefinition invoiceStateDefinition)
	{
		super(organisationID, stateID, user, invoice, invoiceStateDefinition);
	}

	/**
	 * This is a convenience method calling the super method {@link State#getStatable()}.
	 * @return the result of {@link State#getStatable()}
	 */
	public Invoice getInvoice()
	{
		return (Invoice) getStatable();
	}

	/**
	 * This is a convenience method calling the super method {@link State#getStateDefinition()}.
	 * @return the result of {@link State#getStateDefinition()}
	 */
	public InvoiceStateDefinition getInvoiceStateDefinition()
	{
		return (InvoiceStateDefinition) getStateDefinition();
	}
}
