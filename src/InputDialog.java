import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.prefs.Preferences;

public class InputDialog extends Dialog<InfosInput> {

    // ========= Clés Preferences =========
    private static final String PREF_SIMPLE_MIN_NUMBER = "simpleMinNumber";
    private static final String PREF_SIMPLE_MAX_NUMBER = "simpleMaxNumber";
    private static final String PREF_CAPTURES_NUMBER   = "capturesNumber";
    private static final String PREF_SELECTED_MODE     = "selectedMode";
    private static final String PREF_MAX_SUM_VALUE     = "maxSumValue";
    private static final String PREF_MAX_NUMBER_VALUE  = "maxNumberValue";
    private static final String PREF_RANDOM_NUMBERS    = "randomNumbers";
    private static final String PREF_RANDOM_IMAGES     = "randomImages";

    // ========= Valeurs par défaut =========
    private static final int DEFAULT_CAPTURES            = 10;
    private static final int DEFAULT_MODE_INDEX          = 0;
    private static final int DEFAULT_MAX_SUM             = 30;
    private static final int DEFAULT_MAX_NUMBER          = 15;
    private static final int DEFAULT_SIMPLE_MIN_NUMBER   = 1;
    private static final int DEFAULT_SIMPLE_MAX_NUMBER   = 15;
    private static final boolean DEFAULT_RANDOM_NUMBERS  = false;
    private static final boolean DEFAULT_RANDOM_IMAGES   = true;

    // ========= FXML =========

    @FXML
    private TextField txtCapturesNumber;
    @FXML
    private ComboBox<String> modeComboBox;

    @FXML
    private StackPane stackModeOptions;

    @FXML
    private VBox paneSimple;
    @FXML
    private HBox paneMaxSum;
    @FXML
    private HBox paneMaxNumber;

    // Mode simple
    @FXML
    private TextField txtSimpleMinNumber;
    @FXML
    private TextField txtSimpleMaxNumber;
    @FXML
    private CheckBox chkSimpleRandomImages;
    @FXML
    private CheckBox chkSimpleRandomNumbers;

    // Mode somme max
    @FXML
    private TextField txtMaxSum;
    @FXML
    private CheckBox chkSumRandomNumbers;
    @FXML
    private CheckBox chkSumRandomImages;

    // Mode nombre max
    @FXML
    private TextField txtMaxNumber;
    @FXML
    private CheckBox chkNumberRandomNumbers;
    @FXML
    private CheckBox chkNumberRandomImages;

    @FXML
    private Label lblError;

    @FXML
    private ButtonType btnTypeOk; // défini dans le FXML

    private Preferences prefs;

    // ========= Constructeur =========

    public InputDialog(javafx.stage.Window owner) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/InputDialog.fxml"));
        loader.setController(this);

        DialogPane pane;
        try {
            pane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Erreur chargement InputDialog.fxml", e);
        }

        setDialogPane(pane);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Programmer");
        setResizable(true);
        getDialogPane().setPrefSize(500, 300);

