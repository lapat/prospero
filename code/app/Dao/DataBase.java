package com.coinflash.app.Dao;


import com.coinflash.app.Pojo.Schedule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Set;

public interface DataBase {

    /**Dao Method: fetch schedule information from db and returned Set of schedule */
    Set<Schedule> getUserScheduleDetails(int id) throws ClassNotFoundException, SQLException;

    /**Dao Method: add one schedule information to the db and returned true if no issue, false otherwise */
    boolean addOneSchedule(int user_id, double btc_amount, double eth_amount, int chooseSchedule) throws ClassNotFoundException, SQLException;

    /**Dao Method: remove one schedule information from the db and returned true if no issue, false otherwise */
    boolean removeOneSchedule(int user_id, int scheduler_id)throws ClassNotFoundException, SQLException;

    /**Dao Method: update one schedule information from the db and returned true if no issue, false otherwise */
    boolean updateScheduleBuy(int user_id, double btc_amount, double eth_amount, int chooseSchedule, int schedule_id)throws ClassNotFoundException, SQLException;


        /**Dao Method: add one altcoin conversion to the db */
        long addAltcoin(String user_id, String pair, String withdrawalAdress, String withdrawalAmount,
                        String deposit, String returnAdress) throws ClassNotFoundException, SQLException;

        /**Dao Method: get all altcoins conversion from the db */
        JSONArray getAltcoins(int i) throws ClassNotFoundException, SQLException;

        /**Dao Method: update all altcoins status */
        void updateAltCoinStatus(JSONArray returnArray) throws ClassNotFoundException, SQLException;

        /**Dao Method: save altcoin transaction*/
        JSONObject saveAltcoinTransaction(String user_id, String users_wallet_id, Double network_transaction_amount_amount, String coinSymbol, String addressToSend, double native_amount, String native_amount_currency, String created_at, Double network_transaction_fee_amount, String withdrawalAddress,String idemForEmail) throws ClassNotFoundException, SQLException;

        /**Dao Method: get all altcoin transactions*/
        JSONArray getAltcoinsTransaction(int user_id) throws ClassNotFoundException, SQLException;


        /**Dao Method: get all altcoin transactions status for one user*/
        JSONArray status(int user_id) throws ClassNotFoundException, SQLException;

        /**Dao Method: check if user consumed the first buy from multicurrency features*/
        boolean hasConsumedFirstBuy(String user_id) throws SQLException, ClassNotFoundException;

        /** Dao Method: after the user make a first success transaction we change the consume of first buy to true*/
        void consumeFirstBuy(int user_id) throws ClassNotFoundException, SQLException;

        /**     Dao Method: update coinflash user limit */
        void RelinkHigherLimit(String relinked_higher_limit, int user_id) throws ClassNotFoundException, SQLException;

    /** Dao Method: check if user authorized coinbase and have link coinbase from accountsetting or multicurrency*/
        //String checkAuthorization(String user_id) throws ClassNotFoundException, SQLException;

        /** Dao Method: set user authorization to 50 usd per week in DB*/
        //void setAuthorizeToFifty(String user_id) throws ClassNotFoundException, SQLException;
}
