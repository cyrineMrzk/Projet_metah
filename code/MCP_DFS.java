import java.util.Stack;
import java.util.BitSet;

class MCPinstant {
    int n;
    int m;
    BitSet[] S;
    BitSet U;
    
    public MCPinstant(int n, int m) {
        this.n = n;
        this.m = m;
        this.S = new BitSet[m];
        this.U = new BitSet(n);
        U.set(0, n);
        
        for (int i = 0; i < m; i++) {
            S[i] = new BitSet(n);
            for (int j = 0; j < n; j++) {
                if (Math.random() < 0.5) {
                    S[i].set(j);
                }
            }
        }
    }
}

class Etat {
    BitSet[] X;  // Array of BitSets
    int k;       // Current depth in the search
    int nbrSousEnsembles;
    
    public Etat(BitSet[] X, int k) {
        // Deep copy of the BitSet array
        this.X = new BitSet[X.length];
        for (int i = 0; i < X.length; i++) {
            this.X[i] = (BitSet) X[i].clone();
        }
        this.k = k;
        this.nbrSousEnsembles = X.length;
    }
}

class MCP {
    MCPinstant instance;
    
    public MCP(MCPinstant instance) {
        this.instance = instance;
    }
    
    public static int evaluateEtat(BitSet[] solution, MCPinstant instance) {
        // Count how many elements are covered by the selected sets
        BitSet covered = new BitSet(instance.n);
        
        for (int i = 0; i < solution.length; i++) {
            covered.or(solution[i]);  // Union with the set i
        }
        
        return covered.cardinality();  // Return the number of covered elements
    }
    
    public void printS() {
        System.out.println("Generated Sets:");
        for (int i = 0; i < instance.m; i++) {
            System.out.print("Set " + i + " -> Elements: ");
            for (int j = 0; j < instance.n; j++) {
                if (instance.S[i].get(j)) {
                    System.out.print(j + " ");
                }
            }
            System.out.println();
        }
        System.out.println("Universe: " + instance.U);
    }
    
    public void printX(BitSet[] X) {
        System.out.println("Current Solution:");
        
        // Calculate and print covered elements
        BitSet covered = new BitSet(instance.n);
        for (BitSet set : X) {
            covered.or(set);
        }
        
        System.out.print("Covered elements: ");
        for (int i = 0; i < instance.n; i++) {
            if (covered.get(i)) {
                System.out.print(i + " ");
            }
        }
        System.out.println();
        System.out.println("Total covered: " + covered.cardinality() + "/" + instance.n);
    }
    
    public static BitSet[] MCPDFS(MCPinstant instance) {
        // Initialize best solution and current solution with all sets included
        BitSet[] bestSolution = new BitSet[instance.m];
        BitSet[] currentSolution = new BitSet[instance.m];
        
        for (int i = 0; i < instance.m; i++) {
            bestSolution[i] = new BitSet(instance.n);
            currentSolution[i] = (BitSet) instance.S[i].clone(); // Start with all sets
        }
        
        // Initialize the stack with the starting state (all sets included, k=0)
        Stack<Etat> stack = new Stack<>();
        stack.push(new Etat(currentSolution, 0));
        
        int bestValue = 0;
        int selectedSets = instance.m;
        
        while (!stack.isEmpty()) {
            Etat etat = stack.pop();
            currentSolution = etat.X;
            int k = etat.k;
            
            // If we've tried removing all sets
            if (k >= instance.m) {
                // Count how many sets we've removed
                int removedSets = 0;
                for (int i = 0; i < instance.m; i++) {
                    if (currentSolution[i].isEmpty()) {
                        removedSets++;
                    }
                }
                
                if (removedSets > bestValue) {
                    // Check if we still have full coverage
                    int coverage = evaluateEtat(currentSolution, instance);
                    if (coverage == instance.n) {
                        bestValue = removedSets;
                        System.out.println("New best: removed sets = " + bestValue);
                        
                        // Deep copy of the solution
                        for (int i = 0; i < instance.m; i++) {
                            bestSolution[i] = (BitSet) currentSolution[i].clone();
                        }
                        
                        selectedSets = instance.m - removedSets;
                    }
                }
            } else {
                // Try two paths: with and without set k
                
                // Path 1: Keep set k (don't remove it)
                BitSet[] solutionWithSet = new BitSet[instance.m];
                for (int i = 0; i < instance.m; i++) {
                    solutionWithSet[i] = (BitSet) currentSolution[i].clone();
                }
                stack.push(new Etat(solutionWithSet, k + 1));
                
                // Path 2: Try removing set k
                BitSet[] solutionWithoutSet = new BitSet[instance.m];
                for (int i = 0; i < instance.m; i++) {
                    solutionWithoutSet[i] = (BitSet) currentSolution[i].clone();
                }
                solutionWithoutSet[k] = new BitSet(instance.n); // Empty set (removed)
                
                // Check if we still have full coverage after removing set k
                int coverageWithoutSet = evaluateEtat(solutionWithoutSet, instance);
                if (coverageWithoutSet == instance.n) {
                    stack.push(new Etat(solutionWithoutSet, k + 1));
                }
            }
        }
        
        System.out.println("Valeur maximale du k avec le DFS: " + bestValue + 
                          " avec le nbr de sous ensemble : " + selectedSets);
        return bestSolution;
    }
    
