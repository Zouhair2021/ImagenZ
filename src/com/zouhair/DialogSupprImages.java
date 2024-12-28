package com.zouhair;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class DialogSupprImages extends JDialog {
    NombresImages parent;
    ArrayList<String> arrayImages;
    JCheckBox[] checkBoxes;
    JLabel[] labels;
    String[] listImages;
    JButton btnSupprimer;
    JPanel panIcones;
    JScrollPane scrollPane;

    public DialogSupprImages(NombresImages parent) {
        setTitle("Supprimer images");
        setSize(600, 400);
        this.parent = parent;
        setModal(true);
        setLocationRelativeTo(parent);
        arrayImages = NombresImages.arrayImages;

        // Création du layout principal
        setLayout(new BorderLayout());

        btnSupprimer = new JButton("Supprimer la sélection");
        add(btnSupprimer, BorderLayout.SOUTH);
        btnSupprimer.addActionListener(ee -> ecoutBtnSupprimer());

        // Initialisation des panels
        panIcones = new JPanel(new GridLayout(0, 8, 0, 0));
        scrollPane = new JScrollPane(panIcones);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        initGUI();

    }

    void initGUI() {
        // Nettoyer le panel avant de le reremplir
        panIcones.removeAll();

        listImages = arrayImages.toArray(String[]::new);
        int listImagesLength = listImages.length;
        checkBoxes = new JCheckBox[listImagesLength];
        labels = new JLabel[listImagesLength];

        for (int i = 0; i < listImagesLength; i++) {
            checkBoxes[i] = new JCheckBox();
            panIcones.add(checkBoxes[i]);

            labels[i] = new JLabel();
            ImageIcon imageIc = new ImageIcon(listImages[i]);
            Image image = imageIc.getImage();
            Image imageScaled = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(imageScaled);
            labels[i].setIcon(icon);
            panIcones.add(labels[i]);
        }

        // Forcer le rafraîchissement de l'interface
        panIcones.revalidate();
        panIcones.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    void ecoutBtnSupprimer() {
        supprimerImages();
    }

    public void supprimerImages() {
        for (int i = checkBoxes.length - 1; i >= 0; i--) {
            if (checkBoxes[i].isSelected()) {
                // Récupérer le chemin du fichier
                String cheminImage = arrayImages.get(i);

                // Créer un objet File pour le fichier image
                File fichierImage = new File(cheminImage);

                // Supprimer le fichier physiquement
                if (fichierImage.exists()) {
                    try {
                        boolean supprime = fichierImage.delete();
                        if (!supprime) {
                            System.err.println("Impossible de supprimer le fichier: " + cheminImage);
                        }
                    } catch (SecurityException e) {
                        System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
                    }
                }

                // Supprimer le chemin de l'ArrayList
                arrayImages.remove(i);
            }
        }
        initGUI();
    }
}