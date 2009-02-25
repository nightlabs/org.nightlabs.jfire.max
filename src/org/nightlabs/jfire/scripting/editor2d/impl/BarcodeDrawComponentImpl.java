/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.scripting.editor2d.impl;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.linear.code128.Code128Barcode;

import org.apache.log4j.Logger;
import org.nightlabs.editor2d.DrawComponent;
import org.nightlabs.editor2d.DrawComponentContainer;
import org.nightlabs.editor2d.impl.DrawComponentImpl;
import org.nightlabs.editor2d.render.BaseRenderer;
import org.nightlabs.editor2d.render.Renderer;
import org.nightlabs.i18n.unit.resolution.DPIResolutionUnit;
import org.nightlabs.i18n.unit.resolution.IResolutionUnit;
import org.nightlabs.i18n.unit.resolution.Resolution;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.scripting.editor2d.BarcodeDrawComponent;
import org.nightlabs.jfire.scripting.editor2d.render.j2d.J2DBarcodeDefaultRenderer;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class BarcodeDrawComponentImpl
extends DrawComponentImpl
implements BarcodeDrawComponent
{
	private static final long serialVersionUID = 1L;
	public static final Logger logger = Logger.getLogger(BarcodeDrawComponentImpl.class);

	private int barcodeHeight = HEIGHT_DEFAULT;
//	private int originalX = 0;
//	private int originalY = 0;

	public BarcodeDrawComponentImpl() {
		super();
	}

	public BarcodeDrawComponentImpl(Type type, String value, int x, int y, WidthScale widthScale,
			int height, Orientation orientation, boolean printHumanReadable,
			DrawComponentContainer parent, ScriptRegistryItemID scriptID)
	{
		super();

		if (value == null)
			throw new IllegalArgumentException("Param value must not be null!");

		if (scriptID == null)
			throw new IllegalArgumentException("Param scriptID must not be null!");

		setParent(parent);
		this.type = type;
		this.x = x;
		this.y = y;
//		this.originalX = x;
//		this.originalY = y;

		this.text = value;
		this.humanReadable = printHumanReadable;
		this.widthScale = widthScale;
		this.orientation = orientation;
		this.barcodeHeight = height;
		this.scriptRegistryItemID = scriptID;
		this.scriptRegistryItemIDKeyStr = scriptRegistryItemID.toString();

		refresh();
//		getGeneralShape();
	}

//	private transient GeneralShape generalShape = null;
//	private GeneralShape getGeneralShape()
//	{
//		if (generalShape == null) {
//			Rectangle initialBounds = null;
//			if (orientation == Orientation.HORIZONTAL) {
////				initialBounds = new Rectangle(originalX, originalY, getBarcode().getWidth(), barcodeHeight);
//				initialBounds = new Rectangle(x, y, getBarcode().getWidth(), barcodeHeight);
//			}
//			if (orientation == Orientation.VERTICAL) {
////				initialBounds = new Rectangle(originalX, originalY, barcodeHeight, getBarcode().getWidth());
//				initialBounds = new Rectangle(x, y, barcodeHeight, getBarcode().getWidth());
//			}
//			this.generalShape = new GeneralShape(initialBounds);
//		}
//		return generalShape;
//	}

	private static final IResolutionUnit dpiUnit = new DPIResolutionUnit();
	private int getModelResolution() {
		return (int) getRoot().getResolution().getResolutionX(dpiUnit);
	}

	private transient Barcode barcode = null;
	public Barcode getBarcode()
	{
		if (barcode == null)
		{
			try {
				barcode = getBarcode(getType());
			} catch (BarcodeException e) {
				throw new RuntimeException(e);
			}
		}
		return barcode;
	}

	protected Barcode getBarcode(Type type)
	throws BarcodeException
	{
		Barcode barcode = null;
		switch (type)
		{
			case TYPE_128:
				barcode = new Code128Barcode(getText());
		}
		return barcode;
	}

	private Type type = TYPE_DEFAULT;
	public Type getType() {
		return type;
	}
	public void setType(Type type)
	{
		if (this.type != type)
		{
			Type oldType = this.type;
			this.type = type;
			barcode = null;
			refresh();
			firePropertyChange(PROP_TYPE, oldType, type);
		}
	}

	private boolean humanReadable = HUMAN_READABLE_DEFAULT;
	public boolean isHumanReadable() {
		return humanReadable;
	}
	public void setHumanReadable(boolean humanReadbale) {
		this.humanReadable = humanReadbale;
		getBarcode().setDrawingText(humanReadbale);
		clearBounds();
		firePropertyChange(PROP_HUMAN_READABLE, !humanReadbale, humanReadbale);
	}

	private transient ScriptRegistryItemID scriptRegistryItemID = null;
	private String scriptRegistryItemIDKeyStr = null;

	public ScriptRegistryItemID getScriptRegistryItemID() {
		if (scriptRegistryItemID == null && scriptRegistryItemIDKeyStr != null)
			scriptRegistryItemID = (ScriptRegistryItemID) ObjectIDUtil.createObjectID(scriptRegistryItemIDKeyStr);
		return scriptRegistryItemID;
	}

	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID)
	{
		ScriptRegistryItemID oldID = this.scriptRegistryItemID;
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.scriptRegistryItemIDKeyStr = scriptRegistryItemID == null ? null : scriptRegistryItemID.toString();
		refresh();
		firePropertyChange(PROP_SCRIPT_REGISTRY_ITEM_ID, oldID, scriptRegistryItemID);
	}

	private Orientation orientation = ORIENTATION_DEFAULT;
	public Orientation getOrientation() {
		return orientation;
	}
	public void setOrientation(Orientation orientation)
	{
		if (this.orientation != orientation) {
			Orientation oldOrientation = this.orientation;
			this.orientation = orientation;
			refresh();
			firePropertyChange(PROP_ORIENTATION, oldOrientation, orientation);
		}
	}

	private WidthScale widthScale = WIDTH_SCALE_DEFAULT;
	public WidthScale getWidthScale() {
		return widthScale;
	}
	public void setWidthScale(WidthScale widthScale)
	{
		if (this.widthScale != widthScale)
		{
			WidthScale oldWidthScale = this.widthScale;
			this.widthScale = widthScale;
			refresh();
			firePropertyChange(PROP_WIDTH_SCALE, oldWidthScale, widthScale);
		}
	}

	protected void refresh()
	{
//		generalShape = null;
		clearBounds();

		Font scaledFont = getScaledFont(DEFAULT_FONT);
		getBarcode().setFont(scaledFont);
		getBarcode().setDrawingText(isHumanReadable());
		getBarcode().setResolution(getModelResolution());
		double barWidth = getBarWidth(getWidthScale());
		getBarcode().setBarWidth((int)barWidth);
		getBarcode().setBarHeight(barcodeHeight);
		if (logger.isDebugEnabled())
		{
			logger.debug("Resolution = "+getModelResolution());
			logger.debug("Orientation = "+getOrientation());
			logger.debug("x = "+x);
			logger.debug("y = "+y);
			logger.debug("barWidth = "+barWidth);
			logger.debug("width = "+this.width);
			logger.debug("height = "+this.height);
			logger.debug("barcodeHeight = "+this.barcodeHeight);
			logger.debug("getBarcode.getBounds() = "+getBarcode().getBounds());
			logger.debug("this.getBounds() = "+this.getBounds());
			logger.debug("");
		}
	}

