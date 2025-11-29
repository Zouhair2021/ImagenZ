import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.Collections;

public class DialogImages extends Dialog<List<File>> {

    @FXML
    private ComboBox<String> cmbCategories;

    @FXML
    private GridPane gridImages;

    private final HashMap<String, ArrayList<String>> categories;
    private final File imagesFolder; // dossier racine des images

    private static final int COLS    = 5;
    private static final int THUMB_W = 96;
    private static final int THUMB_H = 96;

    // fichiers choisis (max 2)
    private final List<File> fichiersSelectionnes = new ArrayList<>();

    private static final String PREF_LAST_CATEGORY = "last_category_dialogimages";
    private final Preferences prefs = Preferences.userNodeForPackage(DialogImages.class);

    public DialogImages(File imagesFolder,
                        HashMap<String, ArrayList<String>> categories) {

        this.imagesFolder = imagesFolder;
        this.categories   = categories;

        setTitle("Choisir des images");
        getDialogPane().setPrefSize(800, 600);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DialogImagesLayout.fxml"));
        loader.setController(this);
        try {
            BorderPane root = loader.load();
            getDialogPane().setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ✅ on renvoie la vraie liste de fichiers sélectionnés
        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new ArrayList<>(fichiersSelectionnes);
            }
            return null;
        });

        Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new javafx.scene.image.Image(
                        getClass().getResource("/resources/icons/reves.png").toExternalForm()
                )
        );

    }

    @FXML
    private void initialize() {
        // 1) Remplir la ComboBox
        if (categories != null && !categories.isEmpty()) {
            // Optionnel : trier les catégories
            ArrayList<String> noms = new ArrayList<>(categories.keySet());
            Collections.sort(noms);
            cmbCategories.getItems().setAll(noms);
        }

        // 2) Listener UNIQUE : changement de catégorie
        cmbCategories.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        // Sauvegarder dans les préférences
                        prefs.put(PREF_LAST_CATEGORY, newVal);
                        // Afficher les images de cette catégorie
                        afficherImagesPourCategorie(newVal);
                    } else {
                        gridImages.getChildren().clear();
                        fichiersSelectionnes.clear();
                    }
                });

        // 3) Restaurer la dernière catégorie choisie
        restaurerDerniereCategorie();
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
        fichiersSelectionnes.clear();

        ArrayList<String> fichiers = categories.get(categorie);
        if (fichiers == null || fichiers.isEmpty()) return;

        int col = 0;
        int row = 0;

        for (String nomFichier : fichiers) {
            File fichierImage = new File(new File(imagesFolder, categorie), nomFichier);
            Button btn = creerBoutonImage(fichierImage);
            gridImages.add(btn, col, row);

            col++;
            if (col >= COLS) {
                col = 0;
                row++;
            }
        }
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

        final String styleNormal    = "-fx-background-color: white; -fx-border-color: gray;";
        final String styleSelection = "-fx-background-color: #d0e8ff; -fx-border-color: #0066cc;";

        b.setStyle(styleNormal);
        b.setContentDisplay(ContentDisplay.CENTER);
        b.setMaxWidth(Double.MAX_VALUE);

        b.setOnAction(e -> {
            if (fichiersSelectionnes.contains(fichier)) {
                // dé-sélection
                fichiersSelectionnes.remove(fichier);
                b.setStyle(styleNormal);
            } else {
                // nouvelle sélection (max 2)
                if (fichiersSelectionnes.size() >= 2) {
                    return;
                }
                fichiersSelectionnes.add(fichier);
                b.setStyle(styleSelection);
            }
        });

        return b;
    }


}
