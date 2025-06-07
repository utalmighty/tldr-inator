package inc.huduk.tldr_inator.models;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModelRequest {

    String model;
    String prompt;
}
