# Country

select distinct
  concat_ws(';',
    concat_ws('', txt_CountryID.text_val),
    concat_ws('', txt_CountryName.text_locale),
    concat_ws('', txt_CountryName.text_val))
  as 'CountryID;LanguageID;CountryName'
# select txt_CountryID.text_val CountryID, txt_CountryName.text_locale LanguageID, txt_CountryName.text_val CountryName
from geodb_hierarchies h
left join geodb_textdata txt_CountryName
  on txt_CountryName.loc_id = h.loc_id
  and txt_CountryName.text_type = 500100000
left join geodb_textdata txt_CountryID
  on txt_CountryID.loc_id = h.loc_id
  and txt_CountryID.text_type = 500100001
where h.level = 2 and h.loc_id = h.id_lvl2
order by txt_CountryID.text_val;

