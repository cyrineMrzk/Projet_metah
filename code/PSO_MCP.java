import java.util.Random;

/**
 * Particle class for PSO algorithm
 */
class Particle {
    boolean[] position; // Selected subsets (true = selected, false = not selected)
    double[] velocity;  // Velocity for each dimension
    int fitness;        // Number of elements covered by current position
    int pBest;          // Best fitness found by this particle
    boolean[] pBestPosition; // Position that gave the best fitness
    
    public Particle(int numSubsets, int k, Random rand) {
        position = new boolean[numSubsets];
        velocity = new double[numSubsets];
        pBestPosition = new boolean[numSubsets];
        
        // Initialize with exactly k random subsets selected
        int selected = 0;
        while (selected < k) {
            int index = rand.nextInt(numSubsets);
            if (!position[index]) {
                position[index] = true;
                selected++;
            }
        }
        
        // Initialize velocities to small random values
        for (int i = 0; i < numSubsets; i++) {
            velocity[i] = rand.nextDouble() * 2 - 1; // Between -1 and 1
        }
        
        // Copy initial position to pBestPosition
        System.arraycopy(position, 0, pBestPosition, 0, numSubsets);
        pBest = 0; // Initialize pBest to 0
    }
    
    // Update position based on velocity using sigmoid function
    public void updatePosition(Random rand, int k, double velocityLimit) {
        boolean[] newPosition = new boolean[position.length];
        
        // First pass: calculate probabilities and make initial selections
        for (int i = 0; i < position.length; i++) {
            double sigmoid = 1.0 / (1.0 + Math.exp(-velocity[i]));
            newPosition[i] = rand.nextDouble() < sigmoid;
        }
        
        // Second pass: enforce exactly k selections
        int selected = 0;
        for (boolean b : newPosition) {
            if (b) selected++;
        }
        
        // If too many subsets are selected, deselect some
        while (selected > k) {
            int index;
            do {
                index = rand.nextInt(position.length);
            } while (!newPosition[index]);
            
            newPosition[index] = false;
            selected--;
        }
        
        // If too few subsets are selected, select more
        while (selected < k) {
            int index;
            do {
                index = rand.nextInt(position.length);
            } while (newPosition[index]);
            
            newPosition[index] = true;
            selected++;
        }
        
        // Update position
        position = newPosition;
    }
}

/**
 * PSO algorithm for Maximum Coverage Problem
 */
public class PSO_MCP {
    // Problem-specific data
    private boolean[][] subsets;     // Each subset contains a set of elements
    private int numElements;         // Total number of unique elements
    
    // PSO parameters
    private double w;                // Inertia weight
    private double c1;               // Cognitive coefficient
    private double c2;               // Social coefficient
    private double velocityLimit;    // Maximum velocity magnitude
    private int maxIterations;       // Maximum iterations
    private boolean earlyTermination; // Whether to terminate early
    private int maxIterationsWithoutImprovement; // Early stopping condition
    private Random rand;
    
    public PSO_MCP(boolean[][] subsets, int numElements, PSOConfig config) {
        this.subsets = subsets;
        this.numElements = numElements;
        
        // Set parameters from config
        this.w = config.w;
        this.c1 = config.c1;
        this.c2 = config.c2;
        this.maxIterations = config.maxIterations;
        this.velocityLimit = config.velocityLimit;
        this.earlyTermination = config.earlyTermination;
        this.maxIterationsWithoutImprovement = config.maxIterationsWithoutImprovement;
        
        // Initialize random with seed if provided
        if (config.randomSeed > 0) {
            this.rand = new Random(config.randomSeed);
        } else {
            this.rand = new Random();
        }
    }
    
