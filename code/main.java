
class particule {
    boolean[] position; // table des sous-ensembles sélectionnés 1: sélectionné, 0: non sélectionné
    int[] V; // table des vitesses de chaque sous-ensemble
    int fitness; // nombre d'éléments couverts par la solution
    int pBest; // nombre d'éléments couverts par la meilleure solution trouvée par la particule
    boolean[] pBestPosition; // table des sous-ensembles sélectionnés par la meilleure solution trouvée par la particule

    particule(int N) {
        position = new boolean[N];
        V = new int[N];
        pBestPosition = new boolean[N];
        fitness = 0;
        
        for (int i = 0; i < N; i++) {
            position[i] = Math.random() < 0.5;
            V[i] =0; 
        }
        
        // Calculate initial fitness
        fitness = calculateCoverage(position);
        pBest = fitness;
        
        // Copy position to pBestPosition
        System.arraycopy(position, 0, pBestPosition, 0, N);
    }

    // Calculate coverage for the entire position array
   int calculateCoverage(boolean[] pos, boolean[][] subsets, int numElements) {
    boolean[] covered = new boolean[numElements];
    int coverage = 0;

    for (int i = 0; i < pos.length; i++) {
        if (pos[i]) {  // Si le sous-ensemble est sélectionné
            for (int j = 0; j < numElements; j++) {
                if (subsets[i][j] && !covered[j]) {  // Si l'élément est couvert et non encore compté
                    covered[j] = true;
                    coverage++;
                }
            }
        }
    }
    return coverage;
}

    
    // Update position based on velocity
      void updatePosition(int index) {
       position[index] = Math.random() < sigmoid(V[index]);

    }
}
class solution {
    int m; // Nombre des particules
    int k; // Nombre des sous-ensembles choisis
    particule[] particules;
    int gBest; // nombre d'éléments couverts par la meilleure solution trouvée par l'ensemble des particules
    boolean[] gBestPosition;
    int IT; // Nombre des itérations
    double r1, r2;
    double w = 0.729;
    double c1 = 1.49445;
    double c2 = 1.49445;
    
    solution(int m, int k, int N, int iterations) {
        this.m = m;
        this.k = k;
        this.IT = iterations;
        this.particules = new particule[m];
        this.gBestPosition = new boolean[N];
        this.gBest = 0;
        
        // Initialize particles
        for (int i = 0; i < m; i++) {
            particules[i] = new particule(N);
            if (particules[i].fitness > gBest) {
                gBest = particules[i].fitness;
                System.arraycopy(particules[i].position, 0, gBestPosition, 0, N);
            }
        }
    }
    
    void updateVelocity(particule p, int index) {
        r1 = Math.random();
        r2 = Math.random();
        
        int cognitive = p.pBestPosition[index] ? 1 : -1;
        int social = gBestPosition[index] ? 1 : -1;
        
        // Update velocity using PSO formula
        p.V[index] = (int)(w * p.V[index] + 
                      c1 * r1 * (cognitive) * (p.position[index] ? -1 : 1) + 
                      c2 * r2 * (social) * (p.position[index] ? -1 : 1));
        
        // Limit velocity to prevent explosion
        if (p.V[index] > 4) p.V[index] = 4;
        if (p.V[index] < -4) p.V[index] = -4;
    }
    
    void run() {
        int N = gBestPosition.length;
        
        for (int iter = 0; iter < IT; iter++) {
            for (int i = 0; i < m; i++) {
                particule p = particules[i];
                
                // Update velocity and position
                for (int j = 0; j < N; j++) {
                    updateVelocity(p, j);
                    p.updatePosition(j);
                }
                
                // Enforce constraint: exactly k subsets should be selected
                enforceKSubsets(p, k);
                
                // Calculate new fitness
                p.fitness = p.calculateCoverage(p.position);
                
                // Update personal best
                if (p.fitness > p.pBest) {
                    p.pBest = p.fitness;
                    System.arraycopy(p.position, 0, p.pBestPosition, 0, N);
                }
                
                // Update global best
                if (p.fitness > gBest) {
                    gBest = p.fitness;
                    System.arraycopy(p.position, 0, gBestPosition, 0, N);
                }
            }
        }
    }
    
    // Ensure exactly k subsets are selected
    void enforceKSubsets(particule p, int k) {
        int selected = 0;
        for (boolean b : p.position) {
            if (b) selected++;
        }
        // If too many subsets are selected, randomly deselect some
        while (selected > k) {
            int index = (int)(Math.random() * p.position.length);
            if (p.position[index]) {
                p.position[index] = false;
                selected--;
            }
        }
        // If too few subsets are selected, randomly select more
        while (selected < k) {
            int index = (int)(Math.random() * p.position.length);
            if (!p.position[index]) {
                p.position[index] = true;
                selected++;
            }
        }
    }
}
---------------------------------------------------------
class PSO_MCP {
    // Problem-specific data
    boolean[][] subsets; // Each subset contains a set of elements
    int numElements;     // Total number of unique elements
    
