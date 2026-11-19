
package com.mycompany.huythanhstudio;

import java.sql.Connection;

public class TestConnection {
    

    public static void main(String[] args) {

        try (Connection conn = DBConnection.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("KET NOI MYSQL THANH CONG!");
                System.out.println("Database: " + conn.getCatalog());
            } else {
                System.out.println("KET NOI THAT BAI!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
