package main;
import javax.swing.*;
import com.zouhair.*;
public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            NombresImages nombresImages = new NombresImages();
            nombresImages.setVisible(true);
        });
    }
}
