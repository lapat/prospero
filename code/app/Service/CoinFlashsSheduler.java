package com.coinflash.app.Service;

import com.coinflash.app.Dao.DataBase;
import com.coinflash.app.Dao.DataBaseImpl;
import com.coinflash.app.Pojo.Schedule;
import com.coinflash.app.coinflashhelper;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

@WebServlet(urlPatterns = {"/getBuys","/addScheduler","/RemoveScheduler","/UpdateScheduler"},loadOnStartup = 1,asyncSupported = true)
public class CoinFlashsSheduler extends HttpServlet {
    DataBase _dbInstance = new DataBaseImpl();
    private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CoinFlashsSheduler() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String _uri =request.getRequestURI();
        Set<Schedule> schedules = new LinkedHashSet<>();
        PrintWriter out = response.getWriter();
        if(_uri.equalsIgnoreCase("/getBuys")){
            String user_id = coinflashhelper.checkSession((HttpServletRequest)request, (HttpServletResponse)response, (PrintWriter)out);
            if(user_id!=null &&user_id.length()>0){
                try {
                    schedules = _dbInstance.getUserScheduleDetails(Integer.parseInt(user_id));
                } catch (ClassNotFoundException |SQLException e) {
                }
            }
        }
        JSONArray myArray = new JSONArray();
        schedules.stream().forEachOrdered(schedule -> {
            Gson gson = new Gson();
            JSONObject _json = new JSONObject(gson.toJson(schedule));
            myArray.put(_json);
        });
        out.print(myArray.toString());
        out.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String _uri =request.getRequestURI();
        PrintWriter out = response.getWriter();
        String user_id = coinflashhelper.checkSession((HttpServletRequest)request, (HttpServletResponse)response, (PrintWriter)out);
        boolean success = false;
        if(_uri.equalsIgnoreCase("/addScheduler")) {
            double BTC_amount= Double.parseDouble(request.getParameter("BTC_amount"));
            double ETH_amount = Double.parseDouble(request.getParameter("ETH_amount"));
            int chooseSchedule = Integer.parseInt(request.getParameter("chooseSchedule"));
            if(user_id!=null &&user_id.length()>0) {
                try {
                    success = _dbInstance.addOneSchedule(Integer.parseInt(user_id), BTC_amount, ETH_amount, chooseSchedule);
                    if (success) {
                        HandleBuySchedule handleBuySchedule = HandleBuySchedule.getInstance();
                        handleBuySchedule.addScheduleBuy(user_id,BTC_amount,ETH_amount,chooseSchedule );
                    }
                } catch (ClassNotFoundException | SQLException e) {
                }
            }
            out.print(success);
        }
        else if (_uri.equalsIgnoreCase("/RemoveScheduler")){
            int scheduler_id = Integer.parseInt(request.getParameter("scheduler_id"));
            if(user_id!=null &&user_id.length()>0) {
                try {
                    success = _dbInstance.removeOneSchedule(Integer.parseInt(user_id),scheduler_id);
                    if (success) {
                        HandleBuySchedule handleBuySchedule = HandleBuySchedule.getInstance();
                        handleBuySchedule.removeScheduleBuy(user_id,scheduler_id);
                    }
                } catch (ClassNotFoundException | SQLException e) {
                }
            }
            out.print(success);
        }
        //TODO FOR LATER USE.
        else if (_uri.equalsIgnoreCase("/UpdateScheduler")){
            double BTC_amount= Double.parseDouble(request.getParameter("BTC_amount"));
            double ETH_amount = Double.parseDouble(request.getParameter("ETH_amount"));
            int chooseSchedule = Integer.parseInt(request.getParameter("chooseSchedule"));
            int Schedule_id = Integer.parseInt(request.getParameter("Schedule_id"));
            if(user_id!=null &&user_id.length()>0) {
                try {
                    success = _dbInstance.updateScheduleBuy(Integer.parseInt(user_id), BTC_amount, ETH_amount, chooseSchedule,Schedule_id);
                    if (success) {
                        HandleBuySchedule handleBuySchedule = HandleBuySchedule.getInstance();
                        handleBuySchedule.updateScheduleBuy(user_id,BTC_amount,ETH_amount,chooseSchedule ,Schedule_id);
                    }
                } catch (ClassNotFoundException | SQLException e) {
                }
            }
            out.print(success);
        }

        out.close();
    }

}