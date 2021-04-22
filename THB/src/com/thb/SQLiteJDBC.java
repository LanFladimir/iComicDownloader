package com.thb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteJDBC {
    static SQLiteJDBC msqliteJdbc;
    static String create_table_girl = "create table girls(\n" +
            "id int primary key,\n" +
            "girlid varchar(20),\n" +
            "price varchar(20),\n" +
            "age varchar(20),\n" +
            "address varchar(50),\n" +
            "wxqq varchar(50)\n" +
            "guocheng varchar(50)\n" +
            ");";

    static String create_table_imgs = "create table girls(\n" +
            "id int primary key,\n" +
            "img varchar(50),\n" +
            "girlid varchar(50),\n" +
            ");";
    static Connection conn = null;
    static Statement stmt = null;

    static synchronized SQLiteJDBC getInstance() {
        if (msqliteJdbc == null) {
            msqliteJdbc = new SQLiteJDBC();
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:thb.db");
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Opened database successfully");
        }
        return msqliteJdbc;
    }

    void doSQl(String sql) {
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
