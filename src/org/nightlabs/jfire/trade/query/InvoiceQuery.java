package org.nightlabs.jfire.trade.query;

import java.util.Date;
import java.util.List;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.CurrencyID;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class InvoiceQuery
	extends AbstractArticleContainerQuery
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

	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "InvoiceQuery.";
	public static final String PROPERTY_AMOUNT_PAID_MAX = PROPERTY_PREFIX + "amountPaidMax";
	public static final String PROPERTY_AMOUNT_PAID_MIN = PROPERTY_PREFIX + "amountPaidMin";
	public static final String PROPERTY_AMOUNT_TO_PAY_MAX = PROPERTY_PREFIX + "amountToPayMax";
	public static final String PROPERTY_AMOUNT_TO_PAY_MIN = PROPERTY_PREFIX + "amountToPayMin";
	public static final String PROPERTY_BOOK_DATE_MAX = PROPERTY_PREFIX + "bookDTMax";
	public static final String PROPERTY_BOOK_DATE_MIN = PROPERTY_PREFIX + "bookDTMin";
	public static final String PROPERTY_BOOKED = PROPERTY_PREFIX + "booked";
	public static final String PROPERTY_CURRENCY = PROPERTY_PREFIX + "currencyID";

	@Override
	public String getArticleContainerIDMemberName() {
		return "invoiceID";
	}
	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "invoiceIDPrefix";
	}

	@Override
	protected Class<Invoice> initCandidateClass()
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
		final Long oldAmountPaidMax = this.amountPaidMax;
		this.amountPaidMax = amountPaidMax;
		notifyListeners(PROPERTY_AMOUNT_PAID_MAX, oldAmountPaidMax, amountPaidMax);
	}

	public Long getAmountPaidMin()
	{
		return amountPaidMin;
	}

	public void setAmountPaidMin(Long amountPaidMin)
	{
		final Long oldAmountPaidMin = this.amountPaidMin;
		this.amountPaidMin = amountPaidMin;
		notifyListeners(PROPERTY_AMOUNT_PAID_MIN, oldAmountPaidMin, amountPaidMin);
	}

	public Long getAmountToPayMax()
	{
		return amountToPayMax;
	}

	public void setAmountToPayMax(Long amountToPayMax)
	{
		final Long oldAmountToPayMax = this.amountToPayMax;
		this.amountToPayMax = amountToPayMax;
		notifyListeners(PROPERTY_AMOUNT_TO_PAY_MAX, oldAmountToPayMax, amountToPayMax);
	}

	public Long getAmountToPayMin()
	{
		return amountToPayMin;
	}

	public void setAmountToPayMin(Long amountToPayMin)
	{
		final Long oldAmountToPayMin = this.amountToPayMin;
		this.amountToPayMin = amountToPayMin;
		notifyListeners(PROPERTY_AMOUNT_TO_PAY_MIN, oldAmountToPayMin, amountToPayMin);
	}

	public Boolean getBooked()
	{
		return booked;
	}

	public void setBooked(Boolean booked)
	{
		final Boolean oldBooked = this.booked;
		this.booked = booked;
		notifyListeners(PROPERTY_BOOKED, oldBooked, booked);
	}

	public Date getBookDTMax()
	{
		return bookDTMax;
	}
	public void setBookDTMax(Date bookDTMax)
	{
		final Date oldBookDTMax = this.bookDTMax;
		this.bookDTMax = bookDTMax;
		notifyListeners(PROPERTY_BOOK_DATE_MAX, oldBookDTMax, bookDTMax);
	}

	public Date getBookDTMin()
	{
		return bookDTMin;
	}
	public void setBookDTMin(Date bookDTMin)
	{
		final Date oldBookDTMin = this.bookDTMin;
		this.bookDTMin = bookDTMin;
		notifyListeners(PROPERTY_BOOK_DATE_MIN, oldBookDTMin, bookDTMin);
	}

	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		if (allFields || PROPERTY_AMOUNT_PAID_MAX.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_AMOUNT_PAID_MAX, amountPaidMax) );
		}
		if (allFields || PROPERTY_AMOUNT_PAID_MIN.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_AMOUNT_PAID_MIN, amountPaidMin) );
		}
		if (allFields || PROPERTY_AMOUNT_TO_PAY_MAX.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_AMOUNT_TO_PAY_MAX, amountToPayMax) );
		}
		if (allFields || PROPERTY_AMOUNT_TO_PAY_MIN.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_AMOUNT_TO_PAY_MIN, amountToPayMin) );
		}
		if (allFields || PROPERTY_BOOKED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_BOOKED, booked) );
		}
		if (allFields || PROPERTY_BOOK_DATE_MAX.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_BOOK_DATE_MAX, bookDTMax) );
		}
		if (allFields || PROPERTY_BOOK_DATE_MIN.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_BOOK_DATE_MIN, bookDTMin) );
		}
		return changedFields;
	}
}
