import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class BrankasServer {

    // =====================================================
    // KONFIGURASI
    // =====================================================

    static final long WAKTU_PERUBAHAN = 60;

    static final String SECRET =
        System.getenv().getOrDefault(
            "BRANKAS_SECRET",
            "SANG_PRABU_BRANKAS_SECRET_2026"
        );


    // =====================================================
    // CLASS BRANKAS
    // =====================================================

    static class Brankas {

        protected String kode;

        public Brankas() {

            kode = generateKode();

        }


        // =================================================
        // METHOD YANG AKAN DI-OVERRIDE
        // =================================================

        public String generateKode() {

            return "000000";

        }


        // =================================================
        // MEMBUKA BRANKAS
        // =================================================

        public boolean bukaBrankas(
            String kodeMasuk
        ) {

            return kode.equals(kodeMasuk);

        }


        // =================================================
        // MENGAMBIL KODE
        // =================================================

        public String getKode() {

            return kode;

        }


        // =================================================
        // MENGUBAH KODE
        // =================================================

        public void ubahKode() {

            kode = generateKode();

        }

    }


    // =====================================================
    // CLASS TURUNAN
    // METHOD OVERRIDING
    // =====================================================

    static class BrankasKeamanan
        extends Brankas {

        private long periodeAktif =
            -1;


        @Override
        public String generateKode() {

            long periode =
                System.currentTimeMillis()
                / (WAKTU_PERUBAHAN * 1000L);

            return generateKodeUntukPeriode(
                periode
            );

        }


        // =================================================
        // MEMBUAT KODE BERDASARKAN PERIODE WAKTU
        // =================================================

        private String generateKodeUntukPeriode(
            long periode
        ) {

            try {

                String data =
                    SECRET
                    + ":"
                    + periode;

                MessageDigest digest =
                    MessageDigest.getInstance(
                        "SHA-256"
                    );

                byte[] hash =
                    digest.digest(
                        data.getBytes(
                            StandardCharsets.UTF_8
                        )
                    );


                long angka = 0;

                for (
                    int i = 0;
                    i < 4;
                    i++
                ) {

                    angka =
                        (angka << 8)
                        | (
                            hash[i]
                            & 0xff
                        );

                }


                angka =
                    angka % 1000000;


                if (angka < 0) {

                    angka =
                        -angka;

                }


                return String.format(
                    "%06d",
                    angka
                );

            }

            catch (
                Exception e
            ) {

                return "000000";

            }

        }


        // =================================================
        // MEMASTIKAN KODE SESUAI PERIODE SEKARANG
        // =================================================

        public synchronized void perbaruiKodeJikaPerlu() {

            long periodeSekarang =
                System.currentTimeMillis()
                / (WAKTU_PERUBAHAN * 1000L);


            if (
                periodeSekarang
                != periodeAktif
            ) {

                periodeAktif =
                    periodeSekarang;

                kode =
                    generateKodeUntukPeriode(
                        periodeSekarang
                    );

            }

        }

    }


    // =====================================================
    // CLASS PEMEGANG KODE
    //
    // Tetap ada untuk kebutuhan konsep OOP.
    // Tidak ditampilkan di website.
    // =====================================================

    static class PemegangKode {

        private String nama;


        public PemegangKode(
            String nama
        ) {

            this.nama =
                nama;

        }


        public String getNama() {

            return nama;

        }

    }


    // =====================================================
    // OBJECT BRANKAS
    // =====================================================

    static BrankasKeamanan brankas =
        new BrankasKeamanan();


    // =====================================================
    // OBJECT PEMEGANG KODE
    // =====================================================

    static PemegangKode pemegang1 =
        new PemegangKode(
            "Pemegang Kode 1"
        );


    static PemegangKode pemegang2 =
        new PemegangKode(
            "Pemegang Kode 2"
        );


    // =====================================================
    // STATUS BRANKAS
    // =====================================================

    static boolean brankasTerbuka =
        false;


    static long periodeTerakhir =
        -1;


    // =====================================================
    // MAIN
    // =====================================================

    public static void main(
        String[] args
    ) throws Exception {


        // =================================================
        // PORT VERCEL
        // =================================================

        int port =
            Integer.parseInt(
                System.getenv()
                    .getOrDefault(
                        "PORT",
                        "8081"
                    )
            );


        // =================================================
        // MEMBUAT SERVER
        // =================================================

        HttpServer server =
            HttpServer.create(
                new InetSocketAddress(
                    "0.0.0.0",
                    port
                ),
                0
            );


        // =================================================
        // HALAMAN UTAMA
        // =================================================

        server.createContext(
            "/",
            BrankasServer::handleHome
        );


        // =================================================
        // FILE CSS
        // =================================================

        server.createContext(
            "/style.css",
            BrankasServer::handleCSS
        );


        // =================================================
        // STATUS BRANKAS
        // =================================================

        server.createContext(
            "/status",
            BrankasServer::handleStatus
        );


        // =================================================
        // BUKA BRANKAS
        // =================================================

        server.createContext(
            "/buka",
            BrankasServer::handleBuka
        );


        // =================================================
        // EXECUTOR
        // =================================================

        server.setExecutor(
            Executors.newCachedThreadPool()
        );


        // =================================================
        // MENJALANKAN SERVER
        // =================================================

        server.start();


        // =================================================
        // PASTIKAN KODE PERTAMA SUDAH TERSEDIA
        // =================================================

        brankas.perbaruiKodeJikaPerlu();


        long periodeSekarang =
            System.currentTimeMillis()
            / (WAKTU_PERUBAHAN * 1000L);


        periodeTerakhir =
            periodeSekarang;


        // =================================================
        // LOG TERMINAL
        // =================================================

        System.out.println();

        System.out.println(
            "=========================================="
        );

        System.out.println(
            "        SISTEM KEAMANAN BRANKAS"
        );

        System.out.println(
            "=========================================="
        );

        System.out.println();

        System.out.println(
            "Java Brankas Server ONLINE"
        );

        System.out.println(
            "Port: "
            + port
        );

        System.out.println();

        System.out.println(
            "Pemegang kode:"
        );

        System.out.println(
            "- "
            + pemegang1.getNama()
        );

        System.out.println(
            "- "
            + pemegang2.getNama()
        );

        System.out.println();

        System.out.println(
            "Kode aktif untuk pengujian: "
            + brankas.getKode()
        );

        System.out.println();

        System.out.println(
            "Pergantian kode setiap "
            + WAKTU_PERUBAHAN
            + " detik."
        );

        System.out.println();

        System.out.println(
            "=========================================="
        );


        // =================================================
        // THREAD LOG PERGANTIAN KODE
        //
        // Hanya untuk log/demo ketika container aktif.
        // Pergantian kode sebenarnya tidak bergantung
        // pada thread ini.
        // =================================================

        Thread monitorKode =
            new Thread(() -> {

                long periodeSebelumnya =
                    periodeTerakhir;


                while (true) {

                    try {

                        Thread.sleep(
                            1000
                        );


                        long periodeSekarang2 =
                            System.currentTimeMillis()
                            / (
                                WAKTU_PERUBAHAN
                                * 1000L
                            );


                        if (
                            periodeSekarang2
                            != periodeSebelumnya
                        ) {

                            brankas
                                .perbaruiKodeJikaPerlu();


                            brankasTerbuka =
                                false;


                            System.out.println();

                            System.out.println(
                                "=========================================="
                            );

                            System.out.println(
                                "        KODE BRANKAS BERUBAH!"
                            );

                            System.out.println(
                                "=========================================="
                            );

                            System.out.println(
                                "Kode baru: "
                                + brankas.getKode()
                            );

                            System.out.println(
                                "Brankas kembali TERKUNCI."
                            );

                            System.out.println(
                                "=========================================="
                            );


                            periodeSebelumnya =
                                periodeSekarang2;

                        }

                    }

                    catch (
                        InterruptedException e
                    ) {

                        break;

                    }

                }

            });


        monitorKode.setDaemon(
            true
        );


        monitorKode.start();

    }


    // =====================================================
    // HALAMAN UTAMA
    // =====================================================

    static void handleHome(
        HttpExchange exchange
    ) throws IOException {


        if (
            !exchange
                .getRequestMethod()
                .equalsIgnoreCase(
                    "GET"
                )
        ) {

            kirimText(
                exchange,
                "Method tidak didukung.",
                405,
                "text/plain"
            );

            return;

        }


        Path file =
            Paths.get(
                "index.html"
            );


        if (
            !Files.exists(file)
        ) {

            kirimText(
                exchange,
                "index.html tidak ditemukan.",
                500,
                "text/plain"
            );

            return;

        }


        byte[] data =
            Files.readAllBytes(
                file
            );


        Headers headers =
            exchange
                .getResponseHeaders();


        headers.set(
            "Content-Type",
            "text/html; charset=UTF-8"
        );


        exchange.sendResponseHeaders(
            200,
            data.length
        );


        OutputStream output =
            exchange.getResponseBody();


        output.write(
            data
        );


        output.close();

    }


    // =====================================================
    // FILE CSS
    // =====================================================

    static void handleCSS(
        HttpExchange exchange
    ) throws IOException {


        if (
            !exchange
                .getRequestMethod()
                .equalsIgnoreCase(
                    "GET"
                )
        ) {

            kirimText(
                exchange,
                "Method tidak didukung.",
                405,
                "text/plain"
            );

            return;

        }


        Path file =
            Paths.get(
                "style.css"
            );


        if (
            !Files.exists(file)
        ) {

            kirimText(
                exchange,
                "style.css tidak ditemukan.",
                500,
                "text/plain"
            );

            return;

        }


        byte[] data =
            Files.readAllBytes(
                file
            );


        exchange
            .getResponseHeaders()
            .set(
                "Content-Type",
                "text/css; charset=UTF-8"
            );


        exchange.sendResponseHeaders(
            200,
            data.length
        );


        OutputStream output =
            exchange.getResponseBody();


        output.write(
            data
        );


        output.close();

    }


    // =====================================================
    // ENDPOINT STATUS
    // =====================================================

    static void handleStatus(
        HttpExchange exchange
    ) throws IOException {


        tambahCORS(
            exchange
        );


        brankas
            .perbaruiKodeJikaPerlu();


        String status;


        if (
            brankasTerbuka
        ) {

            status =
                "TERBUKA";

        }

        else {

            status =
                "TERKUNCI";

        }


        String response =
            "{"
            + "\"status\":\""
            + status
            + "\""
            + "}";


        kirimJSON(
            exchange,
            response
        );

    }


    // =====================================================
    // ENDPOINT BUKA BRANKAS
    // =====================================================

    static void handleBuka(
        HttpExchange exchange
    ) throws IOException {


        tambahCORS(
            exchange
        );


        brankas
            .perbaruiKodeJikaPerlu();


        String query =
            exchange
                .getRequestURI()
                .getQuery();


        String kodeMasuk =
            "";


        if (
            query != null
            &&
            query.startsWith(
                "kode="
            )
        ) {

            kodeMasuk =
                query.substring(
                    5
                );


            kodeMasuk =
                URLDecoder.decode(
                    kodeMasuk,
                    StandardCharsets.UTF_8
                );

        }


        // =================================================
        // LOG DI SERVER
        // =================================================

        System.out.println();

        System.out.println(
            "Percobaan membuka brankas."
        );


        // =================================================
        // CEK KODE
        // =================================================

        boolean benar =
            brankas.bukaBrankas(
                kodeMasuk
            );


        String response;


        if (
            benar
        ) {

            brankasTerbuka =
                true;


            response =
                "{"
                + "\"status\":\"success\","
                + "\"message\":"
                + "\"Brankas berhasil dibuka!\""
                + "}";


            System.out.println(
                "HASIL: Kode benar."
            );


            System.out.println(
                "STATUS: BRANKAS TERBUKA."
            );

        }

        else {

            brankasTerbuka =
                false;


            response =
                "{"
                + "\"status\":\"error\","
                + "\"message\":"
                + "\"Kode salah atau sudah tidak berlaku!\""
                + "}";


            System.out.println(
                "HASIL: Kode salah."
            );


            System.out.println(
                "STATUS: BRANKAS TETAP TERKUNCI."
            );

        }


        kirimJSON(
            exchange,
            response
        );

    }


    // =====================================================
    // CORS
    // =====================================================

    static void tambahCORS(
        HttpExchange exchange
    ) {

        exchange
            .getResponseHeaders()
            .set(
                "Access-Control-Allow-Origin",
                "*"
            );


        exchange
            .getResponseHeaders()
            .set(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS"
            );


        exchange
            .getResponseHeaders()
            .set(
                "Access-Control-Allow-Headers",
                "Content-Type"
            );

    }


    // =====================================================
    // KIRIM JSON
    // =====================================================

    static void kirimJSON(
        HttpExchange exchange,
        String response
    ) throws IOException {


        byte[] data =
            response.getBytes(
                StandardCharsets.UTF_8
            );


        exchange
            .getResponseHeaders()
            .set(
                "Content-Type",
                "application/json; charset=UTF-8"
            );


        exchange.sendResponseHeaders(
            200,
            data.length
        );


        OutputStream output =
            exchange.getResponseBody();


        output.write(
            data
        );


        output.close();

    }


    // =====================================================
    // KIRIM TEXT
    // =====================================================

    static void kirimText(
        HttpExchange exchange,
        String response,
        int statusCode,
        String contentType
    ) throws IOException {


        byte[] data =
            response.getBytes(
                StandardCharsets.UTF_8
            );


        exchange
            .getResponseHeaders()
            .set(
                "Content-Type",
                contentType
                + "; charset=UTF-8"
            );


        exchange.sendResponseHeaders(
            statusCode,
            data.length
        );


        OutputStream output =
            exchange.getResponseBody();


        output.write(
            data
        );


        output.close();

    }

}