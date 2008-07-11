package org.nightlabs.jfire.store.reverse;

import java.io.Serializable;

/**
 * Interface for object describing why the reverse of an Product was not successful.
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public interface IReverseProductError
extends Serializable
{
	/**
	 * Returns the description.
	 * @return the description
	 */
	String getDescription();
	
	/**
	 * Sets the description;
	 * @param description the description to set.
	 */
	void setDescription(String description);
}
