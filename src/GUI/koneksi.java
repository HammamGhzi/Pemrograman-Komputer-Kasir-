package GUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class koneksi {
    private static Connection conn;
    
    public static Connection getConnection() {
        if (conn == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/laundry_app";
                String user = "root";
                String pass = "";
                conn = DriverManager.getConnection(url, user, pass);
                System.out.println("Koneksi berhasil!");
            } catch (SQLException e) {
                System.err.println("Koneksi gagal: " + e.getMessage());
            }
        }
        return conn;
    }

   
}
