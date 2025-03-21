package io.github.tkjonesy.utils;

import javax.swing.*;

/**
 * DialogManager is a utility class for displaying various types of dialog boxes.
 */
public class DialogManager {

    /**
     * Displays an error dialog with the specified message.
     * @param message Message to be displayed in the dialog
     */
    public static void displayErrorDialog(String message){
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a fatal error dialog with the specified message and exits the program.
     * @param message Message to be displayed in the dialog
     */
    public static void displayErrorDialogFatal(String message){
        JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /**
     * Displays a confirmation dialog with the specified message.
     * @param message Message to be displayed in the dialog
     */
    public static void displayInfoDialog(String message){
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a warning dialog with the specified message.
     * @param message Message to be displayed in the dialog
     */
    public static void displayWarningDialog(String message){
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

}
