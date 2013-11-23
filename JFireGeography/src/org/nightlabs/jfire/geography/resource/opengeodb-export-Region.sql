# Region

select distinct
  concat_ws(';',
    concat_ws('', txt_CountryID.text_val),
    concat_ws('',
    	case when txt_RegionID.text_val is null then cast(h.id_lvl3 as char CHARACTER SET utf8) else txt_RegionID.text_val end),
    concat_ws('', txt_RegionName.text_locale),
    concat_ws('', txt_RegionName.text_val))
  as 'CountryID;RegionID;LanguageID;RegionName'
# select txt_RegionID.loc_id, txt_CountryID.text_val CountryID, txt_RegionID.text_val RegionID, txt_RegionName.text_locale LanguageID, txt_RegionName.text_val RegionName
from geodb_hierarchies h
inner join country on country.loc_id = h.id_lvl2
left join geodb_textdata txt_CountryID
  on txt_CountryID.loc_id = h.id_lvl2
  and txt_CountryID.text_type = 500100001
left join geodb_textdata txt_RegionName
  on txt_RegionName.loc_id = h.loc_id
  and txt_RegionName.text_type = 500100000
left join geodb_textdata txt_RegionID
  on txt_RegionID.loc_id = h.loc_id
  and txt_RegionID.text_type = 500100001
where h.level = 3 and h.loc_id = h.id_lvl3
order by txt_CountryID.text_val, h.id_lvl3;


