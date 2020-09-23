package com.coinflash.app.Service;

import com.coinflash.app.Dao.DataBase;
import com.coinflash.app.Dao.DataBaseImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class coinBaseConnectionIMPL implements  coinBaseConnection{

  DataBase _dbInstance = new DataBaseImpl();

    private static coinBaseConnectionIMPL _coinBaseConnectionIMPL= null;
    private static final String baseUrlCoinBase = "https://api.coinbase.com/v2/accounts/";
    private static final String Buys = "/buys";
    private static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json");
    OkHttpClient client = new OkHttpClient();

    Response response = null;

    protected static String getApiUrl(String path) {
        return baseUrlCoinBase + path;
    }


    public static coinBaseConnectionIMPL getInstance() {
        if(_coinBaseConnectionIMPL==null)
            return new coinBaseConnectionIMPL();
        return _coinBaseConnectionIMPL;
    }


    @Override
    public double spent(String user_id) throws SQLException, ClassNotFoundException {
        final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"; //format from coinbase. utc.
        final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);

        final boolean[] flagNoError = {false};
        Date dateLastWeek = new Date(System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 7));
        final double[] totalWithoutFees = {0.0};
        JSONArray jsonArrayData= _dbInstance.getAltcoinsTransaction(Integer.parseInt(user_id));
        if(jsonArrayData!=null && jsonArrayData.length()>0){
                jsonArrayData.forEach(o -> {
                    JSONObject jsonObject = (JSONObject) o;
                    if(jsonObject!=null){
                        if(jsonObject.has("created_at")){
                            Date date = null;
                            try {
                                date = sdf.parse(jsonObject.getString("created_at"));
                                if(date.compareTo(dateLastWeek)>=0){
                                    if(jsonObject.has("native_amount")){
                                        Double amount =jsonObject.getDouble("native_amount");
                                        if(amount!=null){
                                            totalWithoutFees[0]+=amount;
                                        }
                                    }
                                }

                            } catch (ParseException e) {
                                flagNoError[0] = true;
                            }
                        }
                    }
                });
        }


        /*if(totalWithoutFees[0]<50.0&& !flagNoError[0] ){
            return totalWithoutFees[0];
        }*/
        return totalWithoutFees[0];
    }




    }