//	@Override
//	public Rectangle getBounds()
//	{
//		if (getGeneralShape() != null) {
//			return getGeneralShape().getBounds();
//		}
//		else {
//			return super.getBounds();
//		}
//	}

	@Override
	public Rectangle getBounds()
	{
		if (bounds == null) {
			Rectangle initialBounds = null;
			if (orientation == Orientation.HORIZONTAL) {
				initialBounds = new Rectangle(x, y, getBarcode().getWidth(), barcodeHeight);
//				initialBounds = new Rectangle(x, y, getBarcode().getWidth(), getBarcode().getHeight());
			}
			if (orientation == Orientation.VERTICAL) {
				initialBounds = new Rectangle(x, y, barcodeHeight, getBarcode().getWidth());
//				initialBounds = new Rectangle(x, y, getBarcode().getHeight(), getBarcode().getWidth());
			}
			this.bounds = initialBounds;
		}
		return bounds;
	}

	protected double getBarWidth(WidthScale scale)
	{
		double width = 1;
		switch (scale)
		{
			case SCALE_1:
				width = 2d;
				break;
			case SCALE_2:
				width = 3d;
				break;
			case SCALE_3:
				width = 4d;
				break;
			case SCALE_4:
				width = 5d;
				break;
			default:
				width = 3d;
				break;
		}

		int resolution = getModelResolution();
		double factor = (resolution) / 300d;
		double scaledWidth = width * factor;
		if (logger.isDebugEnabled()) {
			logger.debug("width = " + width);
			logger.debug("ModelUnit Factor = "+factor);
			logger.debug("ScaledBarWidth = " + scaledWidth);
		}
		return scaledWidth;
	}

	protected Font getScaledFont(Font f)
	{
		double defaultFontSize = f.getSize();
		double defaultResolutionDPI = 72;
//		double resolutionScale = getModelResolution() / defaultResolutionDPI;
		double resolutionScale = 300 / defaultResolutionDPI;
		int newFontSize = (int) (defaultFontSize * resolutionScale);

		if (logger.isDebugEnabled()) {
			logger.debug("defaultFontSize = "+defaultFontSize);
			logger.debug("defaultResolutionDPI = "+defaultResolutionDPI);
			logger.debug("getModelResolution() = "+getModelResolution());
			logger.debug("resolutionScale = "+resolutionScale);
			logger.debug("newFontSize = "+newFontSize);
		}
		return new Font(f.getName(), f.getStyle(), newFontSize);
	}

