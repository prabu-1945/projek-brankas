public class Brankas {

    protected String kode;

    public Brankas() {
        kode = generateKode();
    }

    // Method yang akan dioverride oleh subclass
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
