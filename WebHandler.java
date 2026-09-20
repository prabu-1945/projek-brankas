import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class WebHandler {

    // ==========================================
    // HALAMAN UTAMA
    // ==========================================
    static void handleHome(
            HttpExchange exchange)
            throws IOException {

        String path =
                exchange.getRequestURI()
                        .getPath();

        if (!path.equals("/")) {

            Utils.sendResponse(
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

            Utils.sendResponse(
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

        Utils.addCors(exchange);

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


    // ==========================================
    // CSS
    // ==========================================
    static void handleCSS(
            HttpExchange exchange)
            throws IOException {

        File file =
                new File("style.css");

        if (!file.exists()) {

            Utils.sendResponse(
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

        Utils.addCors(exchange);

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


    // ==========================================
    // STATUS
    // ==========================================
    static void handleStatus(
            HttpExchange exchange)
            throws IOException {

        Utils.addCors(exchange);

        String response =
                "{"
                + "\"status\":\"TERKUNCI\","
                + "\"server\":\"ONLINE\""
                + "}";

        Utils.sendResponse(
                exchange,
                200,
                "application/json",
                response
        );

        // Kode hanya ditampilkan di terminal/log.
        System.out.println(
                "\nKode aktif : "
                + BrankasServer.brankas
                        .getKodeAktif()
        );
    }


    // ==========================================
    // BUKA BRANKAS
    // ==========================================
    static void handleBuka(
            HttpExchange exchange)
            throws IOException {

        Utils.addCors(exchange);

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


        // Validasi kode menggunakan method
        // yang dioverride pada BrankasKeamanan.
        if (
                kodeMasuk.matches("\\d{6}")
                &&
                BrankasServer.brankas
                        .bukaBrankas(kodeMasuk)
        ) {

            System.out.println(
                    "\nKode benar."
            );

            System.out.println(
                    "BRANKAS TERBUKA!"
            );

            Utils.sendResponse(
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

            Utils.sendResponse(
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
}


// ==============================================
// UTILITAS HTTP
// ==============================================
class Utils {

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
