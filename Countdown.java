public class Countdown {

    public static void start(
            BrankasKeamanan brankas,
            int waktuPerubahan) {

        Thread countdown =
                new Thread(() -> {

            long periodeSebelumnya =
                    System.currentTimeMillis()
                    / 1000
                    / waktuPerubahan;

            while (true) {

                try {

                    long sekarang =
                            System.currentTimeMillis();

                    long periodeSekarang =
                            sekarang
                            / 1000
                            / waktuPerubahan;

                    // Menghitung sisa waktu sampai
                    // kode berganti.
                    long sisa =
                            waktuPerubahan
                            - (
                                (sekarang / 1000)
                                % waktuPerubahan
                            );

                    System.out.print(
                            "\rKode akan berubah dalam : "
                            + sisa
                            + " detik   "
                    );

                    // Jika masuk periode baru,
                    // tampilkan kode yang baru.
                    if (
                            periodeSekarang
                            != periodeSebelumnya
                    ) {

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

        // Countdown tidak menghalangi server.
        countdown.setDaemon(true);

        countdown.start();
    }
}
