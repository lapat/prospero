package com.coinflash.app.Service;

import org.json.JSONArray;

import java.io.IOException;
import java.sql.SQLException;

public interface HandleAltcoinTransfer {

    JSONArray sendAltcoin(JSONArray jsonArray, String users_access_token_coinbase, String users_wallet_id, String user_id, double currencyWallet_price_now, String idemForEmail, String currencyWallet) throws IOException, SQLException, ClassNotFoundException;

    JSONArray BatchBuy(JSONArray json, String user_id, String users_wallet_id, double currencyWallet_price_now, boolean ConsumedFirstBuy, String currencyWallet) throws IOException, SQLException, ClassNotFoundException;

    void chargeUserOneDollarforBatchBuy(int user_id, String users_access_token_coinbase, String users_wallet_id) throws IOException;
}
