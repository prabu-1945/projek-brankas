import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class BrankasServer {

    // ==================================================
    // CLASS DASAR BRANKAS
    // ==================================================
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


    // ==================================================
    // SUBCLASS BRANKAS KEAMANAN
    // ==================================================
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

        public String getKodeAktif() {
            return generateKode();
        }

        @Override
        public boolean bukaBrankas(String kodeMasuk) {

            String kodeAktif =
                    getKodeAktif();

            return kodeAktif.equals(kodeMasuk);
        }
    }


    // ==================================================
    // CLASS PEMEGANG KODE
    // ==================================================
    static class PemegangKode {

        private String nama;

        public PemegangKode(String nama) {
            this.nama = nama;
        }

        public String getNama() {
            return nama;
        }
    }


    // ==================================================
    // OBJECT
    // ==================================================
    static BrankasKeamanan brankas =
            new BrankasKeamanan();

    static PemegangKode pemegang1 =
            new PemegangKode("Pemegang Kode 1");

    static PemegangKode pemegang2 =
            new PemegangKode("Pemegang Kode 2");


    // ==================================================
    // WAKTU PERUBAHAN
    // ==================================================
    static final int WAKTU_PERUBAHAN = 60;


    // ==================================================
    // MAIN
    // ==================================================
    public static void main(String[] args)
            throws Exception {

        int port = 8081;

        String portEnv =
                System.getenv("PORT");

        // Kalau dijalankan di Vercel,
        // gunakan PORT dari environment.
        if (portEnv != null) {

            try {

                port =
                        Integer.parseInt(
                                portEnv
                        );

            } catch (NumberFormatException e) {

                port = 8081;
            }
        }


        // ==================================================
        // SERVER
        // ==================================================
        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(
                                "0.0.0.0",
                                port
                        ),
                        0
                );


        server.createContext(
                "/",
                BrankasServer::handleHome
        );

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


        // ==================================================
        // INFORMASI SERVER
        // ==================================================
        System.out.println();
        System.out.println(
                "======================================"
        );
        System.out.println(
                "       SERVER BRANKAS AKTIF"
        );
        System.out.println(
                "======================================"
        );
        System.out.println(
                "Alamat : http://localhost:" + port
        );
        System.out.println(
                "Status : ONLINE"
        );
        System.out.println(
                "======================================"
        );

        System.out.println(
                "Pemegang kode : 2 orang"
        );

        System.out.println(
                "Pergantian kode : "
                        + WAKTU_PERUBAHAN
                        + " detik"
        );

        System.out.println(
                "======================================"
        );


        // ==================================================
        // TAMPILKAN KODE PERTAMA
        // ==================================================
        System.out.println();
        System.out.println(
                "KODE BRANKAS AKTIF : "
                        + brankas.getKodeAktif()
        );

        System.out.println(
                "======================================"
        );


        // ==================================================
        // THREAD COUNTDOWN
        // ==================================================
        Thread countdown =
                new Thread(() -> {

                    long periodeSebelumnya =
                            System.currentTimeMillis()
                            / 1000
                            / WAKTU_PERUBAHAN;

                    while (true) {

                        try {

                            long sekarang =
                                    System.currentTimeMillis();

                            long periodeSekarang =
                                    sekarang
                                    / 1000
                                    / WAKTU_PERUBAHAN;


                            // Hitung sisa detik
                            long sisa =
                                    WAKTU_PERUBAHAN
                                    - (
                                        (sekarang / 1000)
                                        % WAKTU_PERUBAHAN
                                    );


                            System.out.print(
                                    "\rKode akan berubah dalam : "
                                    + sisa
                                    + " detik   "
                            );


                            // ==================================================
                            // JIKA MASUK PERIODE BARU
                            // ==================================================
                            if (periodeSekarang
                                    != periodeSebelumnya) {

                                periodeSebelumnya =
                                        periodeSekarang;

                                System.out.println();

                                System.out.println();
                                System.out.println(
                                        "======================================"
                                );

                                System.out.println(
                                        "      KODE BRANKAS BERUBAH!"
                                );

                                System.out.println(
                                        "======================================"
                                );

                                System.out.println(
                                        "KODE BARU : "
                                                + brankas.getKodeAktif()
                                );

                                System.out.println(
                                        "Status : TERKUNCI"
                                );

                                System.out.println(
                                        "======================================"
                                );

                            }


                            Thread.sleep(1000);

                        } catch (
                                InterruptedException e
                        ) {

                            Thread.currentThread()
                                    .interrupt();

                            break;
                        }
                    }

                });


        countdown.setDaemon(true);
        countdown.start();
    }


    // ==================================================
    // HALAMAN UTAMA
    // ==================================================
    static void handleHome(
            HttpExchange exchange)
            throws IOException {

        String path =
                exchange.getRequestURI()
                        .getPath();

        if (!path.equals("/")) {

            sendResponse(
                    exchange,
                    404,
                    "text/plain",
                    "Halaman tidak ditemukan"
            );

            return;
        }


        File file =
                new File("index.html");


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


        try (
                OutputStream os =
                        exchange.getResponseBody()
        ) {

            os.write(data);
        }
    }


    // ==================================================
    // CSS
    // ==================================================
    static void handleCSS(
            HttpExchange exchange)
            throws IOException {

        File file =
                new File("style.css");


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


        try (
                OutputStream os =
                        exchange.getResponseBody()
        ) {

            os.write(data);
        }
    }


    // ==================================================
    // STATUS
    // ==================================================
    static void handleStatus(
            HttpExchange exchange)
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


        System.out.println(
                "\nKode aktif : "
                        + brankas.getKodeAktif()
        );
    }


    // ==================================================
    // BUKA BRANKAS
    // ==================================================
    static void handleBuka(
            HttpExchange exchange)
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


                if (
                        bagian.length == 2
                        &&
                        bagian[0].equals("kode")
                ) {

                    kodeMasuk =
                            URLDecoder.decode(
                                    bagian[1],
                                    StandardCharsets.UTF_8
                            );
                }
            }
        }


        // ==================================================
        // CEK KODE
        // ==================================================
        if (
                kodeMasuk.matches("\\d{6}")
                &&
                brankas.bukaBrankas(
                        kodeMasuk
                )
        ) {

            System.out.println(
                    "\nKode benar."
            );

            System.out.println(
                    "BRANKAS TERBUKA!"
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
                    "\nAkses tidak dapat dilakukan."
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


    // ==================================================
    // CORS
    // ==================================================
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


    // ==================================================
    // RESPONSE
    // ==================================================
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


        try (
                OutputStream os =
                        exchange.getResponseBody()
        ) {

            os.write(data);
        }
    }
}
