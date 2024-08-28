package com.himedia.spserver.controller;

import com.himedia.spserver.entity.Images;
import com.himedia.spserver.entity.Likes;
import com.himedia.spserver.entity.Post;
import com.himedia.spserver.entity.Reply;
import com.himedia.spserver.service.PostService;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    PostService ps;

    @GetMapping("/getPostList")
    public HashMap<String, Object> getPostList( @RequestParam(value="word", required = false) String word){
        HashMap<String, Object> result = new HashMap<>();
        result.put( "postlist" , ps.getPostList( word ) );
        return result;
    }

    @GetMapping("/getImages/{postid}")
    public List<Images> getImages(@PathVariable("postid") int postid){
        List<Images> list = ps.getImages(postid);
        return list;
    }

    @GetMapping("/getLikes/{postid}")
    public List<Likes> getLikes(@PathVariable("postid") int postid){
        List<Likes> list = ps.getLikes( postid );
        return list;
    }

    @GetMapping("/getReplys/{postid}")
    public List<Reply> getReplys(@PathVariable("postid") int postid){
        List<Reply> list = ps.getReplys( postid );
        return list;
    }


    @PostMapping("/addlike")
    public HashMap<String, Object> addLike(@RequestParam("postid") int postid, @RequestParam("likenick") String likenick){
        HashMap<String, Object> result = new HashMap<>();
        ps.addLike( postid, likenick);
        return null;
    }


    @PostMapping("/addReply")
    public HashMap<String, Object> addlike(@RequestBody Reply reply){
        ps.insertReply( reply );
        return null;
    }
    @DeleteMapping("/deleteReply/{id}")
    public HashMap<String, Object> deleteReply( @PathVariable("id") int id ){
        ps.deleteReply( id );
        return null;
    }


    @Autowired
    ServletContext context;
    @PostMapping("/imgup")
    public HashMap<String, Object> fileup(
            @RequestParam("image") MultipartFile file ){
        HashMap<String, Object> result = new HashMap<String, Object>();
        String path = context.getRealPath("/uploads");
        Calendar today = Calendar.getInstance();
        long dt = today.getTimeInMillis();
        String filename = file.getOriginalFilename();
        String fn1 = filename.substring(0, filename.indexOf(".") );
        String fn2 = filename.substring(filename.indexOf(".") );
        String uploadPath = path + "/" + fn1 + dt + fn2;
        try {
            file.transferTo( new File(uploadPath) );
            result.put("savefilename", fn1 + dt + fn2);
        } catch (IllegalStateException | IOException e) {e.printStackTrace();}
        return result;
    }


    @PostMapping("/writePost")
    public HashMap<String, Object> writePost(@RequestBody Post post){
        HashMap<String, Object> result = new HashMap<String, Object>();
        Post p = ps.insertPost(post);  // 방금 추가된 레코드의 id 를위해 추가된 레코드를 리턴
        result.put("postid", p.getId() );
        return result;
    }

    @PostMapping("/writeImages")
    public HashMap<String, Object> writeimages( @RequestBody Images images){
        HashMap<String, Object> result = new HashMap<String, Object>();
        ps.insertImages(images);
        return result;
    }

    @GetMapping("/getMyPost")
    public HashMap<String, Object> getMyPost( @RequestParam("nickname") String nickname){
        HashMap<String, Object> result = new HashMap<>();
        List<Post> list = ps.getPostListByNickname(nickname);
        List<String> imglist = new ArrayList<String>();
        for( Post p : list) {
            List<Images> imgl = ps.getImgListByPoistid( p.getId() );
            String imgname = imgl.get(0).getSavefilename();
            imglist.add( imgname );
        }
        result.put("postList", list);
        result.put("imgList", imglist);
        return result;
    }

    @GetMapping("/getPost/{id}")
    public Post getPost( @PathVariable("id") int id){
        return ps.getPost(id);
    }

}
