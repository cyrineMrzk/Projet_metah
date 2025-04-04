import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principale pour exécuter les benchmarks du Problème de Couverture d'Ensemble 
 * avec l'algorithme PSO en utilisant différentes configurations
 */
public class Main {
    public static void main(String[] args) {
        // Créer la configuration par défaut
        PSOConfig defaultConfig = new PSOConfig();
        
        // Créer le répertoire de benchmark
        File dir = new File(defaultConfig.benchmarkDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Erreur: Répertoire de benchmark non trouvé. Exécutez SCPDownloader d'abord.");
            return;
        }
        
        // Lire les configurations depuis le fichier CSV
        List<ConfigReader.ConfigEntry> configurations;
        try {
            configurations = ConfigReader.readConfigFile("pso_params.csv");
            if (configurations.isEmpty()) {
                System.out.println("Aucune configuration trouvée dans le fichier CSV. Utilisation de la configuration par défaut.");
                configurations = new ArrayList<>();
                configurations.add(new ConfigReader.ConfigEntry(
                    defaultConfig.numParticles, 
                    defaultConfig.maxIterations, 
                    defaultConfig.numRuns, 
                    defaultConfig.c1, 
                    defaultConfig.c2, 
                    defaultConfig.w
                ));
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier de configuration: " + e.getMessage());
            System.out.println("Utilisation de la configuration par défaut.");
            configurations = new ArrayList<>();
            configurations.add(new ConfigReader.ConfigEntry(
                defaultConfig.numParticles, 
                defaultConfig.maxIterations, 
                defaultConfig.numRuns, 
                defaultConfig.c1, 
                defaultConfig.c2, 
                defaultConfig.w
            ));
        }
        
        // Préparer le fichier de résultats
        try (PrintWriter resultWriter = new PrintWriter(new FileWriter("pso_results.csv"))) {
            // Écrire l'en-tête du fichier CSV
            resultWriter.println("ConfigID,Benchmark,Elements,Subsets,k,numParticles,maxIterations,c1,c2,w,AvgCoverage,AvgTime(ms)");
            
            // Pour chaque configuration
            for (int configID = 0; configID < configurations.size(); configID++) {
                ConfigReader.ConfigEntry configEntry = configurations.get(configID);
                
                // Créer et configurer les paramètres
                PSOConfig config = new PSOConfig();
                config.updateFromConfigEntry(configEntry);
                
                System.out.println("\n==================================================");
                System.out.println("Configuration #" + (configID + 1) + ":");
                System.out.println("- Particules PSO: " + config.numParticles);
                System.out.println("- Itérations max PSO: " + config.maxIterations);
                System.out.println("- Paramètres PSO: w=" + config.w + ", c1=" + config.c1 + ", c2=" + config.c2);
                System.out.println("- Nombre d'exécutions par benchmark: " + config.numRuns);
                System.out.println("==================================================");
                
                // Exécuter chaque benchmark avec cette configuration
                for (String fileName : config.benchmarkFiles) {
                    System.out.println("\nTest du fichier benchmark: " + fileName);
                    
                    try {
                        // Analyser le fichier benchmark
                        SCPInstance instance = parseSCPFile(config.benchmarkDir + File.separator + fileName);
                        
                        // Calculer k (nombre de sous-ensembles à sélectionner) selon la config
                        int k = instance.m / config.kDivisor;
                        if (k < 1) k = 1;
                        
                        // Afficher les informations d'instance
                        System.out.println("Nombre de lignes (éléments): " + instance.n);
                        System.out.println("Nombre de colonnes (sous-ensembles): " + instance.m);
                        System.out.println("Nombre de sous-ensembles à sélectionner (k): " + k);
                        
                        // Variables pour les résultats moyens
                        long totalTime = 0;
                        int totalCoverage = 0;
                        
                        // Exécuter plusieurs fois pour obtenir les performances moyennes
                        for (int run = 1; run <= config.numRuns; run++) {
                            System.out.println("Exécution " + run + "/" + config.numRuns);
                            
                            // Créer une instance PSO_MCP avec la configuration
                            PSO_MCP pso = new PSO_MCP(instance.subsets, instance.n, config);
                            
                            // Démarrer le chronomètre
                            long startTime = System.currentTimeMillis();
                            
                            // Exécuter l'algorithme PSO
                            Particle[] result = pso.solution(config.numParticles, k, instance.m);
                            
                            // Arrêter le chronomètre
                            long endTime = System.currentTimeMillis();
                            long runTime = endTime - startTime;
                            
                            // Obtenir le meilleur fitness de l'exécution
                            int bestFitness = 0;
                            for (Particle p : result) {
                                if (p.pBest > bestFitness) {
                                    bestFitness = p.pBest;
                                }
                            }
                            
                            // Ajouter aux totaux
                            totalTime += runTime;
                            totalCoverage += bestFitness;
                            
                            // Afficher les résultats de l'exécution
                            System.out.println("Exécution " + run + " temps: " + runTime + " ms, couverture: " +
                                            bestFitness + "/" + instance.n);
                        }
                        
                        // Calculer et afficher les moyennes
                        double avgTime = totalTime / (double)config.numRuns;
                        double avgCoverage = totalCoverage / (double)config.numRuns;
                        
                        System.out.println("\nRésultats moyens pour " + fileName + ":");
                        System.out.println("Temps d'exécution moyen: " + avgTime + " ms");
                        System.out.println("Couverture moyenne: " + avgCoverage + "/" + instance.n +
                                        " (" + (avgCoverage/instance.n*100) + "%)");
                        
                        // Ajouter au résumé CSV
                        resultWriter.println(
                            (configID + 1) + "," +  // ConfigID
                            fileName + "," + 
                            instance.n + "," +
                            instance.m + "," + 
                            k + "," + 
                            config.numParticles + "," +
                            config.maxIterations + "," +
                            config.c1 + "," +
                            config.c2 + "," +
                            config.w + "," +
                            avgCoverage + "," + 
                            avgTime
                        );
                        resultWriter.flush();
                        
                    } catch (IOException e) {
                        System.out.println("Erreur lors du traitement du fichier " + fileName + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("\nTous les tests terminés. Résultats enregistrés dans pso_results.csv");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture des résultats: " + e.getMessage());
        }
    }
    
    /**
     * Analyser un fichier benchmark du Problème de Couverture d'Ensemble au format JSON-like
     */
    private static SCPInstance parseSCPFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Lire tout le contenu du fichier
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
            
            // Analyser le contenu comme une liste de listes
            String fileContent = content.toString();
            
            // Diviser le contenu par "], [" pour obtenir des listes de sous-ensembles individuels
            String[] subsetStrings = fileContent.split("\\],\\s*\\[");
            
            // Nettoyer les chaînes du premier et du dernier sous-ensemble
            if (subsetStrings.length > 0) {
                subsetStrings[0] = subsetStrings[0].replaceFirst("^\\s*\\[", "");
                int lastIndex = subsetStrings.length - 1;
                subsetStrings[lastIndex] = subsetStrings[lastIndex].replaceFirst("\\]\\s*$", "");
            }
            
            // Compter le nombre de sous-ensembles (m)
            int m = subsetStrings.length;
            
            // Trouver le numéro d'élément maximum pour déterminer n
            int maxElement = 0;
            List<List<Integer>> parsedSubsets = new ArrayList<>();
            
            for (String subsetString : subsetStrings) {
                List<Integer> subset = new ArrayList<>();
                // Diviser par des virgules et analyser chaque nombre
                String[] elements = subsetString.split(",");
                for (String element : elements) {
                    // Nettoyer et analyser l'élément
                    element = element.trim();
                    if (!element.isEmpty()) {
                        try {
                            int elementNum = Integer.parseInt(element);
                            subset.add(elementNum);
                            if (elementNum > maxElement) {
                                maxElement = elementNum;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Avertissement: Élément invalide ignoré: " + element);
                        }
                    }
                }
                parsedSubsets.add(subset);
            }
            
            // Le nombre d'éléments (n) est le numéro d'élément maximum
            int n = maxElement;
            
            System.out.println("Fichier analysé avec " + n + " éléments et " + m + " sous-ensembles");
            
            // Créer la représentation matricielle booléenne
            boolean[][] subsets = new boolean[m][n];
            
            // Remplir la matrice
            for (int i = 0; i < parsedSubsets.size(); i++) {
                List<Integer> subset = parsedSubsets.get(i);
                for (int element : subset) {
                    // Convertir en index basé sur 0
                    if (element > 0 && element <= n) {
                        subsets[i][element - 1] = true;
                    }
                }
            }
            
            return new SCPInstance(n, m, subsets);
        }
    }
    
    /**
     * Classe pour contenir une instance du Problème de Couverture d'Ensemble
     */
    static class SCPInstance {
        int n;  // Nombre d'éléments
        int m;  // Nombre de sous-ensembles
        boolean[][] subsets;
        
        public SCPInstance(int n, int m, boolean[][] subsets) {
            this.n = n;
            this.m = m;
            this.subsets = subsets;
        }
    }
}