        prefs = Preferences.userNodeForPackage(InputDialog.class);
        initLogic();
    }

    // ========= Initialisation logique =========

    private void initLogic() {
        // Modes
        modeComboBox.getItems().setAll(
                "Simple",
                "Multiple - Somme maximale",
                "Multiple - Nombre maximal"
        );

        // Écouteur changement de mode
        modeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateModePanel()
        );

        loadPreferences();
        updateModePanel();

        // Configurer le bouton OK pour valider
        Node okButton = getDialogPane().lookupButton(btnTypeOk);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!validateInput()) {
                evt.consume(); // on bloque la fermeture
            } else {
                savePreferences();
            }
        });

        // Result converter
        setResultConverter(btn -> {
            if (btn == btnTypeOk && validateInput()) {
                return createResult();
            }
            return null;
        });
    }

    // ========= Chargement / sauvegarde des prefs =========

    private void loadPreferences() {
        int capturesNumber    = prefs.getInt(PREF_CAPTURES_NUMBER, DEFAULT_CAPTURES);
        int selectedModeIndex = prefs.getInt(PREF_SELECTED_MODE, DEFAULT_MODE_INDEX);
        int maxSumValue       = prefs.getInt(PREF_MAX_SUM_VALUE, DEFAULT_MAX_SUM);
        int maxNumberValue    = prefs.getInt(PREF_MAX_NUMBER_VALUE, DEFAULT_MAX_NUMBER);
        int simpleMinNumber   = prefs.getInt(PREF_SIMPLE_MIN_NUMBER, DEFAULT_SIMPLE_MIN_NUMBER);
        int simpleMaxNumber   = prefs.getInt(PREF_SIMPLE_MAX_NUMBER, DEFAULT_SIMPLE_MAX_NUMBER);
        boolean randomNumbers = prefs.getBoolean(PREF_RANDOM_NUMBERS, DEFAULT_RANDOM_NUMBERS);
        boolean randomImages  = prefs.getBoolean(PREF_RANDOM_IMAGES, DEFAULT_RANDOM_IMAGES);

        txtCapturesNumber.setText(String.valueOf(capturesNumber));

        if (selectedModeIndex >= 0 && selectedModeIndex < modeComboBox.getItems().size()) {
            modeComboBox.getSelectionModel().select(selectedModeIndex);
        } else {
            modeComboBox.getSelectionModel().selectFirst();
        }

        txtMaxSum.setText(String.valueOf(maxSumValue));
        txtMaxNumber.setText(String.valueOf(maxNumberValue));
        txtSimpleMinNumber.setText(String.valueOf(simpleMinNumber));
        txtSimpleMaxNumber.setText(String.valueOf(simpleMaxNumber));

        // Appliquer états des checkboxes
        chkSimpleRandomImages.setSelected(randomImages);
        chkSumRandomImages.setSelected(randomImages);
        chkNumberRandomImages.setSelected(randomImages);

        chkSumRandomNumbers.setSelected(randomNumbers);
        chkNumberRandomNumbers.setSelected(randomNumbers);
        chkSimpleRandomNumbers.setSelected(randomNumbers);
    }

    private void savePreferences() {
        try {
            int captures = Integer.parseInt(txtCapturesNumber.getText().trim());
            prefs.putInt(PREF_CAPTURES_NUMBER, captures);

            int modeIndex = modeComboBox.getSelectionModel().getSelectedIndex();
            prefs.putInt(PREF_SELECTED_MODE, modeIndex);

            if (!txtMaxSum.getText().trim().isEmpty()) {
                prefs.putInt(PREF_MAX_SUM_VALUE,
                        Integer.parseInt(txtMaxSum.getText().trim()));
            }
            if (!txtMaxNumber.getText().trim().isEmpty()) {
                prefs.putInt(PREF_MAX_NUMBER_VALUE,
                        Integer.parseInt(txtMaxNumber.getText().trim()));
            }
            if (!txtSimpleMinNumber.getText().trim().isEmpty()) {
                prefs.putInt(PREF_SIMPLE_MIN_NUMBER,
                        Integer.parseInt(txtSimpleMinNumber.getText().trim()));
            }
            if (!txtSimpleMaxNumber.getText().trim().isEmpty()) {
                prefs.putInt(PREF_SIMPLE_MAX_NUMBER,
                        Integer.parseInt(txtSimpleMaxNumber.getText().trim()));
            }

            boolean randomNumbers = isRandomNumbersSelected();
            boolean randomImages  = isRandomImagesSelected();

            prefs.putBoolean(PREF_RANDOM_NUMBERS, randomNumbers);
            prefs.putBoolean(PREF_RANDOM_IMAGES, randomImages);

            prefs.flush();
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde prefs: " + e.getMessage());
        }
    }

    // ========= Logic UI =========

    private void updateModePanel() {
        String selected = modeComboBox.getSelectionModel().getSelectedItem();
        if (selected == null) selected = "";

        paneSimple.setVisible(false);
        paneSimple.setManaged(false);
        paneMaxSum.setVisible(false);
        paneMaxSum.setManaged(false);
        paneMaxNumber.setVisible(false);
        paneMaxNumber.setManaged(false);

        if (selected.contains("Somme")) {
            paneMaxSum.setVisible(true);
            paneMaxSum.setManaged(true);
        } else if (selected.contains("Nombre")) {
            paneMaxNumber.setVisible(true);
            paneMaxNumber.setManaged(true);
        } else {
            // Mode Simple
            paneSimple.setVisible(true);
            paneSimple.setManaged(true);
        }

        lblError.setText("");
    }

    private boolean validateInput() {
        lblError.setText("");

        try {
            String capturesText = txtCapturesNumber.getText().trim();
            if (capturesText.isEmpty()) {
                showError("Le nombre de captures est requis.");
                return false;
            }
            int captures = Integer.parseInt(capturesText);
            if (captures <= 0) {
                showError("Le nombre de captures doit être positif.");
                return false;
            }

            String selectedMode = modeComboBox.getSelectionModel().getSelectedItem();
            if (selectedMode == null) selectedMode = "";

            if (selectedMode.contains("Somme")) {
                String maxSumText = txtMaxSum.getText().trim();
                if (maxSumText.isEmpty()) {
                    showError("La somme maximale est requise.");
                    return false;
                }
                int maxSum = Integer.parseInt(maxSumText);
                if (maxSum <= 0 || maxSum > 30) {
                    showError("La somme maximale doit être positive et ≤ 30.");
                    txtMaxSum.clear();
                    return false;
                }
            } else if (selectedMode.contains("Nombre")) {
                String maxNumberText = txtMaxNumber.getText().trim();
                if (maxNumberText.isEmpty()) {
                    showError("Le nombre maximal est requis.");
                    return false;
                }
                int maxNumber = Integer.parseInt(maxNumberText);
                if (maxNumber <= 0 || maxNumber > 15) {
                    showError("Le nombre maximal doit être positif et ≤ 15.");
                    txtMaxNumber.clear();
                    return false;
                }
            } else {
                // Mode Simple : vérifier Min et Max
                String simpleMinText = txtSimpleMinNumber.getText().trim();
                String simpleMaxText = txtSimpleMaxNumber.getText().trim();

                if (simpleMinText.isEmpty() || simpleMaxText.isEmpty()) {
                    showError("Les valeurs Min et Max (mode simple) sont requises.");
                    return false;
                }

                int simpleMin = Integer.parseInt(simpleMinText);
                int simpleMax = Integer.parseInt(simpleMaxText);

                if (simpleMin <= 0) {
                    showError("Le Min (mode simple) doit être positif.");
                    txtSimpleMinNumber.requestFocus();
                    return false;
                }
                if (simpleMax <= 0 || simpleMax > 15) {
                    showError("Le Max (mode simple) doit être positif et ≤ 15.");
                    txtSimpleMaxNumber.requestFocus();
                    return false;
                }
                if (simpleMin > simpleMax) {
                    showError("En mode simple, Min doit être ≤ Max.");
                    txtSimpleMinNumber.requestFocus();
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            showError("Veuillez entrer des nombres valides.");
            return false;
        }
    }

    private void showError(String message) {
        lblError.setText(message);
    }

    // ========= Récupération des infos sélectionnées =========

    private boolean isRandomNumbersSelected() {
        String selectedMode = modeComboBox.getSelectionModel().getSelectedItem();
        if (selectedMode == null) selectedMode = "";

        if (selectedMode.contains("Somme")) {
            return chkSumRandomNumbers.isSelected();
        } else if (selectedMode.contains("Nombre")) {
            return chkNumberRandomNumbers.isSelected();
        } else {
            // Mode simple
            return chkSimpleRandomNumbers.isSelected();
        }
    }

    private boolean isRandomImagesSelected() {
        String selectedMode = modeComboBox.getSelectionModel().getSelectedItem();
        if (selectedMode == null) selectedMode = "";

        if (selectedMode.contains("Somme")) {
            return chkSumRandomImages.isSelected();
        } else if (selectedMode.contains("Nombre")) {
            return chkNumberRandomImages.isSelected();
        } else {
            return chkSimpleRandomImages.isSelected();
        }
    }

    private InfosInput createResult() {
        InfosInput result = new InfosInput();

        int captures = Integer.parseInt(txtCapturesNumber.getText().trim());
        result.setCapturesNumber(captures);

        String selectedMode = modeComboBox.getSelectionModel().getSelectedItem();
        if (selectedMode == null) selectedMode = "";

        boolean randomNumbers = isRandomNumbersSelected();
        boolean randomImages  = isRandomImagesSelected();
        result.setRandomImage(randomImages);

        if (selectedMode.contains("Somme")) {
            result.setMultiple(true);
            result.setMaxSum(true);
            result.setMaxNumber(false);
            result.setSimple(false);

            result.setMaxSumValue(Integer.parseInt(txtMaxSum.getText().trim()));
            result.setMaxNumberValue(0);

            result.setMinSimpleValue(0);
            result.setMaxSimpleValue(0);

            result.setRandomNumbers(randomNumbers);

        } else if (selectedMode.contains("Nombre")) {
            result.setMultiple(true);
            result.setMaxSum(false);
            result.setMaxNumber(true);
            result.setSimple(false);

            result.setMaxNumberValue(Integer.parseInt(txtMaxNumber.getText().trim()));
            result.setMaxSumValue(0);

            result.setMinSimpleValue(0);
            result.setMaxSimpleValue(0);

            result.setRandomNumbers(randomNumbers);

        } else {
            // Mode simple
            result.setMultiple(false);
            result.setMaxSum(false);
            result.setMaxNumber(false);
            result.setSimple(true);

            int simpleMin = Integer.parseInt(txtSimpleMinNumber.getText().trim());
            int simpleMax = Integer.parseInt(txtSimpleMaxNumber.getText().trim());

            result.setMaxSumValue(0);
            result.setMaxNumberValue(0);

            result.setMinSimpleValue(simpleMin);
            result.setMaxSimpleValue(simpleMax);

            result.setRandomNumbers(chkSimpleRandomNumbers.isSelected());
        }

        return result;
    }

    // ========= API statique =========

    public static InfosInput showInputDialog(javafx.stage.Window owner) {
        InputDialog dialog = new InputDialog(owner);
        return dialog.showAndWait().orElse(null);
    }
}
