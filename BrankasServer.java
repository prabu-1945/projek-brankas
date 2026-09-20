import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class BrankasServer {

    // =========================
    // CLASS DASAR BRANKAS
    // =========================
    static class Brankas {

        protected String kode;

        public Brankas() {
            kode = generateKode();
        }

        // Method yang akan dioverride
        public String generateKode() {
            return "000000";
        }

        public boolean bukaBrankas(String kodeMasuk) {
            return kode.equals(kodeMasuk);
        }

        public String getKode() {
            return kode;
        }

        public void ubahKode() {
            kode = generateKode();
        }
    }


    // =========================
    // SUBCLASS
    // =========================
    static class BrankasKeamanan extends Brankas {

        private static final String SECRET =
                System.getenv().getOrDefault(
                        "BRANKAS_SECRET",
                        "SANG_PRABU_BRANKAS_SECRET_2026"
                );

        private static final long WAKTU_PERUBAHAN = 60;

        @Override
        public String generateKode() {

            long periode =
                    System.currentTimeMillis() / 1000 / WAKTU_PERUBAHAN;

            String input = SECRET + ":" + periode;

            try {

                MessageDigest md =
                        MessageDigest.getInstance("SHA-256");

                byte[] hasil =
                        md.digest(input.getBytes(StandardCharsets.UTF_8));

                long angka = 0;

                for (int i = 0; i < 4; i++) {
                    angka = (angka << 8) | (hasil[i] & 0xff);
                }

                angka = Math.abs(angka);

                return String.format("%06d", angka % 1000000);

            } catch (Exception e) {
                return "000000";
            }
        }

        public String getKodeAktif() {
            return generateKode();
        }

        @Override
        public boolean bukaBrankas(String kodeMasuk) {

            String kodeAktif = getKodeAktif();

            return kodeAktif.equals(kodeMasuk);
        }
    }


    // =========================
    // PEMEGANG KODE
    // =========================
    static class PemegangKode {

        private String nama;

        public PemegangKode(String nama) {
            this.nama = nama;
        }

        public String getNama() {
            return nama;
        }
    }


    // =========================
    // OBJECT
    // =========================
    static BrankasKeamanan brankas =
            new BrankasKeamanan();

    static PemegangKode pemegang1 =
            new PemegangKode("Pemegang Kode 1");

    static PemegangKode pemegang2 =
            new PemegangKode("Pemegang Kode 2");


    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) throws Exception {

        int port = 80;

        String portEnv = System.getenv("PORT");

        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                port = 80;
            }
        }

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress("0.0.0.0", port),
                        0
                );

        server.createContext("/", BrankasServer::handleHome);

        server.createContext(
                "/style.css",
                BrankasServer::handleCSS
        );

        server.createContext(
                "/status",
                BrankasServer::handleStatus
        );

        server.createContext(
                "/buka",
                BrankasServer::handleBuka
        );

        server.setExecutor(
                Executors.newCachedThreadPool()
        );

        server.start();

        System.out.println("=================================");
        System.out.println("     SERVER BRANKAS ONLINE");
        System.out.println("=================================");
        System.out.println("Port : " + port);
        System.out.println("Status : Server aktif");
        System.out.println("Rotasi kode : 60 detik");
        System.out.println("=================================");

        // Untuk pengujian melalui Vercel Logs
        System.out.println(
                "Kode aktif: " + brankas.getKodeAktif()
        );
    }


    // =========================
    // HALAMAN UTAMA
    // =========================
    static void handleHome(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (!path.equals("/")) {
            sendResponse(
                    exchange,
                    404,
                    "text/plain",
                    "404 - Halaman tidak ditemukan"
            );
            return;
        }

        File file = new File("index.html");

        if (!file.exists()) {

            sendResponse(
                    exchange,
                    404,
                    "text/plain",
                    "index.html tidak ditemukan"
            );

            return;
        }

        byte[] data =
                java.nio.file.Files.readAllBytes(
                        file.toPath()
                );

        addCors(exchange);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/html; charset=UTF-8"
                );

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(data);
        }
    }


    // =========================
    // CSS
    // =========================
    static void handleCSS(HttpExchange exchange)
            throws IOException {

        File file = new File("style.css");

        if (!file.exists()) {

            sendResponse(
                    exchange,
                    404,
                    "text/plain",
                    "style.css tidak ditemukan"
            );

            return;
        }

        byte[] data =
                java.nio.file.Files.readAllBytes(
                        file.toPath()
                );

        addCors(exchange);

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "text/css; charset=UTF-8"
                );

        exchange.sendResponseHeaders(
                200,
                data.length
        );

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(data);
        }
    }


    // =========================
    // STATUS
    // =========================
    static void handleStatus(HttpExchange exchange)
            throws IOException {

        addCors(exchange);

        String response =
                "{"
                        + "\"status\":\"TERKUNCI\","
                        + "\"server\":\"ONLINE\""
                        + "}";

        sendResponse(
                exchange,
                200,
                "application/json",
                response
        );

        // Kode hanya dicetak di log server,
        // tidak dikirim ke browser.
        System.out.println(
                "Kode aktif: " +
                        brankas.getKodeAktif()
        );
    }


    // =========================
    // BUKA BRANKAS
    // =========================
    static void handleBuka(HttpExchange exchange)
            throws IOException {

        addCors(exchange);

        String query =
                exchange.getRequestURI()
                        .getRawQuery();

        String kodeMasuk = "";

        if (query != null) {

            String[] parameter =
                    query.split("&");

            for (String p : parameter) {

                String[] bagian =
                        p.split("=", 2);

                if (bagian.length == 2
                        && bagian[0].equals("kode")) {

                    kodeMasuk =
                            URLDecoder.decode(
                                    bagian[1],
                                    StandardCharsets.UTF_8
                            );
                }
            }
        }


        // Validasi
        if (kodeMasuk.matches("\\d{6}")
                && brankas.bukaBrankas(kodeMasuk)) {

            System.out.println(
                    "Percobaan buka: BERHASIL"
            );

            sendResponse(
                    exchange,
                    200,
                    "application/json",
                    "{"
                            + "\"berhasil\":true,"
                            + "\"status\":\"TERBUKA\","
                            + "\"pesan\":\"Kode benar. Brankas terbuka.\""
                            + "}"
            );

        } else {

            System.out.println(
                    "Percobaan buka: GAGAL"
            );

            sendResponse(
                    exchange,
                    401,
                    "application/json",
                    "{"
                            + "\"berhasil\":false,"
                            + "\"status\":\"TERKUNCI\","
                            + "\"pesan\":\"Kode salah.\""
                            + "}"
            );
        }
    }


    // =========================
    // CORS
    // =========================
    static void addCors(
            HttpExchange exchange) {

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Methods",
                        "GET, OPTIONS"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Headers",
                        "Content-Type"
                );
    }


    // =========================
    // RESPONSE
    // =========================
    static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String contentType,
            String response)
            throws IOException {

        byte[] data =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        contentType
                                + "; charset=UTF-8"
                );

        exchange.sendResponseHeaders(
                statusCode,
                data.length
        );

        try (OutputStream os =
                     exchange.getResponseBody()) {

            os.write(data);
        }
    }
}
