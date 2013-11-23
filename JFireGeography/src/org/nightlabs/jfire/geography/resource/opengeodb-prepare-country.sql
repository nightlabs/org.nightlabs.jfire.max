# This script will be executed before all the country-dependent export-scripts, once
# for each country.

# The following query ensures that there is at least one district for each city. The fields
# DistrictID, LanguageID, DistrictName are NULL in case there is no real district
# existing. JFireGeography will use the CityID as DistrictID and the CityName (i18n)
# as DistrictName in this case.

drop table if exists districts;

create table districts
select distinct
  case when h_district.id_lvl7 is null then h.id_lvl6 else h_district.id_lvl7 end loc_id,
  txt_CountryID.text_val CountryID,
  h.id_lvl6 CityID,
  h_district.id_lvl7 DistrictID,
  txt_DistrictName.text_locale LanguageID,
  txt_DistrictName.text_val DistrictName
from geodb_hierarchies h
inner join country on country.loc_id = h.id_lvl2
left join geodb_textdata txt_CountryID
  on txt_CountryID.loc_id = h.id_lvl2
  and txt_CountryID.text_type = 500100001
left join geodb_hierarchies h_district
  on h_district.id_lvl6 = h.loc_id
  and h_district.level = 7
  and h_district.loc_id = h_district.id_lvl7
left join geodb_textdata txt_DistrictName
  on txt_DistrictName.loc_id = h_district.id_lvl7
  and txt_DistrictName.text_type = 500100000
left join geodb_locations l using (loc_id)
left join geodb_type_names tn on tn.type_id = l.loc_type
where h.level = 6 and h.loc_id = h.id_lvl6
order by txt_CountryID.text_val, h.id_lvl6, h_district.id_lvl7;

