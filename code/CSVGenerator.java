import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVGenerator {
    public static void main(String[] args) {
        String csvFile = "config.csv";  // Le nom du fichier CSV à créer
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Écrire les entêtes
            writer.println("numParticles,maxIterations,numRuns,c1,c2,w");

            // Exemple de configurations variées de test
            for (int numParticles = 50; numParticles <= 500; numParticles += 50) {
                for (int maxIterations = 100; maxIterations <= 1500; maxIterations += 500) {
                    for (int numRuns = 5; numRuns <= 50; numRuns += 5) {
                        for (double c1 = 1.5; c1 <= 2.0; c1 += 0.5) {
                            for (double c2 = 1.5; c2 <= 2.0; c2 += 0.5) {
                                for (double w = 0.4; w <= 0.9; w += 0.1) {
                                    // Écrire chaque configuration dans le fichier CSV
                                    writer.printf("%d,%d,%d,%.1f,%.1f,%.1f%n", 
                                        numParticles, maxIterations, numRuns, c1, c2, w);
                                }
                            }
                        }
                    }
                }
            }
            
            System.out.println("Fichier CSV généré avec succès !");
        } catch (IOException e) {
            System.out.println("Erreur lors de la création du fichier CSV");
            e.printStackTrace();
        }
    }
}
