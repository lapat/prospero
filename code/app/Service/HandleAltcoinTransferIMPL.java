package com.coinflash.app.Service;

import com.coinflash.app.Dao.DataBase;
import com.coinflash.app.Dao.DataBaseImpl;
import com.coinflash.app.Log2File;
import com.coinflash.app.coinflashcoinbase;
import com.coinflash.app.constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HandleAltcoinTransferIMPL  implements  HandleAltcoinTransfer{
    DataBase _dbInstance = new DataBaseImpl();

    private static HandleAltcoinTransferIMPL _handleAltcoinTransferIMPL= null;
    private static final String baseUrlCoinBase = "https://api.coinbase.com/v2/accounts/";
    private static final String Transaction = "/transactions";
    private static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json");
    OkHttpClient client = new OkHttpClient();

    Response response = null;

    protected static String getApiUrl(String path) {
        return baseUrlCoinBase + path;
    }


    public static HandleAltcoinTransferIMPL getInstance() {
        if(_handleAltcoinTransferIMPL==null)
            return new HandleAltcoinTransferIMPL();
        return _handleAltcoinTransferIMPL;
    }

    String convertTo8DecimalPlaces(String number){
        if (number.contains(".")){
            String splitStringWithDecimal[] =  number.split("\\.");
            if (splitStringWithDecimal.length>0){
                if (splitStringWithDecimal[1].length()>8){
                    return splitStringWithDecimal[0]+"."+splitStringWithDecimal[1].substring(0, 8);
                }
            }
        }
        return number;
    }

    @Override
    public JSONArray sendAltcoin(JSONArray jsonArray, String users_access_token_coinbase, String users_wallet_id, String user_id, double currencyWallet_price_now, String idemForEmail, String currencyWallet) throws IOException, SQLException, ClassNotFoundException {
        JSONObject requestJson = new JSONObject();
        JSONArray jsonArrayReturn = new JSONArray();
        jsonArray.forEach(o -> {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject Jobject = new JSONObject();
            if(jsonObject.has("deposit")) {
                requestJson.put("type", "send");
                requestJson.put("to", jsonObject.get("deposit").toString());
                NumberFormat df = new DecimalFormat("#.########");
                String amount = df.format(Double.parseDouble(jsonObject.get("withdrawalAmount").toString()));
                requestJson.put("amount", amount);
                requestJson.put("currency", currencyWallet);
                requestJson.put("idem", UUID.randomUUID().toString());
                RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, requestJson.toString());
                Request PostOkRequest = new Request.Builder().url(getApiUrl(users_wallet_id + Transaction))
                        .addHeader("Content-Type", MEDIA_TYPE_JSON.toString())
                        .addHeader("CB-VERSION", "2017-05-24")
                        .addHeader("Authorization", "Bearer " + users_access_token_coinbase)
                        .post(body).build();
                try {

                    response = client.newCall(PostOkRequest).execute();
                    if (response.code() > 200 && response.code() < 300) {
                        JSONObject _tempJsonObject = new JSONObject(response.body().string());
                        JSONObject jsonObjectData = _tempJsonObject.getJSONObject("data");
                        jsonObjectData.getString("id");//b343939b-da4d-544b-a69e-02e39ed791d3
                        double native_amount = Math.abs(Double.parseDouble(jsonObjectData.getJSONObject("native_amount").getString("amount")));//-5.62
                        String native_amount_currency = jsonObjectData.getJSONObject("native_amount").getString("currency");//usd
                        String created_at = jsonObjectData.getString("created_at");//2017-11-18T22:12:22Z
                        //DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
                        //Date startDate = df2.parse(created_at);
                        //String newDateString = df2.format(startDate);
                        Double network_transaction_fee_amount = Math.abs(Double.valueOf(jsonObjectData.getJSONObject("network").getJSONObject("transaction_fee").getString("amount")));//0.00041248
                        //String network_transaction_fee_currency = jsonObjectData.getJSONObject("network").getJSONObject("transaction_fee").getString("currency");//BTC
                        Double network_transaction_amount_amount = Math.abs(Double.valueOf(jsonObjectData.getJSONObject("network").getJSONObject("transaction_amount").getString("amount")));//0.00030712
                        //String network_transaction_amount_currency =jsonObjectData.getJSONObject("network").getJSONObject("transaction_amount").getString("currency");//BTC
                        Jobject.put("success", _dbInstance.saveAltcoinTransaction(user_id, users_wallet_id, network_transaction_amount_amount, jsonObject.getString("pair"), jsonObject.get("deposit").toString(), native_amount, native_amount_currency, created_at, (network_transaction_fee_amount * currencyWallet_price_now), jsonObject.get("withdrawalAddress").toString(), idemForEmail));
                    } else {
                        JSONObject jsonObject1 = new JSONObject(response.body().string());
                        if (jsonObject1 != null) {
                            if (jsonObject1.has("errors")) {
                                JSONArray jsonArray1 = jsonObject1.getJSONArray("errors");
                                if (jsonArray1.length() > 0) {
                                    JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
                                    if (jsonObject2 != null && jsonObject2.has("message")) {
                                        Jobject.put("message", jsonObject2.getString("message"));
                                    }
                                }
                            }
                        }

                        Jobject.put("failed", jsonObject.get("pair").toString().substring(4));
                    }

                    jsonArrayReturn.put(Jobject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        return jsonArrayReturn;
    }

    boolean successBuy = false;

    /**
     Description: make a batch buy request to coinbase api*/
    @Override
    public JSONArray BatchBuy(JSONArray json, String user_id, String users_wallet_id, double currencyWallet_price_now, boolean ConsumedFirstBuy, String currencyWallet) throws IOException, SQLException, ClassNotFoundException {
        successBuy = false;
        Object obj = JSONValue.parse(String.valueOf(json));
        org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) obj;
        JSONArray jsonArray1 = new JSONArray();
               coinflashcoinbase coinflashcoinbaseObj=new coinflashcoinbase();
        String users_access_token_coinbase=coinflashcoinbaseObj.refreshIfNeededCoinbase(user_id);
        JSONArray usersWallets=coinflashcoinbaseObj.coinBaseListWalletsHelper2(users_access_token_coinbase);
        String idemForEmail = UUID.randomUUID().toString();
                if (usersWallets.length()>0){
                    try {
                        JSONArray finalJsonArray = jsonArray1;
                        jsonArray.stream().forEachOrdered(o -> {
                                org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) o;
                                finalJsonArray.put(new JSONObject(jsonObject.toJSONString()));
                            });
                        //this line will send the amount to the wallet address i.e altcoin address
                        jsonArray1 =sendAltcoin(finalJsonArray, users_access_token_coinbase,users_wallet_id,user_id,currencyWallet_price_now,idemForEmail, currencyWallet);

                        jsonArray1.forEach(o -> {
                            org.json.JSONObject jsonObject = (org.json.JSONObject) o;
                            if(jsonObject.has("success")) successBuy = true;
                        });

                    } catch (SQLException|ClassNotFoundException e) {
                    }
                    if(successBuy&&ConsumedFirstBuy){
                        chargeUserOneDollarforBatchBuy(Integer.parseInt(user_id), users_access_token_coinbase, users_wallet_id);
                    }
                    else if(successBuy&&!ConsumedFirstBuy){
                        _dbInstance.consumeFirstBuy(Integer.parseInt(user_id));
                    }
                }


        return jsonArray1;
    }

    /**
     Description: charge the user 1$ for batch buy*/
    @Override
    public void chargeUserOneDollarforBatchBuy(int user_id, String users_access_token_coinbase, String users_wallet_id) throws IOException {
        JSONObject requestJson = new JSONObject();
        requestJson.put("type", "send");
        requestJson.put("to", constants.PAYMENT_EMAIL);
        requestJson.put("amount", "1.00");
        requestJson.put("currency","USD");
        requestJson.put("idem", UUID.randomUUID().toString());
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, requestJson.toString());
        Request PostOkRequest = new Request.Builder().url(getApiUrl(users_wallet_id+Transaction))
                .addHeader("Content-Type", MEDIA_TYPE_JSON.toString())
                .addHeader("CB-VERSION", "2017-05-24")
                .addHeader("Authorization", "Bearer "+users_access_token_coinbase)
                .post(body).build();
        response = client.newCall(PostOkRequest).execute();
        if(response.code()>=200){
            Log2File.log("User with id "+user_id+" paid 1$ to coinflash for batch buy at date "+ LocalDateTime.now());

        }else if(response.code()>=400){

            Log2File.log("User with id "+user_id+" have issue for payment 1$ to coinflash for batch buy at date "+ LocalDateTime.now());
        }


    }
}
