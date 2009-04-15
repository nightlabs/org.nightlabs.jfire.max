package org.nightlabs.jfire.department.prop;

import java.util.Map;

import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Helper class to create {@link StructBlock}s and special StructFields.
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class PropHelper {
	public static StructBlock createStructBlock(IStruct struct, StructBlockID structBlockID, Map<String, String> texts) {
		StructBlock block = new StructBlock(struct, structBlockID);
		for (String lang : texts.keySet()) {
			block.getName().setText(lang, texts.get(lang));
		}
		return block;
	}
	
	public static DepartmentStructField createDepartmentField(StructBlock sb, StructFieldID structFieldID, Map<String, String> texts) {
		DepartmentStructField field = new DepartmentStructField(sb, structFieldID);
		for (String lang : texts.keySet()) {
			field.getName().setText(lang, texts.get(lang));
		}
		return field;
	}
}
