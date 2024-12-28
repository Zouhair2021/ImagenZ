package com.zouhair;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;

public class NombresImages extends JFrame implements ActionListener
{
    // Composants graphiques principaux
    protected Pandessin panDessin;
    protected JPanel panControle, panConteneur;
    protected DialogImages dialogImages;

    // Boutons
    public JButton btnRepresenter;
    public JButton btnChoixImage;
    public JButton btnCapturer;

    // Labels et contrôles de saisie
    public JLabel lblNombre1;
    public JLabel lblNombre2;
    public JSpinner spinNombre1;
    public JSpinner spinNombre2;
    public JCheckBox checkMultilple;

    // Fichiers et dossiers statiques
    protected static File dossierApp;
    protected static File dossierImages;
    protected static File dossierDernier;
    protected static File fichierProprietes;
    protected static File fichierListeImages;
    protected static File dernierRepertoireSauvegarde;
    protected static File dernierRepertoireChargement;
    protected static File fichierImage1;
    protected static File fichierImage2;

    // Collections
    protected static ArrayList<String> arrayImages;

    // Constantes
    public final String CHEMIN_DER_REPERT_SAUVEGARDE = "dernier repertoire de sauvegarde";
    public final String CHEMIN_DER_REPERT_CHARGEMENT = "dernier repertoire de chargement";
    public final String MULTIPLE = "multiple";
    public final String DER_IMAGE1 = "derniere image 1";
    public final String DER_IMAGE2 = "derniere image 2";

    // Variables d'état
    protected boolean bMultiple;

