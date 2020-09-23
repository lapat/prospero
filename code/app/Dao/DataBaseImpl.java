package com.coinflash.app.Dao;

import com.coinflash.app.Log2File;
import com.coinflash.app.Pojo.Schedule;
import com.coinflash.app.constants;
import com.mysql.jdbc.JDBC4Connection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DataBaseImpl implements  DataBase {

    /**
     * Dao Method: fetch schedule information from db and returned Set of schedule
     *
     * @param id
    */
    @Override
    public Set<Schedule> getUserScheduleDetails(int id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        Set<Schedule> schedules = new LinkedHashSet<>();
        if(jdbc4Connection!=null){
            String query = "SELECT * FROM coinflashscheduler where user_id ="+id;
            Statement statement = jdbc4Connection.createStatement();
            ResultSet  resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                schedules.add(Schedule.builder()
                        .scheduler_id(resultSet.getInt("scheduler_id"))
                        .schedule_type(resultSet.getInt("schedule_type"))
                        .btc_amount(resultSet.getDouble("btc_amount"))
                        .Eth_amount(resultSet.getDouble("Eth_amount"))
                        .start_date(resultSet.getTimestamp("start_date"))
                        .build());
             }
        }
        return schedules;
    }

    /**
     * Dao Method: add one schedule information to the db and returned true if no issue, false otherwise
     *
     * @param user_id
     * @param btc_amount
     * @param eth_amount
     * @param chooseSchedule
     */
    @Override
    public boolean addOneSchedule(int user_id, double btc_amount, double eth_amount, int chooseSchedule) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);

        if(jdbc4Connection!=null){
            String query = "INSERT INTO coinflashscheduler (user_id,schedule_type,btc_amount,Eth_amount,start_date) VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setInt(1, user_id);
            preparedStatement.setInt(2, chooseSchedule);
            preparedStatement.setDouble(3, btc_amount);
            preparedStatement.setDouble(4, eth_amount);
            preparedStatement.setTimestamp(5, getNextStartDate(chooseSchedule));
            int update = preparedStatement.executeUpdate();
            if(update>0)
                return true;
        }
        return false;
    }

    /**
     * Dao Method: remove one schedule information from the db and returned true if no issue, false otherwise
     *
     * @param user_id
     * @param scheduler_id
     */
    @Override
    public boolean removeOneSchedule(int user_id, int scheduler_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);

        if(jdbc4Connection!=null){
            String query = "DELETE FROM coinflashscheduler WHERE scheduler_id =? and user_id =? ";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setInt(1, scheduler_id);
            preparedStatement.setInt(2, user_id);
            int update = preparedStatement.executeUpdate();
            if(update>0)
                return true;
        }
        return false;
    }

    /**
     * Dao Method: update one schedule information from the db and returned true if no issue, false otherwise
     *
     * @param user_id
     * @param btc_amount
     * @param eth_amount
     * @param chooseSchedule
     * @param schedule_id
     */
    //TODO for later user.
    @Override
    public boolean updateScheduleBuy(int user_id, double btc_amount, double eth_amount, int chooseSchedule, int schedule_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        if(jdbc4Connection!=null){
            String query = "UPDATE coinflashscheduler set schedule_type=?,btc_amount=?,Eth_amount=?,start_date=? WHERE user_id=? and scheduler_id=?";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setInt(1, chooseSchedule);
            preparedStatement.setDouble(2, btc_amount);
            preparedStatement.setDouble(3, eth_amount);
            preparedStatement.setTimestamp(4, getNextStartDate(chooseSchedule));
            preparedStatement.setInt(5, user_id);
            preparedStatement.setInt(6, schedule_id);
            int update = preparedStatement.executeUpdate();
            if(update>0)
                return true;
        }
        return false;
    }

    /**
     * Dao Method: add one altcoin conversion to the db
     *  @param user_id
     * @param pair
     * @param withdrawalAdress
     * @param withdrawalAmount
     * @param deposit
     * @param returnAdress
     */
    @Override
    public long addAltcoin(String user_id, String pair, String withdrawalAdress, String withdrawalAmount,
                           String deposit, String returnAdress) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);

        if(jdbc4Connection!=null) {
            String query = "INSERT INTO coinflash_Altcoin (user_id,pair,withdrawalAdress,btcwithdrawalAmount,depositAdress," +
                    "returnAddress) VALUES (?,?,?,?,?,?)";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, Integer.parseInt(user_id));
            preparedStatement.setString(2, pair);
            preparedStatement.setString(3, withdrawalAdress);
            preparedStatement.setString(4, withdrawalAmount);
            preparedStatement.setString(5, deposit);
            preparedStatement.setString(6, returnAdress);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }
            }
            return 1l;
        }

    /**
     * Dao Method: get all altcoins conversion from the db
     *
     * @param user_id
     */
    @Override
    public JSONArray getAltcoins(int user_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        if(jdbc4Connection!=null){
            String query = "SELECT * FROM coinflash_Altcoin where user_id ="+user_id;
            Statement statement = jdbc4Connection.createStatement();
            ResultSet  resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                jsonObject = new JSONObject();
                jsonObject.put("pair",resultSet.getString("pair"));
                jsonObject.put("amount",resultSet.getString("btcwithdrawalAmount"));
                jsonObject.put("id",resultSet.getInt("id"));
                jsonObject.put("deposit",resultSet.getString("depositAdress"));
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }

    /**
     * TODO Dao Method: update All status ON coinflash_Altcoin_Transaction table instead coinflash_Altcoin table.
     *
     * @param jsonArray
     */
     public void updateAltCoinStatus(JSONArray jsonArray) throws ClassNotFoundException, SQLException{
         JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
         String query = "UPDATE coinflash_Altcoin_transactions SET status =? where id =?";
         PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
         final AtomicInteger count = new AtomicInteger();

         jsonArray.iterator().forEachRemaining(e -> {
             JSONObject _temp  = jsonArray.getJSONObject(count.getAndIncrement());
             String status = _temp.getString("status");
             int id = _temp.getInt("id");
             if(jdbc4Connection!=null) {
                 try {
                     preparedStatement.clearParameters();
                     preparedStatement.setString(1, status);
                     preparedStatement.setInt(2, id);
                     preparedStatement.executeUpdate();
                 } catch (SQLException e1) {
                 }
             }
         });

     }

    /**
     * Dao Method: save altcoin transaction
     * @param network_transaction_amount_amount
     * @param user_id
     * @param users_wallet_id
     * @param coinSymbol
     * @param addressToSend
     * @param native_amount
     * @param native_amount_currency
     * @param created_at
     * @param network_transaction_fee_amount
     * @param idemForEmail
     */
    @Override
    public JSONObject saveAltcoinTransaction(String user_id, String users_wallet_id, Double network_transaction_amount_amount, String coinSymbol, String addressToSend, double native_amount, String native_amount_currency, String created_at, Double network_transaction_fee_amount, String withdrawalAddress, String idemForEmail) throws ClassNotFoundException, SQLException {
            JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);

            JSONObject jsonObject = new JSONObject();
            if(jdbc4Connection!=null) {
                //todo
                String query = "INSERT INTO coinflash_Altcoin_transactions (user_id,users_wallet_id,network_transaction_amount_amount,coinsymbol, addressToSend, status, native_amount, native_amount_currency,created_at,network_transaction_fee_amount , withdrawalAddress, idemForEmail) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setInt(1, Integer.parseInt(user_id));
                preparedStatement.setString(2, users_wallet_id);
                preparedStatement.setDouble(3, network_transaction_amount_amount);
                preparedStatement.setString(4, coinSymbol);
                preparedStatement.setString(5, addressToSend);
                preparedStatement.setString(6, "Pending");
                preparedStatement.setDouble(7, native_amount);
                preparedStatement.setString(8, native_amount_currency);
                preparedStatement.setString(9, created_at);
                preparedStatement.setDouble(10, network_transaction_fee_amount);
                preparedStatement.setString(11, withdrawalAddress);
                preparedStatement.setString(12, idemForEmail);

                int update =preparedStatement.executeUpdate();
                ResultSet rs = preparedStatement.getGeneratedKeys();
                if(update>0){
                    jsonObject.put("user_wallet_id", users_wallet_id);
                    jsonObject.put("network_transaction_amount_amount", network_transaction_amount_amount);
                    jsonObject.put("coinsymbol", coinSymbol);
                    jsonObject.put("addressToSend", addressToSend);
                    jsonObject.put("date", created_at);
                    jsonObject.put("status", "Pending");
                    if (rs.next())
                    jsonObject.put("id", rs.getLong(1));
                }
            }
            return jsonObject;
        }
    /**
     * Dao Method: get all altcoin transactions
     *
     * @param user_id
     */
    @Override
    public JSONArray getAltcoinsTransaction(int user_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);

