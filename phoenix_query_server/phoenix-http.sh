#!/bin/sh

# Client.
/usr/hdp/2.3.0.0-1938/phoenix/bin/sqlline-thin.py localhost

# Via HTTP.
# NOTE! Evolving interface! No promise of backward compatbility.
curl -XPOST -H 'request: {"request":"prepareAndExecute","connectionId":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","sql":"select count(*) from PRICES","maxRowCount":-1}' http://localhost:8765/
