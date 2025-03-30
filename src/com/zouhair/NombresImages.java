package com.zouhair;

import com.util.GestFiles;
import com.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.prefs.Preferences;

public class NombresImages extends JFrame implements ActionListener
{
    // Composants graphiques principaux
    protected Pandessin panDessin;
    protected JPanel panControle, panConteneur;
    protected DialogImages dialogImages;

    // Boutons
    private JButton btnRepresenter;
    private JButton btnChoixImage;
    private JButton btnCapturer;
    private JButton btnOpenFolder;
    private JButton btnPrgram;

    // Labels et contrôles de saisie
    private JLabel lblNombre1;
    private JLabel lblNombre2;
    private JLabel lblCaptureNumber;
    private JSpinner spinNombre1;
    private JSpinner spinNombre2;
    private JCheckBox checkMultilple;

    // Fichiers et dossiers statiques
    protected static File dossierApp;
    protected static File dossierImages;
    protected static File dossierDernier;
    protected static File fichierProprietes;
    protected static File fichierListeImages;
    protected File repertoireSauvegarde;
    protected static File dernierRepertoireChargement;
    protected static File fichierImage1;
    protected static File fichierImage2;

    // Collections
    protected static ArrayList<String> arrayImages;
    private ArrayList<Integer> arrayNumbers = new ArrayList<>();

    // Constantes
    private final String CHEMIN_DER_REPERT_CHARGEMENT = "dernier repertoire de chargement";
    private final String MULTIPLE = "multiple";
    private final String DER_IMAGE1 = "derniere image 1";
    private final String DER_IMAGE2 = "derniere image 2";
    private final String NAME_TYPE = "Name Type";
    protected String fileNameType;
    private final String LIST_IMAGES = "image list";
    private String messageOptAutoCapture;

    // Variables d'état
    protected boolean bMultiple;
    protected int captureNumber = 0;
    Preferences prefs = Preferences.userNodeForPackage(NombresImages.class);
    boolean bFullScreen = false;

    // Menu de langue
    private JMenu menuLanguage;
    private JMenuItem itemFrench, itemEnglish, itemArabic;

