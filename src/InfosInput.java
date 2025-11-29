/**
 * Classe pour encapsuler les informations de configuration du dialogue InputDialog.
 * Contient tous les paramètres sélectionnés par l'utilisateur.
 */
public class InfosInput {

    // ========== ATTRIBUTS ==========

    /** Nombre de captures à générer */
    protected int capturesNumber;

    /** Valeur minimale (mode simple) */
    protected int minSimpleValue;

    /** Valeur maximale (mode simple) */
    protected int maxSimpleValue;

    /** Somme maximale (mode multiple - somme) */
    protected int maxSumValue;

    /** Nombre maximal (mode multiple - nombre) */
    protected int maxNumberValue;

    /** Génération aléatoire des nombres ? */
    protected boolean randomNumbers;

    /** Utilise le mode somme maximale ? */
    protected boolean maxSum;

    /** Utilise le mode nombre maximal ? */
    protected boolean maxNumber;

    /** Images aléatoires ? */
    protected boolean randomImage;

    /** Mode multiple ? (sinon simple) */
    protected boolean multiple;

    /** Mode simple ? */
    protected boolean simple;

    // ========== CONSTRUCTEUR ==========

    /**
     * Constructeur par défaut.
     * Initialise tous les champs à des valeurs neutres.
     */
    public InfosInput() {
        this.capturesNumber  = 0;
        this.minSimpleValue  = 0;
        this.maxSimpleValue  = 0;
        this.maxSumValue     = 0;
        this.maxNumberValue  = 0;

        this.randomNumbers   = false;
        this.maxSum          = false;
        this.maxNumber       = false;
        this.randomImage     = false;
        this.multiple        = false;
        this.simple          = false;
    }

    // ========== GETTERS / SETTERS ==========

    public int getCapturesNumber() {
        return capturesNumber;
    }

    public void setCapturesNumber(int capturesNumber) {
        this.capturesNumber = capturesNumber;
    }

    public int getMinSimpleValue() {
        return minSimpleValue;
    }

    public void setMinSimpleValue(int minSimpleValue) {
        this.minSimpleValue = minSimpleValue;
    }

    public int getMaxSimpleValue() {
        return maxSimpleValue;
    }

    public void setMaxSimpleValue(int maxSimpleValue) {
        this.maxSimpleValue = maxSimpleValue;
    }

    public int getMaxSumValue() {
        return maxSumValue;
    }

    public void setMaxSumValue(int maxSumValue) {
        this.maxSumValue = maxSumValue;
    }

    public int getMaxNumberValue() {
        return maxNumberValue;
    }

    public void setMaxNumberValue(int maxNumberValue) {
        this.maxNumberValue = maxNumberValue;
    }

    public boolean isRandomNumbers() {
        return randomNumbers;
    }

    public void setRandomNumbers(boolean randomNumbers) {
        this.randomNumbers = randomNumbers;
    }

    public boolean isMaxSum() {
        return maxSum;
    }

    public void setMaxSum(boolean maxSum) {
        this.maxSum = maxSum;
    }

    public boolean isMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(boolean maxNumber) {
        this.maxNumber = maxNumber;
    }

    public boolean isRandomImage() {
        return randomImage;
    }

    public void setRandomImage(boolean randomImage) {
        this.randomImage = randomImage;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    // ========== DEBUG (optionnel) ==========

    @Override
    public String toString() {
        return "InfosInput{" +
                "capturesNumber=" + capturesNumber +
                ", minSimpleValue=" + minSimpleValue +
                ", maxSimpleValue=" + maxSimpleValue +
                ", maxSumValue=" + maxSumValue +
                ", maxNumberValue=" + maxNumberValue +
                ", randomNumbers=" + randomNumbers +
                ", maxSum=" + maxSum +
                ", maxNumber=" + maxNumber +
                ", randomImage=" + randomImage +
                ", multiple=" + multiple +
                ", simple=" + simple +
                '}';
    }
}
