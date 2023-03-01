package Mofit.com.api.request;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenReq {
    private String access_token;
    private String refresh_token;
}