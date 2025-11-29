import java.util.ArrayList;
import java.util.HashMap;

public class DefaultCategories {

    public static HashMap<String, ArrayList<String>> create() {

        HashMap<String, ArrayList<String>> categories = new HashMap<>();

        // --- Animaux ---
        ArrayList<String> animaux = new ArrayList<>();
        animaux.add("chat.png");
        animaux.add("chien.png");
        animaux.add("cheval.png");
        animaux.add("ane.png");
        animaux.add("canard.png");
        animaux.add("girafe.png");
        animaux.add("lelephant.png");
        animaux.add("lion.png");
        animaux.add("poulet.png");
        animaux.add("renard.png");
        animaux.add("singe.png");
        animaux.add("souris.png");
        categories.put("Animaux", animaux);

        // --- Moyens de transport ---
        ArrayList<String> transports = new ArrayList<>();
        transports.add("voiture.png");
        transports.add("velo.png");
        transports.add("moto.png");
        transports.add("ambulance.png");
        transports.add("autobus.png");
        transports.add("avion (1).png");
        transports.add("avion.png");
        transports.add("bateau (1).png");
        transports.add("bateau (2).png");
        transports.add("bateau-cargo.png");
        transports.add("bus-scolaire.png");
        transports.add("en-voyageant.png");
        transports.add("helicoptere (1).png");
        transports.add("helicoptere.png");
        transports.add("moto (1).png");
        transports.add("moto (2).png");
        transports.add("navire-pirate.png");
        transports.add("velo (1).png");
        transports.add("voiture-de-luxe.png");
        transports.add("voiture-de-police.png");
        transports.add("voiture-decapotable.png");
        categories.put("Moyens de transports", transports);

        ArrayList<String> fruits = new ArrayList<>();
        fruits.add("abricot.png");
        fruits.add("avocat.png");
        fruits.add("banane.png");
        fruits.add("citron-vert.png");
        fruits.add("citron.png");
        fruits.add("fruit.png");
        fruits.add("grenade.png");
        fruits.add("kiwi.png");
        fruits.add("noix-de-coco.png");
        fruits.add("noix.png");
        fruits.add("orange.png");
        fruits.add("peche.png");
        fruits.add("pomme (1).png");
        fruits.add("pomme.png");
        fruits.add("tannat.png");
        fruits.add("tomate.png");
        categories.put("Fruits", fruits);

        return categories;
    }
}
