#!/bin/sh

# BEGIN CONFIGURATION
# Change the following variables in case the files are named differently:

# What is the pack format? "zip" or "tar.gz"
PACKER=zip
# What is the name of the tar.gz-archive?
#OPENGEODBTGZ=opengeodb-0.1.3-sql.tar.gz
OPENGEODBTGZ=opengeodb-0.2.3c-UTF8-mysql.zip
# How is the file within the tar.gz named?
#OPENGEODBSQL=opengeodb-0.1.3.sql
OPENGEODBSQL=opengeodb-0.2.3c-UTF8-mysql.sql

# The name of the temporary database which will be created and dropped.
DB="TMP_OPEN_GEO_DB"

# END CONFIGURATION


SYNTAX="./convert.sh {host} {user} [{password}]"

DIR=`dirname $0`

HOST=$1
USER=$2
PW=$3

if [ "" == "$HOST" ]; then
  echo "Param {host} is missing! Syntax: $SYNTAX"
  exit 501;
fi

if [ "" == "$USER" ]; then
  echo "Param {user} is missing! Syntax: $SYNTAX"
  exit 502;
fi

if [ "" == "$PW" ]; then
  echo -n "MySQL password for user $USER: "
  read
  PW=$REPLY
fi

TMPDIR=$DIR/tmp

mkdir $TMPDIR

if [ "tar.gz" == "$PACKER" ]; then
  tar -xvzf $DIR/$OPENGEODBTGZ -C $TMPDIR
elif [ "zip" == "$PACKER" ]; then
  unzip $DIR/$OPENGEODBTGZ -d $TMPDIR
fi

if [ "" == "$PW" ]; then
  PWPARAM=""
else
  PWPARAM="-p$PW"
fi

function query()
{
    SCRIPT=$1
    OUT=$2
    mysql -u $USER $DB $PWPARAM < $SCRIPT > $OUT
}

function setcountry()
{
    COUNTRY_ID=$1
    LOC_ID=$2

    if [ "$COUNTRY_ID" == "" ]; then
	return 1
    fi

    if [ "$LOC_ID" == "" ]; then
	return 2
    fi

    mysql -u $USER $DB $PWPARAM -e "UPDATE country SET CountryID = '$COUNTRY_ID', loc_id = '$LOC_ID'" || exit 1
}

function geodbexport()
{
    WHAT=$1
    COUNTRY_ID=$2
    LOC_ID=$3

    setcountry "$COUNTRY_ID" "$LOC_ID"

    if [ "" == "$COUNTRY_ID" ]; then
	SEP=""
    else
	SEP="-"
    fi
    FN=Data-${WHAT}${SEP}${COUNTRY_ID}.csv
    echo "create CSV file $FN..."
    query $DIR/opengeodb-export-$WHAT.sql $DIR/$FN
#    mysql -u $USER $DB $PWPARAM < $DIR/opengeodb-export-$WHAT.sql > $DIR/Data-$WHAT.csv
}

# If the database already exists, we exit to avoid data destruction.
echo "create database $DB..."
mysql -u $USER $PWPARAM -e "CREATE DATABASE $DB;" || exit 1;

echo "import opengeodb data..."
mysql -u $USER $DB $PWPARAM < $TMPDIR/$OPENGEODBSQL

echo "opengeodb-prepare-global.sql..."
mysql -u $USER $DB $PWPARAM < $DIR/opengeodb-prepare-global.sql

echo "create table country..."
mysql -u $USER $DB $PWPARAM -e "DROP TABLE IF EXISTS country"
mysql -u $USER $DB $PWPARAM -e "CREATE TABLE country (CountryID VARCHAR(5) NOT NULL, loc_id INT NOT NULL)" || exit 1
mysql -u $USER $DB $PWPARAM -e "INSERT INTO country VALUES (\"CountryID\", 0)" || exit 1

COUNTRY_ID_FILE=$TMPDIR/CountryIDs
query $DIR/opengeodb-export-CountryID.sql $COUNTRY_ID_FILE

echo "create table country..."
geodbexport "Country"

cat $COUNTRY_ID_FILE | while read COUNTRY_ID LOC_ID; do
    if [ "CountryID" != "$COUNTRY_ID" ]; then
        echo "prepare country $COUNTRY_ID..."
        setcountry "$COUNTRY_ID" "$LOC_ID"
	mysql -u $USER $DB $PWPARAM < $DIR/opengeodb-prepare-country.sql

        geodbexport "Region" "$COUNTRY_ID" "$LOC_ID"
        geodbexport "City" "$COUNTRY_ID" "$LOC_ID"
        geodbexport "District" "$COUNTRY_ID" "$LOC_ID"
        geodbexport "Zip" "$COUNTRY_ID" "$LOC_ID"
    fi
done

# cleanup
mysql -u $USER $PWPARAM -e "DROP DATABASE $DB;"
rm -r $TMPDIR

