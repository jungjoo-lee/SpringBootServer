package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Integer> {

    List<Follow> findByFfrom(String nickname);
    // [ {ffrom:'hong1', fto:'abcd'}, {ffrom:'hong1', fto:'cdef'}, ... ]
    List<Follow> findByFto(String nickname);
    // [ {ffrom:'abcd', fto:'hong1'}, {ffrom:'cdef', fto:'hong1'}, ... ]

    Optional<Follow> findByFfromAndFto(String ffrom, String fto);

}
