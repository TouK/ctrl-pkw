package pl.ctrlpkw.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.ctrlpkw.model.write.Protocol;
import pl.ctrlpkw.model.write.Ward;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WardProtocols {
    private Ward ward;
    private List<Protocol> protocols;
}
