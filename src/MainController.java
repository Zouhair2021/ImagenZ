import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.animation.ScaleTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.prefs.Preferences;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.io.File;


public class MainController {
    @FXML
    private StackPane contentArea;
    @FXML
    private CheckBox chkMultiple;
    @FXML
    private Spinner<Integer> spinNumber1;
    @FXML
    private Spinner<Integer> spinNumber2;
    @FXML
    private Button btnChooseImage;
    @FXML
    private Button btnCapture;
    @FXML
    private Button btnProgramme;
    @FXML
    private Button btnOpenFolder;

    @FXML
    RadioMenuItem rmiContent;
    @FXML
    RadioMenuItem rmiAscending;
    @FXML
    RadioMenuItem rmiRandom;


    @FXML
    private ToggleGroup fileNameGroup;


    private String fileNameType = "ascending";
    private AudioClip captureSound;
    private File appFolder, imagesFolder, capturesFolder;
    protected File repertoireSauvegarde;
    protected static File dernierRepertoireChargement;
    protected static File fichierImage1;
    protected static File fichierImage2;

    private HashMap<String, ArrayList<String>> categories;

    private boolean bMultiple;
    String LAST_UPLOADED_FOLDER = "lastUploadedFolder", MULTIPLE = "multiple",
            DER_IMAGE1 = "derniere image 1", DER_IMAGE2 = "derniere image 2",
            NAME_TYPE = "Name Type", LIST_IMAGES = "image list",
            CATEGORIES_DAT = "categories data";
    private String messageOptAutoCapture = "voulez-vous supprimer les fichiers existants dans ce dossier ?";

    Preferences prefs = Preferences.userNodeForPackage(MainController.class);

    PanDessin pan;

