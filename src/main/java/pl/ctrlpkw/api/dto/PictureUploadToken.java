package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

@ApiModel
@Getter
@Setter
@Builder
public class PictureUploadToken {
    
    private int timestamp;
    
    private String publicId;
    
    private String apiKey;
    
    private String signature;
    
}
