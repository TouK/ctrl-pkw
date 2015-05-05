package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.UUID;

@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PictureUploadToken {

    private int timestamp;
    
    private UUID publicId;
    
    private String apiKey;
    
    private String signature;
    
}
