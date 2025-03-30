import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class Test extends JFrame {
    private JList<String> list;
    private DefaultListModel<String> listModel;

    public Test() {
        // Créer le modèle de liste
        listModel = new DefaultListModel<>();
        listModel.addElement("Paris");
        listModel.addElement("Londres");
        listModel.addElement("Berlin");
        listModel.addElement("Madrid");
        listModel.addElement("Rome");

        // Créer la JList avec le modèle
        list = new JList<>(listModel);

        // Activer la sélection multiple
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Ajouter un ScrollPane pour le défilement
        JScrollPane scrollPane = new JScrollPane(list);

        // Ajouter un bouton pour voir les sélections
        JButton showButton = new JButton("Afficher la sélection");
        showButton.addActionListener(e -> {
            // Récupérer les éléments sélectionnés
            java.util.List<String> selectedItems = list.getSelectedValuesList();

            // Afficher les éléments sélectionnés
            String message = "Items sélectionnés:\n" + String.join("\n", selectedItems);
            JOptionPane.showMessageDialog(this, message);
        });

        // Layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(showButton, BorderLayout.SOUTH);

        // Configuration de la fenêtre
        setTitle("Sélection Multiple Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Test().setVisible(true);
        });
    }
}