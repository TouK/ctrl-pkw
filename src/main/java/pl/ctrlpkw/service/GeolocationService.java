package pl.ctrlpkw.service;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.util.concurrent.AtomicDouble;
import com.vividsolutions.jts.geom.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.ctrlpkw.model.read.Voting;
import pl.ctrlpkw.model.read.Ward;
import pl.ctrlpkw.model.read.WardRepository;

import javax.annotation.Resource;
import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class GeolocationService {

    @Resource
    private WardRepository wardRepository;

    public Stream<Ward> getAllWardsWithinRadiusAndTopUpWithClosestIfAtLeastMinCountNotFound(
            Voting voting, Point location, double radius, short minCount, short maxCount
    ) {
        long before = System.nanoTime();
        try {
            Collection<Ward> wardsWithinRadius = wardRepository.findWithinRadiusAndOrderByDistance(
                    voting, location, radius
            );
            if (minCount <= wardsWithinRadius.size()) {
                return StreamSupport.stream(wardsWithinRadius.spliterator(), false).limit(maxCount);
            } else {
                return getAtLeastMinCountWards(voting, location, minCount, maxCount);
            }
        } finally {
            long after = System.nanoTime();
            log.debug("all geodb queries time: {}", (after - before) / 1000.0);
        }
    }

    protected Stream<Ward> getAtLeastMinCountWards(Voting voting, Point location, short minCount, short maxCount) {
        long before = System.nanoTime();
        try {
            Page<Object[]> closestMinCountWards = wardRepository.findOrderedByDistanceReturnWithDistance(
                    voting, location, new PageRequest(0, maxCount)
            );
            if (closestMinCountWards.getSize() == 0) {
                throw new NotFoundException();
            }
            AtomicDouble lastDistance = new AtomicDouble(0);
            AtomicInteger counter = new AtomicInteger(0);
            return StreamUtils.takeWhile(StreamSupport.stream(closestMinCountWards.spliterator(), false),
                    wardAndDistance -> {
                        double distance = (Double) wardAndDistance[1];
                        boolean sameDistance = lastDistance.getAndSet(distance) == distance;
                        boolean withinCountLimit = counter.getAndIncrement() < minCount;
                        return sameDistance || withinCountLimit;
                    }
            ).map(wardAndDistance -> (Ward) wardAndDistance[0]);
        } finally {
            long after = System.nanoTime();
            log.debug("second geodb query time: {}", (after - before) / 1000.0);
        }
    }
}
