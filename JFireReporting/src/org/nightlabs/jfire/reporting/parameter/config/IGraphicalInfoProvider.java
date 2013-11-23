package org.nightlabs.jfire.reporting.parameter.config;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IGraphicalInfoProvider
{
	public static final String PROP_X = "X";
	public static final String PROP_Y = "Y";
	
	/**
	 * returns the x coordinate
	 * @return the x coordinate
	 */
	int getX();

	/**
	 * sets the x coordinate
	 * @param x the x coordinate to set
	 */
	void setX(int x);
	
	/**
	 * returns the y coordinate
	 * @return the y coordinate
	 */
	int getY();
	
	/**
	 * sets the y coordinate
	 * @param y the y coordinate to set
	 */
	void setY(int y);
	
}
