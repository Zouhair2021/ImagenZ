import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

public class SelectDialogController {

    @FXML
    private GridPane selectPan;

    private HBox hBox;
    private TextField txtN1, txtN2;
    private Label lblPlus, lblImage1, lblImage2;
    @FXML
    private ComboBox<String> cmbCategories;
    @FXML
    private GridPane gridImages;

    private  InfosInput infos;
    private  File imagesFolder;
    private  HashMap<String, ArrayList<String>> categories;

    private final ArrayList<File> toutesImages = new ArrayList<>();
    private final ArrayList<Label> labelsImage1 = new ArrayList<>();
    private final ArrayList<Label> labelsImage2 = new ArrayList<>();
    private final Random rnd = new Random();
    private int index = 0;
    private static final int NB_COL_IMAGES = 10;
    private static final int THUMB_W = 96;
    private static final int THUMB_H = 96;
    private static final String PREF_LAST_CATEGORY = "last_category_dialogimages";
    private final Preferences prefs = Preferences.userNodeForPackage(DialogImages.class);

    private int numberOfCaptures;
    private int[] listNumber1, listNumber2;
    private String[] listPictures1, listPictures2;
    private int clickCount = 0;   // combien d’images déjà affectées aux labels


    // ====== CONSTRUCTEUR AVEC PARAMÈTRES (obligatoire pour toi) ======
    public void setup(InfosInput infos,
                      File imagesFolder,
                      HashMap<String, ArrayList<String>> categories) {

        this.infos = infos;
        this.imagesFolder = imagesFolder;
        this.categories = categories;

        // À partir de maintenant, on PEUT utiliser infos, imagesFolder, categories
        numberOfCaptures = infos.getCapturesNumber();

        initLists();
        initRandomPictures();
        initRandomNumbers();

        remplirCombo();
        restaurerDerniereCategorie();
        drawNumbers();

        System.out.println(">>> setup terminé, selectPan children = " + selectPan.getChildren().size());
    }

