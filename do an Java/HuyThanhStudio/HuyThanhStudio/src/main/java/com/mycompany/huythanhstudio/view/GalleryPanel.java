package com.mycompany.huythanhstudio.view;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GalleryPanel extends JPanel {
    private final JPanel grid = new JPanel(new GridLayout(0,3,14,14));
    private final ArrayList<File> images = new ArrayList<>();
    private final JLabel count = Ui.label("0 ảnh trong thư viện",12,false,Ui.MUTED);

    public GalleryPanel() {
        setLayout(new BorderLayout(0,16)); setBackground(Ui.BG);
        setBorder(new EmptyBorder(25,28,25,28));
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles,BoxLayout.Y_AXIS));
        titles.add(Ui.label("Ảnh mẫu",27,true,Ui.DARK));
        titles.add(Box.createVerticalStrut(5));
        titles.add(Ui.label("Portfolio và thư viện ảnh tham khảo của studio.",13,false,Ui.MUTED));
        titles.add(Box.createVerticalStrut(8)); titles.add(count);
        head.add(titles,BorderLayout.WEST);
        JButton add = Ui.button("+ Thêm ảnh");
        add.addActionListener(e -> chooseImages());
        head.add(add,BorderLayout.EAST);
        add(head,BorderLayout.NORTH);

        grid.setBackground(Ui.BG);
        grid.setBorder(new EmptyBorder(2,2,2,2));
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null); scroll.getViewport().setBackground(Ui.BG);
        add(scroll,BorderLayout.CENTER);
        refresh();
    }

    private void chooseImages() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (JPG, PNG, JPEG, GIF)", "jpg","jpeg","png","gif"));
        if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
            for (File f : chooser.getSelectedFiles()) images.add(f);
            refresh();
        }
    }
    private void refresh() {
        grid.removeAll();
        for (File file : images) {
            JPanel card = Ui.card();
            JLabel preview = new JLabel("",SwingConstants.CENTER);
            preview.setPreferredSize(new Dimension(220,150));
            preview.setBackground(new Color(239,241,244)); preview.setOpaque(true);
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(220,150,Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(scaled));
            card.add(preview,BorderLayout.CENTER);
            JPanel foot = new JPanel(new BorderLayout()); foot.setOpaque(false);
            String name = file.getName();
            JLabel label = Ui.label(name,11,true,Ui.DARK);
            label.setToolTipText(name);
            JButton view = new JButton("Xem");
            view.addActionListener(e -> showImage(file));
            JButton remove = new JButton("Xóa");
            remove.addActionListener(e -> { images.remove(file); refresh(); });
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0)); actions.setOpaque(false);
            actions.add(view); actions.add(remove);
            foot.add(label,BorderLayout.CENTER); foot.add(actions,BorderLayout.EAST);
            card.add(foot,BorderLayout.SOUTH);
            grid.add(card);
        }
        if (images.isEmpty()) {
            JPanel empty = new JPanel(); empty.setOpaque(false); empty.setBorder(new EmptyBorder(50,20,20,20));
            empty.add(Ui.label("Chưa có ảnh. Nhấn “+ Thêm ảnh” để chọn ảnh từ máy tính.",14,false,Ui.MUTED));
            grid.setLayout(new BorderLayout()); grid.add(empty,BorderLayout.NORTH);
        } else grid.setLayout(new GridLayout(0,3,14,14));
        count.setText(images.size()+" ảnh trong thư viện");
        grid.revalidate(); grid.repaint();
    }
    private void showImage(File file) {
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image img = icon.getImage();
        int w=Math.min(900,img.getWidth(null)), h=Math.min(650,img.getHeight(null));
        if (w>0 && h>0) img=img.getScaledInstance(w,h,Image.SCALE_SMOOTH);
        JOptionPane.showMessageDialog(this,new JLabel(new ImageIcon(img)),file.getName(),JOptionPane.PLAIN_MESSAGE);
    }
}
