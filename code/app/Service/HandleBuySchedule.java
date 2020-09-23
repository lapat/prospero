package com.coinflash.app.Service;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
//@AllArgsConstructor
@ToString
public class HandleBuySchedule {
    private static HandleBuySchedule _handleBuySchedule = null;

    public static HandleBuySchedule getInstance() {
        if(_handleBuySchedule==null)
            return new HandleBuySchedule();
        return _handleBuySchedule;
    }

    /** here take care of making a schedule cron job buy for the day choosed*/
    public void addScheduleBuy(String user_id, double btc_amount, double eth_amount, int chooseSchedule) {
    }

    /** removing schedule buy from the cron job*/
    public void removeScheduleBuy(String user_id, int Schedule_id) {
    }

    /** //TODO update schedule buy from the cron job*/
    public void updateScheduleBuy(String user_id, double btc_amount, double eth_amount, int chooseSchedule, int Schedule_id) {
    }

}