    @FXML
    private void initialize() {
        pan = new PanDessin();
        createImageRepert();
        loadPreferences();
        setSpinNumber();
        contentArea.getChildren().add(pan);
        capturesFolder = createImagenZDirectory();
        rmiAscending.setSelected(true);

        try {
            var url = getClass().getResource("/resources/sounds/screenshot.wav");
            if (url != null) {
                captureSound = new AudioClip(url.toExternalForm());
            } else {
                System.err.println("Son de capture introuvable /resources/sounds/capture.wav");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createImageRepert() {
        File appDataPath = new File(System.getenv("LOCALAPPDATA"));
        String nomDossierApp = "ImagenZ";
        String nomDossierImages = "images";
        appFolder = new File(appDataPath, nomDossierApp);
        try {
            // Création des dossiers s'ils n'existent pas
            if (!appFolder.exists()) {
                appFolder.mkdir();
            }

            imagesFolder = new File(appFolder, nomDossierImages);
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir();
                categories = DefaultCategories.create();
                copierDefaultImages();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File createImagenZDirectory() {
        // Dossier Images/Pictures de l'utilisateur
        File imagesDir = new File(System.getProperty("user.home"), "Pictures");

        // Sous-dossier ImagenZ
        File imagenZDir = new File(imagesDir, "ImagenZ");

        if (!imagenZDir.exists()) {
            boolean created = imagenZDir.mkdirs();
            System.out.println("Dossier ImagenZ créé : " + created);
        } else {
            System.out.println("Dossier ImagenZ existe déjà.");
        }

        return imagenZDir;
    }

    private int compteurCapture = 1;

    private File createCaptureFile() {
        if (capturesFolder == null) {
            capturesFolder = createImagenZDirectory();
        }

        if (!capturesFolder.exists()) {
            capturesFolder.mkdirs();
        }

        String name;

        switch (fileNameType) {
            case "content":
                String nom1 = toNomSansExtension(fichierImage1);
                String nom2 = toNomSansExtension(fichierImage2);
                if (bMultiple) {
                    name = pan.getNombre1() + "_" + nom1 + "_" + pan.getNombre2() + "_" + nom2 + ".png";
                } else {
                    name = pan.getNombre1() + "_" + nom1 + ".png";
                }
                break;

            case "ascending":
                name = String.format("capture_%03d.png", compteurCapture++);
                break;

            case "random":
            default:
                int n;
                File fRandom;
                do {
                    n = (int) (Math.random() * 100);
                    fRandom = new File(capturesFolder, String.format("capture_%02d.png", n));
                } while (fRandom.exists());
                name = fRandom.getName();
                break;
        }

        // ====== Gestion de conflit de nom ======
        File candidate = new File(capturesFolder, name);

        if (candidate.exists()) {
            String fileName = candidate.getName();
            String base = fileName;
            String ext = "";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) {
                base = fileName.substring(0, dot);
                ext = fileName.substring(dot); // ex : ".png"
            }

            int index = 1;
            File newFile;
            do {
                newFile = new File(capturesFolder, base + "_" + index + ext);
                index++;
            } while (newFile.exists());

            candidate = newFile;
        }

        return candidate;
    }


    private boolean prepareCaptureFolder() {
        if (capturesFolder == null) {
            capturesFolder = createImagenZDirectory();
        }
        if (!capturesFolder.exists()) {
            capturesFolder.mkdirs();
        }

        // On ne s'intéresse ici qu'aux fichiers PNG
        File[] existing = capturesFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png")
        );

        // S'il n'y a rien, pas besoin d'alerte
        if (existing == null || existing.length == 0) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fichiers existants");
        alert.setHeaderText("Des fichiers existent déjà dans le dossier.");
        alert.setContentText("Que voulez-vous faire ?");

// === Boutons ===
        ButtonType btnSupprimer = new ButtonType("Supprimer", ButtonBar.ButtonData.YES);
        ButtonType btnRenommer  = new ButtonType("Renommer", ButtonBar.ButtonData.NO);
        ButtonType btnAnnuler   = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSupprimer, btnRenommer, btnAnnuler);

// === FORCER affichage côte à côte ===
        DialogPane dialogPane = alert.getDialogPane();
        ButtonBar buttonBar = (ButtonBar) dialogPane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.getButtons().forEach(button ->
                    ButtonBar.setButtonData(button, ButtonBar.ButtonData.OTHER)
            );
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == btnAnnuler) {
            // On annule l'opération
            return false;
        }

        if (result.get() == btnSupprimer) {
            // Supprimer les anciens fichiers
            for (File f : existing) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // On peut remettre le compteur à 1 pour le mode "ascending"
            compteurCapture = 1;
            return true;
        }

        if (result.get() == btnRenommer) {
            
            // On garde le compteur tel quel
            return true;
        }

        return false;
    }


    private String toNomSansExtension(File f) {
        if (f == null) return "none";
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }




    public void copierDefaultImages() {

        File targetFolder;
        for (String categorieName : categories.keySet()) {
            targetFolder = new File(imagesFolder, categorieName);
            targetFolder.mkdir();
            for (String element : categories.get(categorieName)) {
                URL resourceUrl = MainController.class.getResource("/resources/" + element);
                if (resourceUrl != null) {
                    // Créer le fichier de destination
                    File targetFile = new File(targetFolder, element);

                    // Copier le fichier
                    try (InputStream in = resourceUrl.openStream()) {
                        Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    System.err.println("Ressource non trouvée: " + element);
                }
            }

        }
    }

    private void loadPreferences() {
        bMultiple = prefs.getBoolean(MULTIPLE, false);
        chkMultiple.setSelected(bMultiple);

        String userHome = System.getProperty("user.home");
        Path downloadPath = Paths.get(userHome, "Downloads");
        String downloadPathStr = downloadPath.toString();
        dernierRepertoireChargement = new File(prefs.get(LAST_UPLOADED_FOLDER, downloadPathStr));

        String json = prefs.get(CATEGORIES_DAT, "{}");
        Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        categories = new Gson().fromJson(json, type);
        if (categories == null) categories = new HashMap<>();
        if (categories.isEmpty()) categories = DefaultCategories.create();

        syncCategoriesWithDisk();
        savePreferences();

        // === ICI : sécuriser rel1 / rel2 ===
        String relDef1 = "animaux/chat.png";
        String relDef2 = "animaux/chien.png";

        String rel1 = prefs.get(DER_IMAGE1, relDef1);
        String rel2 = prefs.get(DER_IMAGE2, relDef2);

        if (rel1 == null || rel1.isBlank()) {
            rel1 = relDef1;
            prefs.put(DER_IMAGE1, rel1);
        }
        if (rel2 == null || rel2.isBlank()) {
            rel2 = relDef2;
            prefs.put(DER_IMAGE2, rel2);
        }

        fichierImage1 = new File(imagesFolder, rel1);
        fichierImage2 = new File(imagesFolder, rel2);

        pan.setFichierImage1(fichierImage1);
        pan.setFichierImage2(fichierImage2);
    }

    private void syncCategoriesWithDisk() {
        if (categories == null) {
            categories = new HashMap<>();
        }

        // 1) Lister les sous-dossiers de imagesFolder
        File[] sousDossiers = imagesFolder.listFiles(File::isDirectory);
        if (sousDossiers == null) return;

        for (File catDir : sousDossiers) {
            String catName = catDir.getName();

            // Récupérer ou créer la liste pour cette catégorie
            ArrayList<String> listeFichiers =
                    categories.computeIfAbsent(catName, k -> new ArrayList<>());

            // Lister les fichiers images du dossier
            File[] files = catDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".jpeg")
            );

            if (files == null) continue;

            for (File f : files) {
                String nom = f.getName();
                if (nom == null || nom.isBlank()) continue;

                // Si le nom n'est pas encore dans la liste, on l'ajoute
                if (!listeFichiers.contains(nom)) {
                    listeFichiers.add(nom);
                }
            }
        }
    }


