public class BrankasKeamanan extends Brankas {

    private static final long WAKTU_PERUBAHAN = 60;

    @Override
    public String generateKode() {

        return KodeGenerator.generate();
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

    public static long getWaktuPerubahan() {

        return WAKTU_PERUBAHAN;
    }
}
