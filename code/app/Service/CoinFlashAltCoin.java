package com.coinflash.app.Service;


import com.coinflash.app.Dao.DataBase;
import com.coinflash.app.Dao.DataBaseImpl;
import com.coinflash.app.Pojo.Connection;
import com.coinflash.app.coinflashhelper;
import com.coinflash.app.constants;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/checkIdemForEmail","/checkUserAcceptanceLimit" ,"/checkFirstBuy","/limitForOneWeek","/getGainLoss","/status","/convertAltcoin","/buyaltcoin","/multicoin","/addAltcoin","/getAltCoins","/removeAltcoin","/updatePercentage"},loadOnStartup = 1,asyncSupported = true)
public class CoinFlashAltCoin extends HttpServlet {
    private static final String GET_COINS_API = "getcoins";
    private static final String Rate_Pair = "rate/btc_ltc";
    JSONArray myArray =null;

    private OkHttpClient client = new OkHttpClient();
    private static final String baseUrl  = "https://shapeshift.io/";
    private String publicKey = constants.shapeShiftPublicKey;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public CoinFlashAltCoin() {
        super();
        // TODO Auto-generated constructor stub
    }
    //todo
/*
    Double Sum_network_transaction_amount_amount_LTC =0.0;
    Double Sum_network_transaction_fee_amount_LTC=0.0;
    int Sum_native_amount_LTC_worth_usd =0
            ;
    Double Sum_network_transaction_amount_amount_ZEC =0.0;
    Double Sum_network_transaction_fee_amount_ZEC=0.0;
    int Sum_native_amount_ZEC_worth_usd =0;

    Double Sum_network_transaction_amount_amount_DASH =0.0;
    Double Sum_network_transaction_fee_amount_DASH=0.0;
    int Sum_native_amount_DASH_worth_usd =0;

    Double Sum_network_transaction_amount_amount_DCR =0.0;
    Double Sum_network_transaction_fee_amount_DCR=0.0;
    int Sum_native_amount_DCR_worth_usd =0;*/


