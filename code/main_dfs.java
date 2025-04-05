import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class main_dfs {
    public static void main(String[] args) {
        // Directory containing benchmark files
        String benchmarkDir = "scp_benchmark";
        File dir = new File(benchmarkDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Error: Benchmark directory not found. Run SCPDownloader first.");
            return;
        }
        
        // Configure testing parameters
        int numRuns = 1;         // Number of runs for each benchmark
        // Configure DFS parameters
        long timeLimit = 60000;  // 60 seconds time limit
        boolean useGreedyInitial = true;
        boolean usePruning = true;
        
        // List of benchmark files to test
        String[] benchmarkFiles = {
            "scp41.txt", "scp42.txt", "scp43.txt", "scp44.txt", "scp410.txt",
            "scpa1.txt", "scpa2.txt", "scpa3.txt", "scpa4.txt", "scpa5.txt",  
            "scpb1.txt", "scpb2.txt", "scpb3.txt", "scpc1.txt", "scpc2.txt", "scpc3.txt"
        };
        
        // Results summary
        System.out.println("Benchmark,Elements,Subsets,k,AvgCoverage,AvgTime(ms)");
        
        for (String fileName : benchmarkFiles) {
            System.out.println("\n=======================================================");
            System.out.println("Testing benchmark file: " + fileName);
            System.out.println("=======================================================");
            
            try {
                // Parse the benchmark file
                MCPinstant instance = parseSCPFile(benchmarkDir + "/" + fileName);
                
                // Calculate k (number of subsets to select)
                int k = instance.m / 25;
                if (k < 1) k = 1;
                
                // Print instance information
                System.out.println("Number of rows (elements): " + instance.n);
                System.out.println("Number of columns (subsets): " + instance.m);
                System.out.println("Number of subsets to select (k): " + k);
                System.out.println("DFS parameters: timeLimit=" + timeLimit + "ms, useGreedyInitial=" + 
                                 useGreedyInitial + ", usePruning=" + usePruning);
                
                // Variables for averaging results
                long totalTime = 0;
                int totalCoverage = 0;
                
                // Run multiple times to get average performance
                for (int run = 1; run <= numRuns; run++) {
                    System.out.println("\nRun " + run + "/" + numRuns);
                    
                    // Start timer
                    long startTime = System.currentTimeMillis();
                    
                    // Run the DFS algorithm with parameters
                    BitSet[] result = DFS.MCPDFS(instance, k, timeLimit, useGreedyInitial, usePruning);
                    
                    // End timer
                    long endTime = System.currentTimeMillis();
                    long runTime = endTime - startTime;
                    
                    // Get coverage from the run
                    int coverage = MCP.evaluateEtat(result, instance);
                    
                    // Add to totals
                    totalTime += runTime;
                    totalCoverage += coverage;
                    
                    // Output run results
                    System.out.println("Run " + run + " time: " + runTime + " ms, coverage: " + 
                                      coverage + "/" + instance.n);
                }
                
                // Calculate and display averages
                double avgTime = totalTime / (double)numRuns;
                double avgCoverage = totalCoverage / (double)numRuns;
                
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
     * Parse a Set Covering Problem benchmark file
     */
    private static MCPinstant parseSCPFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
            
            String fileContent = content.toString();
            String[] subsetStrings = fileContent.split("\\],\\s*\\[");
            
            if (subsetStrings.length > 0) {
                subsetStrings[0] = subsetStrings[0].replaceFirst("^\\s*\\[", "");
                int lastIndex = subsetStrings.length - 1;
                subsetStrings[lastIndex] = subsetStrings[lastIndex].replaceFirst("\\]\\s*$", "");
            }
            
            int m = subsetStrings.length;
            int maxElement = 0;
            List<List<Integer>> parsedSubsets = new ArrayList<>();
            
            for (String subsetString : subsetStrings) {
                List<Integer> subset = new ArrayList<>();
                String[] elements = subsetString.split(",");
                for (String element : elements) {
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
            
            int n = maxElement;
            System.out.println("Parsed file with " + n + " elements and " + m + " subsets");
            
            // Create MCPinstant
            MCPinstant instance = new MCPinstant(n, m);
            instance.U.set(0, n); // All elements need to be covered
            
            // Fill the subsets
            for (int i = 0; i < parsedSubsets.size(); i++) {
                List<Integer> subset = parsedSubsets.get(i);
                for (int element : subset) {
                    if (element > 0 && element <= n) {
                        instance.S[i].set(element - 1);
                    }
                }
            }
            
            return instance;
        }
    }
}