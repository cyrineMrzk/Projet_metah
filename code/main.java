import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class to run Set Covering Problem benchmarks with PSO algorithm
 */
public class Main {
    public static void main(String[] args) {
        // Create and configure parameters
        PSOConfig config = new PSOConfig();
        
        // Create benchmark directory
        File dir = new File(config.benchmarkDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Error: Benchmark directory not found. Run SCPDownloader first.");
            return;
        }
        
        // Results summary
        System.out.println("Test Parameters:");
        System.out.println("- Number of runs per benchmark: " + config.numRuns);
        System.out.println("- PSO particles: " + config.numParticles);
        System.out.println("- PSO max iterations: " + config.maxIterations);
        System.out.println("- PSO parameters: w=" + config.w + ", c1=" + config.c1 + ", c2=" + config.c2);
        System.out.println("\nBenchmark,Elements,Subsets,k,AvgCoverage,AvgTime(ms)");
        
        for (String fileName : config.benchmarkFiles) {
            System.out.println("\n=======================================================");
            System.out.println("Testing benchmark file: " + fileName);
            System.out.println("=======================================================");
            
            try {
                // Parse the benchmark file
                SCPInstance instance = parseSCPFile(config.benchmarkDir + File.separator + fileName);
                
                // Calculate k (number of subsets to select) based on config
                int k = instance.m / config.kDivisor;
                if (k < 1) k = 1;
                
                // Print instance information
                System.out.println("Number of rows (elements): " + instance.n);
                System.out.println("Number of columns (subsets): " + instance.m);
                System.out.println("Number of subsets to select (k): " + k);
                
                // Variables for averaging results
                long totalTime = 0;
                int totalCoverage = 0;
                
                // Run multiple times to get average performance
                for (int run = 1; run <= config.numRuns; run++) {
                    System.out.println("\nRun " + run + "/" + config.numRuns);
                    
                    // Create PSO_MCP instance with configuration
                    PSO_MCP pso = new PSO_MCP(instance.subsets, instance.n, config);
                    
                    // Start timer
                    long startTime = System.currentTimeMillis();
                    
                    // Run the PSO algorithm
                    Particle[] result = pso.solution(config.numParticles, k, instance.m);
                    
                    // End timer
                    long endTime = System.currentTimeMillis();
                    long runTime = endTime - startTime;
                    
                    // Get best fitness from the run
                    int bestFitness = 0;
                    for (Particle p : result) {
                        if (p.pBest > bestFitness) {
                            bestFitness = p.pBest;
                        }
                    }
                    
                    // Add to totals
                    totalTime += runTime;
                    totalCoverage += bestFitness;
                    
                    // Output run results
                    System.out.println("Run " + run + " time: " + runTime + " ms, coverage: " +
                                       bestFitness + "/" + instance.n);
                }
                
                // Calculate and display averages
                double avgTime = totalTime / (double)config.numRuns;
                double avgCoverage = totalCoverage / (double)config.numRuns;
                
                System.out.println("\nAverage Results for " + fileName + ":");
                System.out.println("Average execution time: " + avgTime + " ms");
                System.out.println("Average coverage: " + avgCoverage + "/" + instance.n +
                                   " (" + (avgCoverage/instance.n*100) + "%)");
                
                // Add to CSV-style summary
                System.out.println(fileName + "," + instance.n + "," +
                                   instance.m + "," + k + "," +
                                   avgCoverage + "," + avgTime);
                
            } catch (IOException e) {
                System.out.println("Error processing file " + fileName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Parse a Set Covering Problem benchmark file in JSON-like format
     */
    private static SCPInstance parseSCPFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Read the entire file content
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
            
            // Parse the content as a list of lists
            String fileContent = content.toString();
            
            // Split the content by "], [" to get individual subset lists
            String[] subsetStrings = fileContent.split("\\],\\s*\\[");
            
            // Clean up the first and last subset strings
            if (subsetStrings.length > 0) {
                subsetStrings[0] = subsetStrings[0].replaceFirst("^\\s*\\[", "");
                int lastIndex = subsetStrings.length - 1;
                subsetStrings[lastIndex] = subsetStrings[lastIndex].replaceFirst("\\]\\s*$", "");
            }
            
            // Count the number of subsets (m)
            int m = subsetStrings.length;
            
            // Find the maximum element number to determine n
            int maxElement = 0;
            List<List<Integer>> parsedSubsets = new ArrayList<>();
            
            for (String subsetString : subsetStrings) {
                List<Integer> subset = new ArrayList<>();
                // Split by commas and parse each number
                String[] elements = subsetString.split(",");
                for (String element : elements) {
                    // Clean and parse the element
                    element = element.trim();
                    if (!element.isEmpty()) {
                        try {
                            int elementNum = Integer.parseInt(element);
                            subset.add(elementNum);
                            if (elementNum > maxElement) {
                                maxElement = elementNum;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Warning: Skipping invalid element: " + element);
                        }
                    }
                }
                parsedSubsets.add(subset);
            }
            
            // The number of elements (n) is the maximum element number
            int n = maxElement;
            
            System.out.println("Parsed file with " + n + " elements and " + m + " subsets");
            
            // Create the boolean matrix representation
            boolean[][] subsets = new boolean[m][n];
            
            // Fill the matrix
            for (int i = 0; i < parsedSubsets.size(); i++) {
                List<Integer> subset = parsedSubsets.get(i);
                for (int element : subset) {
                    // Convert to 0-based index
                    if (element > 0 && element <= n) {
                        subsets[i][element - 1] = true;
                    }
                }
            }
            
            return new SCPInstance(n, m, subsets);
        }
    }
    
    /**
     * Class to hold a Set Covering Problem instance
     */
    static class SCPInstance {
        int n;  // Number of elements
        int m;  // Number of subsets
        boolean[][] subsets;
        
        public SCPInstance(int n, int m, boolean[][] subsets) {
            this.n = n;
            this.m = m;
            this.subsets = subsets;
        }
    }
}
