package com.himedia.spserver.dao;


import com.himedia.spserver.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplysRepository  extends JpaRepository<Reply, Integer> {

    List<Reply> findByPostidOrderByIdDesc(int postid);
}
