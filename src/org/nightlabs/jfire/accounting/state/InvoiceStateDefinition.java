package org.nightlabs.jfire.accounting.state;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceLocal;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.InvoiceStateDefinitionID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceStateDefinitionID"
 *
 * @jdo.fetch-group name="InvoiceStateDefinition.name" fields="name"
 */
public class InvoiceStateDefinition
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "InvoiceStateDefinition.name";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String invoiceStateDefinitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="invoiceStateDefinition"
	 */
	private InvoiceStateDefinitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean publicState = false;

	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceStateDefinition() { }

	public InvoiceStateDefinition(String organisationID, String invoiceStateDefinitionID)
	{
		this.organisationID = organisationID;
		this.invoiceStateDefinitionID = invoiceStateDefinitionID;
		this.name = new InvoiceStateDefinitionName(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getInvoiceStateDefinitionID()
	{
		return invoiceStateDefinitionID;
	}

	public static String getPrimaryKey(String organisationID, String invoiceStateDefinitionID)
	{
		return organisationID + '/' + invoiceStateDefinitionID;
	}

	public InvoiceStateDefinitionName getName()
	{
		return name;
	}

	/**
	 * If a state definition is marked as <code>publicState</code>, it will be exposed to other organisations
	 * by storing it in both the {@link InvoiceLocal} and the {@link Invoice} instance. If it is not public,
	 * it is only stored in the {@link InvoiceLocal}.
	 *
	 * @return true, if it shall be registered in the non-local instance and therefore published to business partners.
	 */
	public boolean isPublicState()
	{
		return publicState;
	}

	/**
	 * This method creates a new {@link InvoiceState} and registers it in the {@link Invoice} and {@link InvoiceLocal}.
	 * Note, that it won't be added to the {@link Invoice} (but only to the {@link InvoiceLocal}), if {@link #isPublicState()}
	 * returns false.
	 * <p>
	 * This method calls {@link #_createInvoiceState(User, Invoice)} in order to obtain the new instance. Override that method
	 * if you feel the need for subclassing {@link InvoiceState}.
	 * </p>
	 * 
	 * @param user The user who is responsible for the action.
	 * @param invoice The invoice that is transitioned to the new state.
	 * @return Returns the new newly created InvoiceState instance.
	 */
	public InvoiceState createInvoiceState(User user, Invoice invoice)
	{
		InvoiceState invoiceState = _createInvoiceState(user, invoice);

		invoice.getInvoiceLocal().setInvoiceState(invoiceState);

		if (isPublicState())
			invoice.setInvoiceState(invoiceState);

		return invoiceState;
	}

	/**
	 * This method creates an instance of {@link InvoiceState}. It is called by {@link #createInvoiceState(User, Invoice)}.
	 * This method does NOT register anything. You should override this method if you want to subclass {@link InvoiceState}.
	 */
	protected InvoiceState _createInvoiceState(User user, Invoice invoice)
	{
		return new InvoiceState(user.getOrganisationID(), IDGenerator.nextID(InvoiceState.class), user, invoice, this);
	}
}
