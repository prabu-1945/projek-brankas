import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class BrankasServer {

    // Object brankas
    static BrankasKeamanan brankas =
            new BrankasKeamanan();

    public static void main(String[] args)
            throws Exception {

        int port = 8081;

        String portEnv =
                System.getenv("PORT");

        // Vercel memberikan PORT melalui environment.
        // Saat dijalankan lokal, gunakan 8081.
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

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(
                                "0.0.0.0",
                                port
                        ),
                        0
                );

        // Menghubungkan alamat dengan handler.
        server.createContext(
                "/",
                WebHandler::handleHome
        );

        server.createContext(
                "/style.css",
                WebHandler::handleCSS
        );

        server.createContext(
                "/status",
                WebHandler::handleStatus
        );

        server.createContext(
                "/buka",
                WebHandler::handleBuka
        );

        server.setExecutor(
                Executors.newCachedThreadPool()
        );

        server.start();

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
                "Pergantian kode : "
                + BrankasKeamanan.getWaktuPerubahan()
                + " detik"
        );
        System.out.println(
                "======================================"
        );

        // Menampilkan kode aktif untuk pengujian lokal.
        System.out.println(
                "KODE BRANKAS AKTIF : "
                + brankas.getKodeAktif()
        );

        System.out.println(
                "======================================"
        );

        // Menjalankan countdown.
        Countdown.start(
                brankas,
                (int) BrankasKeamanan.getWaktuPerubahan()
        );
    }
}
