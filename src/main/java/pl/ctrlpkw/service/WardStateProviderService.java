package pl.ctrlpkw.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.ctrlpkw.model.write.Ward;

@Service
public class WardStateProviderService {



    @CachePut(value="wardProtocolStatus", cacheManager = "redisCacheManager")
    public pl.ctrlpkw.api.dto.Ward.ProtocolStatus setVagueProtocolStatus(String commnityCode, int wardNo) {
        return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.VAGUE;
    }

    @CachePut(value="wardProtocolStatus", cacheManager = "redisCacheManager")
    public pl.ctrlpkw.api.dto.Ward.ProtocolStatus setConfirmedProtocolStatus(String commnityCode, int wardNo) {
        return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.CONFIRMED;
    }

    @Cacheable(value="wardProtocolStatus")
    public pl.ctrlpkw.api.dto.Ward.ProtocolStatus getConfirmedProtocolStatus(String commnityCode, int wardNo) {
        return pl.ctrlpkw.api.dto.Ward.ProtocolStatus.LACK;
    }


}
