package Mofit.com.api.controller;

import Mofit.com.api.request.SignReq;
import Mofit.com.api.response.SignRes;
import Mofit.com.api.service.SignService;
import Mofit.com.repository.MemberRepository;
import Mofit.com.api.request.TokenReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mofit")
public class SignController {

    private final MemberRepository memberRepository;
    private final SignService memberService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public SignRes login(@RequestBody SignReq request) throws Exception {
        return memberService.login(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/register")
    public Boolean signup(@RequestBody SignReq request) throws Exception {
        String name = request.getAccount();
        for (int i = 0; i < 100000;i++) {
            request.setAccount(name+i);
            memberService.ee(request);
        }


        request.setAccount(request.getAccount() + "이므아니");
        return memberService.register(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/get")
    public SignRes getUser(@RequestParam String account) throws Exception {
        return memberService.getMember(account);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/admin/get")
    public SignRes getUserForAdmin(@RequestParam String account) throws Exception {
        return memberService.getMember(account);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/refresh")
    public TokenReq refresh(@RequestBody TokenReq token) throws Exception {

        if (token.getRefresh_token().isEmpty()){
            return token;
        }
        return memberService.refreshAccessToken(token);
    }
}