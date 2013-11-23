package org.nightlabs.jfire.store.reverse;

/**
 * Abstract base implementation of the {@link IReverseProductError} interface. 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractReverseProductError 
implements IReverseProductError 
{
	private String description;

	public AbstractReverseProductError() {}
	
	public AbstractReverseProductError(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
