import java.util.Stack;
import java.util.BitSet;

public class DFS {
    public static BitSet[] MCPDFS(MCPinstant instance, int targetK, long timeLimit) {
        BitSet[] bestSolution = new BitSet[instance.m];
        for (int i = 0; i < instance.m; i++) {
            bestSolution[i] = new BitSet(instance.n);
        }

        BitSet[] currentSolution = new BitSet[instance.m];
        for (int i = 0; i < instance.m; i++) {
            currentSolution[i] = new BitSet(instance.n);
        }

        int maxCoverage = evaluateEtat(bestSolution, instance);
        long startTime = System.currentTimeMillis();

        Stack<Etat> stack = new Stack<>();
        stack.push(new Etat(currentSolution, 0, 0));

        while (!stack.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeLimit) break;

            Etat etat = stack.pop();
            currentSolution = etat.X;
            int index = etat.k;
            int selectedCount = etat.selectedCount;

            if (index >= instance.m) {
                if (selectedCount == targetK) {
                    int coverage = evaluateEtat(currentSolution, instance);
                    if (coverage > maxCoverage) {
                        maxCoverage = coverage;
                        for (int i = 0; i < instance.m; i++) {
                            bestSolution[i] = (BitSet) currentSolution[i].clone();
                        }
                    }
                }
            } else {
                if (selectedCount < targetK) {
                    BitSet[] solutionWithSet = new BitSet[instance.m];
                    for (int i = 0; i < instance.m; i++) {
                        solutionWithSet[i] = (BitSet) currentSolution[i].clone();
                    }
                    solutionWithSet[index] = (BitSet) instance.S[index].clone();
                    stack.push(new Etat(solutionWithSet, index + 1, selectedCount + 1));
                }

                BitSet[] solutionWithoutSet = new BitSet[instance.m];
                for (int i = 0; i < instance.m; i++) {
                    solutionWithoutSet[i] = (BitSet) currentSolution[i].clone();
                }
                stack.push(new Etat(solutionWithoutSet, index + 1, selectedCount));
            }
        }

        return bestSolution;
    }

    private static int evaluateEtat(BitSet[] solution, MCPinstant instance) {
        BitSet covered = new BitSet(instance.n);
        for (int i = 0; i < solution.length; i++) {
            covered.or(solution[i]);
        }
        return covered.cardinality();
    }
}
