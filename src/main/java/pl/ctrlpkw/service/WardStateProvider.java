package pl.ctrlpkw.service;

import org.joda.time.LocalDate;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WardStateProvider {

    @CachePut(
            value="localResults", cacheManager = "redisCacheManager",
            key = "#votingDate.toString() + ':' + #communityCode + ':' + #wardNo")
    public pl.ctrlpkw.api.dto.Ward.ProtocolStatus save(
            LocalDate votingDate, String communityCode, Integer wardNo, pl.ctrlpkw.api.dto.Ward.ProtocolStatus status) {
        return status;
    }

    @Cacheable(
            value="localResults", cacheManager = "redisCacheManager",
            key = "#votingDate.toString() + ':' + #communityCode + ':' + #wardNo")
    public pl.ctrlpkw.api.dto.Ward.ProtocolStatus read(
            LocalDate votingDate, String communityCode, Integer wardNo) {
        return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK;
    }

}
