SET PAGESIZE 100
SET LINESIZE 200
SELECT username, account_status FROM dba_users WHERE username IN ('SYSTEM', 'SYS', 'MYBUDG');
EXIT;
