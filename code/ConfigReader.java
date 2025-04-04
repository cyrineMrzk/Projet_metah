import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lecteur de configuration pour les paramètres PSO à partir d'un fichier CSV
 */
public class ConfigReader {
    
    /**
     * Classe pour stocker une entrée de configuration
     */
    public static class ConfigEntry {
        public int numParticles;
        public int maxIterations;
        public int numRuns;
        public double c1;
        public double c2;
        public double w;
        
        public ConfigEntry(int numParticles, int maxIterations, int numRuns, 
                           double c1, double c2, double w) {
            this.numParticles = numParticles;
            this.maxIterations = maxIterations;
            this.numRuns = numRuns;
            this.c1 = c1;
            this.c2 = c2;
            this.w = w;
        }
        
        @Override
        public String toString() {
            return "ConfigEntry{" +
                   "numParticles=" + numParticles +
                   ", maxIterations=" + maxIterations +
                   ", numRuns=" + numRuns +
                   ", c1=" + c1 +
                   ", c2=" + c2 +
                   ", w=" + w +
                   '}';
        }
    }
    
    /**
     * Lit le fichier de paramètres PSO et retourne une liste d'entrées de configuration
     * @param filename Le nom du fichier CSV
     * @return Liste des entrées de configuration
     * @throws IOException Si une erreur se produit lors de la lecture
     */
    public static List<ConfigEntry> readConfigFile(String filename) throws IOException {
        List<ConfigEntry> entries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Lire l'en-tête (ignorer)
            String header = reader.readLine();
            
            // Lire chaque ligne
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Séparer les valeurs
                String[] values = line.split(",");
                if (values.length >= 6) {
                    try {
                        int numParticles = Integer.parseInt(values[0]);
                        int maxIterations = Integer.parseInt(values[1]);
                        int numRuns = Integer.parseInt(values[2]);
                        
                        // Pour gérer les nombres avec des virgules comme séparateur décimal
                        String c1Str = values[3].replace(",", ".");
                        String c2Str = values[4].replace(",", ".");
                        String wStr = values[5].replace(",", ".");
                        
                        double c1 = Double.parseDouble(c1Str);
                        double c2 = Double.parseDouble(c2Str);
                        double w = Double.parseDouble(wStr);
                        
                        ConfigEntry entry = new ConfigEntry(numParticles, maxIterations, numRuns, c1, c2, w);
                        entries.add(entry);
                    } catch (NumberFormatException e) {
                        System.out.println("Erreur de format dans la ligne: " + line);
                        System.out.println("Exception: " + e.getMessage());
                    }
                } else {
                    System.out.println("Format incorrect: " + line);
                }
            }
        }
        
        System.out.println("Lecture de " + entries.size() + " configurations depuis " + filename);
        return entries;
    }
}