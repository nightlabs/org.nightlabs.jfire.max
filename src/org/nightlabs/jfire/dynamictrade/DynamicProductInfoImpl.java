/**
 * 
 */
package org.nightlabs.jfire.dynamictrade;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.store.Unit;

/**
 * Helper implementation of {@link DynamicProductInfo}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
class DynamicProductInfoImpl implements DynamicProductInfo {

	private I18nText name;
	private long quantity;
	private Unit unit;
	private Price singlePrice;
	
	public DynamicProductInfoImpl(I18nText name, long quantity, Unit unit,
			Price singlePrice) {
		super();
		this.name = name;
		this.quantity = quantity;
		this.unit = unit;
		this.singlePrice = singlePrice;
	}



	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getName()
	 */
	@Override
	public I18nText getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getQuantity()
	 */
	@Override
	public long getQuantity() {
		return quantity;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getQuantityAsDouble()
	 */
	@Override
	public double getQuantityAsDouble() {
		return unit.toDouble(quantity);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getSinglePrice()
	 */
	@Override
	public Price getSinglePrice() {
		return singlePrice;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getUnit()
	 */
	@Override
	public Unit getUnit() {
		return unit;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setQuantity(long)
	 */
	@Override
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setSinglePrice(org.nightlabs.jfire.accounting.Price)
	 */
	@Override
	public void setSinglePrice(Price singlePrice) {
		this.singlePrice = singlePrice;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setUnit(org.nightlabs.jfire.store.Unit)
	 */
	@Override
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

}
