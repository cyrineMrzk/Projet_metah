import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class MainDFS {
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
       
        // List of benchmark files to test
        String[] benchmarkFiles = { "scp41.txt", "scp42.txt", "scp43.txt", "scp44.txt", "scp410.txt",
        "scpa1.txt", "scpa2.txt", "scpa3.txt", "scpa4.txt", "scpa5.txt",
        "scpb1.txt", "scpb2.txt", "scpb3.txt", "scpb4.txt", "scpb5.txt",
        "scpc1.txt", "scpc2.txt", "scpc3.txt", "scpc4.txt", "scpc5.txt"
        };
       //
      //  
       //
        // Create CSV file for results
        String csvFile = "C:\\Users\\x13\\Desktop\\PROJECTS\\Projet_metah\\code\\dfs_results.csv";
       
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile))) {
            // Write CSV header
            csvWriter.write("Benchmark,Elements,Subsets,k,AvgCoverage,CoveragePercentage,AvgTime(ms)\n");
           
            // Results summary header
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
                    System.out.println("DFS parameters: timeLimit=" + timeLimit );
                   
                    // Variables for averaging results
                    long totalTime = 0;
                    int totalCoverage = 0;
                   
                    // Run multiple times to get average performance
                    for (int run = 1; run <= numRuns; run++) {
                        System.out.println("\nRun " + run + "/" + numRuns);
                       
                        // Start timer
                        long startTime = System.currentTimeMillis();
                       
                        // Run the DFS algorithm with parameters
                        BitSet[] result = DFS.MCPDFS(instance, k, timeLimit);
                       
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
                    double coveragePercentage = (avgCoverage / instance.n) * 100;
                   
                    System.out.println("\nAverage Results for " + fileName + ":");
                    System.out.println("Average execution time: " + avgTime + " ms");
                    System.out.println("Average coverage: " + avgCoverage + "/" + instance.n +
                                      " (" + coveragePercentage + "%)");
                   
                    // Write to CSV file
                    csvWriter.write(String.format("%s,%d,%d,%d,%.2f,%.2f%%,%.2f\n",
                        fileName,
                        instance.n,
                        instance.m,
                        k,
                        avgCoverage,
                        coveragePercentage,
                        avgTime));
                   
                    // Add to console summary
                    System.out.println(fileName + "," + instance.n + "," +
                                      instance.m + "," + k + "," +
                                      avgCoverage + "," + avgTime);
                   
                } catch (IOException e) {
                    System.out.println("Error processing file " + fileName + ": " + e.getMessage());
                }
            }
           
            System.out.println("\nResults saved to " + csvFile);
           
        } catch (IOException e) {
            System.out.println("Error creating results file: " + e.getMessage());
        }
    }
/**
 * Parse an SCP file in custom format with sets represented as [element1, element2, ...]
 */
private static MCPinstant parseSCPFile(String filePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(" ");
        }
        
        // Extract all sets from the content using regex
        String fileContent = content.toString();
        String[] subsetStrings = fileContent.split("\\],\\s*\\[");
        
        // Clean up the first and last subsets
        if (subsetStrings.length > 0) {
            subsetStrings[0] = subsetStrings[0].replaceFirst("^\\s*\\[", "");
            int lastIndex = subsetStrings.length - 1;
            subsetStrings[lastIndex] = subsetStrings[lastIndex].replaceFirst("\\]\\s*$", "");
        }
        
        int m = subsetStrings.length; // Number of subsets
        int maxElement = 0; // Track highest element number
        List<List<Integer>> parsedSubsets = new ArrayList<>();
        
        // Parse each subset
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
        
        int n = maxElement; // Number of elements is the highest element ID
        System.out.println("Parsed file with " + n + " elements and " + m + " subsets");
        
        // Debug: Print sample of parsed subsets
        System.out.println("Sample of parsed subsets:");
        for (int i = 0; i < Math.min(3, parsedSubsets.size()); i++) {
            System.out.println("  Subset " + i + ": " + parsedSubsets.get(i).size() + " elements, first few: " + 
                             parsedSubsets.get(i).subList(0, Math.min(5, parsedSubsets.get(i).size())));
        }
        
        // Create MCPinstant
        MCPinstant instance = new MCPinstant(n, m);
        instance.U.set(0, n); // All elements need to be covered
        
        // Fill the subsets
        for (int i = 0; i < parsedSubsets.size(); i++) {
            List<Integer> subset = parsedSubsets.get(i);
            for (int element : subset) {
                if (element > 0 && element <= n) {
                    instance.S[i].set(element - 1); // Convert to 0-based index
                }
            }
        }
        
        return instance;
    }
}
}