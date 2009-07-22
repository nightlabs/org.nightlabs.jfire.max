package org.nightlabs.jfire.accounting.book.uncollectable;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.book.uncollectable.id.UncollectableInvoiceBookerID;
import org.nightlabs.jfire.security.User;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		objectIdClass=UncollectableInvoiceBookerID.class,
		detachable="true",
		table="JFireTrade_UncollectableInvoiceBooker"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class UncollectableInvoiceBooker
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String uncollectableInvoiceBookerID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected UncollectableInvoiceBooker() { }

	public UncollectableInvoiceBooker(String organisationID, String uncollectableInvoiceBookerID) {
		this.organisationID = organisationID;
		this.uncollectableInvoiceBookerID = uncollectableInvoiceBookerID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getUncollectableInvoiceBookerID() {
		return uncollectableInvoiceBookerID;
	}

	/**
	 * Book an invoice as "uncollectable".
	 * <p>
	 * <b>Important:</b> This method must not be called directly! It is invoked by
	 * {@link Accounting#bookUncollectableInvoice(Invoice)}.
	 * </p>
	 * @param user the user who is responsible for this action.
	 * @param invoice the invoice that is uncollectible.
	 */
	public abstract void bookUncollectableInvoice(User user, Invoice invoice);

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass() + " is currently not persistent! Cannot obtain a PersistenceManager!");

		return pm;
	}
}