//       network_transaction_amount_amount,coinsymbol, addressToSend, status, native_amount, native_amount_currency,created_at,network_transaction_fee_amount


        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        if(jdbc4Connection!=null){
          String query = "SELECT status,coinsymbol,network_transaction_amount_amount,native_amount,native_amount_currency,created_at,network_transaction_fee_amount,withdrawalAddress, idemForEmail " +
                   " FROM coinflash_Altcoin_transactions where user_id ="+user_id;
            Statement statement = jdbc4Connection.createStatement();
            ResultSet  resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                jsonObject = new JSONObject();
                jsonObject.put("coinsymbol",resultSet.getString("coinsymbol")); //from coin to coin exp : ETH_LTC
                jsonObject.put("network_transaction_amount_amount",resultSet.getDouble("network_transaction_amount_amount")); //IN BTC
                jsonObject.put("native_amount",resultSet.getDouble("native_amount")); //IN USD
                jsonObject.put("native_amount_currency",resultSet.getString("native_amount_currency")); //USD
                jsonObject.put("created_at",resultSet.getString("created_at"));//DATE
                jsonObject.put("status",resultSet.getString("status"));
                jsonObject.put("idemForEmail",resultSet.getString("idemForEmail"));

                jsonObject.put("withdrawalAddress",resultSet.getString("withdrawalAddress"));
                jsonObject.put("network_transaction_fee_amount",resultSet.getDouble("network_transaction_fee_amount"));//in btc
                jsonArray.put(jsonObject);
            }
        }

        return jsonArray;

    }

    /**
 * Dao Method: get all altcoin transactions status for one user
 *
 * @param user_id
 */
