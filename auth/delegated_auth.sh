create_namespace ‘foo’
grant ’dba-bar', 'RWXCA', '@foo’
create ’foo:t1', 'f1’
grant ‘user-x’, ‘RWXCA’, ‘foo:t1’