    public static void main(String[] args) {
        // Test cases with different sizes
        int[][] testCases = {
            {10, 5},    // Small case: 10 elements, 5 sets
            {20, 10},   // Medium case: 20 elements, 10 sets
            {30, 15},   // Larger case: 30 elements, 15 sets
            {50, 20}    // Even larger: 50 elements, 20 sets
        };
        
        // Run each test case
        for (int i = 0; i < testCases.length; i++) {
            int n = testCases[i][0];  // Number of elements in the universe
            int m = testCases[i][1];  // Number of sets
            
            System.out.println("\n========================================");
            System.out.println("TEST CASE " + (i+1) + ": n=" + n + ", m=" + m);
            System.out.println("========================================");
            
            // Create an instance of the problem
            MCPinstant instance = new MCPinstant(n, m);
            MCP mcp = new MCP(instance);
            
            // Print the generated sets
            mcp.printS();
            
            // Measure execution time
            long startTime = System.currentTimeMillis();
            
            // Run the DFS algorithm
            System.out.println("\nRunning DFS algorithm...");
            BitSet[] solution = MCP.MCPDFS(instance);
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Print the solution
            System.out.println("\nBest solution found:");
            mcp.printX(solution);
            
            // Count how many sets were selected
            int selectedSets = 0;
            for (int j = 0; j < m; j++) {
                if (!solution[j].isEmpty()) {
                    selectedSets++;
                }
            }
            System.out.println("Number of sets selected: " + selectedSets);
            
            // Calculate coverage percentage
            int covered = evaluateEtat(solution, instance);
            double coveragePercentage = (double) covered / n * 100;
            System.out.println("Coverage: " + covered + "/" + n + " elements (" + 
                              String.format("%.2f", coveragePercentage) + "%)");
            
            // Print execution time
            System.out.println("Execution time: " + executionTime + " ms");
            
            // Calculate and print the ratio of selected sets to total sets
            double selectionRatio = (double) selectedSets / m * 100;
            System.out.println("Selection ratio: " + selectedSets + "/" + m + " sets (" + 
                              String.format("%.2f", selectionRatio) + "%)");
        }
        
        // Optional: Add a specific test case with a predefined instance
        // This could be useful for debugging or comparing with known optimal solutions
        System.out.println("\n========================================");
        System.out.println("CUSTOM TEST CASE");
        System.out.println("========================================");
        
        // Create a custom instance with specific parameters
        int customN = 15;
        int customM = 8;
        MCPinstant customInstance = new MCPinstant(customN, customM);
        MCP customMcp = new MCP(customInstance);
        
        // Run the same process for the custom instance
        customMcp.printS();
        
        long startTime = System.currentTimeMillis();
        System.out.println("\nRunning DFS algorithm...");
        BitSet[] customSolution = MCP.MCPDFS(customInstance);
        long executionTime = System.currentTimeMillis() - startTime;
        
        System.out.println("\nBest solution found:");
        customMcp.printX(customSolution);
        
        int customSelectedSets = 0;
        for (int i = 0; i < customM; i++) {
            if (!customSolution[i].isEmpty()) {
                customSelectedSets++;
            }
        }
        
        System.out.println("Number of sets selected: " + customSelectedSets);
        int customCovered = evaluateEtat(customSolution, customInstance);
        System.out.println("Coverage: " + customCovered + "/" + customN + " elements (" + 
                          String.format("%.2f", (double)customCovered/customN*100) + "%)");
        System.out.println("Execution time: " + executionTime + " ms");
    }
    
}
