package com.mycompany.huythanhstudio.view;

import java.awt.*;
import javax.swing.*;

public class SimplePagePanel extends JPanel {
    public SimplePagePanel(String title,String description) {
        setLayout(new BorderLayout(0,18)); setBackground(Ui.BG);
        setBorder(BorderFactory.createEmptyBorder(25,28,25,28));
        add(Ui.label(title,27,true,Ui.DARK),BorderLayout.NORTH);
        JPanel card=Ui.card();
        card.add(Ui.label(description,14,false,Ui.MUTED),BorderLayout.NORTH);
        card.add(Ui.label("Khu vực này sẽ được hoàn thiện ở giai đoạn tiếp theo.",13,false,Ui.DARK),BorderLayout.CENTER);
        add(card,BorderLayout.CENTER);
    }
}
