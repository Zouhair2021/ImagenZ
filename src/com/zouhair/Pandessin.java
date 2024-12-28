package com.zouhair;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Pandessin extends JPanel
{
    File fichierImage1, fichierImage2;
    final static int LARGEUR_ICONE = 120, HAUTEUR_ICONE = LARGEUR_ICONE;
    int nombre1, nombre2 = 0;

    public Pandessin(File fichierImage1, File fichierImage2)
    {
        this.fichierImage1 = fichierImage1;
        this.fichierImage2 = fichierImage2;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.translate(10, 10);
        final int origX = 0, origY = 0;
        Image image1, image2;
        Image icon1, icon2;

        try
        {
            image1 = ImageIO.read(fichierImage1);
            icon1 = image1.getScaledInstance(LARGEUR_ICONE, HAUTEUR_ICONE,
                    Image.SCALE_SMOOTH);
            image2 = ImageIO.read(fichierImage2);
            icon2 = image2.getScaledInstance(LARGEUR_ICONE, HAUTEUR_ICONE,
                    Image.SCALE_SMOOTH);

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        g.translate(origX, origY);
        for (int i = 1; i <= nombre1; i++)
        {
            g.drawImage(icon1, origX, origY, null);
            if (i % 3 != 0)
            {
                g.translate(LARGEUR_ICONE + 2, 0);
            } else
            {
                g.translate(-(2 * (LARGEUR_ICONE + 2)), HAUTEUR_ICONE + 10);
            }
        }
        int translateVerical = -nombre1 / 3 * (HAUTEUR_ICONE + 10);
        int translateHorizontal;
        if (nombre1 < 3)
            translateHorizontal = 0;
        else
            translateHorizontal = (3 - nombre1 % 3) * (LARGEUR_ICONE + 2);
        g.translate(translateHorizontal, translateVerical);
        for (int i = 1; i <= nombre2; i++)
        {
            g.drawImage(icon2, origX, origY, null);
            if (i % 3 != 0)
            {
                g.translate(LARGEUR_ICONE + 2, 0);
            } else
            {
                g.translate(-(2 * (LARGEUR_ICONE + 2)), HAUTEUR_ICONE + 10);
            }
        }

        resizePandessin();


    }

    void resizePandessin()
    {
        if (nombre2 == 0)
        {
            int largPane, hautPane;
            if (nombre1 == 1)
                largPane = LARGEUR_ICONE + 2 + 10;
            else if (nombre1 == 2)
                largPane = 2 * (LARGEUR_ICONE + 2) + 15;
            else largPane = 3 * (LARGEUR_ICONE + 2) + 15;
            hautPane = ((nombre1 - 1) / 3 + 1) * (HAUTEUR_ICONE + 10) + 10;
            SwingUtilities.invokeLater(() ->
            {
                this.setPreferredSize(new Dimension(largPane, hautPane));
                Border border = BorderFactory.createLineBorder(Color.BLACK, 2);
                border.paintBorder(this, this.getGraphics(), 0, 0, this.getWidth(),
                        this.getHeight());
                this.revalidate();
            });
        } else
        {
            int largPane1, hautPane1;
            if (nombre1 == 1)
                largPane1 = LARGEUR_ICONE + 2 + 10;
            else if (nombre1 == 2)
                largPane1 = 2 * (LARGEUR_ICONE + 2) + 15;
            else largPane1 = 3 * (LARGEUR_ICONE + 2) + 15;
            hautPane1 = ((nombre1 - 1) / 3 + 1) * (HAUTEUR_ICONE + 10) + 10;
            int largPane2, hautPane2;
            if (nombre2 == 1)
                largPane2 = LARGEUR_ICONE + 2 + 10;
            else if (nombre2 == 2)
                largPane2 = 2 * (LARGEUR_ICONE + 2) + 10;
            else largPane2 = 3 * (LARGEUR_ICONE + 2) + 10;
            hautPane2 = ((nombre2 - 1) / 3 + 1) * (HAUTEUR_ICONE + 10) + 10;
            int largePaneTotal = largPane1 + largPane2;
            int hautPaneTotal = Math.max(hautPane1, hautPane2);
            SwingUtilities.invokeLater(() ->
            {
                this.setPreferredSize(new Dimension(largePaneTotal, hautPaneTotal));
                Border border = BorderFactory.createLineBorder(Color.BLACK, 2);
                border.paintBorder(this, this.getGraphics(), 0, 0, this.getWidth(),
                        this.getHeight());
                this.revalidate();
            });


        }


    }
    public int getNombre1()
    {
        return nombre1;
    }
    public int getNombre2()
    {
        return nombre2;
    }

    public void setNombre1(int nombre1)
    {
        this.nombre1 = nombre1;

    }

    public void setNombre2(int nombre2)
    {
        this.nombre2 = nombre2;
    }

    public void setFichierImage1(File fichierImage1)
    {
        this.fichierImage1 = fichierImage1;
    }

    public void setFichierImage2(File fichierImage2)
    {
        this.fichierImage2 = fichierImage2;
    }
}

