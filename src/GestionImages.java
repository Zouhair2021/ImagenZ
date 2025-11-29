import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class GestionImages extends Dialog<Void> {

    @FXML
    private ComboBox<String> cmbCategories;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAjouterCategorie;
    @FXML
    private Button btnSupprimerCategorie;

    @FXML
    private GridPane gridImages;

    private final File imagesRoot;  // dossier racine: .../ImagenZ/images
    private final HashMap<String, ArrayList<String>> categories;

    private static final int COLS    = 5;
    private static final int THUMB_W = 96;
    private static final int THUMB_H = 96;

    public GestionImages(File imagesRoot,
                         HashMap<String, ArrayList<String>> categories) {

        this.imagesRoot = imagesRoot;
        this.categories = categories;

        setTitle("Gestion des images");
        getDialogPane().setPrefSize(800, 600);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionImagesLayout.fxml"));
        loader.setController(this);

        try {
            BorderPane root = loader.load();
            getDialogPane().setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // juste un bouton "Fermer"
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new javafx.scene.image.Image(
                        getClass().getResource("/resources/icons/reves.png").toExternalForm()
                )
        );

    }

    @FXML
    private void initialize() {
        // Remplir le ComboBox avec les noms de catégories existantes
        if (categories != null && !categories.isEmpty()) {
            cmbCategories.getItems().addAll(categories.keySet());
            cmbCategories.getSelectionModel().selectFirst();
        }

        // Quand on change de catégorie => rafraîchir la grille
        cmbCategories.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        afficherImagesPourCategorie(newVal);
                    }
                });

        // Bouton "Ajouter des images..."
        btnAjouter.setOnAction(e -> onAjouterImages());
        btnAjouterCategorie.setOnAction(e -> onAjouterCategorie());
        btnSupprimerCategorie.setOnAction(e -> onSupprimerCategorie());

        // Afficher la catégorie actuelle au démarrage
        String current = cmbCategories.getSelectionModel().getSelectedItem();
        if (current != null) {
            afficherImagesPourCategorie(current);
        }
    }

    // ---------- Ajout d’images ----------
    private void onAjouterImages() {
        String categorie = cmbCategories.getSelectionModel().getSelectedItem();
        if (categorie == null || categorie.isEmpty()) {
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir des images à ajouter");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        List<File> choisis = fc.showOpenMultipleDialog(getDialogPane().getScene().getWindow());
        if (choisis == null || choisis.isEmpty()) return;

        for (File src : choisis) {
            try {
                addImageToCategory(src, categorie);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // rafraîchir
        afficherImagesPourCategorie(categorie);
    }

    // ---------- Affichage des images ----------
    private void afficherImagesPourCategorie(String categorie) {
        gridImages.getChildren().clear();

        ArrayList<String> fichiers = categories.get(categorie);
        if (fichiers == null || fichiers.isEmpty()) return;

        int col = 0;
        int row = 0;

        for (String nomFichier : fichiers) {
            File fichierImage = new File(new File(imagesRoot, categorie), nomFichier);
            Button btn = creerBoutonImage(fichierImage, categorie, nomFichier);
            gridImages.add(btn, col, row);

            col++;
            if (col >= COLS) {
                col = 0;
                row++;
            }
        }
    }

    // ---------- Bouton d’image avec menu contextuel "Supprimer" ----------
    private Button creerBoutonImage(File fichier, String categorie, String nomFichier) {
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
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        b.setContentDisplay(ContentDisplay.CENTER);
        b.setPrefSize(THUMB_W + 10, THUMB_H + 10);

        // Menu contextuel (clic droit) pour supprimer
        MenuItem miSupprimer = new MenuItem("Supprimer cette image");
        miSupprimer.setOnAction(e -> {
            boolean ok = removeImageFromCategory(categorie, nomFichier);
            if (ok) {
                afficherImagesPourCategorie(categorie);
            }
        });

        ContextMenu ctx = new ContextMenu(miSupprimer);
        b.setContextMenu(ctx);

        return b;
    }

    // ---------- Gestion interne des fichiers et HashMap ----------

    /** Ajoute une image dans une catégorie (copie fichier + mise à jour HashMap). */
    private File addImageToCategory(File sourceFile, String categorie) throws IOException {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IllegalArgumentException("Fichier source invalide : " + sourceFile);
        }

        ensureCategoryExists(categorie);

        File catDir = new File(imagesRoot, categorie);
        String baseName = sourceFile.getName();
        String targetName = makeUniqueName(catDir, baseName);

        File targetFile = new File(catDir, targetName);
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        ArrayList<String> list = categories.get(categorie);
        if (!list.contains(targetName)) {
            list.add(targetName);
        }

        return targetFile;
    }

    /** Supprime une image d’une catégorie (HashMap + fichier disque). */
    private boolean removeImageFromCategory(String categorie, String fileName) {
        ArrayList<String> list = categories.get(categorie);
        if (list == null || !list.contains(fileName)) {
            return false;
        }

        File catDir = new File(imagesRoot, categorie);
        File targetFile = new File(catDir, fileName);

        boolean okFile = true;
        if (targetFile.exists()) {
            okFile = targetFile.delete();
        }

        boolean okList = list.remove(fileName);

        return okFile && okList;
    }

    private void ensureCategoryExists(String categorie) {
        if (!categories.containsKey(categorie)) {
            categories.put(categorie, new ArrayList<>());
        }
        File catDir = new File(imagesRoot, categorie);
        if (!catDir.exists()) {
            catDir.mkdirs();
        }
    }

    private String makeUniqueName(File directory, String baseName) {
        File test = new File(directory, baseName);
        if (!test.exists()) {
            return baseName;
        }

        String name = baseName;
        String ext = "";
        int dot = baseName.lastIndexOf('.');
        if (dot > 0 && dot < baseName.length() - 1) {
            name = baseName.substring(0, dot);
            ext = baseName.substring(dot);
        }

        int count = 1;
        while (true) {
            String candidate = name + "_" + count + ext;
            File f = new File(directory, candidate);
            if (!f.exists()) {
                return candidate;
            }
            count++;
        }
    }

    private void onAjouterCategorie() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouvelle catégorie");
        dialog.setHeaderText(null);
        dialog.setContentText("Nom de la nouvelle catégorie :");

        dialog.initOwner(getDialogPane().getScene().getWindow());

        dialog.showAndWait().ifPresent(nomSaisi -> {
            String nom = nomSaisi.trim();
            if (nom.isEmpty()) {
                return; // on ignore vide
            }

            // Éviter les doublons
            if (categories.containsKey(nom)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Catégorie existante");
                alert.setHeaderText(null);
                alert.setContentText("La catégorie \"" + nom + "\" existe déjà.");
                alert.initOwner(getDialogPane().getScene().getWindow());
                alert.showAndWait();
                return;
            }

            // Créer la catégorie (HashMap + dossier sur disque)
            ensureCategoryExists(nom);

            // Ajouter dans la combo
            cmbCategories.getItems().add(nom);
            // Optionnel : re-trier la liste
            // FXCollections.sort(cmbCategories.getItems());

            // Sélectionner la nouvelle catégorie
            cmbCategories.getSelectionModel().select(nom);
        });
    }

    private void onSupprimerCategorie() {
        String categorie = cmbCategories.getSelectionModel().getSelectedItem();
        if (categorie == null || categorie.isEmpty()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer une catégorie");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Voulez-vous vraiment supprimer la catégorie \"" + categorie + "\" ?\n\n"
                        + "Toutes les images de cette catégorie seront supprimées du disque."
        );
        confirm.initOwner(getDialogPane().getScene().getWindow());

        confirm.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;

            // 1) Supprimer du HashMap
            categories.remove(categorie);

            // 2) Supprimer le dossier sur disque
            File catDir = new File(imagesRoot, categorie);
            if (catDir.exists()) {
                deleteDirectoryRecursive(catDir);
            }

            // 3) Mettre à jour la ComboBox
            cmbCategories.getItems().remove(categorie);

            // 4) Mettre à jour la sélection / affichage
            if (cmbCategories.getItems().isEmpty()) {
                cmbCategories.getSelectionModel().clearSelection();
                gridImages.getChildren().clear();
            } else {
                cmbCategories.getSelectionModel().selectFirst();
            }
        });
    }

    private boolean deleteDirectoryRecursive(File dir) {
        if (dir == null || !dir.exists()) return true;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursive(f);
                } else {
                    if (!f.delete()) {
                        System.err.println("Impossible de supprimer fichier : " + f);
                    }
                }
            }
        }

        if (!dir.delete()) {
            System.err.println("Impossible de supprimer dossier : " + dir);
            return false;
        }
        return true;
    }

}
