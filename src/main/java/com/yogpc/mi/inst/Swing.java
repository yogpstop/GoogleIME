package com.yogpc.mi.inst;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class Swing implements ActionListener {
  private static final JRadioButton gp = new JRadioButton("Profile", true);
  private static final JRadioButton gv = new JRadioButton("Version");
  private static final JComboBox<String> cp = new JComboBox<String>(Main.getProfiles());
  private static final JComboBox<String> cv = new JComboBox<String>(Main.getVersions());
  private static final JTextField dst = new JTextField(20);
  private static final JCheckBox rew = new JCheckBox("Fix profile", true);
  private static final JButton jb = new JButton("OK");
  private static final JFrame jf = new JFrame();
  private static String last = null;
  private static boolean lv = false;

  private Swing() {
    gp.addActionListener(this);
    gv.addActionListener(this);
    cp.addActionListener(this);
    cv.addActionListener(this);
    jb.addActionListener(this);
    final ButtonGroup bg = new ButtonGroup();
    bg.add(gp);
    bg.add(gv);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.setTitle("Minecraft IME installer");
    jf.setLayout(new BoxLayout(jf.getContentPane(), BoxLayout.Y_AXIS));
    jf.add(gp);
    jf.add(gv);
    jf.add(cp);
    jf.add(cv);
    final JLabel jl = new JLabel("New version name: ");
    jf.add(jl);
    jf.add(dst);
    jf.add(rew);
    jf.add(jb);
    jf.setSize(640, 360);
    jf.setVisible(true);
    actionPerformed(null);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e != null && e.getSource() == jb) {
      final String v;
      if (!lv)
        v = Main.convert(last);
      else
        v = last;
      Main.installLib();
      Main.installJson(v, dst.getText());
      if (!lv && rew.isSelected())
        Main.overrideProfile(last, dst.getText());
      jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
    } else {
      String nsel = null;
      boolean nv = false;
      if (gv.isSelected()) {
        nv = true;
        cp.setEnabled(false);
        rew.setEnabled(false);
        cv.setEnabled(true);
        nsel = (String) cv.getSelectedItem();
      } else if (gp.isSelected()) {
        cp.setEnabled(true);
        rew.setEnabled(true);
        cv.setEnabled(false);
        nsel = (String) cp.getSelectedItem();
      }
      if (nsel == null || nsel.equals(last) && nv == lv)
        return;
      dst.setText((nv ? nsel : Main.convert(nsel)) + "-IME");
      last = nsel;
      lv = nv;
    }
  }

  static final void show() {
    new Swing();
  }
}