    public NombresImages()
    {
        setTitle("ImagenZ 1.0");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        creerFichiersEtDossiers();
        chargerProprietes();
        initGUI();
        activerDesactiverNombre2(bMultiple);
        initEcouteurs();
        dialogImages = new DialogImages(this);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                enregistrerProprietes();
            }
        });
    }

    private void initGUI()
    {
        panConteneur = new JPanel();
        panDessin = new Pandessin(fichierImage1, fichierImage2);
        panDessin.setPreferredSize(new Dimension(620, 300));
        panControle = new JPanel();
        checkMultilple = new JCheckBox("Multiple");
        lblNombre1 = new JLabel("Nombre 1");
        lblNombre2 = new JLabel("Nombre 2");
        spinNombre1 = new JSpinner(new SpinnerNumberModel(5, 1, 15, 1));
        spinNombre2 = new JSpinner(new SpinnerNumberModel(5, 1, 15, 1));
        btnRepresenter = new JButton("Représenter");
        btnChoixImage = new JButton("Choisir images");
        btnCapturer = new JButton("Capturer");
        panControle.add(checkMultilple);
        panControle.add(lblNombre1);
        panControle.add(spinNombre1);
        panControle.add(lblNombre2);
        panControle.add(spinNombre2);
        panControle.add(btnRepresenter);
        panControle.add(btnChoixImage);
        panControle.add(btnCapturer);
        add(panControle, BorderLayout.SOUTH);
        panDessin.setBackground(Color.white);
        panConteneur.add(panDessin);
        add(panConteneur);
    }

    void initEcouteurs()
    {
        checkMultilple.addActionListener(this);
        btnRepresenter.addActionListener(this);
        btnChoixImage.addActionListener(this);
        btnCapturer.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == checkMultilple)
            ecoutCheckMutiple();
        if (source == btnRepresenter)
            ecoutBtnRepresenter();
        if (source == btnChoixImage)
            ecoutBtnChoixImage();
        if (source == btnCapturer)
            ecoutBtnCapturer();
    }

    void ecoutCheckMutiple()
    {
        if (checkMultilple.isSelected())
            bMultiple = true;
        else bMultiple = false;
        activerDesactiverNombre2(bMultiple);
    }

    void ecoutBtnRepresenter()
    {
        int nombre1, nombre2;
        nombre1 = (Integer) spinNombre1.getValue();
        panDessin.setNombre1(nombre1);
        if (bMultiple)
        {
            nombre2 = (Integer) spinNombre2.getValue();
            panDessin.setNombre2(nombre2);
        }
        panDessin.repaint();


    }

    void ecoutBtnChoixImage()
    {
        dialogImages.setVisible(true);
    }

    void ecoutBtnCapturer()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer l'image");
        fileChooser.setSelectedFile(selectedFile());
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers PNG", "png"));
        fileChooser.setCurrentDirectory(dernierRepertoireSauvegarde);
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            dernierRepertoireSauvegarde = file.getParentFile();
            if (!file.getName().toLowerCase().endsWith(".png"))
                file = new File(file.getAbsolutePath() + ".png");
            BufferedImage image = new BufferedImage(panDessin.getWidth(), panDessin.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            panDessin.printAll(g2d);
            g2d.dispose();
            try
            {
                ImageIO.write(image, "png", file);
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }

            JOptionPane.showMessageDialog(this, "Image sauvegardée avec succès");
        }

    }

    File selectedFile()
    {
        String nomFichImg1 = panDessin.fichierImage1.getName(), nomFichImg2 = panDessin.fichierImage2.getName();
        String nom1, nom2;
        int nombre1 = panDessin.getNombre1(), nombre2 = panDessin.getNombre2();
        if (nombre2 == 0)
        {
            nom1 = nomFichImg1.substring(0, nomFichImg1.length() - 4);
            return new File(nombre1 + "_" + nom1);
        } else
        {
            nom1 = nomFichImg1.substring(0, nomFichImg1.length() - 4);
            nom2 = nomFichImg2.substring(0, nomFichImg2.length() - 4);
            return new File(nombre1 + "_" + nom1 + "_" + nombre2 + "_" + nom2);
        }
    }

    void creerFichiersEtDossiers()
    {
        File appDataPath = new File(System.getenv("LOCALAPPDATA"));
        String nomDossierApp = "nombresImages";
        String nomDossierImages = "images";
        dossierApp = new File(appDataPath, nomDossierApp);
        String nomFichierProprietes = "configApp.properties";
        String nomFichierListeImages = "listImages.dat";

        try
        {
            // Création des dossiers s'ils n'existent pas
            if (!dossierApp.exists())
            {
                dossierApp.mkdir();
            }

            dossierImages = new File(dossierApp, nomDossierImages);
            if (!dossierImages.exists())
            {
                dossierImages.mkdir();
            }

            fichierProprietes = new File(dossierApp, nomFichierProprietes);

            // Création du fichier propriétés s'il n'existe pas
            if (!fichierProprietes.exists())
            {
                arrayImages = copierDefaultImages();
                String[] listImages = arrayImages.toArray(String[]::new);

                Properties properties = new Properties();
                String userHome = System.getProperty("user.home");
                Path picturesDirectory = Paths.get(userHome, "Pictures");
                String picturesPathStr = picturesDirectory.toString();
                Path downloadsDirectory = Paths.get(userHome, "Downloads");
                String downloadsPathStr = downloadsDirectory.toString();
                properties.setProperty(CHEMIN_DER_REPERT_SAUVEGARDE, picturesPathStr);
                properties.setProperty(CHEMIN_DER_REPERT_CHARGEMENT, downloadsPathStr);
                properties.setProperty(MULTIPLE, "false");
                properties.setProperty(DER_IMAGE1, listImages[0]);
                properties.setProperty(DER_IMAGE2, listImages[1]);

                try (FileOutputStream out = new FileOutputStream(fichierProprietes))
                {
                    properties.store(out, "Propriétés de l'application");
                }

            }
            fichierListeImages = new File(dossierApp, nomFichierListeImages);
            if (!fichierListeImages.exists())
            {
                try (ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(fichierListeImages)))
                {
                    objOut.writeObject(arrayImages);
                }
            }
        } catch (Exception e)
        {
            System.err.println("Erreur lors de la création des fichiers/dossiers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ArrayList<String> copierDefaultImages() throws IOException
    {
        ArrayList<String> copiedFiles = new ArrayList<>();

        // Copier chaque image par défaut
        for (String nomImage : new String[]{"banane.png", "orange.png", "poire.png", "pomme.png", "abricot.png",
                "avocat.png", "pasteque.png", "peche.png", "chat.png", "cheval.png", "chevre.png",
                "chien.png", "oiseau.png", "souris.png", "ambulance.png", "avion.png",
                "bateau.png", "bus-scolaire.png", "camion.png", "moto.png",
                "velo-de-montagne.png"})
        {

            try
            {
                // Obtenir l'URL de la ressource
                URL resourceUrl = NombresImages.class.getResource("/resources/" + nomImage);
                if (resourceUrl != null)
                {
                    // Créer le fichier de destination
                    File targetFile = new File(dossierImages, nomImage);

                    // Copier le fichier
                    try (InputStream in = resourceUrl.openStream())
                    {
                        Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        copiedFiles.add(targetFile.getAbsolutePath());
                    }
                } else
                {
                    System.err.println("Ressource non trouvée: " + nomImage);
                }
            } catch (IOException e)
            {
                System.err.println("Erreur lors de la copie de " + nomImage + ": " + e.getMessage());
            }
        }

        return copiedFiles;
    }

    private static boolean isImage(String file)
    {
        String lower = file.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".gif") ||
                lower.endsWith(".bmp");
    }

    void enregistrerProprietes()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(CHEMIN_DER_REPERT_SAUVEGARDE, dernierRepertoireSauvegarde.toString());
            properties.setProperty(CHEMIN_DER_REPERT_CHARGEMENT, dernierRepertoireChargement.toString());
            properties.setProperty(MULTIPLE, bMultiple + "");
            properties.setProperty(DER_IMAGE1, panDessin.fichierImage1.toString());
            properties.setProperty(DER_IMAGE2, panDessin.fichierImage2.toString());
            FileOutputStream out = new FileOutputStream(fichierProprietes);
            properties.store(out, "Propriétés de l'application");
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(fichierListeImages));
            objOut.writeObject(arrayImages);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    void chargerProprietes()
    {
        try
        {
            FileInputStream in = new FileInputStream(fichierProprietes);
            Properties properties = new Properties();
            properties.load(in);
            dernierRepertoireSauvegarde = new File(properties.getProperty(CHEMIN_DER_REPERT_SAUVEGARDE));
            dernierRepertoireChargement = new File(properties.getProperty(CHEMIN_DER_REPERT_CHARGEMENT));
            if (properties.getProperty(MULTIPLE).equals("true"))
                bMultiple = true;
            else bMultiple = false;
            fichierImage1 = new File(properties.getProperty(DER_IMAGE1));
            fichierImage2 = new File(properties.getProperty(DER_IMAGE2));
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(fichierListeImages));
            arrayImages = (ArrayList<String>) objIn.readObject();

        } catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    void activerDesactiverNombre2(boolean bMultiple)
    {
        checkMultilple.setSelected(bMultiple);
        spinNombre2.setEnabled(bMultiple);
        if (!bMultiple)
            lblNombre2.setForeground(Color.gray.brighter());
        else lblNombre2.setForeground(Color.black);
        panDessin.setNombre1(0);
        panDessin.setNombre2(0);
        panDessin.repaint();
    }




}

