
package com.mycompany.huythanhstudio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/huythanh_studio?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";

    private static final String USER = "root";

    private static final String PASSWORD = "Huythanh1@";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    URL, USER, PASSWORD
            );
        } catch (SQLException e) {
            System.out.println("Ket noi MySQL that bai!");
            e.printStackTrace();
            return null;
        }
    }
}
