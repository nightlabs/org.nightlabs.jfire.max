package org.nightlabs.jfire.trade.query;

import java.util.Date;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.CurrencyID;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class InvoiceQuickSearchQuery
	extends AbstractArticleContainerQuickSearchQuery<Invoice>
{
	private static final long serialVersionUID = 1L;

	private CurrencyID currencyID = null;
	private Currency currency = null;

	private Long amountToPayMin = null;
	private Long amountToPayMax = null;
	private Long amountPaidMin = null;
	private Long amountPaidMax = null;
	private Boolean booked = null;
	
	private Date bookDTMin = null;
	private Date bookDTMax = null;

//	@Override
//	public Class getArticleContainerClass() {
//		return Invoice.class;
//	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "invoiceID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "invoiceIDPrefix";
	}

	@Override
	protected Class<Invoice> init()
	{
		return Invoice.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuffer filter)
	{
		if (currencyID != null) {
//		filter.append("\n && JDOHelper.getObjectId(this.price.currency) == :currencyID");
		currency = (Currency) getPersistenceManager().getObjectById(currencyID);
		filter.append("\n && this.price.currency == :currency");
	}

	if (amountToPayMin != null)
		filter.append("\n && this.price.amount - this.invoiceLocal.amountPaid >= :amountToPayMin");

	if (amountToPayMax != null)
		filter.append("\n && this.price.amount - this.invoiceLocal.amountPaid <= :amountToPayMax");

	if (amountPaidMin != null)
		filter.append("\n && this.invoiceLocal.amountPaid >= :amountPaidMin");

	if (amountPaidMax != null)
		filter.append("\n && this.invoiceLocal.amountPaid <= :amountPaidMax");

	if (bookDTMin == null && bookDTMax == null) {
		if (booked != null && booked.booleanValue())
			filter.append("\n && this.invoiceLocal.bookDT != null");

		if (booked != null && !booked.booleanValue())
			filter.append("\n && this.invoiceLocal.bookDT == null");
	}

	if (bookDTMin != null)
		filter.append("\n && this.bookDT >= :bookDTMin");

	if (bookDTMax != null)
		filter.append("\n && this.bookDT >= :bookDTMax");
	}
	
	public CurrencyID getCurrencyID()
	{
		return currencyID;
	}
	public void setCurrencyID(CurrencyID currencyID)
	{
		this.currencyID = currencyID;
	}

	public Long getAmountPaidMax()
	{
		return amountPaidMax;
	}

	public void setAmountPaidMax(Long amountPaidMax)
	{
		this.amountPaidMax = amountPaidMax;
	}

	public Long getAmountPaidMin()
	{
		return amountPaidMin;
	}

	public void setAmountPaidMin(Long amountPaidMin)
	{
		this.amountPaidMin = amountPaidMin;
	}

	public Long getAmountToPayMax()
	{
		return amountToPayMax;
	}

	public void setAmountToPayMax(Long amountToPayMax)
	{
		this.amountToPayMax = amountToPayMax;
	}

	public Long getAmountToPayMin()
	{
		return amountToPayMin;
	}

	public void setAmountToPayMin(Long amountToPayMin)
	{
		this.amountToPayMin = amountToPayMin;
	}

	public Boolean getBooked()
	{
		return booked;
	}

	public void setBooked(Boolean booked)
	{
		this.booked = booked;
	}

	public Date getBookDTMax()
	{
		return bookDTMax;
	}
	public void setBookDTMax(Date bookDTMax)
	{
		this.bookDTMax = bookDTMax;
	}

	public Date getBookDTMin()
	{
		return bookDTMin;
	}
	public void setBookDTMin(Date bookDTMin)
	{
		this.bookDTMin = bookDTMin;
	}

}
