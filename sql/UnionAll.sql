-- Our dataset.
select * from movies where title like '%1995%';
select * from movies where title like '%1996%';

-- Basic UNION ALL.
select * from movies where title like '%1995%' union all select * from movies where title like '%1996%';

-- UNION ALL used in a derived table.
select count(*) from (
    select movieid from movies where title like '%1995%' union all select movieid from movies where title like '%1996%'
);

-- UNION ALL two derived tables.
select count(movieid) from (
    select movieid from movies where title like '%1995%' union all select movieid from movies where title like '%1996%'
) union all
select count(movieid) from (
    select movieid from movies where title like '%1991%' union all select movieid from movies where title like '%1992%'
);
