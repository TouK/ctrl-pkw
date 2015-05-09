package pl.ctrlpkw.api.dto;

import com.wordnik.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Builder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public String toString() {
        return Arrays.asList(timestamp, publicId, apiKey, signature).stream()
                .map(i -> Optional.ofNullable(i).map(o -> o.toString()).orElse("")).collect(Collectors.joining(","));
    }

}
