package com.yogpc.mi.inst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class Swing implements ActionListener {
  private static final Swing I = new Swing();
  private static final JFrame jf = new JFrame();
  private static final JRadioButton gp = new JRadioButton("Profile", true);
  private static final JComboBox<String> cp = new JComboBox<String>(Main.getProfiles());
  private static final JCheckBox rew = new JCheckBox("Fix profile", true);
  private static final JRadioButton gv = new JRadioButton("Version");
  private static final JComboBox<String> cv = new JComboBox<String>(Main.getVersions());
  private static final JLabel jl1 = new JLabel("New version name");
  private static final JLabel jl2 = new JLabel("IME is already installed. Update only");
  private static final JTextField dst = new JTextField(20);
  private static final JButton jb = new JButton("OK");
  private static String last = null;
  private static boolean lv = false;

  private Swing() {}

  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e != null && e.getSource() == jb) {
      Main.install(last, dst.getText(), !lv);
      JOptionPane.showMessageDialog(null, "Install succeeded!", "Minecraft IME installer",
          JOptionPane.INFORMATION_MESSAGE);
      jf.dispose();
    } else {
      String nsel = null;
      boolean nv = false;
      if (gv.isSelected()) {
        nv = true;
        cp.setEnabled(false);
        rew.setVisible(false);
        rew.setSelected(false);
        cv.setVisible(true);
        nsel = (String) cv.getSelectedItem();
      } else if (gp.isSelected()) {
        cp.setEnabled(true);
        rew.setSelected(true);
        rew.setVisible(true);
        cv.setVisible(false);
        nsel = (String) cp.getSelectedItem();
      }
      if (nsel != null && nsel.equals(last) && nv == lv)
        return;
      final String v;
      if (nv)
        v = nsel;
      else
        v = Main.p2v(nsel);
      if (Main.isIME(v)) {
        jl1.setVisible(false);
        jl2.setVisible(true);
        dst.setVisible(false);
        dst.setText("");
      } else {
        jl1.setVisible(true);
        jl2.setVisible(false);
        dst.setText(v + "-IME");
        dst.setVisible(true);
      }
      last = nsel;
      lv = nv;
    }
  }

  static final void show() {
    gp.addActionListener(I);
    gv.addActionListener(I);
    cp.addActionListener(I);
    cv.addActionListener(I);
    jb.addActionListener(I);
    final ButtonGroup bg = new ButtonGroup();
    bg.add(gp);
    bg.add(gv);
    jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    jf.setTitle("Minecraft IME installer");
    final GroupLayout lo = new GroupLayout(jf.getContentPane());
    lo.setHonorsVisibility(false);
    lo.setVerticalGroup(lo
        .createSequentialGroup()
        .addGroup(
            lo.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(gp)
                .addComponent(cp))
        .addComponent(rew)
        .addGroup(
            lo.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(gv)
                .addComponent(cv))
        .addGroup(
            lo.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jl1)
                .addComponent(jl2).addComponent(dst)).addComponent(jb));
    lo.setHorizontalGroup(lo
        .createParallelGroup(GroupLayout.Alignment.CENTER)
        .addGroup(
            lo.createSequentialGroup()
                .addGroup(
                    lo.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(gp)
                        .addComponent(gv).addComponent(jl1))
                .addGroup(
                    lo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(cp)
                        .addComponent(cv).addComponent(jl2).addComponent(dst))).addComponent(rew)
        .addComponent(jb));
    jf.setLayout(lo);
    jf.pack();
    jf.setVisible(true);
    I.actionPerformed(null);
  }
}
