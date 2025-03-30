package com.zouhair;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Classe utilitaire pour la gestion des langues dans l'application
 */
public class LanguageManager {
    private static final String LANGUAGE_PREF_KEY = "app_language";
    private static final String RESOURCE_BUNDLE_NAME = "resources.i18n.messages";

    private static Locale currentLocale;
    private static ResourceBundle bundle;

    // Locales disponibles
    public static final Locale LOCALE_FR = new Locale("fr", "FR");
    public static final Locale LOCALE_EN = new Locale("en", "US");
    public static final Locale LOCALE_AR = new Locale("ar", "MA");

    private static final Preferences prefs = Preferences.userNodeForPackage(NombresImages.class);

    /**
     * Initialise le gestionnaire de langue avec la langue sauvegardée ou la locale par défaut
     */
    public static void initialize() {
        String savedLanguage = prefs.get(LANGUAGE_PREF_KEY, Locale.getDefault().getLanguage());

        switch (savedLanguage) {
            case "fr":
                currentLocale = LOCALE_FR;
                break;
            case "en":
                currentLocale = LOCALE_EN;
                break;
            case "ar":
                currentLocale = LOCALE_AR;
                break;
            default:
                currentLocale = LOCALE_FR; // Français par défaut
        }

        loadResourceBundle();
    }

    /**
     * Charge le bundle de ressources correspondant à la locale actuelle
     */
    private static void loadResourceBundle() {
        bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, currentLocale);
    }

    /**
     * Change la langue de l'application
     * @param locale La nouvelle locale à utiliser
     * @return true si la langue a été changée, false sinon
     */
    public static boolean changeLanguage(Locale locale) {
        if (locale == null || locale.equals(currentLocale)) {
            return false;
        }

        currentLocale = locale;
        prefs.put(LANGUAGE_PREF_KEY, locale.getLanguage());
        loadResourceBundle();
        return true;
    }

    /**
     * Obtient la chaîne de caractères correspondant à la clé dans la langue actuelle
     * @param key La clé de la ressource
     * @return La chaîne traduite ou la clé si non trouvée
     */
    public static String getString(String key) {
        if (bundle == null) {
            loadResourceBundle();
        }

        try {
            return bundle.getString(key);
        } catch (Exception e) {
            System.err.println("Clé de ressource non trouvée: " + key);
            return key; // Retourne la clé comme fallback
        }
    }

    /**
     * Obtient la chaîne de caractères et remplace les paramètres
     * @param key La clé de la ressource
     * @param params Les paramètres à remplacer dans la chaîne
     * @return La chaîne traduite avec les paramètres remplacés
     */
    public static String getString(String key, Object... params) {
        String value = getString(key);
        if (params != null && params.length > 0) {
            return String.format(value, params);
        }
        return value;
    }

    /**
     * Retourne la locale actuelle
     * @return La locale utilisée
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Vérifie si la langue actuelle est une langue RTL (arabe, hébreu, etc.)
     * @return true si la langue est RTL, false sinon
     */
    public static boolean isRTL() {
        return "rtl".equals(getString("text.ltr"));
    }

    /**
     * Retourne l'orientation des composants à utiliser (LEFT_TO_RIGHT ou RIGHT_TO_LEFT)
     * @return java.awt.ComponentOrientation approprié à la langue actuelle
     */
    public static java.awt.ComponentOrientation getComponentOrientation() {
        String orientation = getString("component.orientation");
        return "right_to_left".equals(orientation) ?
                java.awt.ComponentOrientation.RIGHT_TO_LEFT :
                java.awt.ComponentOrientation.LEFT_TO_RIGHT;
    }
}