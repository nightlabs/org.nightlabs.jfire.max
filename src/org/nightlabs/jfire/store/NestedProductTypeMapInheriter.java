package org.nightlabs.jfire.store;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.MapEntryMetaData;
import org.nightlabs.inheritance.MapFieldMetaData;

public class NestedProductTypeMapInheriter
		implements FieldInheriter
{

	public void copyFieldValue(Inheritable mother, Inheritable child,
			Class motherClass, Class childClass, Field field,
			FieldMetaData motherFieldMetaData, FieldMetaData childFieldMetaData)
	{
		ProductType childProductType = (ProductType) child;
		ProductType motherProductType = (ProductType) mother;
		childProductType.getNestedProductTypes();
		motherProductType.getNestedProductTypes();
		MapFieldMetaData motherMeta = (MapFieldMetaData) motherFieldMetaData;
		MapFieldMetaData childMeta = (MapFieldMetaData) childFieldMetaData;

		try {
			field.setAccessible(true);
			Map<String, NestedProductType> motherNestedProductTypes = (Map<String, NestedProductType>) field.get(mother);
			Map<String, NestedProductType> childNestedProductTypes = (Map<String, NestedProductType>) field.get(child);

			HashSet<String> keysToDelete = new HashSet<String>();
			for (Map.Entry<String, NestedProductType> me : childNestedProductTypes.entrySet()) {
				String key = me.getKey();
				if (!motherNestedProductTypes.containsKey(key)) {
					MapEntryMetaData memd = childMeta.getMapEntryMetaData(key);
					if (memd.isValueInherited())
						keysToDelete.add(key);
				}
			}

			for (String key : keysToDelete)
				childNestedProductTypes.remove(key);

			for (Map.Entry<String, NestedProductType> me : motherNestedProductTypes.entrySet()) {
				String key = me.getKey();
				MapEntryMetaData memd = childMeta.getMapEntryMetaData(key);
				if (memd.isValueInherited()) {
					NestedProductType m_npt = me.getValue();
					NestedProductType c_npt = childNestedProductTypes.get(key);
					if (c_npt == null) {
						c_npt = new NestedProductType(childProductType, m_npt.getInnerProductType(), m_npt.getQuantity());
						childNestedProductTypes.put(key, c_npt);
					}
					else {
						if (m_npt.getQuantity() != c_npt.getQuantity())
							c_npt.setQuantity(m_npt.getQuantity());
					}
				}
			}

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
