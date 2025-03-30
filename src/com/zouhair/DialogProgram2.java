package com.zouhair;

import com.util.GestFiles;
import com.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DialogProgram2 extends JDialog {
    private NombresImages parent;
    JPanel panelResult, panelControle, panelPictures;
    // Composants de panControle
    JLabel lblMessage;
    JTextField txtNumber;
    JButton btnEnter, btnCancel, btnRandomNumbers;
    String[] listPictures;
    int numberOfCaptures;
    JLabel[] lblNumbers, lblPicture1, lblPicture2;
    JButton[] btnPictures;
    private int[] listNumber1, listNumber2;
    private String[] listPictures1, listPictures2;
    private String messageOptAutoCapture;

    public DialogProgram2(NombresImages parent, int numberOfCaptures) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = ge.getMaximumWindowBounds(); // Obtient la zone maximale en excluant la barre des tâches
        setBounds(bounds);
        this.parent = parent;
        this.numberOfCaptures = numberOfCaptures;

        // Appliquer l'orientation des composants selon la langue
        applyComponentOrientation(LanguageManager.getComponentOrientation());

        messageOptAutoCapture = LanguageManager.getString("warning.existingFiles");

        initGui();
        initLists();
    }

    private void initGui() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        panelResult = new JPanel();
        panelResult.setBackground(Color.white);
        initPanelResult();

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 45;
        c.weightx = 100;
        add(panelResult, c);
        panelControle = new JPanel();
        initPanelControle();
        c.gridy = 1;
        c.weighty = 10;
        add(panelControle, c);
        panelPictures = new JPanel();
        panelPictures.setPreferredSize(new Dimension(600, 350));
        initPanelPictures();
        c.gridy = 2;
        c.weighty = 45;
        c.fill = GridBagConstraints.NONE;
        JScrollPane scrollPane = new JScrollPane(panelPictures);
        add(scrollPane, c);
    }

    private void initLists() {
        lblNumbers = new JLabel[numberOfCaptures];
        lblPicture1 = new JLabel[numberOfCaptures];
        lblPicture2 = new JLabel[numberOfCaptures];
        listNumber1 = new int[numberOfCaptures];
        listNumber2 = new int[numberOfCaptures];
        listPictures1 = new String[numberOfCaptures];
        listPictures2 = new String[numberOfCaptures];
    }

    private void initPanelControle() {
        panelControle.setLayout(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        lblMessage = new JLabel(LanguageManager.getString("label.enterNumbersOneByOne"));
        cc.anchor = GridBagConstraints.CENTER;
        cc.gridx = 0;
        cc.gridy = 0;
        cc.gridwidth = 3;
        cc.insets = new Insets(5, 5, 5, 5);
        panelControle.add(lblMessage, cc);
        txtNumber = new JTextField(10);
        txtNumber.addActionListener(e -> actionInput(e));
        cc.gridy = 1;
        panelControle.add(txtNumber, cc);
        btnEnter = new JButton(LanguageManager.getString("btn.enter", "Entrer"));
        btnEnter.addActionListener(e -> actionInput(e));
        cc.gridy = 2;
        cc.gridwidth = 1;
        panelControle.add(btnEnter, cc);

        btnRandomNumbers = new JButton(LanguageManager.getString("btn.randomImages"));
        btnRandomNumbers.addActionListener(e -> initRandomNumbers());
        cc.gridx = 1;
        panelControle.add(btnRandomNumbers, cc);
        btnCancel = new JButton(LanguageManager.getString("btn.cancel", "Annuler"));
        btnCancel.addActionListener(e -> {
            initLists();
            panelResult.removeAll();
            panelResult.repaint();
            n = 0;
            this.setVisible(false);
            this.dispose();
        });
        cc.gridx = 2;
        panelControle.add(btnCancel, cc);
    }

    private void initPanelPictures() {
        listPictures = NombresImages.arrayImages.toArray(new String[0]);
        panelPictures.setLayout(new GridLayout(listPictures.length / 10 + 1, 3, 5, 5));
        btnPictures = new JButton[listPictures.length];
        for (int i = 0; i < listPictures.length; i++) {
            // Obtenir le chemin complet de l'image
            String cheminImage = listPictures[i];

            // Extraire le nom du fichier du chemin complet
            ImageIcon icon = getIcon(cheminImage);

            btnPictures[i] = new JButton();
            btnPictures[i].setBackground(Color.white);
            btnPictures[i].setIcon(icon);
            btnPictures[i].addActionListener(e -> actionInput(e));

            // Définir le tooltip avec le nom du fichier
            btnPictures[i].setToolTipText(new File(cheminImage).getName());

            panelPictures.add(btnPictures[i]);
        }
        setBtnsEnabled(false);
    }

    private ImageIcon getIcon(String cheminImage) {
        // Créer et configurer le bouton avec l'image
        ImageIcon imageIc = new ImageIcon(cheminImage);
        Image image = imageIc.getImage();
        Image imageScaled = image.getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(imageScaled);
        return icon;
    }

    private void initPanelResult() {
        panelResult.setLayout(new GridBagLayout());
        cr = new GridBagConstraints();
        cr.insets = new Insets(5, 5, 5, 5);
        cr.gridwidth = 1;
    }

    private boolean isStart = true;
    GridBagConstraints cr;
    int n = 0;
    int posYimg = 0;
    int indexPic1 = 0, indexPic2 = 0;
    boolean isNumber2 = false;
    boolean isFirstImg = true;
    int number1, number2;

    private void initRandomNumbers() {
        String response = (String) JOptionPane.showInputDialog(this,
                "Veuillez entrez la somme maximale des deux nombres",
                "Somme maximale", JOptionPane.QUESTION_MESSAGE, null, null, 9);
        if (response != null && !response.equals("")) {
            int maxAdd = Integer.parseInt(response);
            maxAddResult(maxAdd);
        } else {
            response = (String) JOptionPane.showInputDialog(this,
                    "Veuillez entrez la valeur maximale",
                    "Valeur maximale", JOptionPane.QUESTION_MESSAGE, null, null, 9);
            if (response != null && !response.equals("")) {
                int maxValue = Integer.parseInt(response);
                maxValueResult(maxValue);
                endOfNumbrsInput();
                showRandomNumbersGenerated();
            }
        }
    }

    private void maxAddResult(int maxAdd) {
        int maxCombin = calculateNumberPssibilities(maxAdd);
        if (numberOfCaptures > maxCombin) {
            String message = "Le nombre de captures demandé dépasse le nombre des combinaisons possible.\n" +
                    "le nombre de capture sera limité à " + maxCombin;
            JOptionPane.showMessageDialog(this, message, "Limite dépassée", JOptionPane.WARNING_MESSAGE);
            numberOfCaptures = maxCombin;
            initLists();
        }
        record Intpair(int first, int seconde) {}
        ArrayList<Intpair> pairs = new ArrayList<>();
        for (int i = 1; i < maxAdd; i++) {
            int j = 1;
            while (i + j <= maxAdd) {
                pairs.add(new Intpair(i, j));
                j++;
            }
        }
        Collections.shuffle(pairs);

        Intpair[] listNumber = pairs.stream().limit(numberOfCaptures).toArray(Intpair[]::new);
        for (int i = 0; i < listNumber.length; i++) {
            Intpair currentPair = listNumber[i];
            listNumber1[i] = currentPair.first();
            listNumber2[i] = currentPair.seconde();
        }

        endOfNumbrsInput();
        showRandomNumbersGenerated();
    }

    private void maxValueResult(int maxValue) {
        record Intpair(int first, int seconde) {}
        ArrayList<Intpair> pairs = new ArrayList<>();
        for (int i = 1; i < maxValue; i++) {
            int j = 1;
            while (j < maxValue) {
                Intpair pair = new Intpair(i, j);
                if (!pairs.contains(pair))
                    pairs.add(pair);
                j++;
            }
        }
        Collections.shuffle(pairs);

        Intpair[] listNumber = pairs.stream().limit(numberOfCaptures).toArray(Intpair[]::new);
        for (int i = 0; i < listNumber.length; i++) {
            Intpair currentPair = listNumber[i];
            listNumber1[i] = currentPair.first();
            listNumber2[i] = currentPair.seconde();
        }
    }

    private static int calculateNumberPssibilities(int n) {
        int somme = 0;
        for (int i = n - 1; i >= 0; i--) {
            somme += i;
        }
        return somme;
    }

    private void endOfNumbrsInput() {
        btnEnter.setEnabled(false);
        txtNumber.setEnabled(false);
        btnCancel.setEnabled(false);
        btnRandomNumbers.setEnabled(false);
        lblMessage.setText(LanguageManager.getString("label.imageChoice"));
        lblMessage.setForeground(Color.red);
        setBtnsEnabled(true);
        n = 0;
    }

    private void actionInput(ActionEvent e) {
        Object source = e.getSource();
        if (source == txtNumber || source == btnEnter) {
            try {
                if (n < numberOfCaptures) {
                    int number = Integer.parseInt(txtNumber.getText());
                    if (number <= 0 || number > 15) {
                        JOptionPane.showMessageDialog(this,
                                LanguageManager.getString("error.numberRange"),
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (!isNumber2) {
                        lblNumbers[n] = new JLabel();

                        if (n < 5) {
                            cr.gridx = 0;
                            cr.gridy = n;
                        } else if (n < 10) {
                            cr.gridx = 3;
                            cr.gridy = n - 5;
                        } else if (n < 15) {
                            cr.gridx = 6;
                            cr.gridy = n - 10;
                        }
                        number1 = number;
                        listNumber1[n] = number1;
                        isNumber2 = true;
                        txtNumber.setText("");
                    } else {
                        number2 = number;
                        listNumber2[n] = number2;
                        lblNumbers[n].setText(number1 + " + " + number2 + " = " + (number1 + number2));
                        panelResult.add(lblNumbers[n], cr);
                        txtNumber.setText("");
                        panelResult.revalidate();
                        panelResult.repaint();
                        isNumber2 = false;
                        n++;
                        if (n == numberOfCaptures) {
                            endOfNumbrsInput();
                        }
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        LanguageManager.getString("error.enterInteger"),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                txtNumber.setText("");
            }
        } else if (source instanceof JButton) {
            for (int i = 0; i < listPictures.length; i++) {
                source = e.getSource();
                if (btnPictures[i] == source) {
                    if (n < 10) {
                        if (isFirstImg) {
                            cr.gridx = 1;
                            cr.gridy = posYimg;
                            selectImage1(i);
                        } else {
                            cr.gridx = 2;
                            posYimg++;
                            selectImage2(i);
                        }
                    } else if (n < 20) {
                        if (isFirstImg) {
                            cr.gridx = 4;
                            cr.gridy = posYimg - 5;
                            selectImage1(i);
                        } else {
                            cr.gridx = 5;
                            posYimg++;
                            selectImage2(i);
                        }
                    } else if (n < 30) {
                        if (isFirstImg) {
                            cr.gridx = 7;
                            cr.gridy = posYimg - 10;
                            selectImage1(i);
                        } else {
                            cr.gridx = 8;
                            posYimg++;
                            selectImage2(i);
                        }
                    }
                    panelResult.revalidate();
                    panelResult.repaint();
                    break;
                }
            }
            n++;
        }
    }

    private void showRandomNumbersGenerated() {
        for (int i = 0; i < numberOfCaptures; i++) {
            if (i < 5) {
                cr.gridx = 0;
                cr.gridy = i;
            } else if (i < 10) {
                cr.gridx = 3;
                cr.gridy = i - 5;
            } else if (i < 15) {
                cr.gridx = 6;
                cr.gridy = i - 10;
            }
            int n1 = listNumber1[i];
            int n2 = listNumber2[i];
            lblNumbers[i] = new JLabel(n1 + " + " + n2 + " = " + (n1 + n2));
            panelResult.add(lblNumbers[i], cr);
        }
        panelResult.repaint();
        panelResult.revalidate();

        Object[] options = {
                LanguageManager.getString("option.yes", "Oui"),
                LanguageManager.getString("option.no", "non")
        };

        int choice = JOptionPane.showOptionDialog(this,
                LanguageManager.getString("dialog.randomImagesQuestion"),
                "Images aléatoires", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            selectRandomImages();
        }
    }

    private void selectImage1(int i) {
        lblPicture1[indexPic1] = new JLabel();
        lblPicture1[indexPic1].setIcon(getIcon(listPictures[i]));
        listPictures1[indexPic1] = listPictures[i];
        panelResult.add(lblPicture1[indexPic1], cr);
        indexPic1++;
        isFirstImg = false;
    }

    private void selectImage2(int i) {
        lblPicture2[indexPic2] = new JLabel();
        lblPicture2[indexPic2].setIcon(getIcon(listPictures[i]));
        listPictures2[indexPic2] = listPictures[i];
        panelResult.add(lblPicture2[indexPic2], cr);
        indexPic2++;
        if (indexPic2 == listPictures2.length) {
            setVisible(false);
            startWorker();
        }
        isFirstImg = true;
    }

    private void selectRandomImages() {
        Random random = new Random();
        for (int i = 0; i < numberOfCaptures; i++) {
            int indexRandPic1 = random.nextInt(listPictures.length);
            listPictures1[i] = listPictures[indexRandPic1];
            int indexRandPic2 = random.nextInt(listPictures.length);
            while (indexRandPic1 == indexRandPic2) {
                indexRandPic2 = random.nextInt(listPictures.length);
            }
            listPictures2[i] = listPictures[indexRandPic2];
        }
        setVisible(false);
        startWorker();
    }

    private void setBtnsEnabled(boolean isEnabled) {
        for (int i = 0; i < btnPictures.length; i++) {
            btnPictures[i].setEnabled(isEnabled);
        }
    }

    private SwingWorker<Void, Void> worker;

    private void startWorker() {
        final boolean isToRename;
        if (Util.chooseYesNoOption(messageOptAutoCapture, parent)) {
            try {
                GestFiles.deleteFiles(parent.repertoireSauvegarde);
                isToRename = false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else isToRename = true;

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    for (int i = 0; i < listNumber1.length; i++) {
                        final int number1 = listNumber1[i];
                        final int number2 = listNumber2[i];
                        final String image1 = listPictures1[i];
                        final String image2 = listPictures2[i];
                        SwingUtilities.invokeAndWait(() -> {
                            parent.panDessin.setNombre1(number1);
                            parent.panDessin.setNombre2(number2);
                            parent.panDessin.fichierImage1 = new File(image1);
                            parent.panDessin.fichierImage2 = new File(image2);
                            parent.panDessin.repaint();
                        });

                        Thread.sleep(100);
                        String filName;
                        if (parent.fileNameType.equals("content"))
                            filName = parent.fileName();
                        else if (parent.fileNameType.equals("random"))
                            filName = parent.randomName();
                        else filName = parent.numberName();
                        if (isToRename) filName = parent.renameFile(filName);
                        final String fileName = filName;
                        SwingUtilities.invokeAndWait(() -> parent.capture(new File(parent.repertoireSauvegarde,
                                fileName)));
                    }
                } catch (Exception ex) {
                    if (!isCancelled()) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                int n = listNumber1.length;
                JOptionPane.showMessageDialog(DialogProgram2.this,
                        n + " " + LanguageManager.getString("label.imagesGenerated"));
                DialogProgram2.this.dispose();
            }
        };
        worker.execute();
    }
}