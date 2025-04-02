import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SCPDownloader {
    public static void main(String[] args) {
        // Dossier où stocker les fichiers téléchargés
        String saveDir = "scp_benchmark";
        new File(saveDir).mkdirs();

        // Liste des fichiers à télécharger
        String[] files = {
                "scp41", "scp42", "scp43", "scp44", "scp45", "scp46", "scp47", "scp48", "scp49",
                "scp51", "scp52", "scp53", "scp54", "scp55", "scp56", "scp57", "scp58", "scp59",
                "scp61", "scp62", "scp63", "scp64", "scp65", "scp410",
                "scpa1", "scpa2", "scpa3", "scpa4", "scpa5",
                "scpb1", "scpb2", "scpb3", "scpb4", "scpb5",
                "scpc1", "scpc2", "scpc3", "scpc4", "scpc5"
        };

        // URL de base des fichiers sur OR-Library
        String baseUrl = "https://people.brunel.ac.uk/~mastjjb/jeb/orlib/files/";

        for (String file : files) {
            String fileUrl = baseUrl + file + ".txt";  // 🔹 Ajout de ".txt"
            String savePath = saveDir + "/" + file + ".txt";

            // Télécharger le fichier
            boolean success = downloadFile(fileUrl, savePath);
            if (success) {
                System.out.println("✅ Téléchargé avec succès : " + file + ".txt");
            } else {
                System.out.println("❌ Échec du téléchargement : " + file + ".txt");
            }
        }
    }

    public static boolean downloadFile(String fileURL, String savePath) {
        try {
            // Créer une connexion HTTP
            HttpURLConnection connection = (HttpURLConnection) new URL(fileURL).openConnection();
            connection.setInstanceFollowRedirects(true); // 🔹 Suivi des redirections
            connection.setRequestMethod("GET");

            // Vérifier si le fichier existe (code HTTP 200)
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("⚠️ Erreur HTTP " + responseCode + " pour " + fileURL);
                return false;
            }

            // Lire et sauvegarder le fichier
            try (InputStream in = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 BufferedWriter writer = Files.newBufferedWriter(Paths.get(savePath))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            return true;

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du téléchargement de " + fileURL + " : " + e.getMessage());
            return false;
        }
    }
}
