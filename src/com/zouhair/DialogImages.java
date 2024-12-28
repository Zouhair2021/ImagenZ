package com.zouhair;
import javax.swing.*;               // Pour les composants Swing (JDialog, JPanel, JButton, etc.)
import javax.swing.filechooser.*;  // Pour FileNameExtensionFilter
import java.awt.*;                 // Pour les layouts et composants AWT (GridLayout, BorderLayout, etc.)
import java.awt.event.*;           // Pour ActionListener et ActionEvent
import java.io.File;              // Pour la manipulation des fichiers
import java.io.IOException;        // Pour la gestion des exceptions d'E/S
import java.nio.file.*;           // Pour les opérations sur les fichiers (Files, Path)

class DialogImages extends JDialog implements ActionListener {
    // Variables d'instance
    JPanel panIcones;                  // Panel contenant les icônes d'images
    String[] listImages;               // Tableau des chemins d'images
    NombresImages parent;              // Référence à la fenêtre principale
    boolean deuxImages = false;        // État de la sélection d'images
    JButton premBouton;                // Premier bouton sélectionné en mode multiple
    JButton btnAjouter;                // Bouton pour ajouter des images
    JButton btnSupprimer;              // Bouton pour supprimer des images
    JButton btnReset;                  // Bouton pour restaurer les images par défaut
    JButton[] btnIcons;                // Tableau des boutons d'images
    Dimension dimBoutons = new Dimension(200, 30);  // Dimension standard des boutons
    JScrollPane scrollPane;            // ScrollPane pour le défilement

    // Constructeur
    public DialogImages(NombresImages parent) {
        super(parent, true);           // Création d'un dialogue modal
        setTitle("Choix images");
        setSize(1000, 600);
        this.parent = parent;
        setLocationRelativeTo(parent);
        initGUI();
    }

    // Initialisation de l'interface
    void initGUI() {
        setLayout(new BorderLayout());

        // Création et configuration du panel d'icônes
        panIcones = new JPanel();
        updatePanIconesLayout();

        // Création et configuration des boutons de contrôle
        btnAjouter = new JButton("Ajouter");
        btnAjouter.setPreferredSize(dimBoutons);
        btnAjouter.addActionListener(this);

        btnSupprimer = new JButton("Supprimer");
        btnSupprimer.setPreferredSize(dimBoutons);
        btnSupprimer.addActionListener(this);

        btnReset = new JButton("Restaurer");
        btnReset.setPreferredSize(dimBoutons);
        btnReset.addActionListener(this);

        // Panel de contrôle pour les boutons
        JPanel panControle = new JPanel();
        panControle.add(btnAjouter);
        panControle.add(btnSupprimer);
        panControle.add(btnReset);
        add(panControle, BorderLayout.SOUTH);

        // Configuration du ScrollPane
        scrollPane = new JScrollPane(panIcones);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        initPanIcons();
    }

    // Mise à jour de la disposition des icônes
    void updatePanIconesLayout() {
        panIcones.setLayout(new GridLayout(parent.arrayImages.size() / 6 + 1, 3, 10, 10));
    }

    // Initialisation des icônes d'images
    void initPanIcons() {
        panIcones.removeAll();
        updatePanIconesLayout();

        btnIcons = new JButton[parent.arrayImages.size()];
        listImages = parent.arrayImages.toArray(String[]::new);

        // Création des boutons d'images
        for (int i = 0; i < btnIcons.length; i++) {
            // Obtenir le chemin complet de l'image
            String cheminImage = listImages[i];

            // Extraire le nom du fichier du chemin complet
            String nomFichier = new File(cheminImage).getName();

            // Créer et configurer le bouton avec l'image
            ImageIcon imageIc = new ImageIcon(cheminImage);
            Image image = imageIc.getImage();
            Image imageScaled = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(imageScaled);

            btnIcons[i] = new JButton();
            btnIcons[i].setIcon(icon);

            // Définir le tooltip avec le nom du fichier
            btnIcons[i].setToolTipText(nomFichier);

            panIcones.add(btnIcons[i]);
            btnIcons[i].addActionListener(this);
        }

        // Rafraîchissement de l'interface
        panIcones.revalidate();
        panIcones.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    // Gestion des événements
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        if (source == btnAjouter)
            ecoutBtnajouter();
        else if (source == btnSupprimer)
            ecoutBtnSupprimer();
        else if (source == btnReset)
            ecoutBtnReset();
        else
            ecoutBtnIcone(e);
    }

