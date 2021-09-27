package com.komputerkit.pointofsaletokopluskeuangan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class ActivityTransaksi extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    Database db;
    Config config;
    Toolbar appbar;
    static boolean status;
    BillingProcessor bp;
    SharedPreferences getPrefs ;
    String deviceid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaksi);

        db = new Database(this);
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Transaksi",getSupportActionBar());

        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        deviceid = Function.getDecrypt(getPrefs.getString("deviceid","")) ;

        bp = new BillingProcessor(this, Function.getBase64Code(), this);
        bp.initialize();

        if (!bp.isPurchased("postpkpro")){
            status = false;
        } else {
            status = true;
        }
    }

    private boolean limit(String item){
        int batas = Function.strToInt(config.getCustom(item, "1"));
        if (batas>5){
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void penjualan(View view) {
        Cursor c = db.sq("SELECT * FROM tblbarang WHERE idbarang");
        Cursor cur = db.sq("SELECT * FROM tblsatuan WHERE idsatuan");
        if (c.getCount()==0 && cur.getCount()==0){
            Toast.makeText(this, "Silahkan Masukan Data di menu Master", Toast.LENGTH_SHORT).show();
        } else {
        if (status){
                Intent intent = new Intent(this, ActivityPenjualan.class);
                startActivity(intent);
            } else {
            if (limit("penjualan")) {
                Intent intent = new Intent(this, ActivityPenjualan.class);
                startActivity(intent);
            } else {
                bp.purchase(this, "postpkpro");
            }
        }
        }
    }

    public void kembali(View view) {
        Cursor c = db.sq("SELECT * FROM tblorder WHERE bayar>0");
        if (c.getCount()==0){
            Toast.makeText(this, "Silahkan Lakukan Transaksi di menu Penjualan", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ActivityReturn.class);
            startActivity(intent);
        }
    }

    public void pemasukan(View view) {
        Intent intent = new Intent(this, ActivityTambahPemasukan.class);
        startActivity(intent);
    }

    public void pengeluaran(View view) {
        Cursor c = db.sq("SELECT * FROM tbltransaksi WHERE idtransaksi");
        if (c.getCount()==0){
            Toast.makeText(this, "Silahkan Inputkan Pemasukan", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ActivityTambahPengeluaran.class);
            startActivity(intent);
        }
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        status = true;
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }
}
