import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.File;

/**
 * Canvas qui se redimensionne selon le contenu dessiné :
 * deux grilles d'images (nombre1 et nombre2) côte à côte,
 * 3 colonnes max par grille, avec marges et espace entre les 2.
 */
public class PanDessin extends Canvas {

    // Fichiers + cache d’images
    private File fichierImage1, fichierImage2;
    private Image img1, img2;

    // Quantités
    private int nombre1 = 0, nombre2 = 0;

    // Paramètres d’affichage
    public static final int LARGEUR_ICONE = 105;
    public static final int HAUTEUR_ICONE = LARGEUR_ICONE;

    private static final int COLS = 3;            // nb de colonnes par nombre
    private static final double H_GAP = 2;        // espace entre icônes sur une ligne
    private static final double V_GAP = 10;       // espace entre lignes
    private static final double PADDING = 10;     // marge interne du canvas
    private static final double BLOCK_GAP = 20;   // espace entre nombre1 et nombre2

    private static final double MIN_W = 60;       // tailles min
    private static final double MIN_H = 60;

    // Pour éventuellement visualiser la zone du canvas
    private static final boolean SHOW_DEBUG = false;

    public PanDessin() {
        // Couleur de fond (au cas où le parent est gris)
        setStyle("-fx-background-color: white;");

        // Taille initiale avant premier calcul
        setWidth(200);
        setHeight(150);

        // Redessiner quand la scène est prête (optionnel, mais utile)
        sceneProperty().addListener((obs, oldS, newS) -> draw());
    }

    // ----------------- API publique -----------------
    public void setNombre1(int n) {
        this.nombre1 = Math.max(0, n);
        draw();
    }

    public void setNombre2(int n) {
        this.nombre2 = Math.max(0, n);
        draw();
    }



    public int getNombre1() { return nombre1; }
    public int getNombre2() { return nombre2; }

    public void setFichierImage1(File f) {
        this.fichierImage1 = f;
        this.img1 = loadImage(f);
        draw();
    }

    public void setFichierImage2(File f) {
        this.fichierImage2 = f;
        this.img2 = loadImage(f);
        draw();
    }

    // ----------------- Chargement d’images -----------------
    private Image loadImage(File f) {
        if (f == null) return null;
        try {
            Image im = new Image(f.toURI().toString(), false);
            if (im.isError()) {
                System.err.println("Erreur chargement image: " + f + " -> " + im.getException());
                return null;
            }
            return im;
        } catch (Exception ex) {
            System.err.println("Exception image: " + f + " -> " + ex);
            return null;
        }
    }

    // ----------------- Dessin + redimensionnement -----------------
    public void draw() {
        boolean empty1 = (nombre1 <= 0 || img1 == null);
        boolean empty2 = (nombre2 <= 0 || img2 == null);

        // Taille de chaque bloc (nombre1 / nombre2)
        double w1 = empty1 ? 0 : gridWidth(nombre1);
        double h1 = empty1 ? 0 : gridHeight(nombre1);
        double w2 = empty2 ? 0 : gridWidth(nombre2);
        double h2 = empty2 ? 0 : gridHeight(nombre2);

        double contentW = w1 + w2;
        if (!empty1 && !empty2) {
            contentW += BLOCK_GAP;   // espace entre les deux groupes
        }
        double contentH = Math.max(h1, h2);

        // Taille nécessaire du canvas (contenu + marges)
        double needW = Math.max(MIN_W, contentW + 2 * PADDING);
        double needH = Math.max(MIN_H, contentH + 2 * PADDING);

        // Redimensionnement du canvas
        if (getWidth() != needW)  setWidth(needW);
        if (getHeight() != needH) setHeight(needH);

        double W = getWidth();
        double H = getHeight();

        GraphicsContext g = getGraphicsContext2D();

        // Fond blanc
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, W, H);

        if (SHOW_DEBUG) {
            g.setFill(Color.web("#f6f8ff"));
            g.fillRect(0, 0, W, H);
            g.setStroke(Color.DARKGRAY);
            g.setLineWidth(1);
            g.strokeRect(0.5, 0.5, W - 1, H - 1);
        }

        if (empty1 && empty2) {
            // rien à dessiner
            g.setStroke(Color.BLACK);
            g.setLineWidth(1);
            g.strokeRect(0.5, 0.5, W - 1, H - 1);
            return;
        }

        // Centrer horizontalement le contenu
        double baseX = (W - contentW) / 2.0;
        if (baseX < PADDING) baseX = PADDING; // sécurité
        double baseY = PADDING;

        // Dessiner le premier nombre
        if (!empty1) {
            drawGrid(g, baseX, baseY, nombre1, img1);
        }

        // Dessiner le deuxième nombre à droite avec un espace
        if (!empty2) {
            double startX2;
            if (empty1) {
                // si seule la 2e image existe, on la centre sans w1
                startX2 = baseX;
            } else {
                startX2 = baseX + w1 + BLOCK_GAP;
            }
            drawGrid(g, startX2, baseY, nombre2, img2);
        }

        // Cadre global du canvas
        g.setStroke(Color.BLACK);
        g.setLineWidth(1);
        g.strokeRect(0.5, 0.5, W - 1, H - 1);
    }

    // ----------------- Helpers de layout -----------------
    private static int rowsFor(int count) {
        return count <= 0 ? 0 : (count - 1) / COLS + 1;
    }

    private static int colsFor(int count) {
        return Math.min(COLS, Math.max(0, count));
    }

    private static double gridWidth(int count) {
        int cols = colsFor(count);
        return cols == 0 ? 0 : cols * (LARGEUR_ICONE + H_GAP) - H_GAP;
    }

    private static double gridHeight(int count) {
        int rows = rowsFor(count);
        return rows == 0 ? 0 : rows * (HAUTEUR_ICONE + V_GAP) - V_GAP;
    }

    private void drawGrid(GraphicsContext g, double startX, double startY,
                          int count, Image img) {
        if (count <= 0 || img == null) return;

        for (int i = 0; i < count; i++) {
            int row = i / COLS;
            int col = i % COLS;

            double x = startX + col * (LARGEUR_ICONE + H_GAP);
            double y = startY + row * (HAUTEUR_ICONE + V_GAP);

            g.drawImage(img, x, y, LARGEUR_ICONE, HAUTEUR_ICONE);
        }
    }
}