    // Gestion du clic sur une icône d'image
    void ecoutBtnIcone(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        if (!parent.bMultiple) {
            // Mode simple sélection
            for (int i = 0; i < btnIcons.length; i++) {
                if (source == btnIcons[i]) {
                    parent.panDessin.setFichierImage1(new File(listImages[i]));
                    break;
                }
            }
            setVisible(false);
        } else {
            // Mode double sélection
            if (!deuxImages) {
                // Sélection de la première image
                for (int i = 0; i < btnIcons.length; i++) {
                    if (source == btnIcons[i]) {
                        parent.panDessin.setFichierImage1(new File(listImages[i]));
                        source.setEnabled(false);
                        deuxImages = true;
                        premBouton = source;
                        break;
                    }
                }
            } else {
                // Sélection de la deuxième image
                for (int i = 0; i < btnIcons.length; i++) {
                    if (source == btnIcons[i]) {
                        parent.panDessin.setFichierImage2(new File(listImages[i]));
                        premBouton.setEnabled(true);
                        deuxImages = false;
                        break;
                    }
                }
                setVisible(false);
            }
        }
    }

    // Ajout de nouvelles images
    void ecoutBtnajouter() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png", "gif", "bmp"
        );
        chooser.setCurrentDirectory(parent.dernierRepertoireChargement);
        chooser.setFileFilter(filter);
        int resultat = chooser.showOpenDialog(this);

        if (resultat == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            parent.dernierRepertoireChargement = selectedFile.getParentFile();
            File[] fichiers = chooser.getSelectedFiles();

            for (File fichier : fichiers) {
                Path source = fichier.toPath();
                Path cible = NombresImages.dossierImages.toPath().resolve(fichier.getName());

                // Obtenir le nom du fichier sans extension
                String nomSansExtension = getNomSansExtension(fichier.getName());

                // Vérifier si un fichier avec le même nom (sans extension) existe déjà dans l'ArrayList
                boolean fichierExiste = parent.arrayImages.stream()
                        .map(Path::of)
                        .map(Path::getFileName)
                        .map(p -> getNomSansExtension(p.toString()))
                        .anyMatch(nom -> nom.equals(nomSansExtension));

                if (fichierExiste) {
                    // Options personnalisées pour les boutons
                    Object[] options = {"Remplacer", "Ignorer"};

                    // Demander à l'utilisateur s'il veut remplacer le fichier
                    int choix = JOptionPane.showOptionDialog(
                            this,
                            "Un fichier nommé '" + nomSansExtension + "' existe déjà. Que souhaitez-vous faire ?",
                            "Fichier existant",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]  // Option par défaut : Ignorer
                    );

                    if (choix != 0) {  // 0 correspond à "Remplacer"
                        // L'utilisateur a choisi d'ignorer le fichier
                        continue;
                    }

                    // Si le fichier existe déjà dans l'ArrayList, le supprimer
                    String fichierASupprimer = parent.arrayImages.stream()
                            .filter(path -> getNomSansExtension(Path.of(path).getFileName().toString())
                                    .equals(nomSansExtension))
                            .findFirst()
                            .orElse(null);

                    if (fichierASupprimer != null) {
                        parent.arrayImages.remove(fichierASupprimer);
                    }
                }

                try {
                    // Copier le nouveau fichier
                    Files.copy(source, cible, StandardCopyOption.REPLACE_EXISTING);
                    parent.arrayImages.add(cible.toAbsolutePath().toString());
                } catch (IOException e) {
                    System.err.println("Erreur lors de la copie de " + fichier.getName());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Erreur lors de la copie du fichier '" + fichier.getName() + "'",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            initPanIcons();
        }
    }

    // Méthode utilitaire pour obtenir le nom du fichier sans extension
    private String getNomSansExtension(String nomFichier) {
        int lastDot = nomFichier.lastIndexOf('.');
        return lastDot > 0 ? nomFichier.substring(0, lastDot) : nomFichier;
    }
    // Suppression d'images
    void ecoutBtnSupprimer() {
        DialogSupprImages dialogSupprImages = new DialogSupprImages(parent);
        dialogSupprImages.setVisible(true);
        initPanIcons();
    }

    // Restauration des images par défaut
    void ecoutBtnReset() {
        Object[] options = {"Oui", "Non"};
        int choix = JOptionPane.showOptionDialog(this,
                "Attention ! Les images que vous avez ajoutées seront perdues.\nVoulez-vous vraiment restaurer les images par défaut ?",
                "Avertissement",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]); // Option "Non" par défaut

        if (choix == JOptionPane.YES_OPTION) {
            try {
                // Supprimer physiquement les fichiers actuels
                for (String cheminImage : parent.arrayImages) {
                    File fichierImage = new File(cheminImage);
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
                }

                // Vider la liste et restaurer les images par défaut
                parent.arrayImages.clear();
                parent.arrayImages.addAll(NombresImages.copierDefaultImages());
                initPanIcons();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la restauration des images par défaut : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}