
    <!-- jdbc connection properties -->
<!-- 
    <property name="hibernate.dialect">org.hibernate.dialect.HSQLDialect</property>
    <property name="hibernate.connection.datasource">java:/DefaultDS</property>
-->

    <property name="hibernate.connection.datasource">${datasourceJNDIName_absolute}</property>

    <property name="hibernate.transaction.factory_class">org.hibernate.transaction.JTATransactionFactory</property>
	<property name="hibernate.transaction.manager_lookup_class">org.hibernate.transaction.JBossTransactionManagerLookup</property>

	<property name="hibernate.dialect">org.hibernate.dialect.MySQLInnoDBDialect</property>
<!--   <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property> -->

