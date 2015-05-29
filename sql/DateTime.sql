-- NOW UDF
select now() from system.catalog limit 1;

-- YEAR
select year(now()) from system.catalog limit 1;

-- Day of month.
select dayofmonth(now()) from system.catalog limit 1;

-- Current hour.
select hour(now()) from system.catalog limit 1;

-- List of new date/time UDFs:
NOW
MONTH, WEEK, DAYOFMONTH
HOUR, MINUTE, SECOND
