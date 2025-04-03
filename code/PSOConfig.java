/**
 * Configuration class for PSO algorithm and benchmark testing parameters
 */
public class PSOConfig {
    // Problem parameters
    public String benchmarkDir = "scp_benchmark";  // Changed to a relative path
    public String[] benchmarkFiles = {
        "scp41.txt", "scp42.txt", "scp43.txt", "scp44.txt", "scp410.txt",
        "scpa1.txt", "scpa2.txt", "scpa3.txt", "scpa4.txt", "scpa5.txt",
        "scpb1.txt", "scpb2.txt", "scpb3.txt", "scpb4.txt", "scpb5.txt",
        "scpc1.txt", "scpc2.txt", "scpc3.txt", "scpc4.txt", "scpc5.txt"
    };
    
    // Benchmark test parameters
    public int numRuns = 5;         // Number of runs for each benchmark
    public int kDivisor = 25;       // k = m / kDivisor (number of subsets to select)
    
    // PSO algorithm parameters
    public int numParticles = 30;   // Number of particles
    public int maxIterations = 100000; // Maximum iterations for PSO
    public double w = 0.729;        // Inertia weight
    public double c1 = 1.49445;     // Cognitive coefficient
    public double c2 = 1.49445;     // Social coefficient
    public double velocityLimit = 4.0; // Maximum velocity magnitude
    
    // Random seed (use -1 for random seed, or specific value for reproducibility)
    public long randomSeed = -1;
    
    // Termination criteria
    public boolean earlyTermination = true; // Whether to terminate when all elements are covered
    public int maxIterationsWithoutImprovement = 10000; // Early stopping if no improvement
    
    /**
     * Update configuration from a ConfigEntry
     * @param entry The configuration entry to use for updating
     */
    public void updateFromConfigEntry(ConfigReader.ConfigEntry entry) {
        this.numParticles = entry.numParticles;
        this.maxIterations = entry.maxIterations;
        this.numRuns = entry.numRuns;
        this.c1 = entry.c1;
        this.c2 = entry.c2;
        this.w = entry.w;
    }
}
