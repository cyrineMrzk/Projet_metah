import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Directory containing benchmark files
        String benchmarkDir = "scp_benchmark";
        File dir = new File(benchmarkDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Error: Benchmark directory not found. Run SCPDownloader first.");
            return;
        }
        
        // Configure testing parameters
        int numParticles = 30;   // Number of particles
        int maxIterations = 100; // Maximum iterations for PSO
        int numRuns = 5;         // Number of runs for each benchmark
      
        // List of benchmark files to test (you can customize this)
        String[] benchmarkFiles = {
            "scp41.txt",
            "scp51.txt",
            "scpa1.txt",
            "scpb1.txt",
            "scpc1.txt"
        };
        
        // Results summary
        System.out.println("Benchmark,Elements,Subsets,k,AvgCoverage,AvgTime(ms)");
        
        for (String fileName : benchmarkFiles) {
            System.out.println("\n=======================================================");
            System.out.println("Testing benchmark file: " + fileName);
            System.out.println("=======================================================");
            
            try {
                // Parse the benchmark file
                SCPInstance instance = parseSCPFile(benchmarkDir + "/" + fileName);
                
                // Calculate k (number of subsets to select)
                int k = (int)(instance.numSubsets / 25);
                if (k < 1) k = 1;
                
                // Print instance information
                System.out.println("Number of rows (elements): " + instance.numElements);
                System.out.println("Number of columns (subsets): " + instance.numSubsets);
                System.out.println("Number of subsets to select (k): " + k);
                
                // Variables for averaging results
                long totalTime = 0;
                int totalCoverage = 0;
                
                // Run multiple times to get average performance
                for (int run = 1; run <= numRuns; run++) {
                    System.out.println("\nRun " + run + "/" + numRuns);
                    
                    // Create PSO_MCP instance
                    PSO_MCP pso = new PSO_MCP(instance.subsets, instance.numElements);
                    pso.setMaxIterations(maxIterations);
                    
                    // Start timer
                    long startTime = System.currentTimeMillis();
                    
                    // Run the PSO algorithm
                    Particle[] result = pso.solution(numParticles, k, instance.numSubsets);
                    
                    // End timer
                    long endTime = System.currentTimeMillis();
                    long runTime = endTime - startTime;
                    
                    // Get best fitness from the run
                    int bestFitness = 0;
                    for (Particle p : result) {
                        if (p.fitness > bestFitness) {
                            bestFitness = p.fitness;
                        }
                    }
                    
                    // Add to totals
                    totalTime += runTime;
                    totalCoverage += bestFitness;
                    
                    // Output run results
                    System.out.println("Run " + run + " time: " + runTime + " ms, coverage: " + 
                                      bestFitness + "/" + instance.numElements);
                }
                
                // Calculate and display averages
                double avgTime = totalTime / (double)numRuns;
                double avgCoverage = totalCoverage / (double)numRuns;
                
                System.out.println("\nAverage Results for " + fileName + ":");
                System.out.println("Average execution time: " + avgTime + " ms");
                System.out.println("Average coverage: " + avgCoverage + "/" + instance.numElements + 
                                  " (" + (avgCoverage/instance.numElements*100) + "%)");
                
                // Add to CSV-style summary
                System.out.println(fileName + "," + instance.numElements + "," + 
                                  instance.numSubsets + "," + k + "," + 
                                  avgCoverage + "," + avgTime);
                
            } catch (IOException e) {
                System.out.println("Error processing file " + fileName + ": " + e.getMessage());
            }
        }
    }
    
/**
 * Parse a Set Covering Problem benchmark file
 */
private static SCPInstance parseSCPFile(String filePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        String line;
        
        // Read the first line: number of rows (elements) and columns (subsets)
        line = reader.readLine().trim();
        String[] firstLineTokens = line.split("\\s+");
        int numElements = Integer.parseInt(firstLineTokens[0]);
        int numSubsets = Integer.parseInt(firstLineTokens[1]);
        
        System.out.println("Parsing file with " + numElements + " elements and " + numSubsets + " subsets");
        
        // Initialize the subsets matrix
        boolean[][] subsets = new boolean[numSubsets][numElements];
        
        // Skip the cost line(s)
        // The costs might span multiple lines, so we need to count how many numbers we've read
        int costsRead = 0;
        while (costsRead < numSubsets) {
            line = reader.readLine();
            if (line == null) break;
            
            String[] tokens = line.trim().split("\\s+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    costsRead++;
                    if (costsRead >= numSubsets) break;
                }
            }
        }
        
        // Now read the subset information
        // First, read how many elements are in each subset
        int[] elementsInSubset = new int[numSubsets];
        int subsetsRead = 0;
        
        while (subsetsRead < numSubsets) {
            line = reader.readLine();
            if (line == null) break;
            
            String[] tokens = line.trim().split("\\s+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    elementsInSubset[subsetsRead] = Integer.parseInt(token);
                    subsetsRead++;
                    if (subsetsRead >= numSubsets) break;
                }
            }
        }
        
        // Now read which elements are in each subset
        for (int i = 0; i < numSubsets; i++) {
            int elementsToRead = elementsInSubset[i];
            int elementsRead = 0;
            
            while (elementsRead < elementsToRead) {
                line = reader.readLine();
                if (line == null) break;
                
                String[] tokens = line.trim().split("\\s+");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        try {
                            int element = Integer.parseInt(token);
                            // Convert 1-based index to 0-based
                            if (element > 0 && element <= numElements) {
                                subsets[i][element - 1] = true;
                            }
                            elementsRead++;
                            if (elementsRead >= elementsToRead) break;
                        } catch (NumberFormatException e) {
                            // Skip invalid tokens
                            System.out.println("Warning: Skipping invalid token: " + token);
                        }
                    }
                }
            }
        }
        
        return new SCPInstance(numElements, numSubsets, subsets);
    }
}

    
    /**
     * Class to hold a Set Covering Problem instance
     */
    static class SCPInstance {
        int numElements;
        int numSubsets;
        boolean[][] subsets;
        
        public SCPInstance(int numElements, int numSubsets, boolean[][] subsets) {
            this.numElements = numElements;
            this.numSubsets = numSubsets;
            this.subsets = subsets;
        }
    }
}
