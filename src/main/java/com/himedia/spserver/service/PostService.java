package com.himedia.spserver.service;

import com.himedia.spserver.dao.*;
import com.himedia.spserver.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PostService {

    @Autowired
    PostRepository pr;

    @Autowired
    HashtagRepository hr;

    public List<Post> getPostList(String word) {
        // return pr.findAll( Sort.by(Sort.Direction.DESC, "id") );
        // pr.findAll( Sort.by(Sort.Direction.DESC, "id", "writer" ) );
        // pr.findAll( Sort.by(Sort.Order.desc("id"), Sort.Order.asc("writer") ) );

        // word로 hashtag 테이블 검색
        // 검색결과에 있는 tagid 들로  posthash테이블에서 postid 들을 검색
        // postid 들로 post 테이블에서  post 들을 검색
        // select id from hashtag where word=?
        // select postid from posthash where hashid=?
        // select * from post where id=?
        List<Post> posts = null;

        if( word == null ){
            posts = pr.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }else {
            Optional<Hashtag> h = hr.findByWord(word);
            if (!h.isPresent()) {  // 검색어가 hashtag 테이블에 없는 단어라면  모두 검색
                posts = pr.findAll(Sort.by(Sort.Direction.DESC, "id"));
            } else { // 있는 단어라면   hasgtag 테이블의 단어 id 로 검색
                posts = pr.getPostListByTag(h.get().getId());
            }
        }
        return posts;
    }

    @Autowired
    ImagesRepository ir;
    @Autowired
    LikesRepostory lr;

    @Autowired
    ReplysRepository rr;

    public List<Images> getImages(int postid) {
        return ir.findByPostid(postid);
    }

    public List<Likes> getLikes(int postid) {
        return lr.findByPostid( postid );
    }

    public List<Reply> getReplys(int postid) {
        return rr.findByPostidOrderByIdDesc( postid );
    }

    public void addLike(int postid, String likenick) {
        Optional<Likes> rec = lr.findByPostidAndLikenick( postid, likenick );
        if( !rec.isPresent() ){
            Likes likes = new Likes();
            likes.setPostid( postid );
            likes.setLikenick( likenick );
            lr.save( likes );
        }

    }

    public void insertReply(Reply reply) {
        rr.save( reply );
    }

    public void deleteReply(int id) {
        Optional<Reply> rec = rr.findById(id);
        if( rec.isPresent() ) {
            rr.delete(rec.get());
        }
    }



    @Autowired
    PosthashRepository phr;

    public Post insertPost(Post post) {

        // 포스트 추가
        Post p = pr.save( post );
        int postid= p.getId();

        // 포스트의 content 추출
        String content = post.getContent();

        // content 에서 해시태그들만 추출
        Matcher m = Pattern.compile("#([0-9a-zA-Z가-힣]*)").matcher(content);
        List<String> tags = new ArrayList<String>();
        while (m.find()) {
            //System.out.println(m.group(1));
            tags.add(m.group(1));
        }
        //System.out.println(tags);

        // 추출된 해시테그들로 해시테그 작업
        int tagid = 0;
        for( String word : tags ){
            Optional<Hashtag> rec = hr.findByWord(word);
            // 현재 워드가 없으면  hashtag 테이블에 새레코드 추가하고 아이디 추출
            if( !rec.isPresent() ){
                Hashtag hdnew = new Hashtag();
                hdnew.setWord(word);
                Hashtag hdsave = hr.save(hdnew);
                tagid = hdsave.getId();
            }else{
                tagid = rec.get().getId();
                // 있으면 아이디만 추출
            }
            Posthash ph = new Posthash();
            ph.setPostid( postid );
            ph.setHashid( tagid );
            // 추출된 포스트 아이디와 테그 아이디로 posthash 테이블에 레코드 추가
            phr.save(ph);
        }
        return p;
    }

    public void insertImages(Images images) {
        ir.save(images);
    }

    public List<Post> getPostListByNickname(String nickname) {
        return pr.findByWriterOrderByIdDesc( nickname );
    }

    public List<Images> getImgListByPoistid(int id) {
        return ir.findByPostid( id );
    }


    public Post getPost(int id) {
        Optional<Post> p = pr.findById(id);
        if( p.isPresent() ){
            return p.get();
        }else{
            return null;
        }
    }
}

// Distinct	: findDistinctByName("scott")   중복제거 검색
// And : findByNameAndGender("scott", "gender")   이름과 성별 동시 만족
// Or : findByNameOrGender("scott", "gender")  이름이 만족하거나 성별이 만족
// Is, Equals : findByName("scott"), findByNameIs("scott"), findByNameEquals("scott")   값이 같음
// Between	: findByStartDateBetween( 1, 10)   1과 10의 사이 값들 검색
// LessThan	: findByAgeLessThan(10)   10보다 작은(<)
// LessThanEqual :	findByAgeLessThanEqual(10)   10보다 작거나 같은 (<=)
// GreaterThan	: findByAgeGreaterThan(10)   10보다 큰(>)
// GreaterThanEqual : findByAgeGreaterThanEqual(10)   10보다 크거나 같은(>=)
// After : findByStartDateAfter(날짜)    날짜 이후
// Before : findByStartDateBefore(날짜)   날짜 이전
// Like : findByNameLike("scott")  이름을 포함하는
// StartingWith	: findByNameStartingWith("scott")    이름으로 시작하는
// StartingWith	: findByNameEndingWith("scott")    이름으로 끝나는

// Containing : findByNameContaining("scott")   이름을 포함하는 (Like 와 유사)
// 평소에 사용하던  where name like '%철수%' 는 Containing 을 사용합니다
// Like 를 사용하면   where name like '철수' 와 같이 동작하므로 결과가 없을수도 잇습니다

// OrderBy : findByAgeOrderByIdDesc()  id필드기준으로 내림차순 정렬
// In :	findByAgeIn(Collection<Age> ages)   In함수 사용
// true : findByActiveTrue()    active 필드값이  true
// False : findByActiveFalse()    active 필드값이  false
// IgnoreCase	findByNameIgnoreCase("scott")  이름 검색하되 대소문자 구분하지 않음