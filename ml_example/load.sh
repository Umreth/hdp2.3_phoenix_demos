#!/bin/sh

/usr/hdp/current/phoenix-client/bin/sqlline.py localhost:2181:/hbase-unsecure CreateTables.sql

/usr/hdp/current/phoenix-client/bin/psql.py -t MOVIES  -d $'\t' localhost:2181:/hbase-unsecure movies.csv
/usr/hdp/current/phoenix-client/bin/psql.py -t RATINGS -d $'\t' localhost:2181:/hbase-unsecure ratings.csv
