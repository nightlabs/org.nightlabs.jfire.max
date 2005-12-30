# District

# See opengeodb-prepare.sql

select distinct
  concat_ws(';', concat_ws('', d.CountryID), concat_ws('', d.CityID), concat_ws('', d.DistrictID), concat_ws('', d.LanguageID), concat_ws('', d.DistrictName), concat_ws('', c.lat), concat_ws('', c.lon))
  as 'CountryID;CityID;DistrictID;LanguageID;DistrictName;Latitute;Longitude'
from districts d
left join geodb_coordinates c on c.loc_id = d.loc_id and c.coord_type = 200100000;

