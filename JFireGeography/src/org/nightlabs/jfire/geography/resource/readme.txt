******************************************************
*** Source
******************************************************

  http://opengeodb.sourceforge.net


******************************************************
*** Generate JFireGeography CSV files
******************************************************

  The file opengeodb-0.1.3-sql.tar.gz cannot be used directly, but must be
  transformed into a valid import format. This can be done with a mysql server
  and the script "convert.sh" which is located in the same directory as this
  file:

  1. Make the script "convert.sh" executable.

  2. Execute the following command:

       ./convert.sh {host} {user} [{password}]

     example:

       ./convert.sh localhost myuser

     If you omit the password, it will be prompted. If you don't have a password
     defined, just leave it empty and hit enter. The script will automatically
     create a database, load the sql from opengeodb and dump the needed csv
     files. Afterwards, the temporary database is dropped. If the database does
     already exist before, the script doesn't do anything to avoid loss of data.


******************************************************
*** Format of JFireGeography CSV files
******************************************************

All CSVs have the same format: First line is table header and ignored. If other
lines shall be ignored, they can be commented with a "#" at the beginning of the
line. Empty lines are ignored, too.


******************************************************
*** Format of opengeodb 0.2.3c
******************************************************

Level:
	1: Continent (Erdteil)
	2: Country (Land)
	3: Region (Bundesland, Kanton)
	4: Administrative Region (Regierungsbezirk)
	5: Rural District (Landkreis)
	6: City (Stadt, Ortschaft)
	7: District (Stadtteil)
	8: Sub-district ???

sql:
------
select * from geodb_hierarchies h
left join geodb_textdata t on h.loc_id = t.loc_id and t.text_type = 500100000
left join geodb_locations l using (loc_id)
left join geodb_type_names tn on tn.type_id = l.loc_type
where h.level = 4
------

******************

text_type:
	500100000: Name
	500300000: Zip


sql:
------
select * from geodb_type_names
------

******************