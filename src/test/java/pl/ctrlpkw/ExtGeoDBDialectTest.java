package pl.ctrlpkw;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import geodb.GeoDB;
import junit.framework.TestCase;

public class ExtGeoDBDialectTest extends TestCase {

    public void testDistanceSphere() throws Exception {

        //given
        GeometryFactory geometryFactory = new GeometryFactory();
        Point p1 = geometryFactory.createPoint(new Coordinate(20.9589744, 52.2124388));
        Point p2 = geometryFactory.createPoint(new Coordinate(21.045976, 52.239574));

        //when
        double distance = ExtGeoDBDialect.distanceSphere(GeoDB.gToWKB(p1), GeoDB.gToWKB(p2));

        //then
        assertEquals(6600, distance, 6600 * 0.01);
    }
}