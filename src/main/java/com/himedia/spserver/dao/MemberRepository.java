package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findBySnsid(String id);
    Optional<Member> findByNickname(String nickname);
}