@Override
public JSONArray status(int user_id) throws ClassNotFoundException, SQLException {

    JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
    JSONObject jsonObject = null;
    JSONArray jsonArray = new JSONArray();
    if(jdbc4Connection!=null){
        String query = "SELECT id,coinsymbol,network_transaction_amount_amount,network_transaction_fee_amount,addressToSend,created_at FROM coinflash_Altcoin_transactions where user_id ="+user_id;
        Statement statement = jdbc4Connection.createStatement();
        ResultSet  resultSet = statement.executeQuery(query);
        while(resultSet.next()){
            jsonObject = new JSONObject();
            jsonObject.put("network_transaction_amount_amount",resultSet.getDouble("network_transaction_amount_amount")); //LTC
            jsonObject.put("network_transaction_fee_amount",resultSet.getDouble("network_transaction_fee_amount")); //usd
            jsonObject.put("addressToSend",resultSet.getString("addressToSend"));
            jsonObject.put("coinsymbol",resultSet.getString("coinsymbol"));
            jsonObject.put("created_at",resultSet.getString("created_at"));
            jsonObject.put("id",resultSet.getInt("id"));
            jsonArray.put(jsonObject);
        }
    }
    return jsonArray;
}

    /**
     * Dao Method: check if user consumed the first buy from multicurrency features
     *
     * @param user_id
     */
    @Override
    public boolean hasConsumedFirstBuy(String user_id) throws SQLException, ClassNotFoundException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        String query = "SELECT ConsumedFirstBuy FROM coinflash_users where user_id ="+Integer.parseInt(user_id);
        Statement statement = jdbc4Connection.createStatement();
        ResultSet  resultSet = statement.executeQuery(query);
        while(resultSet.next()){
            return resultSet.getBoolean("ConsumedFirstBuy");
        }
        return false;
    }

    /**
     * Dao Method: after the user make a first success transaction we change the consume of first buy to true
     *
     * @param user_id
     */
    @Override
    public void consumeFirstBuy(int user_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        if(jdbc4Connection!=null){
            String query = "UPDATE coinflash_users set ConsumedFirstBuy=? WHERE user_id=?";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setDouble(2, user_id);
            preparedStatement.executeUpdate();
        }

    }

    /**
     * Dao Method: update coinflash user limit
     *
     * @param relinked_higher_limit
     * @param user_id
     */
    @Override
    public void RelinkHigherLimit(String relinked_higher_limit, int user_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        if(jdbc4Connection!=null){
            String query = "UPDATE coinflash_users set relinked_higher_limit = ? where user_id = ?";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setBoolean(1, Boolean.valueOf(relinked_higher_limit));
            preparedStatement.setInt(2, user_id);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Dao Method: check if user authorized coinbase and have link coinbase from accountsetting or multicurrency
     *
     * @param user_id
     */
   /* @Override
    public String checkAuthorization(String user_id) throws ClassNotFoundException, SQLException {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        if(jdbc4Connection!=null){
            String query = "SELECT authorize_fifty_usd,coinbase_access_token, coinbase_refresh_access_token FROM coinflash_users where user_id ="+ Integer.parseInt(user_id);
            Statement statement = jdbc4Connection.createStatement();
            ResultSet  resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                if(resultSet.getString("coinbase_access_token")!=null&&resultSet.getString("coinbase_refresh_access_token") !=null && resultSet.getBoolean("authorize_fifty_usd"))
                return "authorized";
            }
        }


        return "not_authorized";
    }*/

    /**
     * Dao Method: set user authorization to 50 usd per week in DB
     *

     */
   /* @Override
    public void setAuthorizeToFifty(String user_id)throws ClassNotFoundException, SQLException  {
        JDBC4Connection jdbc4Connection =makeConnection(constants.JDBC_DRIVER, constants.DB_URL, constants.DB_USER, constants.DB_PASSWORD);
        if(jdbc4Connection!=null){
            String query = "UPDATE coinflash_users set authorize_fifty_usd=? WHERE user_id=?";
            PreparedStatement preparedStatement = jdbc4Connection.prepareStatement(query);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, Integer.parseInt(user_id));
            preparedStatement.executeUpdate();
        }
    }*/


    private Timestamp getNextStartDate(int chooseSchedule) {
        switch (chooseSchedule) {
            //if the date is daily then we choose 2moro.
            case 1:
                return Timestamp.valueOf(LocalDateTime.now().plusDays(1));
            case 2: {
                //if the date is weekly then we choose the first Monday of the week
                LocalDateTime dateTime = LocalDateTime.now();
                return Timestamp.valueOf(dateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)));
            }
            case 3: {
                //if the date is monthly then we choose the first day of  the next month
                LocalDateTime dateTime = LocalDateTime.now();
                return Timestamp.valueOf(dateTime.with(TemporalAdjusters.firstDayOfNextMonth()));
            }
        }
        //if not return the actual date
        return Timestamp.valueOf(LocalDateTime.now());
    }


    private JDBC4Connection makeConnection(String jdbcDriver, String dbUrl, String dbUser, String dbPassword) throws ClassNotFoundException {
        JDBC4Connection con = null;
        try {
            Class.forName(jdbcDriver);
            con = (JDBC4Connection) DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
        catch (SQLException e) {
            Log2File.log((String)("sql exception " + e));
        }
        return con;
    }
}
