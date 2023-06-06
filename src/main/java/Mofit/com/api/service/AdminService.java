package Mofit.com.api.service;


import Mofit.com.Domain.Authority;
import Mofit.com.Domain.Member;
import Mofit.com.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository repository;

    private void CheckAdmin(Member member){
        List<Authority> roles = member.getRoles();
    }

}
