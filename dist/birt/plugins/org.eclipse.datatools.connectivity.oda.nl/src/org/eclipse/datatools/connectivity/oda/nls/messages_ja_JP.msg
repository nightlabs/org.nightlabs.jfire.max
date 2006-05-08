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
logManager_duplicateName=同じ名前のロガーが存在します。
#
manifest_nullArgument=引数は null にすることができません。
manifest_NO_DRIVER_RUNTIME_CONFIGURATION_DEFINED=ODA ドライバの plugin.xml に、<dataSource> 要素がありません。
manifest_NO_DATA_SOURCE_EXTN_ID_DEFINED=ODA ドライバの plugin.xml のデータソース拡張に、id 属性がありません。
manifest_INVALID_VERSION_VALUE=ODA ドライバの plugin.xml のデータソース拡張 ({1}) にある odaVersion の値 ({0}) が無効です。
manifest_NO_DRIVER_CLASS_DEFINED=ODA ドライバの plugin.xml のデータソース拡張 ({0}) に、driverClass 属性がありません。
manifest_INVALID_SET_THREAD_CONTEXT_CLASSLOADER_VALUE=ODA ドライバの plugin.xml のデータソース拡張 ({1}) にある setThreadContextClassLoader の値 ({0}) が無効です。 
manifest_NO_DATA_SET_TYPES_DEFINED=ODA ドライバの plugin.xml のデータソース拡張 ({0}) には、少なくとも 1 つの <dataSet> を定義する必要があります。
manifest_NO_ATTRIBUTE_ID_DEFINED=The ODA driver plugin extension is missing an attribute ({0}) in the ({1}) element.
manifest_NO_DATA_TYPE_MAPPINGS_DEFINED=ODA ドライバの plugin.xml の <dataSet> ({0}) には、少なくとも 1 つの <dataTypeMapping> 要素を定義する必要があります。
manifest_NO_NATIVE_TYPE_CODE_DEFINED=ODA ドライバの plugin.xml の <dataSet> ({1}) にある <dataTypeMapping> ({0}) に、nativeDataTypeCode 属性がありません。
manifest_INVALID_NATIVE_TYPE_CODE_VALUE=ODA ドライバの plugin.xml で、<dataSet> ({2}) の <dataTypeMapping> ({1}) にある nativeDataTypeCode ({0}) の値が無効です。
manifest_NO_NATIVE_TYPE_NAME_DEFINED=ODA ドライバの plugin.xml の <dataSet> ({0}) にある <dataTypeMapping> に、nativeDataType 属性がありません。
manifest_NO_ODA_SCALAR_DATA_TYPE_DEFINED_1=ODA ドライバの plugin.xml で、<dataSet> ({0}) の <dataTypeMapping> に nativeDataType 属性がありません。
manifest_NO_ODA_SCALAR_DATA_TYPE_DEFINED_2=ODA ドライバの plugin.xml の <dataTypeMapping> ({0}) にある <alternativeOdaDataType> に、odaScalarDataType 属性がありません。
manifest_INVALID_ODA_SCALAR_DATA_TYPE_VALUE=ODA ドライバの plugin.xml の <dataTypeMapping> ({1}) にある odaScalarDataType の値 ({0}) が無効です。
manifest_INVALID_LOG_LEVEL_VALUE=ODA ドライバの plugin.xml のデータソース拡張 ({1}) にある logLevel の値 ({0}) が無効です。
manifest_NO_LOG_FILENAME_PREFIX_DEFINED=ODA ドライバの plugin.xml で、データソースの拡張 ({0}) の <traceLogging> に logFileNamePrefix 属性がありません。
#
sortSpec_INVALID_SORT_MODE_SPECIFIED=無効なソートモードが指定されました : {0}.
sortSpec_NULL_COLUMN_NAME_SPECIFIED=カラムの名前に null が指定されました。
sortSpec_INVALID_COLUMN_NAME_SPECIFIED=無効なカラム名が指定されました : {0}.
sortSpec_INVALID_SORT_ORDER_SPECIFIED=無効なソート順が指定されました : {0}.
sortSpec_NO_DYNAMIC_SORT_KEY_FOR_SORTMODENONE=この sortSpec で sortModeNone を使うと、動的ソートキーを指定できません。
sortSpec_ONE_SORTCOLUMN_FOR_SINGLE_COLUMN_MODE=ソートカラムはすでに存在します。sortModeSingleColumn モードを使った場合は、1 つの動的ソートカラムしか指定できません。
sortSpec_ONE_SORTORDER_FOR_SINGLE_ORDER_MODE=sortModeSingleOrder モードでは、1 つのソート順しか使用できません。
sortSpec_ONLY_IN_SINGLE_ORDER_MODE=sortModeSingleOrder モードでのみ指定できます。
#
stringSubUtil_NO_STRING_VALUE_TO_REPLACE=置換する文字列値がありません : {0}
stringSubUtil_DELIMITER_CANNOT_BE_EMPTY=区切り文字には、空白やスペースを指定することができません。
stringSubUtil_DELIMITER_CANNOT_BE_NULL=区切り文字は null にできません。
stringSubUtil_SUBSTITUTION_LIST_CANNOT_BE_NULL=文字列の代替リストは null にできません。
stringSubUtil_SUBSTITUTION_VALUE_CANNOT_BE_NULL=文字列の代替値は null にできません。
stringSubUtil_TEXT_STRING_CANNOT_BE_NULL=テキスト文字列は null にできません。
stringSubUtil_NAME_VALUE_MAP_CANNOT_BE_NULL=名前と値のマップは null にできません。
