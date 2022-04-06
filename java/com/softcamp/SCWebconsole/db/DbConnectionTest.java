package com.softcamp.SCWebconsole.db;

import org.junit.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public class DbConnectionTest {
    @Test
    public void mariaDB() throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mariadb://10.50.10.198:3306/tftdb", "root", "socam2021@");
        System.out.println(con);
    }
}
