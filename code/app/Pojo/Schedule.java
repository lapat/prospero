package com.coinflash.app.Pojo;

import lombok.*;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Schedule {
    private int scheduler_id;
    private int schedule_type;
    private double btc_amount;
    private double Eth_amount;
    private Timestamp start_date;
    private static Schedule _schedule = null;

    public static Schedule getInstance() {
        if(_schedule==null)
            return new Schedule();
        return _schedule;
    }
}
