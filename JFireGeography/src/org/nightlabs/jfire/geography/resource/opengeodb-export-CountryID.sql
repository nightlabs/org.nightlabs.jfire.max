# This query is used to export only the CountryIDs in order to generate
# one CSV per country. Its data is only saved temporarily.

select distinct
  concat_ws(' ', txt_CountryID.text_val, h.loc_id)
  as 'CountryID Country_loc_id'
from geodb_hierarchies h
inner join geodb_textdata txt_CountryID
  on txt_CountryID.loc_id = h.loc_id
  and txt_CountryID.text_type = 500100001
where h.level = 2 and h.loc_id = h.id_lvl2
order by txt_CountryID.text_val;