    DataBase _dbInstance = new DataBaseImpl();
    ShapeShiftConnection _shapeShiftConnection =ShapeShiftConnection.getInstance();
    HandleAltcoinTransferIMPL handleAltcoinTransferIMPL =HandleAltcoinTransferIMPL.getInstance();
    private static final long serialVersionUID = 1L;

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }



    protected String getApiUrl(String path) {
        return baseUrl + path;
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String _uri =request.getRequestURI();
        myArray = new JSONArray();
        PrintWriter out = response.getWriter();
        String user_id = coinflashhelper.checkSession((HttpServletRequest)request, (HttpServletResponse)response, (PrintWriter)out);
        if((_uri.equalsIgnoreCase("/multicoin")||_uri.equalsIgnoreCase("/convertAltcoin"))&& user_id!=null &&user_id.length()>0){
            this.getServletContext().getRequestDispatcher("/multicoin.html").forward(request, response);
        }
        else if((_uri.equalsIgnoreCase("/getAltCoins"))&& user_id!=null &&user_id.length()>0){
            try {
                myArray =_dbInstance.getAltcoins(Integer.parseInt(user_id));
            } catch (ClassNotFoundException|SQLException e) {
            }
        }
        else if((_uri.equalsIgnoreCase("/limitForOneWeek"))&& user_id!=null &&user_id.length()>0) {
           coinBaseConnection _coinBaseConnection = coinBaseConnectionIMPL.getInstance();
           double totalSpentOneWeek =0.0;
           try {
               totalSpentOneWeek = _coinBaseConnection.spent(user_id);

           } catch (SQLException |ClassNotFoundException e) {
               e.printStackTrace();
           }
           out.print(totalSpentOneWeek);
           out.close();


       }
        else if((_uri.equalsIgnoreCase("/checkFirstBuy"))&& user_id!=null &&user_id.length()>0) {
            boolean ConsumedFirstBuy = false ;
            try {
                ConsumedFirstBuy = _dbInstance.hasConsumedFirstBuy(user_id);
            } catch (SQLException |ClassNotFoundException e) {
                e.printStackTrace();
            }
            out.print(ConsumedFirstBuy);
            out.close();


        }
        /*else if((_uri.equalsIgnoreCase("/checkUserAcceptanceLimit"))&& user_id!=null &&user_id.length()>0) {
            String authorization = "" ;
            try {
                authorization = _dbInstance.checkAuthorization(user_id);
            } catch (SQLException |ClassNotFoundException e) {
                System.out.println("failed with msg "+e.getMessage());
            }
            out.print(authorization);
            out.close();
        }*/


        //TODO complete email
        else if((_uri.equalsIgnoreCase("/checkIdemForEmail"))&& user_id!=null &&user_id.length()>0) {
            List<JSONObject> objects = new ArrayList<>();
            try {
               _dbInstance.getAltcoinsTransaction(Integer.parseInt(user_id)).forEach(o -> {
                   JSONObject jsonObject = (JSONObject) o;
                   objects.add(jsonObject);
               });
            } catch (SQLException |ClassNotFoundException e) {
                    e.printStackTrace();
            }
            Function<JSONObject, String> jsonObjectStringFunction = jsonArrayStringFunctionOne ->jsonArrayStringFunctionOne.getString("idemForEmail");
            Map<String, List<JSONObject>> stringListMap = objects.stream()
                    .collect(Collectors.groupingBy(jsonObjectStringFunction));

            stringListMap.entrySet().stream().forEachOrdered(stringListEntry -> {
                List<JSONObject> jsonObjectsSameIdem = stringListEntry.getValue();

                if(jsonObjectsSameIdem.size() ==jsonObjectsSameIdem.stream().filter(distinctByKey(p -> ((JSONObject) p).getString("status"))).filter(jsonObject -> jsonObject.getString("status").equalsIgnoreCase("complete")).collect(Collectors.toList()).size()){
                    //only here send email, u have the jsonObjectsSameIdem which means one batch buym this jsonObjectsSameIdem contains
                    //all information u need to send to the user.
                }
            });

        }

        else if((_uri.equalsIgnoreCase("/getGainLoss")&& user_id!=null &&user_id.length()>0)){
            //todo
            JSONObject jsonObjectalt = new JSONObject();
            try {
                             Set<String> ltcAddresses = new LinkedHashSet<>();
                             Set<String> zecAddresses = new LinkedHashSet<>();
                             Set<String> dashAddresses = new LinkedHashSet<>();
                                Set<String> dcrAddresses = new LinkedHashSet<>();
                Set<String> xmrAddresses = new LinkedHashSet<>();
                Set<String> xrpAddresses = new LinkedHashSet<>();
                Set<String> dogeAddresses = new LinkedHashSet<>();
                Set<String> bchAddresses = new LinkedHashSet<>();
                Set<String> etcAddresses = new LinkedHashSet<>();
                Set<String> vtcAddresses = new LinkedHashSet<>();
                Set<String> steemAddresses = new LinkedHashSet<>();
                Set<String> dgbAddresses = new LinkedHashSet<>();
                myArray = _dbInstance.getAltcoinsTransaction(Integer.parseInt(user_id));
                myArray.forEach(o -> {
                    JSONObject jsonObject1 = (JSONObject) o;

                    if(jsonObject1.getString("coinsymbol").endsWith("_XRP")){
                        xrpAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_BCH")){
                        bchAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_STEEM")){
                        steemAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_DGB")){
                        dgbAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_VTC")){
                        vtcAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_ETC")){
                        etcAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_DOGE")){
                        dogeAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_XMR")){
                        xmrAddresses.add(jsonObject1.getString("withdrawalAddress"));
                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_LTC")){
                      ltcAddresses.add(jsonObject1.getString("withdrawalAddress"));
                      //todo
                        //in btc
                        /*Sum_network_transaction_amount_amount_LTC = Sum_network_transaction_amount_amount_LTC+jsonObject1.getDouble("network_transaction_amount_amount");
                        Sum_network_transaction_fee_amount_LTC =Sum_network_transaction_fee_amount_LTC+jsonObject1.getDouble("network_transaction_fee_amount");
                        //in usd
                        Sum_native_amount_LTC_worth_usd =Sum_native_amount_LTC_worth_usd+jsonObject1.getInt("native_amount");*/

                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_DASH")){
                      dashAddresses.add(jsonObject1.getString("withdrawalAddress"));
                      //System.out.println("dash addresses is "+dashAddresses.size());

                      /*  Sum_network_transaction_amount_amount_DASH = Sum_network_transaction_amount_amount_DASH+jsonObject1.getDouble("network_transaction_amount_amount");
                        Sum_network_transaction_fee_amount_DASH =Sum_network_transaction_fee_amount_DASH+jsonObject1.getDouble("network_transaction_fee_amount");
                        Sum_native_amount_DASH_worth_usd=Sum_native_amount_DASH_worth_usd+jsonObject1.getInt("native_amount");*/

                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_DCR")){
                      dcrAddresses.add(jsonObject1.getString("withdrawalAddress"));

                      /*  Sum_network_transaction_amount_amount_DCR= Sum_network_transaction_amount_amount_DCR+jsonObject1.getDouble("network_transaction_amount_amount");
                        Sum_network_transaction_fee_amount_DCR =Sum_network_transaction_fee_amount_DCR+jsonObject1.getDouble("network_transaction_fee_amount");
                        Sum_native_amount_DCR_worth_usd=Sum_native_amount_DCR_worth_usd+jsonObject1.getInt("native_amount");*/

                    }
                    else if(jsonObject1.getString("coinsymbol").endsWith("_ZEC")){
                      zecAddresses.add(jsonObject1.getString("withdrawalAddress"));

                     /*   Sum_network_transaction_amount_amount_ZEC = Sum_network_transaction_amount_amount_ZEC+jsonObject1.getDouble("network_transaction_amount_amount");
                        Sum_network_transaction_fee_amount_ZEC =Sum_network_transaction_fee_amount_ZEC+jsonObject1.getDouble("network_transaction_fee_amount");
                        Sum_native_amount_ZEC_worth_usd=Sum_native_amount_ZEC_worth_usd+jsonObject1.getInt("native_amount");*/
                    }
                });

				if(ltcAddresses.size()>0)
                    jsonObjectalt.put("ltc",ltcAddresses);
				if(zecAddresses.size()>0)
                    jsonObjectalt.put("zec",zecAddresses);
				if(dashAddresses.size()>0)
                    jsonObjectalt.put("dash",dashAddresses);
				if(dcrAddresses.size()>0)
                    jsonObjectalt.put("dcr",dcrAddresses);
                if(xmrAddresses.size()>0)
                    jsonObjectalt.put("xmr",xmrAddresses);
                if(xrpAddresses.size()>0)
                    jsonObjectalt.put("xrp",xrpAddresses);
                if(dogeAddresses.size()>0)
                    jsonObjectalt.put("doge",dogeAddresses);
                if(bchAddresses.size()>0)
                    jsonObjectalt.put("bch",bchAddresses);
                if(etcAddresses.size()>0)
                    jsonObjectalt.put("etc",etcAddresses);
                if(vtcAddresses.size()>0)
                    jsonObjectalt.put("vtc",vtcAddresses);
                if(steemAddresses.size()>0)
                    jsonObjectalt.put("steem",steemAddresses);
                if(dgbAddresses.size()>0)
                    jsonObjectalt.put("dgb",dgbAddresses);

                /*jsonObjectalt.put("Sum_network_transaction_amount_amount_LTC",Sum_network_transaction_amount_amount_LTC);
                jsonObjectalt.put("Sum_network_transaction_fee_amount_LTC",Sum_network_transaction_fee_amount_LTC);
                jsonObjectalt.put("Sum_native_amount_LTC_worth_usd",Sum_native_amount_LTC_worth_usd);
                jsonObjectalt.put("Sum_network_transaction_amount_amount_DASH",Sum_network_transaction_amount_amount_DASH);
                jsonObjectalt.put("Sum_network_transaction_fee_amount_DASH",Sum_network_transaction_fee_amount_DASH);
                jsonObjectalt.put("Sum_native_amount_DASH_worth_usd",Sum_native_amount_DASH_worth_usd);
                jsonObjectalt.put("Sum_network_transaction_amount_amount_ZEC",Sum_network_transaction_amount_amount_ZEC);
                jsonObjectalt.put("Sum_network_transaction_fee_amount_ZEC",Sum_network_transaction_fee_amount_ZEC);
                jsonObjectalt.put("Sum_native_amount_ZEC_worth_usd",Sum_native_amount_ZEC_worth_usd);
                jsonObjectalt.put("Sum_network_transaction_amount_amount_DCR",Sum_network_transaction_amount_amount_DCR);
                jsonObjectalt.put("Sum_network_transaction_fee_amount_DCR",Sum_network_transaction_fee_amount_DCR);
                jsonObjectalt.put("Sum_native_amount_DCR_worth_usd",Sum_native_amount_DCR_worth_usd);*/

                //myArray.put(jsonObjectalt);

            } catch (Exception e) {
              e.printStackTrace();
            }
            out.print(jsonObjectalt.toString());
            out.close();
        }
    }

    double amount_to_invest =0.0;
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String _uri = request.getRequestURI();
        PrintWriter out = response.getWriter();
        String user_id = coinflashhelper.checkSession((HttpServletRequest) request, (HttpServletResponse) response, (PrintWriter) out);

        if(_uri.equalsIgnoreCase("/convertAltcoin")&& user_id!=null &&user_id.length()>0) {

            myArray = new JSONArray();
            String data = request.getParameter("json");
            final boolean[] socketTimeOutError = {false};
            Object obj = JSONValue.parse(data);
            org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;
            JSONArray returnArray = new JSONArray();
            boolean ConsumedFirstBuy =false;
            JSONArray finalReturnArray = returnArray;
            final String[] wallet_address = {""};
            final String[] wallet_currency = {""};
            if (user_id != null && user_id.length() > 0)
                if (jsonObject != null) {

                      amount_to_invest = Double.parseDouble(jsonObject.get("amount_to_invest").toString());
                    wallet_address[0] = jsonObject.get("wallet_address").toString();
                    wallet_currency[0] = jsonObject.get("wallet_currency").toString();
                    String consumedFirstBuy = jsonObject.get("ConsumedFirstBuy").toString();
                    if(consumedFirstBuy.equalsIgnoreCase("true"))
                        ConsumedFirstBuy = true;
                    jsonObject.keySet().stream().forEachOrdered(o ->
                    {
                        if (o.toString().equalsIgnoreCase("Z_cach_coin")) {
                            double ZCach_percent = Double.parseDouble(jsonObject.get("ZCach_percent").toString());
                            String Z_cach_coin = jsonObject.get("Z_cach_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_ZEC").returnAddress(wallet_address[0]).withdrawalAdress(Z_cach_coin)
                                        .amount(amount_to_invest).build(), ZCach_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                  socketTimeOutError[0] = true;
                               // e.printStackTrace();
                            }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("bitcoincash_coin")) {
                            double bitcoincash_percent= Double.parseDouble(jsonObject.get("bitcoincash_percent").toString());
                            String bitcoincash_coin = jsonObject.get("bitcoincash_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_BCH").returnAddress(wallet_address[0]).withdrawalAdress(bitcoincash_coin)
                                        .amount(amount_to_invest).build(), bitcoincash_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("vert_coin")) {
                            double vert_percent= Double.parseDouble(jsonObject.get("vert_percent").toString());
                            String vert_coin = jsonObject.get("vert_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_VTC").returnAddress(wallet_address[0]).withdrawalAdress(vert_coin)
                                        .amount(amount_to_invest).build(), vert_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }

                        if (o.toString().equalsIgnoreCase("digibyte_coin")) {
                            double digibyte_percent= Double.parseDouble(jsonObject.get("digibyte_percent").toString());
                            String digibyte_coin = jsonObject.get("digibyte_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_DGB").returnAddress(wallet_address[0]).withdrawalAdress(digibyte_coin)
                                        .amount(amount_to_invest).build(), digibyte_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("steem_coin")) {
                            double steem_percent= Double.parseDouble(jsonObject.get("steem_percent").toString());
                            String steem_coin = jsonObject.get("steem_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_STEEM").returnAddress(wallet_address[0]).withdrawalAdress(steem_coin)
                                        .amount(amount_to_invest).build(), steem_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("etherclassic_coin")) {
                            double etherclassic_percent= Double.parseDouble(jsonObject.get("etherclassic_percent").toString());
                            String etherclassic_coin = jsonObject.get("etherclassic_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_ETC").returnAddress(wallet_address[0]).withdrawalAdress(etherclassic_coin)
                                        .amount(amount_to_invest).build(), etherclassic_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }

                        if (o.toString().equalsIgnoreCase("monero_coin")) {
                            double monero_percent= Double.parseDouble(jsonObject.get("monero_percent").toString());
                            String monero_coin = jsonObject.get("monero_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_XMR").returnAddress(wallet_address[0]).withdrawalAdress(monero_coin)
                                        .amount(amount_to_invest).build(), monero_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }

                        if (o.toString().equalsIgnoreCase("doge_coin")) {
                            double doge_percent= Double.parseDouble(jsonObject.get("doge_percent").toString());
                            String doge_coin = jsonObject.get("doge_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_DOGE").returnAddress(wallet_address[0]).withdrawalAdress(doge_coin)
                                        .amount(amount_to_invest).build(), doge_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("ripple_coin")) {
                            double ripple_percent= Double.parseDouble(jsonObject.get("ripple_percent").toString());
                            String ripple_coin = jsonObject.get("ripple_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_XRP").returnAddress(wallet_address[0]).withdrawalAdress(ripple_coin)
                                        .amount(amount_to_invest).build(), ripple_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                    // e.printStackTrace();
                                }

                            }
                        }
                        if (o.toString().equalsIgnoreCase("lite_coin")) {
                            double lite_coin_percent = Double.parseDouble(jsonObject.get("lite_coin_percent").toString());
                            String lite_coin = jsonObject.get("lite_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_LTC").returnAddress(wallet_address[0]).withdrawalAdress(lite_coin)
                                        .amount(amount_to_invest).build(), lite_coin_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                }
                               // e.printStackTrace();
                            }
                        }
                        if (o.toString().equalsIgnoreCase("dash_coin")) {
                            double dash_coin_percent = Double.parseDouble(jsonObject.get("dash_coin_percent").toString());
                            String dash_coin = jsonObject.get("dash_coin").toString();
                            try {

                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_DASH").returnAddress(wallet_address[0]).withdrawalAdress(dash_coin)
                                        .amount(amount_to_invest).build(), dash_coin_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                }
                               // e.printStackTrace();
                            }
                        }

                        if (o.toString().equalsIgnoreCase("decred_coin")) {
                            double decred_coin_percent = Double.parseDouble(jsonObject.get("decred_coin_percent").toString());
                            String decred_coin = jsonObject.get("decred_coin").toString();
                            try {
                                finalReturnArray.put(_shapeShiftConnection.Shift(Connection.builder().ApiKey(publicKey).pair(wallet_currency[0]+"_DCR").returnAddress(wallet_address[0]).withdrawalAdress(decred_coin)
                                        .amount(amount_to_invest).build(), decred_coin_percent, user_id));
                            } catch (Exception e) {
                                if(e.getCause() instanceof SocketException){
                                    socketTimeOutError[0] = true;
                                }
                               // e.printStackTrace();
                            }
                        }
                    });
                }


                if(!socketTimeOutError[0]){
                    String user_wallet_id = jsonObject.get("user_wallet_id").toString();
                    NumberFormat df = new DecimalFormat("#.########");
                    double currencyWallet_price_now = Double.parseDouble(df.format(Double.parseDouble(jsonObject.get("currencyWallet_price_now").toString())));

                    try {
                        returnArray =handleAltcoinTransferIMPL.BatchBuy(finalReturnArray, user_id, user_wallet_id, currencyWallet_price_now, ConsumedFirstBuy, wallet_currency[0]);
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    out.print(returnArray.toString());

                }
                else {
                    out.print("ShapeShiftError");
                }
            out.close();
        }


        else if((_uri.equalsIgnoreCase("/status"))&& user_id!=null &&user_id.length()>0){
            myArray = new JSONArray();
            String data=request.getParameter("json");
            if(data!=null) {
                Object obj = JSONValue.parse(data);
                org.json.simple.JSONArray array = (org.json.simple.JSONArray) obj;
                if(array!=null){
                if (array.size() > 0)
                    array.stream().forEachOrdered(o -> {
                        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) o;
                        myArray.put(jsonObject);
                    });
                if (myArray.length() > 0)
                    try {
                        myArray = _shapeShiftConnection.status(myArray);
                        out.print(myArray.toString());
                        out.close();
                    } catch (SQLException | ClassNotFoundException e) {
                    }
                  }
                  else {
                      try {
                          myArray = _shapeShiftConnection.status(user_id);
                          out.print(myArray.toString());
                          out.close();
                      } catch (SQLException | ClassNotFoundException e) {
                      }
                  }
            }
        }
    }



    }
