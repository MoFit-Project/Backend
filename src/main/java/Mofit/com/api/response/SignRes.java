package Mofit.com.api.response;

import Mofit.com.Domain.Authority;
import Mofit.com.Domain.Member;
import Mofit.com.Domain.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignRes {

    private Long id;

    private String account;

    private String email;

    private List<Authority> roles;

    private TokenDto token;

    public SignRes(Member member) {
        this.id = member.getId();
        this.account = member.getAccount();
        this.roles = member.getRoles();
    }
}