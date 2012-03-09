package org.nightlabs.jfire.voucher.accounting;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.transfer.Anchor;


/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.InvoiceMoneyTransfer"
 *		detachable="true"
 *		table="JFireVoucher_VoucherMoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query name="getVoucherMoneyTransferByArticle"
 *		query="SELECT UNIQUE WHERE this.article == :article"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherMoneyTransfer")
@Queries(
	@javax.jdo.annotations.Query(
		name="getVoucherMoneyTransferByArticle",
		value="SELECT UNIQUE WHERE this.article == :article")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherMoneyTransfer
extends InvoiceMoneyTransfer
{
	private static final long serialVersionUID = 1L;

	public static VoucherMoneyTransfer getVoucherMoneyTransfer(PersistenceManager pm, Article article)
	{
		Query q = pm.newNamedQuery(VoucherMoneyTransfer.class, "getVoucherMoneyTransferByArticle");
		return (VoucherMoneyTransfer) q.execute(article);
	}

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true" null-value="exception"
	 */
	@Element(unique="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Article article;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherMoneyTransfer() { }

	public VoucherMoneyTransfer(BookType bookType,
			MoneyTransfer containerMoneyTransfer,
			Anchor from, Anchor to, Invoice invoice, long amount, Article article)
	{
		super(bookType, containerMoneyTransfer, from, to, invoice, amount);
		this.article = article;
	}

	public Article getArticle()
	{
		return article;
	}
}
