use hashDic

/*
sp_helpdb hashDic

dbcc log(9,3)
*/

/*
drop table store_unit
*/
create table store_unit_0(
text varchar(20) primary key,
md5 binary(16) not null)

/*
exec sp_help store_unit
*/

select top 1000 * from store_unit_0

select count(*) from store_unit_0

select count(*) from store_unit_1

select count(*) from store_unit_2

select count(*) from store_unit_3

select count(*) from store_unit_4

declare @sum int
declare @table varchar(20)
declare @sql varchar(max)
declare @i int
set @i = 0
set @sum = 0
set @table = 'store_unit_0'
set @sql = 'select count(*) from ' + @table
exec(@sql)



delete from store_unit_0

drop table store_unit_8

insert into store_unit_0
	values('a',0x0);

select *
from store_unit_0 as unit1 join store_unit_0 as unit2 on (unit1.md5 = unit2.md5)
where unit1.text <> unit2.text

select * from store_unit_0 where text = ''
