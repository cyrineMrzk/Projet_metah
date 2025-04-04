import java.util.Stack;
import java.util.BitSet;

public class DFS {
    public static BitSet[] MCPDFS(MCPinstant instance, int targetK, long timeLimit, boolean useGreedyInitial, boolean usePruning) {
        // Initialize best solution
        BitSet[] bestSolution;
        if (useGreedyInitial) {
            bestSolution = greedyInitialSolution(instance, targetK);
        } else {
            bestSolution = new BitSet[instance.m];
            for (int i = 0; i < instance.m; i++) {
                bestSolution[i] = new BitSet(instance.n);
            }
        }
        
        BitSet[] currentSolution = new BitSet[instance.m];
        for (int i = 0; i < instance.m; i++) {
            currentSolution[i] = new BitSet(instance.n);
        }
        
        // Calculate current best coverage
        int maxCoverage = evaluateEtat(bestSolution, instance);
        if (useGreedyInitial) {
            System.out.println("Initial greedy solution coverage: " + maxCoverage);
        }
        
        // Keep track of time
        long startTime = System.currentTimeMillis();
        
        // Initialize the stack with the starting state
        Stack<Etat> stack = new Stack<>();
        stack.push(new Etat(currentSolution, 0, 0));
        
        // Keep track of progress
        long nodesExplored = 0;
        long lastPrintTime = startTime;
        
        while (!stack.isEmpty()) {
            // Check time limit
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeLimit) {
                System.out.println("Time limit reached. Stopping search after exploring " + 
                                  nodesExplored + " nodes.");
                break;
            }
            
            // Print progress occasionally
            if (currentTime - lastPrintTime > 5000) { // Every 5 seconds
                System.out.println("Explored " + nodesExplored + " nodes. Current best: " + maxCoverage);
                lastPrintTime = currentTime;
            }
            
            nodesExplored++;
            Etat etat = stack.pop();
            currentSolution = etat.X;
            int index = etat.k;
            int selectedCount = etat.selectedCount;
            
            // If we've tried all sets
            if (index >= instance.m) {
                // Check if we have exactly targetK sets selected
                if (selectedCount == targetK) {
                    // Evaluate the coverage
                    int coverage = evaluateEtat(currentSolution, instance);
                    if (coverage > maxCoverage) {
                        maxCoverage = coverage;
                        System.out.println("New best: coverage = " + maxCoverage + " with " + targetK + " sets");
                        
                        // Deep copy of the solution
                        for (int i = 0; i < instance.m; i++) {
                            bestSolution[i] = (BitSet) currentSolution[i].clone();
                        }
                    }
                }
            } else {
                // Apply pruning if enabled
                if (usePruning) {
                    // Calculate potential upper bound for this branch
                    BitSet remainingCoverage = getRemainingCoverage(instance, currentSolution, index);
                    int currentCoverage = getCurrentCoverage(currentSolution, instance);
                    int potentialMaxCoverage = currentCoverage + remainingCoverage.cardinality();
                    
                    // Pruning: Skip this branch if it can't beat the current best
                    if (potentialMaxCoverage <= maxCoverage) {
                        continue;
                    }
                }
                
                // Calculate how much this set would contribute
                BitSet currentCovered = getCurrentCoveredElements(currentSolution, instance);
                BitSet newCoverage = (BitSet) instance.S[index].clone();
                newCoverage.andNot(currentCovered); // Elements that would be newly covered
                
                // Path 1: Try including the current set if it would add new coverage and we haven't reached targetK
                if (selectedCount < targetK && (!usePruning || newCoverage.cardinality() > 0)) {
                    BitSet[] solutionWithSet = new BitSet[instance.m];
                    for (int i = 0; i < instance.m; i++) {
                        solutionWithSet[i] = (BitSet) currentSolution[i].clone();
                    }
                    solutionWithSet[index] = (BitSet) instance.S[index].clone(); // Add this set
                    stack.push(new Etat(solutionWithSet, index + 1, selectedCount + 1));
                }
                
                // Path 2: Don't include the current set
                BitSet[] solutionWithoutSet = new BitSet[instance.m];
                for (int i = 0; i < instance.m; i++) {
                    solutionWithoutSet[i] = (BitSet) currentSolution[i].clone();
                }
                stack.push(new Etat(solutionWithoutSet, index + 1, selectedCount));
            }
        }
        
        System.out.println("Maximum coverage with " + targetK + " subsets: " + maxCoverage + "/" + instance.n);
        return bestSolution;
    }
    
    // Helper method to get the current coverage
    private static int getCurrentCoverage(BitSet[] solution, MCPinstant instance) {
        return getCurrentCoveredElements(solution, instance).cardinality();
    }
    
    // Helper method to get the currently covered elements
    private static BitSet getCurrentCoveredElements(BitSet[] solution, MCPinstant instance) {
        BitSet covered = new BitSet(instance.n);
        for (int i = 0; i < solution.length; i++) {
            covered.or(solution[i]);
        }
        return covered;
    }
    
    // Helper method to estimate remaining potential coverage
    private static BitSet getRemainingCoverage(MCPinstant instance, BitSet[] currentSolution, int startIndex) {
        BitSet currentCovered = getCurrentCoveredElements(currentSolution, instance);
        BitSet potentialCoverage = (BitSet) currentCovered.clone();
        
        for (int i = startIndex; i < instance.m; i++) {
            potentialCoverage.or(instance.S[i]);
        }
        
        return potentialCoverage;
    }
    
    // Use existing evaluateEtat method from MCP class or reimplement it here
    private static int evaluateEtat(BitSet[] solution, MCPinstant instance) {
        // Count how many elements are covered by the selected sets
        BitSet covered = new BitSet(instance.n);
        
        for (int i = 0; i < solution.length; i++) {
            covered.or(solution[i]);  // Union with the set i
        }
        
        return covered.cardinality();  // Return the number of covered elements
    }
    
    // Greedy initial solution
    private static BitSet[] greedyInitialSolution(MCPinstant instance, int targetK) {
        BitSet[] solution = new BitSet[instance.m];
        for (int i = 0; i < instance.m; i++) {
            solution[i] = new BitSet(instance.n);
        }
        
        BitSet covered = new BitSet(instance.n);
        int selectedCount = 0;
        
        while (selectedCount < targetK) {
            int bestSetIndex = -1;
            int bestNewCoverage = -1;
            
            for (int i = 0; i < instance.m; i++) {
                // Skip if already selected
                if (!solution[i].isEmpty()) continue;
                
                // Calculate new coverage this set would provide
                BitSet newElements = (BitSet) instance.S[i].clone();
                newElements.andNot(covered);
                int newCoverage = newElements.cardinality();
                
                if (newCoverage > bestNewCoverage) {
                    bestNewCoverage = newCoverage;
                    bestSetIndex = i;
                }
            }
            
            // Add the best set to the solution
            if (bestSetIndex >= 0) {
                solution[bestSetIndex] = (BitSet) instance.S[bestSetIndex].clone();
                covered.or(instance.S[bestSetIndex]);
                selectedCount++;
            } else {
                break; // No more sets that add coverage
            }
        }
        
        return solution;
    }
}