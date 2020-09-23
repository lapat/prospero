package com.coinflash.app.Service;

import java.sql.SQLException;

public interface coinBaseConnection {
  double spent(String user_id) throws SQLException, ClassNotFoundException;
}
