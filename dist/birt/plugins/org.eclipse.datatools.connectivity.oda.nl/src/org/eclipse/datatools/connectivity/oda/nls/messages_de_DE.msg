#
#************************************************************************
# Copyright (c) 2004, 2006 Actuate Corporation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#  Actuate Corporation - initial API and implementation
#  
#************************************************************************
#
logManager_duplicateName=Es gibt bereits einen Logger mit diesem Namen.
#
manifest_nullArgument=Das Argument darf nicht null sein.
manifest_NO_DRIVER_RUNTIME_CONFIGURATION_DEFINED=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein <dataSource>-Element.
manifest_NO_DATA_SOURCE_EXTN_ID_DEFINED=In der Datenquellenerweiterung der ODA-Treiberdatei \"plugin.xml\" fehlt ein ID-Attribut.
manifest_INVALID_VERSION_VALUE=Die ODA-Treiberdatei \"plugin.xml\" verfügt über einen ungültigen Wert für \"odaVersion\" ({0}) in der Datenquellenerweiterung ({1}).
manifest_NO_DRIVER_CLASS_DEFINED=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein \"driverClass\"-Attribut in der Datenquellenerweiterung ({0}).
manifest_INVALID_SET_THREAD_CONTEXT_CLASSLOADER_VALUE=Die ODA-Treiberdatei \"plugin.xml\" verfügt über einen ungültigen Wert für \"setThreadContextClassLoader\" ({0}) in der Datenquellenerweiterung ({1}). 
manifest_NO_DATA_SET_TYPES_DEFINED=Die ODA-Treiberdatei \"plugin.xml\" muss über mindestens einen für die Datenquellenerweiterung ({0}) definierten <dataSet> verfügen.
manifest_NO_ATTRIBUTE_ID_DEFINED=The ODA driver plugin extension is missing an attribute ({0}) in the ({1}) element.
manifest_NO_DATA_TYPE_MAPPINGS_DEFINED=Die ODA-Treiberdatei \"plugin.xml\" sollte über mindestens ein für <dataSet> ({0}) definiertes <dataTypeMapping>-Element verfügen.
manifest_NO_NATIVE_TYPE_CODE_DEFINED=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein \"nativeDataTypeCode\"-Attribut in <dataTypeMapping> ({0}) von <dataSet> ({1}).
manifest_INVALID_NATIVE_TYPE_CODE_VALUE=Die ODA-Treiberdatei \"plugin.xml\" verfügt über einen ungültigen Wert für \"nativeDataTypeCode\" ({0}) in <dataTypeMapping> ({1}) für <dataSet> ({2}).
manifest_NO_NATIVE_TYPE_NAME_DEFINED=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein \"nativeDataType\"-Attribut im <dataTypeMapping> für <dataSet> ({0}).
manifest_NO_ODA_SCALAR_DATA_TYPE_DEFINED_1=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein Attribut \"odaScalarDataType\" in <dataTypeMapping> ({0}).
manifest_NO_ODA_SCALAR_DATA_TYPE_DEFINED_2=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein \"odaScalarDataType\"-Attribut in <alternativeOdaDataType> für <dataTypeMapping> ({0}).
manifest_INVALID_ODA_SCALAR_DATA_TYPE_VALUE=Die ODA-Treiberdatei \"plugin.xml\" verfügt über einen ungültigen Wert für \"odaScalarDataType\" ({0}) in <dataTypeMapping> ({1}).
manifest_INVALID_LOG_LEVEL_VALUE=Die ODA-Treiberdatei \"plugin.xml\" verfügt über einen ungültigen Wert für \"logLevel\" ({0}) in der Datenquellenerweiterung ({1}).
manifest_NO_LOG_FILENAME_PREFIX_DEFINED=In der ODA-Treiberdatei \"plugin.xml\" fehlt ein \"logFileNamePrefix\"-Attribut im <traceLogging> der Datenquellenerweiterung ({0}).
#
sortSpec_INVALID_SORT_MODE_SPECIFIED=Ungültiger Sortiermodus angegeben: {0}.
sortSpec_NULL_COLUMN_NAME_SPECIFIED=Kein Spaltenname angegeben.
sortSpec_INVALID_COLUMN_NAME_SPECIFIED=Ungültiger Spaltenname angegeben: {0}.
sortSpec_INVALID_SORT_ORDER_SPECIFIED=Ungültige Sortierreihenfolge angegeben: {0}.
sortSpec_NO_DYNAMIC_SORT_KEY_FOR_SORTMODENONE=Diese Sortierangabe sortSpec mit Sortiermodus sortModeNone lässt keine dynamischen Sortierschlüssel zu.
sortSpec_ONE_SORTCOLUMN_FOR_SINGLE_COLUMN_MODE=Es ist bereits eine Sortierspalte vorhanden. Der Modus sortModeSingleColumn lässt höchstens eine dynamische Sortierspalte zu.
sortSpec_ONE_SORTORDER_FOR_SINGLE_ORDER_MODE=Der Modus \"sortModeSingleOrder\" unterstützt nur eine Sortierreihenfolge.
sortSpec_ONLY_IN_SINGLE_ORDER_MODE=Nur im Modus sortModeSingleOrder unterstützt.
#
stringSubUtil_NO_STRING_VALUE_TO_REPLACE=Es kann kein Zeichenfolgenwert ersetzt werden: {0}
stringSubUtil_DELIMITER_CANNOT_BE_EMPTY=Das Trennzeichen kann keine leere Zeichenfolge sein und darf nicht nur Leerzeichen enthalten.
stringSubUtil_DELIMITER_CANNOT_BE_NULL=Das Trennzeichen darf nicht Null sein.
stringSubUtil_SUBSTITUTION_LIST_CANNOT_BE_NULL=Die Zeichenfolgen-Ersatzliste darf nicht Null sein.
stringSubUtil_SUBSTITUTION_VALUE_CANNOT_BE_NULL=Der Zeichenfolgen-Ersatzwert darf nicht Null sein.
stringSubUtil_TEXT_STRING_CANNOT_BE_NULL=Die Textzeichenfolge darf nicht Null sein.
stringSubUtil_NAME_VALUE_MAP_CANNOT_BE_NULL=Die Name-Wert-Zuordnung darf nicht Null sein.
