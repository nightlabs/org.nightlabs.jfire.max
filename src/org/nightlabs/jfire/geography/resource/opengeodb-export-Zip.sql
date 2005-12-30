# Zip

# See opengeodb-prepare.sql

select distinct
  concat_ws(';',
    concat_ws('', d.CountryID),
    concat_ws('', d.CityID),
    concat_ws('', d.DistrictID),
    concat_ws('', txt_Zip.text_val))
  as 'CountryID;CityID;DistrictID;Zip'
#select d.CountryID, d.CityID, d.DistrictID, txt_Zip.text_val Zip
from districts d
left join geodb_textdata txt_Zip
  on txt_Zip.loc_id = d.loc_id
  and txt_Zip.text_type = 500300000;

