/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicButtonUI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.util.Date;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author LENOVO
 */
public class DashboardOwner extends javax.swing.JFrame {

    /**
     * Creates new form DashboardOwner
     */
    Connection conn;

    public DashboardOwner(String nama) {
        initComponents();
        lblNama.setText("Selamat Datang, " + nama + " (Owner)");
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        styleButtons();
        conn = koneksi.getConnection();
        dcTanggal.addPropertyChangeListener("date", evt -> {
            loadProdukTerlaris();
            loadProdukTerjual();
            loadPendapatan();
            loadPendapatan();
            loadKinerjaKasir();
            loadGrafikPenjualan();

        });

    }

    public DashboardOwner() {
        initComponents();
        Dimension size = new Dimension(149, 97);

    }

    private void styleButtons() {
        btnLogout.setUI(new BasicButtonUI());            // <- ganti dengan nama tombolmu
        btnLogout.setBackground(new Color(204, 0, 0));   // merah, boleh diganti
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setOpaque(true);

    }

    private void loadProdukTerlaris() {

        // 🔒 CEGAH NULL DATE
        if (dcTanggal.getDate() == null) {
            lblProdukTerlaris.setText("-");
            return;
        }

        String sql
                = "SELECT p.nama_produk, SUM(dt.jumlah) AS total_terjual "
                + "FROM transaksi t "
                + "JOIN detail_transaksi dt ON t.no_transaksi = dt.no_transaksi "
                + "JOIN produk p ON dt.id_produk = p.id "
                + "WHERE DATE(t.tanggal) = ? "
                + "GROUP BY p.id "
                + "ORDER BY total_terjual DESC "
                + "LIMIT 1";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            java.sql.Date tanggal
                    = new java.sql.Date(dcTanggal.getDate().getTime());
            ps.setDate(1, tanggal);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String namaProduk = rs.getString("nama_produk");
                int total = rs.getInt("total_terjual");

                lblProdukTerlaris.setText(
                        "<html><b>" + namaProduk + "</html>"
                );
            } else {
                lblProdukTerlaris.setText("Tidak ada data");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadProdukTerjual() {
        try {
            java.util.Date tgl = dcTanggal.getDate();
            if (tgl == null) {
                lblProdukTerjual.setText("0");
                return;
            }

            String sql
                    = "SELECT IFNULL(SUM(dt.jumlah), 0) AS total_terjual "
                    + "FROM transaksi t "
                    + "JOIN detail_transaksi dt ON t.no_transaksi = dt.no_transaksi "
                    + "WHERE DATE(t.tanggal) = ?";

            PreparedStatement ps = koneksi.getConnection().prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tgl.getTime()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblProdukTerjual.setText(rs.getString("total_terjual"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadPendapatan() {
        try {
            java.util.Date tgl = dcTanggal.getDate();
            if (tgl == null) {
                lblPendapatann.setText("0");
                return;
            }

            String sql
                    = "SELECT IFNULL(SUM(dt.subtotal), 0) AS total_pendapatan "
                    + "FROM transaksi t "
                    + "JOIN detail_transaksi dt ON t.no_transaksi = dt.no_transaksi "
                    + "WHERE DATE(t.tanggal) = ?";

            PreparedStatement ps = koneksi.getConnection().prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tgl.getTime()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblPendapatann.setText(
                        "Rp " + String.format("%,d", rs.getInt("total_pendapatan"))
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadKinerjaKasir() {
        try {
            if (dcTanggal.getDate() == null) {
                lblKinerjaKasir.setText("-");
                return;
            }

            Connection c = koneksi.getConnection();

            String sql = """
            SELECT SUM(dt.subtotal) AS total_pendapatan
            FROM transaksi t
            JOIN detail_transaksi dt ON t.no_transaksi = dt.no_transaksi
            WHERE DATE(t.tanggal) = ?
        """;

            PreparedStatement ps = c.prepareStatement(sql);
            java.sql.Date tanggal = new java.sql.Date(dcTanggal.getDate().getTime());
            ps.setDate(1, tanggal);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double totalPendapatan = rs.getDouble("total_pendapatan");

                if (totalPendapatan >= 1_000_000) {
                    lblKinerjaKasir.setText("Baik Sekali");
                } else if (totalPendapatan >= 500_000) {
                    lblKinerjaKasir.setText("Baik");
                } else if (totalPendapatan > 0) {
                    lblKinerjaKasir.setText("Kurang Baik");
                } else {
                    lblKinerjaKasir.setText("Belum Ada Kinerja");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void loadGrafikPenjualan() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            String sql
                    = "SELECT p.nama_produk, SUM(dt.jumlah) total "
                    + "FROM transaksi t "
                    + "JOIN detail_transaksi dt ON t.no_transaksi = dt.no_transaksi "
                    + "JOIN produk p ON dt.id_produk = p.id "
                    + "WHERE DATE(t.tanggal) = ? "
                    + "GROUP BY p.nama_produk";

            PreparedStatement ps = koneksi.getConnection().prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(dcTanggal.getDate().getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dataset.addValue(
                        rs.getInt("total"),
                        "Penjualan",
                        rs.getString("nama_produk")
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Grafik Penjualan",
                "Produk",
                "Jumlah",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1100, 272));
        chartPanel.setMinimumSize(new Dimension(1100, 272));
        chartPanel.setMaximumSize(new Dimension(1100, 272));
        chartPanel.setMouseZoomable(false);

        panelGrafik.setLayout(new BorderLayout());
        panelGrafik.removeAll();
        panelGrafik.add(chartPanel, BorderLayout.CENTER);
        panelGrafik.revalidate();
        panelGrafik.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnLogout = new javax.swing.JButton();
        lblNama = new javax.swing.JLabel();
        dcTanggal = new com.toedter.calendar.JDateChooser();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        panelGrafik = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        lblProdukTerlaris = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lblProdukTerjual = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        lblKinerjaKasir = new javax.swing.JLabel();
        lblPendapatan = new javax.swing.JPanel();
        lblPendapatann = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(31, 95, 120));

        btnLogout.setBackground(new java.awt.Color(201, 74, 74));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        lblNama.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNama.setForeground(new java.awt.Color(255, 255, 255));
        lblNama.setText("jLabel1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(btnLogout)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dcTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(lblNama)
                .addGap(156, 156, 156))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dcTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNama)
                    .addComponent(btnLogout))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setForeground(new java.awt.Color(102, 102, 102));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("Layanan Terlaris");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Layanan Terjual");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 102, 102));
        jLabel4.setText("Pendapatan");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(102, 102, 102));
        jLabel5.setText("Kinerja Kasir");

        javax.swing.GroupLayout panelGrafikLayout = new javax.swing.GroupLayout(panelGrafik);
        panelGrafik.setLayout(panelGrafikLayout);
        panelGrafikLayout.setHorizontalGroup(
            panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1100, Short.MAX_VALUE)
        );
        panelGrafikLayout.setVerticalGroup(
            panelGrafikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(31, 95, 120));

        lblProdukTerlaris.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblProdukTerlaris.setForeground(new java.awt.Color(255, 255, 255));
        lblProdukTerlaris.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblProdukTerlaris.setText("Tidak ada data");
        lblProdukTerlaris.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblProdukTerlaris, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(lblProdukTerlaris)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(31, 95, 120));

        lblProdukTerjual.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblProdukTerjual.setForeground(new java.awt.Color(255, 255, 255));
        lblProdukTerjual.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblProdukTerjual.setText("0.0");
        lblProdukTerjual.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblProdukTerjual, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(lblProdukTerjual)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(31, 95, 120));

        lblKinerjaKasir.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblKinerjaKasir.setForeground(new java.awt.Color(255, 255, 255));
        lblKinerjaKasir.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblKinerjaKasir.setText("Belum ada kinerja");
        lblKinerjaKasir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblKinerjaKasir, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(lblKinerjaKasir)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        lblPendapatan.setBackground(new java.awt.Color(31, 95, 120));

        lblPendapatann.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblPendapatann.setForeground(new java.awt.Color(255, 255, 255));
        lblPendapatann.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPendapatann.setText("Rp 0");
        lblPendapatann.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout lblPendapatanLayout = new javax.swing.GroupLayout(lblPendapatan);
        lblPendapatan.setLayout(lblPendapatanLayout);
        lblPendapatanLayout.setHorizontalGroup(
            lblPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lblPendapatanLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblPendapatann, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        lblPendapatanLayout.setVerticalGroup(
            lblPendapatanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(lblPendapatanLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(lblPendapatann)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addComponent(jLabel2)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(166, 166, 166)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(160, 160, 160)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(141, 141, 141))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(193, 193, 193)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4)
                        .addGap(261, 261, 261)
                        .addComponent(jLabel5)
                        .addGap(194, 194, 194))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(103, 103, 103)
                .addComponent(panelGrafik, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 181, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(123, 123, 123)
                .addComponent(panelGrafik, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose(); // tutup dashboard

            // buka form login lagi
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DashboardOwner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DashboardOwner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DashboardOwner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DashboardOwner.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashboardOwner().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogout;
    private com.toedter.calendar.JDateChooser dcTanggal;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lblKinerjaKasir;
    private javax.swing.JLabel lblNama;
    private javax.swing.JPanel lblPendapatan;
    private javax.swing.JLabel lblPendapatann;
    private javax.swing.JLabel lblProdukTerjual;
    private javax.swing.JLabel lblProdukTerlaris;
    private javax.swing.JPanel panelGrafik;
    // End of variables declaration//GEN-END:variables
}
