/**
 * 
 */
package org.nightlabs.jfire.scripting.condition;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class VisibleScope 
{
	public static final String VARIABLE_NAME = "visibleScope";
	
	private boolean visible = false;

	/**
	 * Return the visible.
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}
