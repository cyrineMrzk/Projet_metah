public class Main {
    public static void main(String[] args) {
        // Example problem: 10 elements, 5 subsets
        int numElements = 10;
        int numSubsets = 5;
        boolean[][] subsets = new boolean[numSubsets][numElements];
        
        // Define subsets (this is just an example)
        // Subset 0: {0, 1, 2}
        subsets[0][0] = true;
        subsets[0][1] = true;
        subsets[0][2] = true;
        
        // Subset 1: {2, 3, 4, 5}
        subsets[1][2] = true;
        subsets[1][3] = true;
        subsets[1][4] = true;
        subsets[1][5] = true;
        
        // Subset 2: {5, 6, 7}
        subsets[2][5] = true;
        subsets[2][6] = true;
        subsets[2][7] = true;
        
        // Subset 3: {7, 8, 9}
        subsets[3][7] = true;
        subsets[3][8] = true;
        subsets[3][9] = true;
        
        // Subset 4: {0, 4, 8}
        subsets[4][0] = true;
        subsets[4][4] = true;
        subsets[4][8] = true;
        
        // Parameters for PSO
        int k = 2;              // Select exactly 2 subsets
        int numParticles = 10;  // Use 10 particles
        int maxIterations = 50; // Run for 50 iterations
        
        // Create and run PSO
        PSO_MCP pso = new PSO_MCP(subsets, numElements, k, numParticles, maxIterations);
        pso.run();
    }
}
