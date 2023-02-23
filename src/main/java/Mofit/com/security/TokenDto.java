package Mofit.com.security;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
    private String access_token;
    private String refresh_token;
}