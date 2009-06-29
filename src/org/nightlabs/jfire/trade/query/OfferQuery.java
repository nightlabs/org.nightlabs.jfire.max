package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author marco schulze - marco at nightlabs dot de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class OfferQuery
	extends AbstractArticleContainerQuery
{
	private static final long serialVersionUID = 20090626;

//	public static final class FieldName
//	{
//		public static final String articleDeliveryDate = "articleDeliveryDate";
//	}

	private boolean reserved;
//	private Date articleDeliveryDate;

	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "offerID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "offerIDPrefix";
	}

	@Override
	protected void checkVendorName(StringBuilder filter) {
		if (getVendorName() != null)
			filter.append("\n && (this.order.vendor.person.displayName.toLowerCase().indexOf(\""+getVendorName().toLowerCase()+"\") >= 0)");
	}

	@Override
	protected void checkCustomerName(StringBuilder filter)
	{
		if (getCustomerName() != null)
			filter.append("\n && (this.order.customer.person.displayName.toLowerCase().indexOf(\""+getCustomerName().toLowerCase()+"\") >= 0)");
	}

	@Override
	protected void checkCustomer(StringBuilder filter) {
		if (getCustomerID() != null) {
			filter.append("\n && JDOHelper.getObjectId(this.order.customer) == :customerID");
		}
	}

	@Override
	protected void checkVendor(StringBuilder filter) {
		if (getVendorID() != null) {
			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
		}
	}

	@Override
	protected Class<Offer> initCandidateClass() {
		return Offer.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuilder filter)
	{
		if (reserved) {
			// reserved means finalized but not accepted
			filter.append("\n && this.offerLocal.acceptDT == null");
			filter.append(" && this.offerLocal.rejectDT == null");
			filter.append(" && this.finalizeDT != null");
		}

//		if (articleDeliveryDate != null) {
//			String varName = "articleVar";
//			addVariable(Article.class, varName);
//			filter.append("\n && (\n" +
//					"  this."+getArticlesMemberName()+"."+"contains("+varName+")"+"\n" +
//					"  && "+varName+".deliveryDateOffer >= :articleDeliveryDate" +
//			" )");
//		}
	}

//	protected String getArticlesMemberName() {
//		return "articles";
//	}
//
//	/**
//	 * Returns the articleDeliveryDate.
//	 * @return the articleDeliveryDate
//	 */
//	public Date getArticleDeliveryDate() {
//		return articleDeliveryDate;
//	}
//
//	/**
//	 * Sets the articleDeliveryDate.
//	 * @param articleDeliveryDate the articleDeliveryDate to set
//	 */
//	public void setArticleDeliveryDate(Date articleDeliveryDate) {
//		Date oldArticleDeliveryDate = this.articleDeliveryDate;
//		this.articleDeliveryDate = articleDeliveryDate;
//		notifyListeners(FieldName.articleDeliveryDate, oldArticleDeliveryDate, articleDeliveryDate);
//	}

}
