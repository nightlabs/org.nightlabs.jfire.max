package org.nightlabs.jfire.prop.file;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.StructFieldID;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFirePropFile_FileStructField")
	@FetchGroups(
		@FetchGroup(
			fetchGroups={"default"},
			name="IStruct.fullData",
			members=@Persistent(name="formats"))
	)
	@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class FileStructField extends StructField<FileDataField>
{
	private static final long serialVersionUID = 1L;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long maxSizeKB;

	@Join
	@Persistent(
		dependentElement="true",
		table="JFirePropFile_FileStructField_formats"
	)
	private List<String> formats = new LinkedList<String>();
	// TODO we should instead or additionally support content-types here. Marco.


	protected FileStructField() { }

	public FileStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID);
	}

	public FileStructField(StructBlock structBlock) {
		super(structBlock);
	}

	public FileStructField(StructBlock structBlock, String structFieldOrganisationID, String structFieldID) {
		super(structBlock, structFieldOrganisationID, structFieldID);
	}


	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected FileDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new FileDataField(dataBlock, this);
	}

	/**
	 * Add a new file format (file extension) to the list of valid formats of data base on this {@link FileStructField}.
	 * @param extension The extension to add.
	 */
	public void addFileFormat(String extension) {
		if (!Pattern.matches("(\\w+|\\*)", extension))
			throw new IllegalArgumentException("Invalid extension specified.");

		if (!formats.contains(extension)) {
			formats.add(extension);

			notifyModifyListeners();
		}
	}

	public void removeFileFormat(String extension) {
		if (formats.contains(extension)) {
			formats.remove(extension);

			notifyModifyListeners();
		}
	}

	public void clearFileFormats() {
		formats.clear();

		notifyModifyListeners();
	}

	public List<String> getFileFormats() {
		return Collections.unmodifiableList(formats);
	}

	public void setMaxSizeKB(int maxKBytes) {
		this.maxSizeKB = maxKBytes;
		notifyModifyListeners();
	}

	public long getMaxSizeKB() {
		return maxSizeKB;
	}

	public boolean validateData() {
		resetValidationError();
		if (formats.isEmpty()) {
			setValidationError("You have to specify at least one extension.");
			return false;
		}
		return true;
	}

	public boolean validateSize(long sizeKB) {
		return (sizeKB <= maxSizeKB);
	}

	@Override
	public Class<FileDataField> getDataFieldClass() {
		return FileDataField.class;
	}

}
