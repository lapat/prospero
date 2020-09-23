package com.coinflash.app.Service;

import com.coinflash.app.Dao.DataBase;
import com.coinflash.app.Dao.DataBaseImpl;
import com.coinflash.app.Pojo.Connection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ShapeShiftConnection {

    DataBase _dbInstance = new DataBaseImpl();
    private static ShapeShiftConnection _shapeShiftconnection = null;
    private static final String baseUrl = "https://shapeshift.io/";
    private static final String blockchainbaseUrl = "https://blockchain.info/tobtc?currency=USD&value=";
    private static final String VallidateAddress = "validateAddress/";
    private static final String shift = "shift";
    private static final String TxStat = "txStat/";

    JSONObject _tempJsonObject =null;
    private static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json");

    public static ShapeShiftConnection getInstance() {
        if(_shapeShiftconnection==null)
            return new ShapeShiftConnection();
        return _shapeShiftconnection;
    }

    OkHttpClient client = new OkHttpClient();
    JsonObject jsonObject =null;
    JsonParser parser =null;
    Gson g =null;
    Response response = null;
    protected static String getApiUrl(String path) {
        return baseUrl + path;
    }


    /*public boolean isValidAddresses(String address, String coinSymbol) {
        Request GetOkRequest = new Request.Builder().url(getApiUrl(VallidateAddress+address+"/"+coinSymbol)).build();
        try {
            response = client.newCall(GetOkRequest).execute();
            try{
                parser = new JsonParser();
                jsonObject =parser.parse(response.body().string()).getAsJsonObject();
                String isValid ="";
                if(jsonObject!=null)
                isValid = jsonObject.get("isvalid").getAsString();
                if(isValid!=null&&isValid.equalsIgnoreCase("true")){
                    return  true;
                }
            }
            catch(JsonSyntaxException jse){
                return false;
            }
        } catch (IOException e) {
        }
        return false;
    }*/

    //TODO
    public JSONObject Shift(Connection connection, double altcoin_percentage_amount, String user_id) throws Exception {

        JSONObject requestJson = new JSONObject();
        requestJson.put("withdrawal", connection.getWithdrawalAdress());
        requestJson.put("pair", connection.getPair());
        requestJson.put("returnAddress", connection.getReturnAddress());
        requestJson.put("apiKey", connection.getApiKey());
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, requestJson.toString());

        Request PostOkRequest = new Request.Builder().url(getApiUrl(shift)).post(body).build();


            response = client.newCall(PostOkRequest).execute();
            String jsonData = response.body().string();
            JSONObject successjson = new JSONObject(jsonData);
            if(successjson!=null) {
                String depositAddress = successjson.getString("deposit");
                if (depositAddress != null && depositAddress.length() > 0) {
                    _tempJsonObject = new JSONObject();
                    _tempJsonObject.put("deposit", depositAddress);
                    _tempJsonObject.put("withdrawalAmount", ((connection.getAmount()*altcoin_percentage_amount)/100));
                    _tempJsonObject.put("pair", connection.getPair());
                    _tempJsonObject.put("txtstatus", "No Deposit");
                    _tempJsonObject.put("withdrawalAddress", connection.getWithdrawalAdress());//the altcoin address that user put manually at first time.

                    _tempJsonObject.put("id", _dbInstance.addAltcoin(user_id, connection.getPair(), connection.getWithdrawalAdress(),
                            String.valueOf((connection.getAmount()*altcoin_percentage_amount)/100), successjson.getString("deposit"),
                            connection.getReturnAddress()));
                    return _tempJsonObject;
                }
                else {_tempJsonObject = null;}
            }
            else {_tempJsonObject = null;}

        return _tempJsonObject;
    }

    public JSONArray status(JSONArray jsonArray) throws SQLException, ClassNotFoundException {
        JSONArray returnArray = new JSONArray();
        final AtomicInteger count = new AtomicInteger();

        jsonArray.iterator().forEachRemaining(e -> {
            JSONObject _temp  = jsonArray.getJSONObject(count.getAndIncrement());
            String deposit_address = _temp.getString("address");
            int id = _temp.getInt("id");
            int rowIdx = _temp.getInt("rowIdx");
            Request GetOkRequest = new Request.Builder().url(getApiUrl(TxStat+deposit_address)).build();
            try {
                response = client.newCall(GetOkRequest).execute();
                String jsonData = response.body().string();
                _tempJsonObject = new JSONObject(jsonData);
                _temp =new JSONObject();
                _temp.put("status",_tempJsonObject.getString("status"));
                _temp.put("id",id);
                _temp.put("rowIdx",rowIdx);
                returnArray.put(_temp);
                if(count.get()==jsonArray.length())_dbInstance.updateAltCoinStatus(returnArray);
            } catch (IOException |SQLException|ClassNotFoundException ex) {
            }
        });
        return returnArray;
    }

    public JSONArray status(String user_id) throws SQLException, ClassNotFoundException {
    JSONArray returnArray = new JSONArray();
    final AtomicInteger count = new AtomicInteger();
    JSONArray jsonArray = _dbInstance.status(Integer.parseInt(user_id));
    jsonArray.iterator().forEachRemaining(e -> {
        JSONObject _temp  = jsonArray.getJSONObject(count.getAndIncrement());
        String addressToSend = _temp.getString("addressToSend");
        String coin_symbol = _temp.getString("coinsymbol");
        String created_at = _temp.getString("created_at");
        int id =  _temp.getInt("id");
        double network_transaction_amount_amount = _temp.getDouble("network_transaction_amount_amount");
        double network_transaction_fee_amount = _temp.getDouble("network_transaction_fee_amount");

        Request GetOkRequest = new Request.Builder().url(getApiUrl(TxStat+addressToSend)).build();
        try {
            response = client.newCall(GetOkRequest).execute();
            String jsonData = response.body().string();
            _tempJsonObject = new JSONObject(jsonData);
            _temp.put("status",_tempJsonObject.getString("status"));
            _temp.put("id",id);
            _temp.put("addressToSend",addressToSend);
            _temp.put("coinsymbol",coin_symbol);
            _temp.put("date",created_at);

            _temp.put("network_transaction_amount_amount",network_transaction_amount_amount);
            _temp.put("network_transaction_fee_amount",network_transaction_fee_amount);

            returnArray.put(_temp);
        } catch (IOException ex) {
        }
    });
    return returnArray;
}
}
