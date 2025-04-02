import java.util.Random;

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
    }
    
    // Update position based on velocity using sigmoid function
    public void updatePosition(Random rand, int k) {
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

public class PSO_MCP {
    private boolean[][] subsets;     // Each subset contains a set of elements
    private int numElements;         // Total number of unique elements
    private int numSubsets;          // Number of available subsets
    private int k;                   // Number of subsets to select
    private int numParticles;        // Number of particles in the swarm
    private int maxIterations;       // Maximum number of iterations
    
    // PSO parameters
    private double w = 0.729;        // Inertia weight
    private double c1 = 1.49445;     // Cognitive coefficient
    private double c2 = 1.49445;     // Social coefficient
    
    private Particle[] swarm;        // The particle swarm
    private int gBest;               // Global best fitness
    private boolean[] gBestPosition; // Global best position
    private Random rand;             // Random number generator
    
    public PSO_MCP(boolean[][] subsets, int numElements, int k, int numParticles, int maxIterations) {
        this.subsets = subsets;
        this.numElements = numElements;
        this.numSubsets = subsets.length;
        this.k = k;
        this.numParticles = numParticles;
        this.maxIterations = maxIterations;
        this.rand = new Random();
        
        // Initialize swarm
        swarm = new Particle[numParticles];
        gBestPosition = new boolean[numSubsets];
        gBest = 0;
        
        initializeSwarm();
    }
    
    private void initializeSwarm() {
        for (int i = 0; i < numParticles; i++) {
            swarm[i] = new Particle(numSubsets, k, rand);
            
            // Evaluate initial fitness
            swarm[i].fitness = evaluateFitness(swarm[i].position);
            swarm[i].pBest = swarm[i].fitness;
            
            // Update global best if needed
            if (swarm[i].fitness > gBest) {
                gBest = swarm[i].fitness;
                System.arraycopy(swarm[i].position, 0, gBestPosition, 0, numSubsets);
            }
        }
    }
    
    // Evaluate fitness: count how many unique elements are covered by selected subsets
    private int evaluateFitness(boolean[] position) {
        boolean[] covered = new boolean[numElements];
        int count = 0;
        
        for (int i = 0; i < numSubsets; i++) {
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
    private void updateVelocity(Particle p) {
        for (int i = 0; i < numSubsets; i++) {
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
            if (p.velocity[i] > 4) p.velocity[i] = 4;
            if (p.velocity[i] < -4) p.velocity[i] = -4;
        }
    }
    
    public void run() {
        System.out.println("Starting PSO with " + numParticles + " particles, k=" + k + ", iterations=" + maxIterations);
        
        for (int iter = 0; iter < maxIterations; iter++) {
            for (Particle p : swarm) {
                // Update velocity
                updateVelocity(p);
                
                // Update position (ensuring exactly k subsets are selected)
                p.updatePosition(rand, k);
                
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
                    }
                }
            }
        }
        
        printSolution();
    }
    
    public void printSolution() {
        System.out.println("\nBest solution found:");
        System.out.println("Coverage: " + gBest + "/" + numElements + " elements");
        System.out.print("Selected subsets: ");
        
        for (int i = 0; i < numSubsets; i++) {
            if (gBestPosition[i]) {
                System.out.print(i + " ");
            }
        }
        System.out.println();
        
        // Print which elements are covered
        boolean[] covered = new boolean[numElements];
        for (int i = 0; i < numSubsets; i++) {
            if (gBestPosition[i]) {
                for (int j = 0; j < numElements; j++) {
                    if (subsets[i][j]) {
                        covered[j] = true;
                    }
                }
            }
        }
        
        System.out.print("Covered elements: ");
        for (int j = 0; j < numElements; j++) {
            if (covered[j]) {
                System.out.print(j + " ");
            }
        }
        System.out.println();
    }
    
    // Getter for the best solution found
    public boolean[] getBestSolution() {
        return gBestPosition;
    }
    
    // Getter for the best fitness found
    public int getBestFitness() {
        return gBest;
    }
}
