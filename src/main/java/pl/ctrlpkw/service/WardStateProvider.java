package pl.ctrlpkw.service;

import org.joda.time.LocalDate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pl.ctrlpkw.api.dto.BallotResult;

import javax.annotation.Resource;

@Service
public class WardStateProvider {

    @Resource
    RedisTemplate redisTemplate;


    @CachePut(
            value="localResults", cacheManager = "redisCacheManager",
            key = "#votingDate.toString() + ':' + #ballotNo + ':' + #communityCode + ':' + #wardNo")
    public BallotResult save(
            LocalDate votingDate, Integer ballotNo, String communityCode, Integer wardNo, BallotResult localResult) {
        return localResult;
    }

    @Cacheable(
            value="localResults", cacheManager = "redisCacheManager",
            key = "#votingDate.toString() + ':' + #ballotNo + ':' + #communityCode + ':' + #wardNo")
    public BallotResult read(
            LocalDate votingDate, Integer ballotNo, String communityCode, Integer wardNo) {
        return null;
    }

}
