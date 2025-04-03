import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to read PSO configuration parameters from a CSV file
 */
public class ConfigReader {
    
    /**
     * Class to hold a single configuration from the CSV file
     */
    public static class ConfigEntry {
        public int numParticles;
        public int maxIterations;
        public int numRuns;
        public double c1;
        public double c2;
        public double w;
        
        @Override
        public String toString() {
            return "ConfigEntry{" +
                   "numParticles=" + numParticles +
                   ", maxIterations=" + maxIterations +
                   ", numRuns=" + numRuns +
                   ", c1=" + c1 +
                   ", c2=" + c2 +
                   ", w=" + w +
                   '}';
        }
    }
    
    /**
     * Read configurations from a CSV file
     * @param filePath Path to the CSV file
     * @return List of configuration entries
     */
    public static List<ConfigEntry> readConfigFile(String filePath) throws IOException {
        List<ConfigEntry> configs = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header line if present
            String line = reader.readLine();
            if (line != null && line.startsWith("numParticles")) {
                // This was a header line, read the next line
                line = reader.readLine();
            }
            
            // Read each line of the CSV file
            while (line != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    ConfigEntry entry = new ConfigEntry();
                    
                    try {
                        entry.numParticles = Integer.parseInt(parts[0].trim());
                        entry.maxIterations = Integer.parseInt(parts[1].trim());
                        entry.numRuns = Integer.parseInt(parts[2].trim());
                        entry.c1 = Double.parseDouble(parts[3].trim());
                        entry.c2 = Double.parseDouble(parts[4].trim());
                        entry.w = Double.parseDouble(parts[5].trim());
                        
                        configs.add(entry);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing line: " + line);
                        System.err.println("Error details: " + e.getMessage());
                    }
                } else {
                    System.err.println("Invalid line format (not enough columns): " + line);
                }
                
                line = reader.readLine();
            }
        }
        
        return configs;
    }
}
