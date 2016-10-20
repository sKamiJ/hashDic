use hashDic

/*
sp_helpdb hashDic

dbcc log(9,3)
*/

/*
drop table store_unit
*/
create table store_unit(
text varchar(20) constraint PK_store_unit_text primary key,
md5 binary(16))

/*
exec sp_help store_unit
*/

select top 1000 * from store_unit

select count(*) from store_unit

delete from store_unit

insert into store_unit
	values('a',0x0);

select *
from store_unit as unit1 join store_unit as unit2 on (unit1.md5 = unit2.md5)
where unit1.text <> unit2.text

select * from store_unit where text = ''
