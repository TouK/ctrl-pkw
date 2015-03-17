package pl.ctrlpkw;

import com.vividsolutions.jts.geom.Geometry;
import geodb.GeoDB;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.hibernate.type.StandardBasicTypes;

public class ExtGeoDBDialect extends GeoDBDialect {

    private static final double EARTH_RADIUS = 6370986;

    public ExtGeoDBDialect() {
        super();
        registerFunction("distance_sphere", new StandardSQLFunction("ST_Distance_Sphere",
                StandardBasicTypes.DOUBLE));
    }

    public static double distanceSphere( byte[] wkb1, byte[] wkb2 ) {
        if ( wkb1 == null || wkb2 == null ) {
            return -1;
        }
        Geometry g1 = GeoDB.gFromWKB(wkb1);
        Geometry g2 = GeoDB.gFromWKB(wkb2);
        return getHaversineDistance(g1, g2);
    }

    private static double getHaversineDistance(Geometry g1, Geometry g2)
    {
        double lat1 = Math.toRadians(g1.getCoordinate().y);
        double lng1 = Math.toRadians(g1.getCoordinate().x);
        double lat2 = Math.toRadians(g2.getCoordinate().y);
        double lng2 = Math.toRadians(g2.getCoordinate().x);

        double dlon = lng2 - lng1;
        double dlat = lat2 - lat1;

        double a = Math.pow((Math.sin(dlat/2)),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return EARTH_RADIUS * c;
    }


}
