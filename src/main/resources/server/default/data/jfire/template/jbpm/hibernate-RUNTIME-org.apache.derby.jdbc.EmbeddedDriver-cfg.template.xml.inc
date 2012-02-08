
    <!-- jdbc connection properties -->
    <property name="hibernate.connection.datasource">${datasourceJNDIName_absolute_localTx}</property>

    <property name="hibernate.transaction.factory_class">org.hibernate.transaction.JTATransactionFactory</property>
	<property name="hibernate.transaction.manager_lookup_class">org.hibernate.transaction.JBossTransactionManagerLookup</property>

	<property name="hibernate.dialect">org.hibernate.dialect.DerbyDialect</property>
