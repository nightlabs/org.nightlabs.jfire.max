package org.nightlabs.jfire.voucher.accounting;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

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
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	private Article article;

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherMoneyTransfer() { }

	public VoucherMoneyTransfer(String bookType,
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