    PSO_MCP(boolean[][] subsets, int numElements) {
        this.subsets = subsets;
        this.numElements = numElements;
    }
    
    // Evaluate fitness: count how many unique elements are covered by selected subsets
    int evaluateFitness(boolean[] position) {
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
    
    // Calculate velocity for a particle
    int calculateV(particule p, int index, boolean[] gBestPosition) {
        double r1 = Math.random();
        double r2 = Math.random();
        double w = 0.729;
        double c1 = 1.49445;
        double c2 = 1.49445;
        
        int cognitive = p.pBestPosition[index] ? 1 : -1;
        int social = gBestPosition[index] ? 1 : -1;
        
        int newV = (int)(w * p.V[index] + 
                    c1 * r1 * (cognitive) * (p.position[index] ? -1 : 1) + 
                    c2 * r2 * (social) * (p.position[index] ? -1 : 1));
        
        // Limit velocity
        if (newV > 4) newV = 4;
        if (newV < -4) newV = -4;
        
        return newV;
    }
    
    // Calculate new position based on velocity
    boolean calculatePosition(particule p, int index) {
        double sigmoid = 1.0 / (1.0 + Math.exp(-p.V[index]));
        return Math.random() < sigmoid;
    }
    
    // Update personal best for a particle
    void miseAjourPBest(particule p) {
        if (p.fitness > p.pBest) {
            p.pBest = p.fitness;
            System.arraycopy(p.position, 0, p.pBestPosition, 0, p.position.length);
        }
    }
    
    // Update global best
    void miseAjourGBest(particule[] particules, int gBest, boolean[] gBestPosition) {
        for (particule p : particules) {
            if (p.fitness > gBest) {
                gBest = p.fitness;
                System.arraycopy(p.position, 0, gBestPosition, 0, p.position.length);
            }
        }
    }
    
    // Calculate initial best solution
    void calculateBest(particule[] particules, int gBest, boolean[] gBestPosition) {
        gBest = 0;
        for (particule p : particules) {
            p.fitness = evaluateFitness(p.position);
            if (p.fitness > gBest) {
                gBest = p.fitness;
                System.arraycopy(p.position, 0, gBestPosition, 0, p.position.length);
            }
        }
    }
    
    particule[] solution(int m, int k, int N) {
        particule[] particules = new particule[m];
        for (int i = 0; i < m; i++) {
            particules[i] = new particule(N);
        }
        
        // Initialize global best
        int gBest = 0;
        int IT = 30;
        boolean[] gBestPosition = new boolean[N];
        calculateBest(particules, gBest, gBestPosition);

        // Main loop
        for (int i = 0; i < IT; i++) {
            for (int j = 0; j < m; j++) {
                // Update velocity and position for each particle
                for (int l = 0; l < N; l++) {
                    particules[j].V[l] = calculateV(particules[j], l, gBestPosition);
                    particules[j].position[l] = calculatePosition(particules[j], l);
                }
                
                // Enforce constraint: exactly k subsets should be selected
                enforceKSubsets(particules[j], k);
                
                // Evaluate fitness and update personal best
                particules[j].fitness = evaluateFitness(particules[j].position);
                miseAjourPBest(particules[j]);
            }
            
            // Update global best
            miseAjourGBest(particules, gBest, gBestPosition);
        }
        
        printSolution(gBestPosition, gBest);
        return particules;
    }
    
    // Ensure exactly k subsets are selected
    void enforceKSubsets(particule p, int k) {
        int selected = 0;
        for (boolean b : p.position) {
            if (b) selected++;
        }
        
        // If too many subsets are selected, randomly deselect some
        while (selected > k) {
            int index = (int)(Math.random() * p.position.length);
            if (p.position[index]) {
                p.position[index] = false;
                selected--;
            }
        }
        
        // If too few subsets are selected, randomly select more
        while (selected < k) {
            int index = (int)(Math.random() * p.position.length);
            if (!p.position[index]) {
                p.position[index] = true;
                selected++;
            }
        }
    }

    public void printSolution(boolean[] bestPosition, int bestFitness) {
        System.out.println("Best solution found:");
        System.out.println("Fitness: " + bestFitness);
        System.out.print("Selected subsets: ");
        
        for (int i = 0; i < bestPosition.length; i++) {
            if (bestPosition[i]) {
                System.out.print((i+1) + " ");
            }
        }
        System.out.println();
    }
}
