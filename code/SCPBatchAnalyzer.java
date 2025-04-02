import java.io.*;
import java.util.*;

public class SCPBatchAnalyzer {
    public static void main(String[] args) {
        String filename = "C:\\Users\\MECHERI INFORMATIQUE\\Projet_metah\\scp_benchmark\\scpc3.txt"; // Fichier à modifier
        
        try {
            // 1. Lire le fichier original
            SCPData data = readSCPFile(filename);
            
            // 2. Préparer le nouveau contenu
            String newContent = formatColumns(data);
            
            // 3. Réécrire dans le même fichier
            writeToFile(filename, newContent);
            
            System.out.println("Fichier mis à jour avec succès !");
        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    // Lit le fichier SCP et extrait les données
    public static SCPData readSCPFile(String filename) throws IOException {
        List<List<Integer>> columns = new ArrayList<>();
        int numRows = 0, numCols = 0;

        try (Scanner scanner = new Scanner(new File(filename))) {
            numRows = scanner.nextInt();
            numCols = scanner.nextInt();
            scanner.nextLine(); // Ignorer la ligne des coûts

            for (int i = 0; i < numCols; i++) {
                List<Integer> coveredRows = new ArrayList<>();
                int nbCovered = scanner.nextInt();
                for (int j = 0; j < nbCovered; j++) {
                    coveredRows.add(scanner.nextInt());
                }
                columns.add(coveredRows);
            }
        }

        return new SCPData(numRows, numCols, columns);
    }

    // Formate les colonnes avec virgules entre elles
    public static String formatColumns(SCPData data) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < data.columns.size(); i++) {
            sb.append(data.columns.get(i).toString());
            if (i < data.columns.size() - 1) {
                sb.append(", ");
            }
            // Nouvelle ligne toutes les 3 colonnes pour lisibilité
            if ((i + 1) % 3 == 0) sb.append("\n"); 
        }
        
        return sb.toString();
    }

    // Écrit le nouveau contenu dans le fichier (écrase l'ancien)
    public static void writeToFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename, false)) { // 'false' pour écraser
            writer.write(content);
        }
    }

    // Structure pour stocker les données
    public static class SCPData {
        public int numRows, numCols;
        public List<List<Integer>> columns;

        public SCPData(int numRows, int numCols, List<List<Integer>> columns) {
            this.numRows = numRows;
            this.numCols = numCols;
            this.columns = columns;
        }
    }
}