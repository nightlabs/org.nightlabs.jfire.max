package org.nightlabs.jfire.store;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.MapEntryMetaData;
import org.nightlabs.inheritance.MapFieldMetaData;

public class NestedProductTypeLocalMapInheriter
		implements FieldInheriter
{

	@SuppressWarnings("unchecked")
	public void copyFieldValue(Inheritable mother, Inheritable child,
			Class motherClass, Class childClass, Field field,
			FieldMetaData motherFieldMetaData, FieldMetaData childFieldMetaData)
	{
		ProductTypeLocal childProductTypeLocal = (ProductTypeLocal) child;
		ProductTypeLocal motherProductTypeLocal = (ProductTypeLocal) mother;
		childProductTypeLocal.getNestedProductTypeLocals();
		motherProductTypeLocal.getNestedProductTypeLocals();
//		MapFieldMetaData motherMeta = (MapFieldMetaData) motherFieldMetaData;
		MapFieldMetaData childMeta = (MapFieldMetaData) childFieldMetaData;

		try {
			field.setAccessible(true);
			Map<String, NestedProductTypeLocal> motherNestedProductTypes = (Map<String, NestedProductTypeLocal>) field.get(mother);
			Map<String, NestedProductTypeLocal> childNestedProductTypes = (Map<String, NestedProductTypeLocal>) field.get(child);

			HashSet<String> keysToDelete = new HashSet<String>();
			for (Map.Entry<String, NestedProductTypeLocal> me : childNestedProductTypes.entrySet()) {
				String key = me.getKey();
				if (!motherNestedProductTypes.containsKey(key)) {
					MapEntryMetaData memd = childMeta.getMapEntryMetaData(key);
					if (memd.isValueInherited())
						keysToDelete.add(key);
				}
			}

			for (String key : keysToDelete)
				childNestedProductTypes.remove(key);

			for (Map.Entry<String, NestedProductTypeLocal> me : motherNestedProductTypes.entrySet()) {
				String key = me.getKey();
				MapEntryMetaData memd = childMeta.getMapEntryMetaData(key);
				if (memd.isValueInherited()) {
					NestedProductTypeLocal m_npt = me.getValue();
					NestedProductTypeLocal c_npt = childNestedProductTypes.get(key);
					if (c_npt == null) {
						c_npt = new NestedProductTypeLocal(childProductTypeLocal, m_npt.getInnerProductTypeLocal(), m_npt.getQuantity());
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