//	@Override
//	protected void primSetHeight(float height)
//	{
//		super.primSetHeight(height);
//		refresh();
//	}

	@Override
	public String getTypeName() {
		return "Ticket Barcode";
	}

	@Override
	public Class<? extends DrawComponent> getRenderModeClass() {
		return BarcodeDrawComponent.class;
	}

//	@Override
//	protected void primSetLocation(int newX, int newY)
//	{
//		this.x = newX;
//		this.y = newY;
//		getBarcode().setLocation(newX, newY);
//	}

	@Override
	protected Renderer initDefaultRenderer() {
		Renderer r = new BaseRenderer();
		r.addRenderContext(new J2DBarcodeDefaultRenderer());
		return r;
	}

	private transient String text = VALUE_DEFAULT;
	public String getText() {
		return text;
	}
	public void setText(String text)
	{
		if (this.text == null ||
			(this.text != null && !this.text.equals(text)) )
		{
			String oldValue = this.text;
			this.text = text;
			barcode = null;
			refresh();
			firePropertyChange(PROP_VALUE, oldValue, text);
		}
	}

	private transient Object scriptValue;
	public Object getScriptValue() {
		return scriptValue;
	}

	public void setScriptValue(Object scriptValue) {
		this.scriptValue = scriptValue;
		if (scriptValue instanceof String) {
			setText(((String)scriptValue));
		}
	}

	@Override
	public Object clone(DrawComponentContainer parent) {
		BarcodeDrawComponentImpl clone = (BarcodeDrawComponentImpl) super.clone(parent);
		clone.barcode = null;
		return clone;
	}

//	/* (non-Javadoc)
//	 * @see org.nightlabs.editor2d.impl.DrawComponentImpl#transform(java.awt.geom.AffineTransform, boolean)
//	 */
//	@Override
//	public void transform(AffineTransform newAT, boolean fromParent) {
//		super.transform(newAT, fromParent);
//		generalShape = null;
//		getGeneralShape().transform(getAffineTransform());
//		if (!fromParent && getParent() != null)
//			getParent().notifyChildTransform(this);
//
//		refresh();
//	}
	/* (non-Javadoc)
	 * @see org.nightlabs.editor2d.impl.DrawComponentImpl#transform(java.awt.geom.AffineTransform, boolean)
	 */
	@Override
	public void transform(AffineTransform newAT, boolean fromParent) {
		super.transform(newAT, fromParent);
		Point2D xy = new Point2D.Double(x,y);
//		Point2D newXY = getAffineTransform().transform(xy, null);
		Point2D newXY = newAT.transform(xy, null);
		x = (int) Math.rint(newXY.getX());
		y = (int) Math.rint(newXY.getY());
		if (!fromParent && getParent() != null)
			getParent().notifyChildTransform(this);

		refresh();
	}

	public void setBarcodeHeight(int barcodeHeight)
	{
		int oldBarcodeHeight = this.barcodeHeight;
		this.barcodeHeight = barcodeHeight;
		refresh();
		firePropertyChange(PROP_VALUE, oldBarcodeHeight, barcodeHeight);
	}

	public int getBarcodeHeight()
	{
		return barcodeHeight;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.editor2d.impl.DrawComponentImpl#resolutionChanged(org.nightlabs.i18n.unit.resolution.Resolution, org.nightlabs.i18n.unit.resolution.Resolution)
	 */
	@Override
	public void resolutionChanged(Resolution oldResolution, Resolution newResolution)
	{
		Point2D scale = getResolutionScale(oldResolution, newResolution);
		barcodeHeight = (int) Math.rint(barcodeHeight * scale.getY());
		super.resolutionChanged(oldResolution, newResolution);
	}

}
