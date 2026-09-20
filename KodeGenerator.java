import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class KodeGenerator {

    private static final String SECRET =
            System.getenv().getOrDefault(
                    "BRANKAS_SECRET",
                    "SANG_PRABU_BRANKAS_SECRET_2026"
            );

    private static final long WAKTU_PERUBAHAN = 60;

    public static String generate() {

        long periode =
                System.currentTimeMillis()
                / 1000
                / WAKTU_PERUBAHAN;

        String input = SECRET + ":" + periode;

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] hasil =
                    md.digest(
                            input.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            long angka = 0;

            for (int i = 0; i < 4; i++) {
                angka =
                        (angka << 8)
                        | (hasil[i] & 0xff);
            }

            angka = Math.abs(angka);

            return String.format(
                    "%06d",
                    angka % 1000000
            );

        } catch (Exception e) {

            return "000000";
        }
    }
}
