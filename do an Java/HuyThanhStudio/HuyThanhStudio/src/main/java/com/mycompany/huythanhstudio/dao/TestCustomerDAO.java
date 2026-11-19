
package com.mycompany.huythanhstudio;

import com.mycompany.huythanhstudio.dao.CustomerDAO;
import javax.swing.table.DefaultTableModel;

public class TestCustomerDAO {

    public static void main(String[] args) {

        CustomerDAO dao = new CustomerDAO();

        DefaultTableModel model = dao.getAllCustomers();

        System.out.println("===== DANH SACH KHACH HANG =====");

        for (int i = 0; i < model.getRowCount(); i++) {

            System.out.println(
                model.getValueAt(i, 1) + " | " +
                model.getValueAt(i, 2) + " | " +
                model.getValueAt(i, 3)
            );
        }

        System.out.println("Tong so khach hang: " + model.getRowCount());
    }
}
