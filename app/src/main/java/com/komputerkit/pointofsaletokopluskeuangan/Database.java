package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    public static  final String nama_database="db_toko";
    public static final int versi_database=1;
    SQLiteDatabase db;
    Context a;

    public Database(Context context) {
        super(context, nama_database, null, versi_database);
        db = this.getWritableDatabase();
        a = context;
        cektbl();
    }


        public Boolean cektbl() {
            //create tabel barang
            try {
                exc("CREATE TABLE IF NOT EXISTS `tblbarang` (\n" +
                        "\t`idbarang`	INTEGER PRIMARY KEY AUTOINCREMENT, \n" +
                        "\t`idkategori`	INTEGER, \n" +
                        "\t `idsatuan`	INTEGER, \n" +
                        "\t `barang`	TEXT, \n" +
                        "\t `stok`	REAL, \n" +
                        "\t `hargabesar`	REAL, \n" +
                        "\t `hargakecil`	REAL, \n" +
                        "\t 'tipe' TEXT, \n" +
                        "\t FOREIGN KEY(`idkategori`) REFERENCES `tblkategori`(`idkategori`) ON UPDATE CASCADE ON DELETE RESTRICT, \n" +
                        "\t FOREIGN KEY(`idsatuan`) REFERENCES `tblsatuan`(`idsatuan`) ON UPDATE CASCADE ON DELETE RESTRICT \n" +
                        ");");

            //create tabel kategori
                exc("CREATE TABLE IF NOT EXISTS `tblkategori` (\n" +
                        "\t`idkategori` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`kategori` \tTEXT\n" +
                        ");");

            //create tabel order
                exc("CREATE TABLE IF NOT EXISTS `tblorder` (\n" +
                        "\t`idorder` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`faktur` \tTEXT,\n" +
                        "\t`tglorder` \tSTRING,\n" +
                        "\t`idpelanggan` \tINTEGER,\n" +
                        "\t`total` \tREAL,\n" +
                        "\t`bayar` \tREAL DEFAULT 0,\n" +
                        "\t`kembali` \tREAL,\n" +
                        "\tFOREIGN KEY(`idpelanggan`) REFERENCES `tblpelanggan`(`idpelanggan`) on update cascade on delete restrict\n" +
                        ");");

            //create tabel order detail
                exc("CREATE TABLE IF NOT EXISTS `tblorderdetail` (\n" +
                        "\t`idorderdetail` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`idorder` \tINTEGER,\n" +
                        "\t`idbarang` \tINTEGER,\n" +
                        "\t`satuanjual` \tREAL,\n" +
                        "\t`hargajual` \tREAL,\n" +
                        "\t`jumlah` \tREAL,\n" +
                        "\t`keterangan` \tTEXT,\n" +
                        "\t`return` \tTEXT DEFAULT 0,\n" +
                        "\tFOREIGN KEY(`idorder`) REFERENCES `tblorder`(`idorder`) ON UPDATE CASCADE ON DELETE RESTRICT,\n" +
                        "\tFOREIGN KEY(`idorder`) REFERENCES `tblbarang`(`idbarang`)\n" +
                        ");");

            //create tabel pelanggan
                exc("CREATE TABLE IF NOT EXISTS `tblpelanggan` (\n" +
                        "\t`idpelanggan` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`pelanggan` \tTEXT,\n" +
                        "\t`alamat` \tTEXT,\n" +
                        "\t`notelp` \tTEXT\n" +
                        ");");

            //create tabel satuan
                exc("CREATE TABLE IF NOT EXISTS `tblsatuan` (\n" +
                        "\t`idsatuan` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`satuankecil` \tTEXT,\n" +
                        "\t`satuanbesar` \tTEXT,\n" +
                        "\t`nilai` \tREAL\n" +
                        ");");

                //create tabel Identitas
                exc("CREATE TABLE IF NOT EXISTS `tblidentitas` (\n" +
                        "\t`idtoko` \tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`namatoko` \tTEXT,\n" +
                        "\t`alamattoko` \tTEXT,\n" +
                        "\t`telp` \tSTRING,\n" +
                        "\t`cappertama` \tTEXT,\n" +
                        "\t`capkedua` \tTEXT,\n" +
                        "\t`capketiga` \tTEXT\n" +
                        ");");

                //create tabel return
                exc("CREATE TABLE IF NOT EXISTS `tblreturn` (\n" +
                        "\t`idreturn` \tINTEGER,\n" +
                        "\t`idbarang` \tINTEGER,\n" +
                        "\t`fakturbayar` \tTEXT,\n" +
                        "\t`tglreturn` \tSTRING,\n" +
                        "\t`jumlah` \tINTEGER,\n" +
                        "\t PRIMARY KEY(`idreturn`)" +
                        ");");

                //create tabel transaksi
                exc("CREATE TABLE `tbltransaksi` (\n" +
                        "\t`idtransaksi`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "\t`tgltransaksi`\tINTEGER,\n" +
                        "\t`notransaksi`\tINTEGER,\n" +
                        "\t`fakturtransaksi`\tTEXT,\n" +
                        "\t`keterangantransaksi`\tTEXT,\n" +
                        "\t`masuk`\tREAL DEFAULT 0,\n" +
                        "\t`keluar`\tREAL DEFAULT 0,\n" +
                        "\t`saldo`\tREAL DEFAULT 0,\n" +
                        "\t`status` \tTEXT\n" +
                        ");");

                //create view
                exc("CREATE VIEW qcart AS SELECT tblorderdetail.idorderdetail, tblorderdetail.idorder, tblorderdetail.idbarang,tblorderdetail.satuanjual, tblorder.faktur, tblorder.total,tblorderdetail.satuanjual, tblorderdetail.hargajual, tblorderdetail.jumlah, tblorderdetail.keterangan, tblpelanggan.pelanggan,tblbarang.idkategori,tblbarang.barang,tblbarang.stok, tblkategori.kategori, tblsatuan.satuankecil, tblsatuan.satuanbesar, tblsatuan.nilai FROM tblsatuan INNER JOIN (tblpelanggan INNER JOIN (tblorder INNER JOIN ((tblkategori INNER JOIN tblbarang ON tblkategori.idkategori = tblbarang.idkategori) INNER JOIN tblorderdetail ON tblbarang.idbarang = tblorderdetail.idbarang) ON tblorder.idorder = tblorderdetail.idorder) ON tblpelanggan.idpelanggan = tblorder.idpelanggan) ON tblsatuan.idsatuan = tblbarang.idsatuan");

                exc("CREATE VIEW qbarang AS SELECT tblbarang.idbarang, tblbarang.idkategori, tblbarang.idsatuan, tblbarang.barang, tblbarang.stok, tblbarang.hargabesar, tblbarang.hargakecil, tblbarang.tipe, tblkategori.kategori, tblsatuan.satuankecil, tblsatuan.satuanbesar, tblsatuan.nilai FROM tblsatuan INNER JOIN (tblkategori INNER JOIN tblbarang ON tblkategori.idkategori = tblbarang.idkategori) ON tblsatuan.idsatuan = tblbarang.idsatuan");

                exc("CREATE VIEW qorder AS SELECT tblorder.idorder, tblorder.faktur, tblorder.tglorder, tblorder.idpelanggan, tblorder.total, tblorder.bayar, tblorder.kembali, tblpelanggan.pelanggan, tblpelanggan.alamat, tblpelanggan.notelp FROM tblpelanggan INNER JOIN tblorder ON tblpelanggan.idpelanggan = tblorder.idpelanggan");

                exc("CREATE VIEW qorderdetail AS SELECT tblorderdetail.idorderdetail, tblorderdetail.idorder, tblorderdetail.idbarang, tblorderdetail.satuanjual, tblorderdetail.hargajual, tblorderdetail.jumlah, tblorderdetail.keterangan, tblorderdetail.return, tblbarang.idkategori, tblbarang.idsatuan, tblbarang.barang, tblbarang.stok, tblbarang.hargabesar, tblbarang.hargakecil, tblbarang.tipe, tblorder.idorder, tblorder.faktur, tblorder.tglorder, tblorder.idpelanggan, tblorder.total, tblorder.bayar, tblorder.kembali, tblpelanggan.pelanggan, tblpelanggan.alamat, tblpelanggan.notelp FROM tblpelanggan INNER JOIN (tblorder INNER JOIN (tblbarang INNER JOIN tblorderdetail ON tblbarang.idbarang = tblorderdetail.idbarang) ON tblorder.idorder = tblorderdetail.idorder) ON tblpelanggan.idpelanggan = tblorder.idpelanggan");

                exc("CREATE VIEW qreturn AS SELECT tblbarang.idkategori, tblbarang.idsatuan, tblbarang.barang, tblbarang.stok, tblbarang.hargabesar, tblbarang.hargakecil, tblreturn.idreturn, tblreturn.idbarang, tblreturn.fakturbayar, tblreturn.tglreturn, tblreturn.jumlah FROM tblbarang INNER JOIN tblreturn ON tblbarang.idbarang = tblreturn.idbarang");

            //create trigger kurang_stok
//                exc("CREATE TRIGGER kurang_stok BEFORE INSERT ON tblorderdetail FOR EACH ROW BEGIN UPDATE tblbarang SET stok=stok - NEW.jumlah WHERE idbarang = NEW.idbarang; END");

                //create trigger tambah_stok
//                exc("CREATE TRIGGER tambah_stok AFTER INSERT ON tblreturn FOR EACH ROW BEGIN UPDATE tblbarang SET stok=stok + NEW.jumlah WHERE idbarang = NEW.idbarang; END");

            //create trigger kurang_total
                exc("CREATE TRIGGER kurang_total AFTER DELETE ON tblorderdetail FOR EACH ROW BEGIN UPDATE tblorder SET total=total - (OLD.hargajual * OLD.jumlah) WHERE idorder = OLD.idorder; END");

            //create trigger tambah_total
                exc("CREATE TRIGGER tambah_total AFTER INSERT ON tblorderdetail FOR EACH ROW BEGIN UPDATE tblorder SET total= total + (NEW.hargajual * NEW.jumlah) WHERE idorder = NEW.idorder; END");

                //create Identitas
             exc("INSERT INTO tblidentitas VALUES (1, 'KomputerKit.com','Sidoarjo','0838 320 320 77','Terima Kasih','Sudah Berbelanja','Di Toko Kami')");

                //create Pelanggan
                exc("INSERT INTO tblpelanggan VALUES ('0', 'Kosong','','')");

            return true;
        }catch (Exception e){
                return false;
            }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +nama_database);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.disableWriteAheadLogging();
        }
    }

    public boolean exc(String query){
        try {
            db.execSQL(query);
            return true ;
        } catch (Exception e){
            return false ;
        }
    }

    public Cursor sq(String query){
        try {
            Cursor cursor = db.rawQuery(query, null);
            return cursor;
        } catch (Exception e){
            return null ;
        }
    }

    //kategori

    public List<String> getIdKategori(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblkategori");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(0));
            }while (c.moveToNext());
        }
        return labels;
    }

    public boolean insertKategori(String Kategori){
        ContentValues cv= new ContentValues();
        cv.put("kategori", Kategori );
        long result= db.insert("tblkategori", null, cv);
        if (result==-1){
            return false;
        }else {
            return true;
        }
    }

    public List<String> getKategori(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblkategori");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(1));
            }while (c.moveToNext());
        }
        return labels;
    }

    public Boolean deleteKategori(Integer idKategori){
        if (db.delete("tblkategori","idkategori= ?",new String[]{String.valueOf(idKategori)})==-1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean updateKategori(Integer idKategori, String kategori) {
        ContentValues cv = new ContentValues();
        cv.put("kategori", kategori);
        long result = db.update("tblkategori", cv, "idkategori=?", new String[]{String.valueOf(idKategori)});

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

        //satuan

        public List<String> getIdSatuan(){
            List<String> labels = new ArrayList<String>();
            String q=Query.select("tblsatuan");
            Cursor c = db.rawQuery(q,null);
            if (c.moveToNext()){
                do {
                    labels.add(c.getString(0));
                }while (c.moveToNext());
            }
            return labels;
        }

        public boolean insertSatuan(String satuankecil, String satuanbesar, String nilai){
            ContentValues cv= new ContentValues();
            cv.put("satuankecil", satuankecil );
            cv.put("satuanbesar", satuanbesar );
            cv.put("nilai", nilai );
            long result= db.insert("tblsatuan", null, cv);
            if (result==-1){
                return false;
            }else {
                return true;
            }
        }

        public List<String> getSatuan(){
            List<String> labels = new ArrayList<String>();
            String q=Query.select("tblsatuan");
            Cursor c = db.rawQuery(q,null);
            if (c.moveToNext()){
                do {
                    labels.add(c.getString(2));
                }while (c.moveToNext());
            }
            return labels;
        }

    public List<String> getSatuan2(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblsatuan");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(1)+" - "+c.getString(2));
            }while (c.moveToNext());
        }
        return labels;
    }

    public List<String> getSatuanBarang(String idbarang){
        List<String> labels = new ArrayList<String>();
        String q=Query.selectwhere("tblbarang")+" idbarang="+idbarang;
        Cursor c = db.rawQuery(q,null);
        c.moveToNext();
        String idkategori=c.getString(2);
        String qq=Query.selectwhere("tblsatuan")+" idsatuan="+idkategori;
        Cursor cc = db.rawQuery(qq,null);
        if (cc.moveToNext()){
            do {
                labels.add(cc.getString(1));
                labels.add(cc.getString(2));
            }while (cc.moveToNext());
        }
        return labels;
    }

        public Boolean deleteSatuan(Integer idSatuan){
            if (db.delete("tblsatuan","idsatuan= ?",new String[]{String.valueOf(idSatuan)})==-1){
                return false;
            }else{
                return true;
            }
        }

        public Boolean updateSatuan(Integer idSatuan, String satuankecil, String satuanbesar, String nilai){
            ContentValues cv = new ContentValues();
            cv.put("satuankecil", satuankecil);
            cv.put("satuanbesar", satuanbesar);
            cv.put("nilai", nilai);
            long result = db.update("tblsatuan", cv, "idsatuan=?", new String[]{String.valueOf(idSatuan)});

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }

    //barang

    public List<String> getIdBarang(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblbarang");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(0));
            }while (c.moveToNext());
        }
        return labels;
    }

    public boolean insertBarang(int idkategori, int idsatuan,String barang, String stok, String hargabesar, String hargakecil, String tipe){
        ContentValues cv= new ContentValues();
        cv.put("idkategori", idkategori );
        cv.put("idsatuan",idsatuan );
        cv.put("tipe", tipe );
        cv.put("barang", barang );
        cv.put("stok", stok );
        cv.put("hargabesar", hargabesar );
        cv.put("hargakecil", hargakecil );
        long result= db.insert("tblbarang", null, cv);
        if (result==-1){
            return false;
        }else {
            return true;
        }
    }

    public List<String> getBarang(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblbarang");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(1));
            }while (c.moveToNext());
        }
        return labels;
    }

    public Boolean deleteBarang(Integer idbarang){
        if (db.delete("tblbarang","idbarang= ?",new String[]{String.valueOf(idbarang)})==-1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean updateBarang(Integer idbarang, Integer idkategori, int idsatuan, String barang, String stok, String hargabesar, String hargakecil, String tipe){
        ContentValues cv = new ContentValues();
        cv.put("idkategori", idkategori);
        cv.put("idsatuan", idsatuan);
        cv.put("tipe", tipe );
        cv.put("barang", barang );
        cv.put("stok", stok );
        cv.put("hargabesar", hargabesar );
        cv.put("hargakecil", hargakecil );
        long result = db.update("tblbarang", cv, "idbarang=?", new String[]{String.valueOf(idbarang)});

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    //pelanggan

    public List<String> getIdPelanggan(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblpelanggan");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(0));
            }while (c.moveToNext());
        }
        return labels;
    }

    public boolean insertPelanggan(String pelanggan, String alamat, String notelp){
        ContentValues cv= new ContentValues();
        cv.put("pelanggan", pelanggan );
        cv.put("alamat", alamat );
        cv.put("notelp", notelp );
        long result= db.insert("tblpelanggan", null, cv);
        if (result==-1){
            return false;
        }else {
            return true;
        }
    }

    public List<String> getPelanggan(){
        List<String> labels = new ArrayList<String>();
        String q=Query.select("tblpelanggan");
        Cursor c = db.rawQuery(q,null);
        if (c.moveToNext()){
            do {
                labels.add(c.getString(1));
            }while (c.moveToNext());
        }
        return labels;
    }

    public Boolean deletePelanggan(Integer idpelanggan){
        if (db.delete("tblpelanggan","idpelanggan= ?",new String[]{String.valueOf(idpelanggan)})==-1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean updatePelanggan(int idpelanggan, String pelanggan, String alamat, String notelp){
        ContentValues cv = new ContentValues();
        cv.put("pelanggan", pelanggan );
        cv.put("alamat", alamat );
        cv.put("notelp", notelp );
        long result = db.update("tblpelanggan", cv, "idpelanggan=?", new String[]{String.valueOf(idpelanggan)});

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
}

