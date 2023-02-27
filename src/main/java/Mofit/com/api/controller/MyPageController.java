package Mofit.com.api.controller;

import Mofit.com.Domain.Member;
import Mofit.com.api.request.MyPageReq;
import Mofit.com.api.request.SignReq;
import Mofit.com.api.response.SignRes;
import Mofit.com.api.service.SignService;
import Mofit.com.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/mofit")
@RequiredArgsConstructor
public class MyPageController {
    private final MemberRepository memberRepository;

    private final SignService memberService;


    @GetMapping("/user/{account}")
    public String myPage(@PathVariable String account) {

        return "ok";
    }
    @PostMapping("/user/{account}")
    public ResponseEntity<String> changeMyPage(@PathVariable String account, @RequestBody MyPageReq sign) throws Exception {    // requestBody 객
        Optional<Member> uId = memberRepository.findByAccount(account);
        if (sign.getPassword().isEmpty()){
            return new ResponseEntity<>("Empty Password", HttpStatus.BAD_REQUEST);
        }
        if (uId.isPresent()){
            boolean update = memberService.update(account, sign);
            if (update){
                return new ResponseEntity<>("OK",HttpStatus.OK);
            }
            return new ResponseEntity<>("Update failed",HttpStatus.NOT_IMPLEMENTED);
        }
        return new ResponseEntity<>("Server Error",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/ranking")
    public String rankPage() {
        return "ok";
    }




}