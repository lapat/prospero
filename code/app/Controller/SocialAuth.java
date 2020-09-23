package com.coinflash.app.Controller;


import com.coinflash.app.Service.FBUserDetailsImpl;
import com.coinflash.app.Service.FBuserDetails;
import com.coinflash.app.coinflashdatabasehelper;
import com.coinflash.app.coinflashhelper;
import com.coinflash.app.constants;
import com.coinflash.app.login3;
import org.json.JSONObject;
import org.springframework.social.facebook.api.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

@WebServlet(urlPatterns = {"/Social/SignInFB"},loadOnStartup = 2,asyncSupported = true)
public class SocialAuth extends HttpServlet {

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SocialAuth() {
        super();
        // TODO Auto-generated constructor stub
    }


    //DataBase _dbInstance = new DataBaseImpl();
    FBuserDetails _fBuserDetails = new FBUserDetailsImpl();
    private static final long serialVersionUID = 1L;
    coinflashdatabasehelper _coinflashdatabasehelperObj = coinflashdatabasehelper.getInstance();
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    public static String getBaseUrl(HttpServletRequest request) {
        URI contextUrl = URI.create(request.getRequestURL().toString()).resolve(request.getContextPath());
        if(contextUrl.getHost().equals("coinflashdev.com")||contextUrl.getHost().equals("coinflashapp.com"))
        return "valid";
        return "";
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        String _uri =request.getRequestURI();
        JSONObject jsonObject = new JSONObject();
        if(getBaseUrl(request).equals("valid")){
            if(_uri.equalsIgnoreCase("/Social/SignInFB")){
                String accessToken = request.getParameter("accessToken");
                String referred_by= request.getParameter("referred_by");
                String mobile_secret = request.getParameter("mobile_secret");

                String [] fetchFields = { "id", "email",  "first_name", "last_name","locale", "name"};
                User userFB = _fBuserDetails.getUserDetails(accessToken, "Coinflash",fetchFields );
                System.out.println("accessToken"+accessToken);

                if(userFB != null) {
                    login3 _login3 = login3.getInstance();
                    String coinflash_user_id = _login3.checkIfUserExist(userFB.getId());
                    HttpSession session = request.getSession();
                    String email_exists_user_id=_coinflashdatabasehelperObj.emailExists(userFB.getEmail());
                    if (email_exists_user_id.length()>0){
                        _login3.updateUserWithFB(userFB.getId(),userFB.getEmail());
                    }
                    String fbEmail=userFB.getEmail();
                    if (fbEmail==null){
                      fbEmail="";
                    }
                    if((coinflash_user_id==null) && (email_exists_user_id.length()==0)) {
                        coinflash_user_id=_login3.registerNewUserFromFB(userFB.getId(), userFB.getName(), fbEmail, referred_by == null ? "" : referred_by);
                        session.setAttribute(constants.USER_ID_SESSION, coinflash_user_id);
                    }
                    else{
                        //coinflash_user_id =email_exists_user_id;
                        session.setAttribute(constants.USER_ID_SESSION,coinflash_user_id);
                    }
                    System.out.println("userFB.getEmail()"+userFB.getEmail());
                    System.out.println("coinflash_user_id="+coinflash_user_id+" email_exists_user_id="+email_exists_user_id);
                    System.out.println("mobile_secret"+mobile_secret);

                    JSONObject user_info = _coinflashdatabasehelperObj.getUserInformation(coinflash_user_id);
                    String onboard_status= coinflashhelper.getJsonStringHelper("onboard_status", user_info);
                    //jsonObject.put("email", fbEmail);
                    //jsonObject.put("id", userFB.getId());
                    //jsonObject.put("name", userFB.getName());
                    jsonObject.put("user_id_mobile",coinflash_user_id);
                    jsonObject.put("status", "logged_in");
                    jsonObject.put("onboard_status", onboard_status);

                    try{
                        if (mobile_secret!=null){
                            if (mobile_secret.equals(constants.MOBILE_SECRET) || mobile_secret.equals(constants.MOBILE_SECRET_ANDROID)){
                                try{
                                    coinflashdatabasehelper coinflashdatabasehelperObj =new coinflashdatabasehelper();
                                    jsonObject.put("mobile_access_token", coinflashdatabasehelperObj.updateWithNewMobileAccessToken(coinflash_user_id));
                                    jsonObject.put("user_id_mobile",coinflash_user_id);
                                }catch(Exception e){
                                    jsonObject.put("exception", "exception updateWithNewMobileAccessToken "+ e);
                                }
                            }else{
                              System.out.println("mobile secret does not match 1");
                            }
                        }else{
                          System.out.println("mobile secret is null");
                        }
                    } catch (Exception e) {
                        jsonObject.put("exception", "exception getting createMobileAccessToken "+ e);
                    }
                } else {
                    //there is no such profile in FB
                    jsonObject.put("status", "logged_out");
                }
                //TODO sign in new user In DB or just enable onBoard and return logged_in or logged_out response to client
            }
        }
        out.print(jsonObject.toString());
        out.close();
    }



    }
