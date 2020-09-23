package com.coinflash.app.Pojo;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"ApiKey"})
@Builder
public class Connection {

    private String withdrawalAdress;
    private Double amount;
    private String pair;
    private String returnAddress;
    private String ApiKey;

    private static Connection _connection = null;

    public static Connection getInstance() {
        if(_connection==null)
            return new Connection();
        return _connection;
    }



}
