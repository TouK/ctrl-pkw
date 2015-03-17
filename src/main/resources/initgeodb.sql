CREATE ALIAS  if not exists InitGeoDB for "geodb.GeoDB.InitGeoDB";
CALL InitGeoDB();
CREATE ALIAS if not EXISTS ST_Distance_Sphere FOR "pl.ctrlpkw.ExtGeoDBDialect.distanceSphere";
