import java.io.File;

public class GenerateArrayListCode {

    public static void main(String[] args) {

        // === Dossier à scanner ===
        File folder = new File("C:\\Users\\Zouhair\\Downloads\\images");

        // === Nom de la liste ===
        String listName = "fruits";

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Dossier invalide : " + folder.getAbsolutePath());
            return;
        }

        // === Parcourir les fichiers ===
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("Aucun fichier trouvé !");
            return;
        }

        System.out.println("===== Code généré =====");

        for (File f : files) {
            if (f.isFile()) {

                String name = f.getName();

                // Si tu veux filtrer uniquement les images :
                if (!name.toLowerCase().endsWith(".png") &&
                        !name.toLowerCase().endsWith(".jpg") &&
                        !name.toLowerCase().endsWith(".jpeg") &&
                        !name.toLowerCase().endsWith(".gif")) {
                    continue;
                }

                System.out.println(listName + ".add(\"" + name + "\");");
            }
        }

        System.out.println("===== Fin =====");
    }
}
