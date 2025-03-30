package com.zouhair;

import com.util.GestFiles;
import com.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DialogProgram1 extends JDialog implements ActionListener
{
    private JLabel lblPlage, lblImagesChoice, lblStart, lblEnd, lblImgNSelected;
    private JSpinner spinStart, spinEnd;
    private JPanel panelImages;
    private NombresImages parent;
    private JButton btnRandomImg, btnReset;
    private JButton[] btnImages;
    private String[] listImages, listImagesSelect;
    boolean isStart = true;
    int i = 0;
    int start, end;
    private String messageOptAutoCapture;
    private SwingWorker<Void, Void> worker;
    private Paint btnBackround;

    public DialogProgram1(NombresImages parent)
    {
        setTitle(LanguageManager.getString("dialog.programGeneration"));
        setSize(700, 700);
        this.parent = parent;
        setLocationRelativeTo(parent);

        // Appliquer l'orientation des composants selon la langue
        applyComponentOrientation(LanguageManager.getComponentOrientation());

        messageOptAutoCapture = LanguageManager.getString("warning.existingFiles");

        initGui();
    }

    private void initGui()
    {
        lblPlage = new JLabel(LanguageManager.getString("label.range"));
        lblStart = new JLabel(LanguageManager.getString("label.start"));
        lblEnd = new JLabel(LanguageManager.getString("label.end"));
        SpinnerNumberModel spinnerNumberModel1 = new SpinnerNumberModel(1, 1, 15, 1);
        SpinnerNumberModel spinnerNumberModel2 = new SpinnerNumberModel(5, 1, 15, 1);
        spinStart = new JSpinner(spinnerNumberModel1);
        spinEnd = new JSpinner(spinnerNumberModel2);
        lblImagesChoice = new JLabel(LanguageManager.getString("label.imageChoice"));
        lblImgNSelected = new JLabel("0/0");
        btnRandomImg = new JButton(LanguageManager.getString("btn.randomImages"));
        btnBackround = btnRandomImg.getBackground();
        btnRandomImg.addActionListener(e ->
        {
            start = (int) spinStart.getValue();
            end = (int) spinEnd.getValue();
            listImagesSelect = new String[end - start + 1];
            List<String> listeB = Arrays.asList(listImages);
            Collections.shuffle(listeB);
            listImagesSelect = listeB.stream()
                    .limit(listImagesSelect.length)
                    .toArray(String[]::new);
            startWorker();
        });
        panelImages = new JPanel();
        btnReset = new JButton(LanguageManager.getString("btn.reset"));
        btnReset.addActionListener(e -> {
            listImagesSelect = new String[0];
            lblImgNSelected.setText("0/0");
            for (JButton button : btnImages)
                button.setBackground(Color.white);
            parent.captureNumber = 0;
            i = 0;
            isStart = true;
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Configuration générale
        gbc.insets = new Insets(5, 5, 5, 5); // Marges
        gbc.anchor = GridBagConstraints.WEST; // Alignement à gauche

        // Adapter l'ancrage selon l'orientation du texte
        if (LanguageManager.isRTL()) {
            gbc.anchor = GridBagConstraints.EAST;
        }

        // lblPlage
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblPlage, gbc);

        // lblStart et lblEnd
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(lblStart, gbc);

        gbc.gridx = 2;
        add(lblEnd, gbc);

        // txtStart et txtEnd
        gbc.gridy = 2;
        gbc.gridx = 0;// Étirement horizontal
        add(spinStart, gbc);

        gbc.gridx = 2;
        add(spinEnd, gbc);

        // lblImagesChoice
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        add(lblImagesChoice, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        add(lblImgNSelected, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.anchor = LanguageManager.isRTL() ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        add(btnRandomImg, gbc);
        JScrollPane scrollPane = new JScrollPane(panelImages);
        //panelImages
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        add(scrollPane, gbc);
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnReset, gbc);
        initPanelImages();
    }

    void updatePanelImagesLayout()
    {
        panelImages.setLayout(new GridLayout(parent.arrayImages.size() / 6 + 1, 3, 10, 10));
    }

    void initPanelImages()
    {
        updatePanelImagesLayout();

        btnImages = new JButton[parent.arrayImages.size()];
        listImages = parent.arrayImages.toArray(String[]::new);

        // Création des boutons d'images
        for (int i = 0; i < btnImages.length; i++)
        {
            // Obtenir le chemin complet de l'image
            String cheminImage = listImages[i];

            // Extraire le nom du fichier du chemin complet
            String nomFichier = new File(cheminImage).getName();

            // Créer et configurer le bouton avec l'image
            ImageIcon imageIc = new ImageIcon(cheminImage);
            Image image = imageIc.getImage();
            Image imageScaled = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(imageScaled);

            btnImages[i] = new JButton();
            btnImages[i].setBackground(Color.white);
            btnImages[i].setIcon(icon);

            // Définir le tooltip avec le nom du fichier
            btnImages[i].setToolTipText(nomFichier);

            panelImages.add(btnImages[i]);
            btnImages[i].addActionListener(this);
        }

        // Rafraîchissement de l'interface
        panelImages.revalidate();
        panelImages.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source instanceof JButton)
        {
            if (isStart)
            {
                start = (int) spinStart.getValue();
                end = (int) spinEnd.getValue();
                listImagesSelect = new String[end - start + 1];
                isStart = false;
            }

            for (int j = 0; j < listImages.length; j++)
            {
                if (source == btnImages[j])
                {
                    listImagesSelect[i] = listImages[j];
                    btnImages[j].setBackground(Color.gray.brighter());
                    i++;
                    if (i == listImagesSelect.length)
                    {
                        i = 0;
                        isStart = true;
                        setVisible(false);
                        startWorker();
                        dispose();
                    }
                    break;
                }
            }
            lblImgNSelected.setText((i) + "/" + listImagesSelect.length);
        }
    }

    private void startWorker()
    {
        final boolean isToRename;
        if (Util.chooseYesNoOption(messageOptAutoCapture, parent))
        {
            try
            {
                GestFiles.deleteFiles(parent.repertoireSauvegarde);
                isToRename = false;
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        } else isToRename = true;

        worker = new SwingWorker<>()
        {
            @Override
            protected Void doInBackground()
            {
                try
                {
                    int firstNumber = (int) spinStart.getValue();
                    for (int i = 0; i < listImagesSelect.length; i++)
                    {
                        final int number = firstNumber;
                        SwingUtilities.invokeAndWait(() -> parent.panDessin.setNombre1(number));
                        parent.panDessin.fichierImage1 = new File(listImagesSelect[i]);
                        parent.panDessin.repaint();
                        Thread.sleep(100);
                        String filName;
                        if (parent.fileNameType.equals("content"))
                            filName = parent.fileName();
                        else if (parent.fileNameType.equals("random"))
                            filName = parent.randomName();
                        else filName = parent.numberName();
                        if(isToRename) filName = parent.renameFile(filName);
                        final String fileName = filName;
                        SwingUtilities.invokeAndWait(() -> parent.capture(new File(parent.repertoireSauvegarde,
                                fileName)));
                        firstNumber++;
                    }
                } catch (Exception ex)
                {
                    if (!isCancelled())
                    {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done()
            {
                int n = end - start + 1;
                JOptionPane.showMessageDialog(DialogProgram1.this,
                        n + " " + LanguageManager.getString("label.imagesGenerated"));
                DialogProgram1.this.dispose();
            }
        };
        worker.execute();
    }
}