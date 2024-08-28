package com.himedia.spserver.controller;

import com.google.gson.Gson;
import com.himedia.spserver.dto.KakaoProfile;
import com.himedia.spserver.dto.OAuthToken;
import com.himedia.spserver.entity.Follow;
import com.himedia.spserver.entity.Member;
import com.himedia.spserver.service.MemberService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberService ms;

    @PostMapping("/loginlocal")
    public HashMap<String, Object> loginLocal(@RequestBody Member member, HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        Member mem = ms.getMember( member.getEmail() );
        if( mem == null){
            result.put("msg", "이메일 또는 패스워드를 확인하세요");
        }else if( !mem.getPwd().equals( member.getPwd() ) ) {
            result.put("msg", "이메일 또는 패스워드를 확인하세요");
        }else{
            result.put("msg", "ok");
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", mem);
        }
        return result;
    }

    @GetMapping("/getLoginUser")
    public HashMap<String, Object> getLoginUser( HttpServletRequest request ) {
        HttpSession session = request.getSession();
        Member loginUser = (Member) session.getAttribute("loginUser");
        HashMap<String, Object> result = new HashMap<>();
        result.put("loginUser", loginUser);

        List<Follow> followinglist  = ms.getFollowings( loginUser.getNickname() );
        List<Follow> followerlist  = ms.getFollowers( loginUser.getNickname() );
        result.put("followings", followinglist);
        result.put("followers", followerlist);

        return result;

    }

    @Value("${kakao.client_id}")
    private String client_id;
    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @RequestMapping("/kakaostart")
    public @ResponseBody String kakaostart() {
        String a = "<script type='text/javascript'>"
                + "location.href='https://kauth.kakao.com/oauth/authorize?"
                + "client_id=" + client_id + "&"
                + "redirect_uri=" + redirect_uri + "&"
                + "response_type=code';" + "</script>";
        return a;
    }

    @RequestMapping("/kakaoLogin")
    public void loginKakao( HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String endpoint = "https://kauth.kakao.com/oauth/token";
        URL url = new URL(endpoint);
        String bodyData = "grant_type=authorization_code&";
        bodyData += "client_id=f67ebc2de23039bbce25c7d2583abd81&";
        bodyData += "redirect_uri=http://localhost:8070/member/kakaoLogin&";
        bodyData += "code=" + code;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        conn.setDoOutput(true);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
        bw.write(bodyData);
        bw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String input = "";
        StringBuilder sb = new StringBuilder();
        while ((input = br.readLine()) != null) {
            sb.append(input);
        }
        Gson gson = new Gson();
        OAuthToken oAuthToken = gson.fromJson(sb.toString(), OAuthToken.class);
        String endpoint2 = "https://kapi.kakao.com/v2/user/me";
        URL url2 = new URL(endpoint2);

        HttpsURLConnection conn2 = (HttpsURLConnection) url2.openConnection();
        conn2.setRequestProperty("Authorization", "Bearer " + oAuthToken.getAccess_token());
        conn2.setDoOutput(true);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(conn2.getInputStream(), "UTF-8"));
        String input2 = "";
        StringBuilder sb2 = new StringBuilder();
        while ((input2 = br2.readLine()) != null) {
            sb2.append(input2);
            System.out.println(input2);
        }
        Gson gson2 = new Gson();
        KakaoProfile kakaoProfile = gson2.fromJson(sb2.toString(), KakaoProfile.class);
        KakaoProfile.KakaoAccount ac = kakaoProfile.getAccount();
        KakaoProfile.KakaoAccount.Profile pf = ac.getProfile();
        System.out.println("id : " + kakaoProfile.getId());
        System.out.println("KakaoAccount-Email : " + ac.getEmail());
        System.out.println("Profile-Nickname : " + pf.getNickname());

        Member member = ms.getMemberBySnsid( kakaoProfile.getId() );
        if( member == null) {
            member = new Member();
            //member.setEmail( pf.getNickname() );
            member.setEmail( ac.getEmail() );  // 전송된 이메일이 없으면 pf.getNickname()
            member.setNickname( pf.getNickname() );
            member.setProvider( "kakao" );
            member.setPwd( "kakao" );
            member.setSnsid( kakaoProfile.getId() );
            ms.insertMember(member);
        }
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", member);
        response.sendRedirect("http://localhost:3000/kakaosaveinfo");
    }


    @GetMapping("/logout")
    public HashMap<String, Object> logout(HttpServletRequest request){
        HashMap<String, Object> result = new HashMap<String, Object>();
        HttpSession session = request.getSession();
        session.removeAttribute("loginUser");
        return null;
    }

    @Autowired
    ServletContext context;
    @PostMapping("/fileupload")
    public HashMap<String, Object> fileup( @RequestParam("image") MultipartFile file){

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
            result.put("filename", fn1 + dt + fn2);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/emailcheck")
    public HashMap<String, Object> emailcheck( @RequestParam("email") String email ){
        HashMap<String, Object> result = new HashMap<String, Object>();
        Member mem = ms.getMember( email );
        if( mem != null ) result.put("msg", "no");
        else result.put("msg", "yes");
        return result;
    }

    @PostMapping("/nicknamecheck")
    public HashMap<String, Object> nicknamecheck( @RequestParam("nickname") String nickname){
        HashMap<String, Object> result = new HashMap<String, Object>();
        Member mem = ms.getMemberByNickname( nickname );
        if( mem != null ) result.put("msg", "no");
        else result.put("msg", "yes");
        return result;
    }

    @PostMapping("/join")
    public HashMap<String, Object> join( @RequestBody Member member){
        HashMap<String, Object> result = new HashMap<String, Object>();
        ms.insertMember(member);
        result.put("msg", "ok");
        return result;
    }

    @PostMapping("/follow")
    public HashMap<String, Object> follow( @RequestParam("ffrom") String ffrom, @RequestParam("fto") String fto){
        ms.onFollow( ffrom, fto );
        return null;
    }

    @PostMapping("/unfollow")
    public HashMap<String, Object> unfollow( @RequestParam("ffrom") String ffrom, @RequestParam("fto") String fto){
        ms.onUnFollow( ffrom, fto );
        return null;
    }

    @GetMapping("/getFollowings")
    public List<Follow> getFollowings( HttpServletRequest request ){
        HttpSession session = request.getSession();
        Member member = (Member) session.getAttribute("loginUser");
        List<Follow> list = ms.getFollowings( member.getNickname() );
        return list;
    }


    @PostMapping("/updateProfile")
    public HashMap<String, Object> updateProfile(@RequestBody Member member, HttpServletRequest request){

        HashMap<String, Object> result = new HashMap<String, Object>();

        ms.updateProfile( member );
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", member );

        result.put("msg", "ok");
        return result;
    }


    @GetMapping("/getMemberInfo/{membernick}")
    public HashMap<String, Object> getMemeberInfo( @PathVariable("membernick") String membernick){
        HashMap<String, Object> result = new HashMap<>();
        Member member = ms.getMemberByNickname(membernick);
        result.put("cuser", member);

        List<Follow> followinglist  = ms.getFollowings( membernick );
        List<Follow> followerlist  = ms.getFollowers( membernick );
        result.put("followings", followinglist);
        result.put("followers", followerlist);

        return result;
    }

}