    // ====== INITIALIZE (appelé après injection de selectPan) ======
    @FXML
    private void initialize() {
        System.out.println(">>> initialize SelectDialogController");
        System.out.println("selectPan = " + selectPan);
        System.out.println("cmbCategories = " + cmbCategories);
        System.out.println("gridImages = " + gridImages);

        // ICI : seulement le câblage des listeners qui ne dépendent PAS de infos

        cmbCategories.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && categories != null) {
                        // categories peut encore être null ici si setup() pas encore appelé
                        afficherImagesPourCategorie(newVal);
                    } else if (gridImages != null) {
                        gridImages.getChildren().clear();
                    }
                });

        selectPan.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
                    if (evt.getCode() == KeyCode.ENTER) {
                        onValidate();   // appelle ta méthode de validation
                        evt.consume();  // empêche le bouton image de recevoir Enter
                    }
                });
            }
        });
    }


    private void initLists() {
        listNumber1 = new int[numberOfCaptures];
        listNumber2 = new int[numberOfCaptures];
        listPictures1 = new String[numberOfCaptures];
        listPictures2 = new String[numberOfCaptures];
    }

    private void initRandomNumbers() {
        // Vérifier les paramètres de génération
        if (infos.isSimple()) {
            // ➜ mode simple : intervalle [min .. max]
            withSimpleGenerate(infos.getMinSimpleValue(), infos.getMaxSimpleValue());
        } else if (infos.getMaxSumValue() != 0) {
            withMaxSumGenerate(infos.getMaxSumValue());
        } else if (infos.getMaxNumberValue() != 0) {
            withMaxNumberGenerate(infos.getMaxNumberValue());
        }
        //endOfNumbersInput();
        //showRandomNumbersGenerated();
    }


    // === Génération avec somme maximale ===
    private void withMaxSumGenerate(int maxSum) {

        record IntPair(int first, int second) {}

        ArrayList<IntPair> pairs = new ArrayList<>();

        // 1) Générer toutes les paires uniques possibles
        for (int i = 1; i < maxSum; i++) {
            for (int j = 1; j <= maxSum - i; j++) {
                pairs.add(new IntPair(i, j));
            }
        }

        Collections.shuffle(pairs);

        int total = numberOfCaptures;

        // 2) Remplir avec des paires uniques tant que possible
        int uniqueCount = Math.min(total, pairs.size());
        for (int i = 0; i < uniqueCount; i++) {
            listNumber1[i] = pairs.get(i).first();
            listNumber2[i] = pairs.get(i).second();
        }

        // 3) Compléter le reste avec des paires aléatoires
        for (int i = uniqueCount; i < total; i++) {
            int a = rnd.nextInt(maxSum - 1) + 1; // 1..maxSum-1
            int b = rnd.nextInt(maxSum - a) + 1; // 1..maxSum-a
            listNumber1[i] = a;
            listNumber2[i] = b;
        }
    }

    // === Génération avec nombre maximal ===
    private void withMaxNumberGenerate(int maxNumber) {

        record IntPair(int first, int second) {}

        ArrayList<IntPair> pairs = new ArrayList<>();

        // 1) Générer toutes les paires uniques
        for (int i = 1; i <= maxNumber; i++) {
            for (int j = 1; j <= maxNumber; j++) {
                pairs.add(new IntPair(i, j));
            }
        }

        Collections.shuffle(pairs);

        int total = numberOfCaptures;
        int uniqueCount = Math.min(total, pairs.size());

        // 2) Remplir avec les paires uniques
        for (int i = 0; i < uniqueCount; i++) {
            listNumber1[i] = pairs.get(i).first();
            listNumber2[i] = pairs.get(i).second();
        }

        // 3) Compléter le reste avec des paires aléatoires
        for (int i = uniqueCount; i < total; i++) {
            listNumber1[i] = rnd.nextInt(maxNumber) + 1; // 1..maxNumber
            listNumber2[i] = rnd.nextInt(maxNumber) + 1; // 1..maxNumber
        }
    }

    // === Génération simple : un seul nombre par "capture" ===
    // === Génération simple : un seul nombre par "capture" (intervalle [min..max]) ===
    private void withSimpleGenerate(int minSimpleValue, int maxSimpleValue) {

        int min = minSimpleValue;
        int max = maxSimpleValue;

        // sécurités
        if (min <= 0) min = 1;
        if (max < min) max = min;

        int range = max - min + 1;

        // Cas 1 : on peut faire SANS répétition
        if (numberOfCaptures <= range) {
            ArrayList<Integer> numbers = new ArrayList<>();

            // Remplir [min..max]
            for (int i = min; i <= max; i++) {
                numbers.add(i);
            }

            // Mélanger
            Collections.shuffle(numbers);

            // Prendre seulement ce qu'il faut
            for (int i = 0; i < numberOfCaptures; i++) {
                int value = numbers.get(i);
                listNumber1[i] = value;
                listNumber2[i] = 0;  // pas de deuxième nombre en mode simple
            }
        } else {
            // Cas 2 : trop de captures pour l'intervalle, on autorise les répétitions
            for (int i = 0; i < numberOfCaptures; i++) {
                int value = rnd.nextInt(range) + min; // [min..max]
                listNumber1[i] = value;
                listNumber2[i] = 0;  // pas de deuxième nombre
            }
        }
    }


    // === Génération aléatoire d'images : chaque paire -> 2 images de la même catégorie ===
    private void initRandomPictures() {
        if (categories == null || categories.isEmpty()) return;

        // 1) Récupérer les catégories qui ont au moins 2 images
        ArrayList<String> categoriesValides = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> entry : categories.entrySet()) {
            ArrayList<String> fichiers = entry.getValue();
            if (fichiers != null && fichiers.size() >= 2) {
                categoriesValides.add(entry.getKey());
            }
        }

        if (categoriesValides.isEmpty()) {
            System.err.println("Aucune catégorie avec au moins 2 images.");
            return;
        }

        // 2) Pour chaque capture, choisir une catégorie et 2 images distinctes
        for (int i = 0; i < numberOfCaptures; i++) {
            // Catégorie aléatoire parmi les valides
            String cat = categoriesValides.get(rnd.nextInt(categoriesValides.size()));
            ArrayList<String> fichiers = categories.get(cat);

            if (fichiers == null || fichiers.size() < 2) {
                // sécurité supplémentaire, mais normalement jamais ici
                i--;
                continue;
            }

            // Deux indices d'images différents
            int idx1 = rnd.nextInt(fichiers.size());
            int idx2;
            do {
                idx2 = rnd.nextInt(fichiers.size());
            } while (idx2 == idx1 && fichiers.size() > 1);

            String fileName1 = fichiers.get(idx1);
            String fileName2 = fichiers.get(idx2);

            // Tu peux choisir ce que tu stockes :
            // - juste le nom de fichier
            // - ou "cat/nomFichier"
            // Ici je mets "cat/nomFichier" pour garder l'info de catégorie.
            listPictures1[i] = cat + File.separator + fileName1;
            listPictures2[i] = cat + File.separator + fileName2;
        }
    }


    private void drawNumbers() {
        if (infos.isRandomImage()) {
            if (infos.isRandomNumbers()) {
                if (infos.isSimple())
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelectionSimple(listNumber1[i], listPictures1[i]);
                else
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelection(listNumber1[i], listNumber2[i], listPictures1[i], listPictures2[i]);
            } else {
                if (infos.isSimple())
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelectionSimple(0, listPictures1[i]);
                else
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelection(0, 0, listPictures1[i], listPictures2[i]);

            }
        } else {
            if (infos.isRandomNumbers()) {
                if (infos.isSimple())
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelectionSimple(listNumber1[i], null);
                else
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelection(listNumber1[i], listNumber2[i], null, null);
            } else {
                if (infos.isSimple())
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelectionSimple(listNumber1[i], null);
                else
                    for (int i = 0; i < numberOfCaptures; i++)
                        createPanSelection(0, 0, null, null);
            }


        }

    }


    private void restaurerDerniereCategorie() {
        String lastCat = prefs.get(PREF_LAST_CATEGORY, null);

        if (lastCat != null && cmbCategories.getItems().contains(lastCat)) {
            cmbCategories.getSelectionModel().select(lastCat);
        } else if (!cmbCategories.getItems().isEmpty()) {
            // Si aucune préférence valide → on prend la première
            cmbCategories.getSelectionModel().select(0);
        }
        // La sélection déclenche automatiquement le listener,
        // donc afficherImagesPourCategorie(...) est déjà appelée.
    }

    private void afficherImagesPourCategorie(String categorie) {
        gridImages.getChildren().clear();
        toutesImages.clear(); // <<< IMPORTANT : on vide puis on remplit

        ArrayList<String> fichiers = categories.get(categorie);
        if (fichiers == null || fichiers.isEmpty()) return;

        int col = 0;
        int row = 0;

        for (String nomFichier : fichiers) {
            File fichierImage = new File(new File(imagesFolder, categorie), nomFichier);

            // On ajoute à la liste source utilisée par selectImages()
            toutesImages.add(fichierImage);   // <<< ICI

            Button btn = creerBoutonImage(fichierImage);
            gridImages.add(btn, col, row);

            col++;
            if (col >= NB_COL_IMAGES) {
                col = 0;
                row++;
            }
        }

        System.out.println("afficherImagesPourCategorie(" + categorie + ") -> " + toutesImages.size() + " images");
    }


    private Button creerBoutonImage(File fichier) {
        ImageView iv = new ImageView();
        iv.setFitWidth(THUMB_W);
        iv.setFitHeight(THUMB_H);
        iv.setPreserveRatio(true);

        try {
            Image img = new Image(fichier.toURI().toString(), THUMB_W, THUMB_H, true, true);
            if (!img.isError()) {
                iv.setImage(img);
            } else {
                System.err.println("Erreur chargement image: " + fichier + " -> " + img.getException());
            }
        } catch (Exception e) {
            System.err.println("Exception image: " + fichier + " -> " + e);
        }

        Button b = new Button("", iv);

        final String styleNormal = "-fx-background-color: white; -fx-border-color: gray;";
        final String styleSelection = "-fx-background-color: #d0e8ff; -fx-border-color: #0066cc;";

        b.setStyle(styleNormal);
        b.setContentDisplay(ContentDisplay.CENTER);
        b.setMaxWidth(Double.MAX_VALUE);

        b.setOnAction(e -> {
            playClickAnimation(b);
            // quand on clique sur cette miniature, on affecte cette image au prochain Label libre
            handleImageClick(fichier);
            // visuel optionnel :
            // b.setStyle(styleSelection);
        });

        return b;
    }

    private void handleImageClick(File fichierImage) {

        // Vérifier si tout est rempli avant même de charger l'image
        if (infos.isSimple()) {
            if (clickCount >= numberOfCaptures || clickCount >= labelsImage1.size()) {
                showAlertFull();
                return;
            }
        } else {
            // Mode paire : 2 images par ligne
            int maxSlots = numberOfCaptures * 2; // total cases : image1 et image2
            if (clickCount >= maxSlots ||
                    clickCount >= labelsImage1.size() + labelsImage2.size()) {
                showAlertFull();
                return;
            }
        }

        // Ici il reste au moins une case libre → on place l'image normalement
        String relPath = toRelativePath(fichierImage);
        ImageView icon = getIcon(relPath);

        if (icon == null) {
            System.err.println("Impossible de charger l'icône pour " + relPath);
            return;
        }

        if (infos.isSimple()) {

            int lineIndex = clickCount;
            labelsImage1.get(lineIndex).setGraphic(icon);
            listPictures1[lineIndex] = relPath;

        } else {
            int captureIndex = clickCount / 2; // ligne
            int side = clickCount % 2;        // 0 = image1, 1 = image2

            if (side == 0) {
                labelsImage1.get(captureIndex).setGraphic(icon);
                listPictures1[captureIndex] = relPath;
            } else {
                labelsImage2.get(captureIndex).setGraphic(icon);
                listPictures2[captureIndex] = relPath;
            }
        }

        clickCount++;
    }

    private void showAlertFull() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sélection complète");
        alert.setHeaderText(null);
        alert.setContentText("Toutes les images ont déjà été sélectionnées.");
        alert.showAndWait();
    }

    private void playClickAnimation(Button btn) {
        // Zoom avant
        ScaleTransition st1 = new ScaleTransition(Duration.millis(80), btn);
        st1.setToX(1.15);
        st1.setToY(1.15);

        // Retour normal
        ScaleTransition st2 = new ScaleTransition(Duration.millis(70), btn);
        st2.setToX(1.0);
        st2.setToY(1.0);

        // Chainer les deux animations
        st1.setOnFinished(ev -> st2.play());
        st1.play();
    }


    // ====== UI dynamique ======
    private void createPanSelection(int number1, int number2, String image1, String image2) {

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10);
        txtN1 = new TextField();
        txtN1.setPrefColumnCount(2);
        if (number1 == 0) {
            txtN1.setText("");
        } else {
            txtN1.setText(String.valueOf(number1));
        }
        hBox.getChildren().add(txtN1);

        lblPlus = new Label("+");
        hBox.getChildren().add(lblPlus);

        txtN2 = new TextField();
        txtN2.setPrefColumnCount(2);
        if (number2 == 0) {
            txtN2.setText("");
        } else {
            txtN2.setText(String.valueOf(number2));
        }
        hBox.getChildren().add(txtN2);

        lblImage1 = new Label("", getIcon(image1));
        hBox.getChildren().add(lblImage1);
        labelsImage1.add(lblImage1);

        lblImage2 = new Label("", getIcon(image2));
        hBox.getChildren().add(lblImage2);
        labelsImage2.add(lblImage2);
        int col = index % 3;   // 0,1,2
        int row = index / 3;   // 0,1,2,...
        selectPan.add(hBox, col, row);
        index++;
    }

    private void createPanSelectionSimple(int number1, String image1) {

        hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        txtN1 = new TextField();
        txtN1.setPrefColumnCount(2);
        if (number1 == 0) {
            txtN1.setText("");
        } else {
            txtN1.setText(String.valueOf(number1));
        }
        hBox.getChildren().add(txtN1);

        lblImage1 = new Label("", getIcon(image1));
        hBox.getChildren().add(lblImage1);
        labelsImage1.add(lblImage1);
        int col = index % 3;
        int row = index / 3;

        selectPan.add(hBox, col, row);
        index++;
    }


    private void randNumbersRandImages() {
        System.out.println(">>> randNumbersRandImages()");
        createPanSelection(1, 2, "image1", "image2");

    }

    private void remplirCombo() {
        cmbCategories.getItems().clear();
        if (categories == null || categories.isEmpty()) return;

        for (Map.Entry<String, ArrayList<String>> e : categories.entrySet()) {
            cmbCategories.getItems().add(e.getKey());
        }

        if (!cmbCategories.getItems().isEmpty()) {
            cmbCategories.getSelectionModel().selectFirst();
            String cat = cmbCategories.getSelectionModel().getSelectedItem();
            afficherImagesPourCategorie(cat);
            ;
        }
    }

    private ImageView getIcon(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        // Fichier réel : imagesFolder / relativePath
        File file = new File(imagesFolder, relativePath);

        if (!file.exists()) {
            System.err.println("Image introuvable: " + file);
            return null;
        }

        Image img = new Image(file.toURI().toString(), 40, 40, true, true);

        ImageView iv = new ImageView(img);
        iv.setFitWidth(40);
        iv.setFitHeight(40);
        iv.setPreserveRatio(true);

        return iv;
    }


    private String toRelativePath(File file) {
        try {
            String basePath = imagesFolder.getCanonicalPath();
            String filePath = file.getCanonicalPath();

            if (filePath.startsWith(basePath)) {
                String rel = filePath.substring(basePath.length());
                if (rel.startsWith(File.separator)) {
                    rel = rel.substring(1);
                }
                return rel;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // fallback
        return file.getName();
    }

    public static class Result {
        private final int[] listNumber1;
        private final int[] listNumber2;
        private final String[] listPictures1;
        private final String[] listPictures2;

        public Result(int[] listNumber1, int[] listNumber2,
                      String[] listPictures1, String[] listPictures2) {
            this.listNumber1 = listNumber1;
            this.listNumber2 = listNumber2;
            this.listPictures1 = listPictures1;
            this.listPictures2 = listPictures2;
        }

        public int[] getListNumber1() {
            return listNumber1;
        }

        public int[] getListNumber2() {
            return listNumber2;
        }

        public String[] getListPictures1() {
            return listPictures1;
        }

        public String[] getListPictures2() {
            return listPictures2;
        }
    }
    private void extractNumbersFromTextFields() {
        // Sécurités de base
        if (listNumber1 == null || listNumber2 == null) return;

        int ligne = 0;

        for (javafx.scene.Node node : selectPan.getChildren()) {
            if (!(node instanceof HBox hbox)) continue;

            if (infos.isSimple()) {
                // Structure : [ txtN1, lblImage1 ]
                if (hbox.getChildren().isEmpty()) continue;

                TextField tf1 = (TextField) hbox.getChildren().get(0);
                int n1 = parseIntSafe(tf1.getText());
                if (ligne < listNumber1.length) {
                    listNumber1[ligne] = n1;
                    listNumber2[ligne] = 0;  // en mode simple, toujours 0
                }
            } else {
                // Structure : [ txtN1, "+", txtN2, lblImage1, lblImage2 ]
                if (hbox.getChildren().size() < 3) continue;

                TextField tf1 = (TextField) hbox.getChildren().get(0);
                TextField tf2 = (TextField) hbox.getChildren().get(2);

                int n1 = parseIntSafe(tf1.getText());
                int n2 = parseIntSafe(tf2.getText());

                if (ligne < listNumber1.length) {
                    listNumber1[ligne] = n1;
                    listNumber2[ligne] = n2;
                }
            }

            ligne++;
            if (ligne >= numberOfCaptures) break;
        }
    }

    private int parseIntSafe(String text) {
        if (text == null || text.isBlank()) return 0;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Result result;


    public Result getResult() {
        return result;
    }


    @FXML
    private void onValidate() {
        // 1) mettre à jour les tableaux de nombres depuis les TextField
        extractNumbersFromTextFields();

        // 2) construire le résultat et le stocker dans le champ result
        result = new Result(
                listNumber1,
                listNumber2,
                listPictures1,
                listPictures2
        );

        // 3) fermer la fenêtre
        Stage stage = (Stage) selectPan.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) selectPan.getScene().getWindow();
        stage.close();
    }






}
