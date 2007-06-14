
<!-- direct connection allowing DDL -->

	<property name="hibernate.dialect">org.hibernate.dialect.MySQLInnoDBDialect</property>
<!--   <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property> -->
    <property name="hibernate.connection.driver_class">${databaseDriverName}</property>
    <property name="hibernate.connection.url">${databaseURL}</property>
    <property name="hibernate.connection.username">${databaseUserName}</property>
    <property name="hibernate.connection.password">${databasePassword}</property>