    public NombresImages()
    {
        // Initialisation du gestionnaire de langue
        LanguageManager.initialize();
        messageOptAutoCapture = LanguageManager.getString("warning.existingFiles");

        try {
            URL iconURL = NombresImages.class.getResource("/resources/icones/iconImagen.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            } else {
                System.err.println("L'icône n'a pas pu être chargée.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'icône: " + e.getMessage());
        }

        createImageRepert();
        loadPreference();
        setTitle(LanguageManager.getString("app.title"));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Définir l'orientation des composants selon la langue
        applyComponentOrientation(LanguageManager.getComponentOrientation());

        initGUI();
        createImageRepert();
        initMenuBar();
        activerDesactiverNombre2(bMultiple);
        initEcouteurs();
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                savePreference();
            }
        });

        String saveFolderName = "ImagenZ";
        Path saveFolderPath = Paths.get(System.getProperty("user.home"), "Pictures", saveFolderName);
        repertoireSauvegarde = GestFiles.createFolder(saveFolderPath);
    }

    private void savePreference()
    {
        prefs.putBoolean(MULTIPLE, bMultiple);
        prefs.put(CHEMIN_DER_REPERT_CHARGEMENT, dernierRepertoireChargement.toString());
        prefs.put(DER_IMAGE1, fichierImage1.toString());
        prefs.put(DER_IMAGE2, fichierImage2.toString());
        String listJoined = String.join(",", arrayImages);
        prefs.put(LIST_IMAGES, listJoined);
        prefs.put(NAME_TYPE, fileNameType);
    }

    private void loadPreference()
    {
        bMultiple = prefs.getBoolean(MULTIPLE, false);
        String userHome = System.getProperty("user.home");
        Path downloadsDirectory = Paths.get(userHome, "Downloads");
        String downloadsPathStr = downloadsDirectory.toString();
        dernierRepertoireChargement = new File(prefs.get(CHEMIN_DER_REPERT_CHARGEMENT, downloadsPathStr));
        String listDefaulJoined = String.join(",", copierDefaultImages());
        String listJoined = prefs.get(LIST_IMAGES, listDefaulJoined);
        arrayImages = new ArrayList<>(Arrays.asList(listJoined.split(",")));
        String image1 = prefs.get(DER_IMAGE1, arrayImages.get(0));
        fichierImage1 = new File(image1);
        String image2 = prefs.get(DER_IMAGE2, arrayImages.get(1));
        fichierImage2 = new File(image2);
        fileNameType = prefs.get(NAME_TYPE, "content");
    }

    private void initMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        // Menu Option
        JMenu menuOption = new JMenu(LanguageManager.getString("menu.option"));

        // Menu Affichage
        JMenu menuDisplay = new JMenu(LanguageManager.getString("menu.display"));
        JMenuItem itemFullScreen = new JMenuItem(LanguageManager.getString("menu.fullscreen"));
        menuDisplay.add(itemFullScreen);
        itemFullScreen.addActionListener(ee -> {
            if (!bFullScreen) {
                // On cache la fenêtre avant de modifier
                setVisible(false);
                dispose();
                setUndecorated(true);
                setVisible(true);
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
                itemFullScreen.setText(LanguageManager.getString("menu.restore"));
                bFullScreen = true;
            } else {
                // Pour quitter le mode plein écran
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                setVisible(false);
                dispose();
                setUndecorated(false);
                setVisible(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                itemFullScreen.setText(LanguageManager.getString("menu.fullscreen"));
                bFullScreen = false;
            }
        });

        // Menu Noms des fichiers
        JMenu menuFilesName = new JMenu(LanguageManager.getString("menu.filenames"));
        JRadioButtonMenuItem itemContent = new JRadioButtonMenuItem(LanguageManager.getString("menu.filenames.content"));
        JRadioButtonMenuItem itemRandNumber = new JRadioButtonMenuItem(LanguageManager.getString("menu.filenames.random"));
        JRadioButtonMenuItem itemNumber = new JRadioButtonMenuItem(LanguageManager.getString("menu.filenames.number"));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(itemContent);
        buttonGroup.add(itemNumber);
        buttonGroup.add(itemRandNumber);

        if(fileNameType.equals("content"))
            itemContent.setSelected(true);
        else if(fileNameType.equals("number"))
            itemNumber.setSelected(true);
        else if(fileNameType.equals("random"))
            itemRandNumber.setSelected(true);

        itemContent.addActionListener(e -> fileNameType = "content");
        itemRandNumber.addActionListener(e -> fileNameType = "random");
        itemNumber.addActionListener(e -> fileNameType = "number");

        menuFilesName.add(itemContent);
        menuFilesName.add(itemNumber);
        menuFilesName.add(itemRandNumber);
        menuOption.add(menuFilesName);

        // Menu Langue
        menuLanguage = new JMenu(LanguageManager.getString("menu.language"));
        itemFrench = new JMenuItem("Français");
        itemEnglish = new JMenuItem("English");
        itemArabic = new JMenuItem("العربية");

        // Sélectionner la langue actuelle
        switch (LanguageManager.getCurrentLocale().getLanguage()) {
            case "fr":
                itemFrench.setFont(itemFrench.getFont().deriveFont(Font.BOLD));
                break;
            case "en":
                itemEnglish.setFont(itemEnglish.getFont().deriveFont(Font.BOLD));
                break;
            case "ar":
                itemArabic.setFont(itemArabic.getFont().deriveFont(Font.BOLD));
                break;
        }

        // Écouteurs pour changer de langue
        itemFrench.addActionListener(e -> changeLanguage(LanguageManager.LOCALE_FR));
        itemEnglish.addActionListener(e -> changeLanguage(LanguageManager.LOCALE_EN));
        itemArabic.addActionListener(e -> changeLanguage(LanguageManager.LOCALE_AR));

        menuLanguage.add(itemFrench);
        menuLanguage.add(itemEnglish);
        menuLanguage.add(itemArabic);

        menuBar.add(menuOption);
        menuBar.add(menuDisplay);
        menuBar.add(menuLanguage);
        setJMenuBar(menuBar);
    }

    /**
     * Change la langue de l'application et met à jour l'interface
     */
    private void changeLanguage(Locale locale) {
        if (LanguageManager.changeLanguage(locale)) {
            // Mise à jour de l'orientation pour l'arabe (RTL)
            applyComponentOrientation(LanguageManager.getComponentOrientation());

            // Mettre à jour les textes
            updateTexts();

            // Rafraîchir l'interface
            SwingUtilities.updateComponentTreeUI(this);

            // Mettre à jour la variable pour les messages
            messageOptAutoCapture = LanguageManager.getString("warning.existingFiles");
        }
    }

    /**
     * Met à jour tous les textes de l'interface en fonction de la langue actuelle
     */
    private void updateTexts() {
        // Titre de la fenêtre
        setTitle(LanguageManager.getString("app.title"));

        // Menu
        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null && menuBar.getMenuCount() >= 3) {
            menuBar.getMenu(0).setText(LanguageManager.getString("menu.option"));
            menuBar.getMenu(1).setText(LanguageManager.getString("menu.display"));
            menuBar.getMenu(2).setText(LanguageManager.getString("menu.language"));

            // Sous-menus
            JMenu menuOption = menuBar.getMenu(0);
            for (int i = 0; i < menuOption.getItemCount(); i++) {
                if (menuOption.getItem(i) instanceof JMenu) {
                    JMenu submenu = (JMenu) menuOption.getItem(i);
                    if (submenu != null) {
                        submenu.setText(LanguageManager.getString("menu.filenames"));

                        // Options du sous-menu
                        for (int j = 0; j < submenu.getItemCount(); j++) {
                            JMenuItem item = submenu.getItem(j);
                            if (item != null) {
                                if (j == 0) item.setText(LanguageManager.getString("menu.filenames.content"));
                                else if (j == 1) item.setText(LanguageManager.getString("menu.filenames.number"));
                                else if (j == 2) item.setText(LanguageManager.getString("menu.filenames.random"));
                            }
                        }
                    }
                }
            }

            // Menu Affichage
            JMenu menuDisplay = menuBar.getMenu(1);
            if (menuDisplay.getItemCount() > 0) {
                JMenuItem fullscreenItem = menuDisplay.getItem(0);
                if (fullscreenItem != null) {
                    if (bFullScreen) {
                        fullscreenItem.setText(LanguageManager.getString("menu.restore"));
                    } else {
                        fullscreenItem.setText(LanguageManager.getString("menu.fullscreen"));
                    }
                }
            }
        }

        // Composants de l'interface
        checkMultilple.setText(LanguageManager.getString("check.multiple"));
        lblNombre1.setText(LanguageManager.getString("label.number1"));
        lblNombre2.setText(LanguageManager.getString("label.number2"));
        btnRepresenter.setText(LanguageManager.getString("btn.represent"));
        btnChoixImage.setText(LanguageManager.getString("btn.chooseImages"));
        btnCapturer.setText(LanguageManager.getString("btn.capture"));
        btnPrgram.setText(LanguageManager.getString("btn.program"));
        btnOpenFolder.setText(LanguageManager.getString("btn.openFolder"));

        // Mettre en gras la langue actuelle dans le menu
        itemFrench.setFont(itemFrench.getFont().deriveFont(Font.PLAIN));
        itemEnglish.setFont(itemEnglish.getFont().deriveFont(Font.PLAIN));
        itemArabic.setFont(itemArabic.getFont().deriveFont(Font.PLAIN));

        switch (LanguageManager.getCurrentLocale().getLanguage()) {
            case "fr":
                itemFrench.setFont(itemFrench.getFont().deriveFont(Font.BOLD));
                break;
            case "en":
                itemEnglish.setFont(itemEnglish.getFont().deriveFont(Font.BOLD));
                break;
            case "ar":
                itemArabic.setFont(itemArabic.getFont().deriveFont(Font.BOLD));
                break;
        }
    }

    private void initGUI()
    {
        panConteneur = new JPanel();
        panDessin = new Pandessin(fichierImage1, fichierImage2);
        panDessin.setPreferredSize(new Dimension(620, 300));
        panControle = new JPanel();
        checkMultilple = new JCheckBox(LanguageManager.getString("check.multiple"));
        lblNombre1 = new JLabel(LanguageManager.getString("label.number1"));
        lblNombre2 = new JLabel(LanguageManager.getString("label.number2"));
        lblCaptureNumber = new JLabel();
        spinNombre1 = new JSpinner(new SpinnerNumberModel(5, 1, 15, 1));
        spinNombre2 = new JSpinner(new SpinnerNumberModel(5, 1, 15, 1));
        btnRepresenter = new JButton(LanguageManager.getString("btn.represent"));
        btnChoixImage = new JButton(LanguageManager.getString("btn.chooseImages"));
        btnCapturer = new JButton(LanguageManager.getString("btn.capture"));
        btnPrgram = new JButton(LanguageManager.getString("btn.program"));
        btnOpenFolder = new JButton(LanguageManager.getString("btn.openFolder"));
        btnOpenFolder.addActionListener(e ->
        {
            try
            {
                GestFiles.showFolder(repertoireSauvegarde);
            } catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });

        panControle.add(checkMultilple);
        panControle.add(lblNombre1);
        panControle.add(spinNombre1);
        panControle.add(lblNombre2);
        panControle.add(spinNombre2);
        panControle.add(btnRepresenter);
        panControle.add(btnChoixImage);
        panControle.add(btnCapturer);
        panControle.add(btnPrgram);
        panControle.add(btnOpenFolder);
        panControle.add(lblCaptureNumber);
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
        btnPrgram.addActionListener(this);
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
        if (source == btnPrgram)
            ecoutBtnProgram();
    }

    void ecoutCheckMutiple()
    {
        if (checkMultilple.isSelected())
            bMultiple = true;
        else bMultiple = false;
        activerDesactiverNombre2(bMultiple);
    }

    private void ecoutBtnProgram()
    {
        if (bMultiple)
        {
            launchProgram2();
        } else new DialogProgram1(this).setVisible(true);
    }

    private void launchProgram2()
    {
        while (true)
        {
            try
            {
                String reponse = (String) JOptionPane.showInputDialog(this,
                        LanguageManager.getString("dialog.enterCaptureCount"),
                        "Saisie du nombre de captures", JOptionPane.QUESTION_MESSAGE, null, null, 10);
                if(reponse == null)
                    break;
                int nombre = Integer.parseInt(reponse);
                if (nombre < 2)
                {
                    JOptionPane.showMessageDialog(this, LanguageManager.getString("error.numberRange"));
                    System.out.println("Nombre saisi : " + nombre);
                    continue;
                }
                DialogProgram2 dialogProgram2 = new DialogProgram2(this, nombre);
                dialogProgram2.setVisible(true);
                break;
            } catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(null,
                        LanguageManager.getString("error.enterInteger"),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
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
        dialogImages = new DialogImages(this);
        dialogImages.setVisible(true);
    }

    boolean isToRename = false;

    void ecoutBtnCapturer()
    {
        String fileName;
        if (fileNameType.equals("content"))
        {
            fileName = fileName();
        } else if (fileNameType.equals("random"))
        {
            fileName = randomName();
        } else fileName = numberName();
        if (captureNumber == 0)
        {
            if (Util.chooseYesNoOption(messageOptAutoCapture, this))
            {
                try
                {
                    GestFiles.deleteFiles(repertoireSauvegarde);
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            } else isToRename = true;
        }
        if (isToRename)
            fileName = renameFile(fileName);
        File captureFile = new File(repertoireSauvegarde, fileName);
        capture(captureFile);
    }

    protected String fileName()
    {
        String nomFichImg1 = panDessin.fichierImage1.getName(), nomFichImg2 = panDessin.fichierImage2.getName();
        String nom1, nom2;
        int nombre1 = panDessin.getNombre1(), nombre2 = panDessin.getNombre2();
        if (nombre2 == 0)
        {
            nom1 = nomFichImg1.substring(0, nomFichImg1.length() - 4);
            return (nombre1 + "_" + nom1);
        } else
        {
            nom1 = nomFichImg1.substring(0, nomFichImg1.length() - 4);
            nom2 = nomFichImg2.substring(0, nomFichImg2.length() - 4);
            return (nombre1 + "_" + nom1 + "_" + nombre2 + "_" + nom2);
        }
    }

    protected String randomName()
    {
        Random random = new Random();
        int number;
        do
        {
            number = random.nextInt(10, 100);
        }
        while (arrayNumbers.contains(number));
        arrayNumbers.add(number);
        return String.valueOf(number);
    }

    int number = 9;

    protected String numberName()
    {
        number++;
        return String.valueOf(number);
    }

    protected void capture(File file)
    {
        if (!file.getName().toLowerCase().endsWith(".png"))
            file = new File(file.getAbsolutePath() + ".png");
        BufferedImage image = new BufferedImage(panDessin.getWidth(), panDessin.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        panDessin.printAll(g2d);
        g2d.dispose();
        try
        {
            captureNumber++;
            ImageIO.write(image, "png", file);
            lblCaptureNumber.setText(captureNumber + " " + LanguageManager.getString("label.imagesGenerated"));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void createImageRepert()
    {
        File appDataPath = new File(System.getenv("LOCALAPPDATA"));
        String nomDossierApp = "ImagenZ";
        String nomDossierImages = "images";
        dossierApp = new File(appDataPath, nomDossierApp);
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
                arrayImages = copierDefaultImages();
                for (String a : arrayImages)
                {
                    System.out.println(a);
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> copierDefaultImages()
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

    protected String renameFile(String fileName)
    {
        File filePath;
        String nameFile = fileName;
        int i = 1;
        do
        {
            fileName = nameFile + "(" + i + ")" + ".png";
            filePath = new File(repertoireSauvegarde, fileName);
            i++;
        } while (filePath.exists());
        return filePath.getName();
    }
}