package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.UUID;

@ApiModel
@Getter
@Setter
@Builder
public class PictureUploadToken {

    private UUID protocolId;

    private int timestamp;
    
    private UUID publicId;
    
    private String apiKey;
    
    private String signature;
    
}
