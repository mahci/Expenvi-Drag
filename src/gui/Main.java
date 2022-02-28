package gui;

import control.Server;
import gui.MainFrame;

import javax.swing.*;

public class Main {

    private static MainFrame mFrame;

    public static void main(String[] args) {
        MainFrame.get().start();
        Server.get().start();
    }

    public static void showDialog(String mssg) {
        JOptionPane.showMessageDialog(mFrame, mssg);
    }
}
