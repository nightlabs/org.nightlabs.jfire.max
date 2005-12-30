# City

select distinct
  concat_ws(';',
    concat_ws('', txt_CountryID.text_val),
    concat_ws('', cast(h.id_lvl6 as char CHARACTER SET utf8)),
    concat_ws('',
    	case when txt_RegionID.text_val is null then cast(h.id_lvl3 as char CHARACTER SET utf8) else txt_RegionID.text_val end),
    concat_ws('', txt_CityName.text_locale),
    concat_ws('', txt_CityName.text_val))
  as
  'CountryID;CityID;RegionID;LanguageID;CityName'
#select txt_CountryID.text_val CountryID, h.id_lvl6 CityID, txt_RegionID.text_val RegionID, txt_CityName.text_locale LanguageID, txt_CityName.text_val CityName
from geodb_hierarchies h
inner join country on country.loc_id = h.id_lvl2
left join geodb_textdata txt_CountryID
  on txt_CountryID.loc_id = h.id_lvl2
  and txt_CountryID.text_type = 500100001
left join geodb_textdata txt_RegionID
  on txt_RegionID.loc_id = h.id_lvl3
  and txt_RegionID.text_type = 500100001
left join geodb_textdata txt_CityName
  on txt_CityName.loc_id = h.id_lvl6
  and txt_CityName.text_type = 500100000
where h.level = 6 and h.loc_id = h.id_lvl6
order by txt_CountryID.text_val, h.id_lvl6;