    // Evaluate fitness: count how many unique elements are covered by selected subsets
    private int evaluateFitness(boolean[] position) {
        boolean[] covered = new boolean[numElements];
        int count = 0;
        
        for (int i = 0; i < position.length; i++) {
            if (position[i]) {
                for (int j = 0; j < numElements; j++) {
                    if (subsets[i][j] && !covered[j]) {
                        covered[j] = true;
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    // Update velocity for a particle
    private void updateVelocity(Particle p, boolean[] gBestPosition) {
        for (int i = 0; i < p.position.length; i++) {
            double r1 = rand.nextDouble();
            double r2 = rand.nextDouble();
            
            // Calculate cognitive and social components
            double cognitive = p.pBestPosition[i] ? 1 : -1;
            double social = gBestPosition[i] ? 1 : -1;
            
            // Update velocity using PSO formula
            p.velocity[i] = w * p.velocity[i] +
                           c1 * r1 * cognitive * (p.position[i] ? -1 : 1) +
                           c2 * r2 * social * (p.position[i] ? -1 : 1);
            
            // Limit velocity to prevent explosion
            if (p.velocity[i] > velocityLimit) p.velocity[i] = velocityLimit;
            if (p.velocity[i] < -velocityLimit) p.velocity[i] = -velocityLimit;
        }
    }
    
    // Main PSO algorithm
    public Particle[] solution(int numParticles, int k, int numSubsets) {
        System.out.println("Starting PSO with " + numParticles + " particles, k=" + k +
                           ", iterations=" + maxIterations +
                           ", w=" + w + ", c1=" + c1 + ", c2=" + c2);
        
        // Initialize particles
        Particle[] particles = new Particle[numParticles];
        for (int i = 0; i < numParticles; i++) {
            particles[i] = new Particle(numSubsets, k, rand);
            particles[i].fitness = evaluateFitness(particles[i].position);
            particles[i].pBest = particles[i].fitness;
        }
        
        // Initialize global best
        int gBest = 0;
        boolean[] gBestPosition = new boolean[numSubsets]; // Initialize properly here
        
        // Find initial global best
        for (Particle p : particles) {
            if (p.fitness > gBest) {
                gBest = p.fitness;
                System.arraycopy(p.position, 0, gBestPosition, 0, numSubsets);
            }
        }
        
        // Main PSO loop
        int iterationsWithoutImprovement = 0;
        for (int iter = 0; iter < maxIterations; iter++) {
            boolean improved = false;
            
            for (Particle p : particles) {
                // Update velocity
                updateVelocity(p, gBestPosition);
                
                // Update position (ensuring exactly k subsets are selected)
                p.updatePosition(rand, k, velocityLimit);
                
                // Evaluate new fitness
                p.fitness = evaluateFitness(p.position);
                
                // Update personal best
                if (p.fitness > p.pBest) {
                    p.pBest = p.fitness;
                    System.arraycopy(p.position, 0, p.pBestPosition, 0, numSubsets);
                    
                    // Update global best
                    if (p.fitness > gBest) {
                        gBest = p.fitness;
                        System.arraycopy(p.position, 0, gBestPosition, 0, numSubsets);
                        System.out.println("Iteration " + iter + ": New global best = " + gBest + "/" + numElements);
                        improved = true;
                        
                        // Early termination if we cover all elements
                        if (earlyTermination && gBest == numElements) {
                            System.out.println("Found optimal solution covering all elements! Terminating early.");
                            break;
                        }
                    }
                }
            }
            
            // Early termination checks
            if (earlyTermination && gBest == numElements) {
                break;
            }
            
            // Check if we've gone too long without improvement
            if (improved) {
                iterationsWithoutImprovement = 0;
            } else {
                iterationsWithoutImprovement++;
                
                // Report lack of progress periodically
                if (iterationsWithoutImprovement % 10000 == 0) {
                    System.out.println("Iteration " + iter + ": No improvement for " +
                                       iterationsWithoutImprovement + " iterations, best = " +
                                       gBest + "/" + numElements);
                }
                
                // Early stopping if no improvement for too long
                if (iterationsWithoutImprovement >= maxIterationsWithoutImprovement) {
                    System.out.println("No improvement for " + maxIterationsWithoutImprovement +
                                       " iterations. Terminating early.");
                    break;
                }
            }
        }
        
        printSolution(gBestPosition, gBest);
        return particles;
    }
    
    // Print the solution
    public void printSolution(boolean[] bestPosition, int bestFitness) {
        System.out.println("\nBest solution found:");
        System.out.println("Coverage: " + bestFitness + "/" + numElements + " elements");
        
        System.out.print("Selected subsets: ");
        int selectedCount = 0;
        for (int i = 0; i < bestPosition.length; i++) {
            if (bestPosition[i]) {
                System.out.print((i+1) + " ");
                selectedCount++;
            }
        }
        System.out.println("(" + selectedCount + " subsets)");
        
        // Print which elements are covered
        boolean[] covered = new boolean[numElements];
        for (int i = 0; i < bestPosition.length; i++) {
            if (bestPosition[i]) {
                for (int j = 0; j < numElements; j++) {
                    if (subsets[i][j]) {
                        covered[j] = true;
                    }
                }
            }
        }
        
        int coveredCount = 0;
        for (boolean b : covered) {
            if (b) coveredCount++;
        }
        
        System.out.println("Total elements covered: " + coveredCount + "/" + numElements);
    }
}
