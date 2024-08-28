package com.himedia.spserver.dao;

import com.himedia.spserver.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("select p from Post p where p.id IN(select ph.postid from Posthash ph where ph.hashid=:hashid) order by p.id desc")
    List<Post> getPostListByTag( @Param("hashid") int hashid  );

    List<Post> findByWriterOrderByIdDesc(String nickname);
}