    private void savePreferences() {
        String json = new Gson().toJson(categories);
        prefs.put(CATEGORIES_DAT, json);
        prefs.putBoolean(MULTIPLE, bMultiple);
        prefs.put(LAST_UPLOADED_FOLDER, dernierRepertoireChargement.toString());

        String rel1 = toRelative(imagesFolder, fichierImage1);
        String rel2 = toRelative(imagesFolder, fichierImage2);

        if (rel1 != null && !rel1.isBlank()) {
            prefs.put(DER_IMAGE1, rel1);
        }
        if (rel2 != null && !rel2.isBlank()) {
            prefs.put(DER_IMAGE2, rel2);
        }

        prefs.put(NAME_TYPE, fileNameType);
    }


    private String toRelative(File base, File f) {
        if (f == null) return ""; // on filtre dans savePreferences

        String bp = base.getAbsolutePath().replace('\\', '/');
        String fp = f.getAbsolutePath().replace('\\', '/');
        if (fp.startsWith(bp)) {
            String rel = fp.substring(bp.length());
            while (rel.startsWith("/")) rel = rel.substring(1);
            return rel; // ex: "animaux/voiture.png"
        }
        return f.getName(); // fallback minimal
    }


    private void setSpinNumber() {
        var vf1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 5);
        var vf2 = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 5);
        spinNumber1.setValueFactory(vf1);
        spinNumber2.setValueFactory(vf2);
        pan.setNombre1(spinNumber1.getValue());
        pan.setNombre2(spinNumber2.getValue());

        spinNumber1.setEditable(true);
        if (bMultiple)
            spinNumber2.setDisable(false);
        else {
            spinNumber2.setDisable(true);
            pan.setNombre2(0);
        }
        vf1.valueProperty().addListener((o, ov, nv) -> pan.setNombre1(nv));
        vf2.valueProperty().addListener((o, ov, nv) -> pan.setNombre2(nv));

    }

    public static void saveCanvasAsPng(Canvas canvas, File file) {
        WritableImage snapshot = new WritableImage(
                (int) canvas.getWidth(),
                (int) canvas.getHeight()
        );

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT); // si tu veux fond transparent
        canvas.snapshot(params, snapshot);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lance la boucle de représentation + capture à partir des résultats
     * retournés par SelectDialogController.
     */
    /**
     * Lance la boucle de représentation + capture avec animation
     * (affichage pendant un certain temps avant chaque capture).
     */
    private void lancerProgramme(InfosInput infos,
                                 SelectDialogController.Result res) {

        // === Vérifier le dossier de captures avant de commencer ===
        if (!prepareCaptureFolder()) {
            // L'utilisateur a annulé
            return;
        }

        int[] listN1 = res.getListNumber1();
        int[] listN2 = res.getListNumber2();
        String[] listP1 = res.getListPictures1();
        String[] listP2 = res.getListPictures2();

        if (listN1 == null) {
            return;
        }

        int total = Math.min(infos.getCapturesNumber(), listN1.length);

        // durée d’affichage avant chaque capture (en ms)
        final int DURATION_MS = 100; // 1 seconde (change si tu veux)

        // Synchroniser l’état "multiple" de l’interface avec infos
        bMultiple = infos.isMultiple();
        chkMultiple.setSelected(bMultiple);
        if (bMultiple) {
            spinNumber2.setDisable(false);
        } else {
            spinNumber2.setDisable(true);
            pan.setNombre2(0);
        }

        // index courant pour la séquence
        final int[] index = {0};

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(DURATION_MS), event -> {
                    int i = index[0];

                    if (i >= total) {
                        // sécurité, normalement on ne devrait pas entrer ici
                        return;
                    }

                    // ===== 1) Appliquer les nombres =====
                    int n1 = listN1[i];
                    int n2 = (listN2 != null && i < listN2.length) ? listN2[i] : 0;

                    if (spinNumber1.getValueFactory() != null) {
                        spinNumber1.getValueFactory().setValue(n1);
                    }
                    pan.setNombre1(n1);

                    if (bMultiple) {
                        if (spinNumber2.getValueFactory() != null) {
                            spinNumber2.getValueFactory().setValue(n2);
                        }
                        pan.setNombre2(n2);
                    } else {
                        pan.setNombre2(0);
                    }

                    // ===== 2) Appliquer les images =====
                    if (listP1 != null && i < listP1.length && listP1[i] != null) {
                        fichierImage1 = new File(imagesFolder, listP1[i]);
                        pan.setFichierImage1(fichierImage1);
                    }

                    if (bMultiple && listP2 != null && i < listP2.length && listP2[i] != null) {
                        fichierImage2 = new File(imagesFolder, listP2[i]);
                        pan.setFichierImage2(fichierImage2);
                    } else if (!bMultiple) {
                        fichierImage2 = null;
                        pan.setFichierImage2(null);
                    }

                    // ===== 3) Capture =====
                    File outFile = createCaptureFile();
                    saveCanvasAsPng(pan, outFile);
                    System.out.println("Capture " + (i + 1) + "/" + total + " : " + outFile.getAbsolutePath());

                    // passer à la "ligne" suivante
                    index[0]++;
                })
        );

        // On veut exactement "total" exécutions
        timeline.setCycleCount(total);

        // Quand la Timeline a fini tous ses cycles
        timeline.setOnFinished(e -> {
            Platform.runLater(() -> {
                showEndCaptureAlert(total);
                savePreferences();
            });
        });

        timeline.play();
    }

    private void showEndCaptureAlert(int total) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Captures terminées");
        alert.setHeaderText(null);
        alert.setContentText("Les " + total + " captures ont été générées.");
        alert.showAndWait();
    }

    private void playCaptureFeedback() {
        // Son
        if (captureSound != null) {
            captureSound.play();
        }

        // Petite animation de zoom sur le pan
        ScaleTransition st1 = new ScaleTransition(Duration.millis(100), pan);
        st1.setToX(1.05);
        st1.setToY(1.05);

        ScaleTransition st2 = new ScaleTransition(Duration.millis(120), pan);
        st2.setToX(1.0);
        st2.setToY(1.0);

        st1.setOnFinished(e -> st2.play());
        st1.play();
    }






    @FXML
    private void onFileName(ActionEvent event) {
        Object source = event.getSource();
        if (source == rmiContent)
            fileNameType = "content";
        else if (source == rmiAscending)
            fileNameType = "ascending";
        else if (source == rmiRandom)
            fileNameType = "random";
    }

    @FXML
    private void onChooseImage() {
        DialogImages dlg = new DialogImages(imagesFolder, categories);

        dlg.showAndWait().ifPresent(fichiers -> {
            if (fichiers == null || fichiers.isEmpty()) return;

            if (fichiers.size() >= 1) {
                fichierImage1 = fichiers.get(0);
                pan.setFichierImage1(fichierImage1);
            }

            if (fichiers.size() >= 2) {
                fichierImage2 = fichiers.get(1);
                pan.setFichierImage2(fichierImage2);
            }


        });
        savePreferences();
    }

    private boolean captureFolderChecked = false;

    @FXML
    private void onCapture() {
        if (!captureFolderChecked) {
            if (!prepareCaptureFolder()) {
                return; // utilisateur a annulé
            }
            captureFolderChecked = true; // Ne plus afficher l'alerte après
        }

        // === Animation + son ===
        playCaptureFeedback();

        File outFile = createCaptureFile();
        saveCanvasAsPng(pan, outFile);
        System.out.println("Capture enregistrée dans : " + outFile.getAbsolutePath());
    }


    @FXML
    private void onProgramme() {
        Window owner = contentArea.getScene().getWindow();

        InfosInput infos = InputDialog.showInputDialog(owner);
        if (infos == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("selectDialog.fxml"));

            Parent root = loader.load();

            // Maintenant que le FXML est chargé, on récupère le contrôleur
            SelectDialogController controller = loader.getController();

            // On lui passe les paramètres
            controller.setup(infos, imagesFolder, categories);

            Stage stage = new Stage();
            stage.initOwner(owner);
            stage.setTitle("Sélection des nombres et des images");
            stage.getIcons().add(new Image(getClass().getResource("/resources/icons/reves.png").toExternalForm()));
            stage.setScene(new Scene(root));

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());



            stage.showAndWait();

            // Récupérer le résultat après fermeture
            SelectDialogController.Result res = controller.getResult();
            if (res == null) {
                // L'utilisateur a cliqué sur "Fermer" ou a fermé la fenêtre
                return;
            }

            // === Lancer la boucle de représentation + captures ===
            lancerProgramme(infos, res);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onOpenFolder() {
        try {
            if (capturesFolder == null) {
                capturesFolder = createImagenZDirectory();
            }

            if (!capturesFolder.exists()) {
                capturesFolder.mkdirs();
            }

            Desktop.getDesktop().open(capturesFolder);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le dossier");
            alert.setContentText("Chemin : " + capturesFolder.getAbsolutePath());
            alert.showAndWait();
        }
    }

    @FXML
    private void onCheckMultiple() {
        if (chkMultiple.isSelected()) {
            // Vérifier que les deux images sont bien définies
            if (fichierImage1 == null || fichierImage2 == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Images manquantes");
                alert.setHeaderText("Deux images sont nécessaires pour le mode multiple.");
                alert.setContentText("Veuillez sélectionner deux images avant d'activer le mode multiple.");
                alert.showAndWait();

                // Revenir à l'état "simple"
                chkMultiple.setSelected(false);
                bMultiple = false;
                spinNumber2.setDisable(true);
                pan.setNombre2(0);
            } else {
                // Tout est OK : activer le mode multiple
                bMultiple = true;
                spinNumber2.setDisable(false);
                pan.setNombre2(spinNumber2.getValue());
            }
        } else {
            // Case décochée → repasser en mode simple
            bMultiple = false;
            spinNumber2.setDisable(true);
            pan.setNombre2(0);
        }

        // Sauvegarder l'état
        savePreferences();
    }


    @FXML
    private void onManageImages() {
        GestionImages dlg = new GestionImages(imagesFolder, categories);
        dlg.showAndWait();
        savePreferences();
    }

        @FXML
        private void onAbout() {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle("À propos");
            alert.setHeaderText("ImagenZ - Application Open Source");

            alert.setContentText(
                    "ImagenZ\n" +
                            "Version : 2.0.0\n" +
                            "Auteur : Zouhair Lamrabet\n\n" +
                            "Cette application est entièrement open source et destinée\n" +
                            "à la création d'exercices pédagogiques basés sur des\n" +
                            "captures automatiques de nombres et d'images.\n\n" +
                            "Contact WhatsApp : +212 659 686 664"
            );

            // Centrer la fenêtre modale sur la fenêtre principale
            if (contentArea != null && contentArea.getScene() != null) {
                alert.initOwner(contentArea.getScene().getWindow());
            }

            alert.showAndWait();
        }




}
