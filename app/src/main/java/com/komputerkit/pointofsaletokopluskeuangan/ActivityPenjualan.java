package com.komputerkit.pointofsaletokopluskeuangan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityPenjualan extends AppCompatActivity {

    Button btnSimpan;
    Spinner spSatuan;
    EditText edtTglOrder,edtNamaPelanggan,edtKeterangan;
    ImageButton btnTglOrder,btnCariPelanggan,btnCariBarang;
    View v;
    Database db;
    Toolbar appbar;

    Config temp;

    int year, month, day ;
    Calendar calendar ;

    String faktur="00000000",Satuan, plgn;
    int tIdsatuan,tIdpelanggan=0,tIdbarang,tIdkategori,tJumlah=0, isikeranjang=0;
    String tnPelanggan,tKeterangan,totalbayar="",tnBarang="",tHargabesar="",tStok="",tHargakecil="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Penjualan",getSupportActionBar());

        temp = new Config(getSharedPreferences("temp",this.MODE_PRIVATE));
        db=new Database(this);
        v = this.findViewById(android.R.id.content);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        cek();
        loadCart();
        btnCari();
        getTotal();

        String date_n=new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Function.setText(v, R.id.edtTglOrder,date_n);
        btnTglOrder = (ImageButton)findViewById(R.id.ibtnTglOrder);
        btnTglOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(1);
            }
        });

//        btnAddJ=(ImageButton)findViewById(R.id.ibtnPlus);
//        btnAddJ.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tJumlah=tJumlah+1;
//                setJumlah(tJumlah,tStok);
//            }
//        });
//        btnRemoveJ=(ImageButton)findViewById(R.id.ibtnMinus);
//        btnRemoveJ.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tJumlah=tJumlah-1;
//                setJumlah(tJumlah,tStok);
//            }
//        });

        final List<String> getIdSat = db.getIdSatuan();
        spSatuan=(Spinner)findViewById(R.id.spSatuan);
        getSatuanData();
        spSatuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Satuan = Function.intToStr(spSatuan.getSelectedItemPosition());
                if (tIdbarang==0){
                    Function.setText(v, R.id.edtJumlah,"0");
                }else{
                    if (position==0){

                        Cursor c= db.sq(Query.selectwhere("tblbarang")+"idbarang="+Function.intToStr(tIdbarang));
                        c.moveToNext();
                        String idSatuan = Function.getString(c, "idsatuan");
                        Cursor cc= db.sq(Query.selectwhere("tblsatuan")+"idsatuan="+idSatuan);
                        cc.moveToNext();
                        Function.setText(v, R.id.edtJumlah, Function.getString(cc, "nilai"));

                        setEdit(tnBarang,tHargakecil,tKeterangan);

                    }else{
                        Function.setText(v, R.id.edtJumlah,"1");
                        setEdit(tnBarang,tHargabesar,tKeterangan);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void cek(){
        Cursor c = db.sq(Query.selectwhere("qorder")+Query.sWhere("bayar", "0"));
        if (c.getCount()>0){
            c.moveToLast();
            String faktur = Function.getString(c, "faktur");
                Function.setText(v, R.id.edtNomorFaktur, faktur);
        } else {
                getFaktur();
        }
    }

    private void getFaktur(){
        List<Integer> idorder = new ArrayList<Integer>();
        String q="SELECT idorder FROM tblorder";
        Cursor c = db.sq(q);
        if (c.moveToNext()){
            do {
                idorder.add(c.getInt(0));
            }while (c.moveToNext());
        }
        String tempFaktur="";
        int IdFaktur=0;
        if (c.getCount()==0){
            tempFaktur=faktur.substring(0,faktur.length()-1)+"1";
        }else {
            IdFaktur = idorder.get(c.getCount()-1)+1;
            tempFaktur = faktur.substring(0,faktur.length()-String.valueOf(IdFaktur).length())+String.valueOf(IdFaktur);
        }
        Function.setText(v,R.id.edtNomorFaktur,tempFaktur);
    }


    private void setEdit(String barang, String tHargabesar, String tHargakecil){
        plgn = temp.getCustom("idpelanggan","") ;

        if (TextUtils.isEmpty(plgn)) {
            temp.setCustom("idpelanggan", "0");
            plgn = "0";
            Function.setText(v, R.id.edtNamaPelanggan, plgn);
        }

        if (!TextUtils.isEmpty(plgn)){
            getPelanggan(Function.intToStr(tIdpelanggan));
        } else {
            Function.setText(v, R.id.edtNamaPelanggan, "");
        }

        Function.setText(v,R.id.edtNamaBarang,barang);
        Function.setText(v,R.id.edtHargaBarang,tHargabesar);
    }

    private void btnCari(){
        btnCariPelanggan=(ImageButton)findViewById(R.id.ibtnNamaPelanggan);
        btnCariPelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(ActivityPenjualan.this,ActivityPenjualanCari.class);
                i.putExtra("cari","pelanggan");
                startActivityForResult(i,1000);
            }
        });
        btnCariBarang=(ImageButton)findViewById(R.id.ibtnNamaBarang);
        btnCariBarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(ActivityPenjualan.this,ActivityPenjualanCari.class);
                i.putExtra("cari","barang");
                startActivityForResult(i,2000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==1000){
            tIdpelanggan=data.getIntExtra("idpelanggan",0);
            tnPelanggan=data.getStringExtra("pelanggan");
            getPelanggan(Function.intToStr(tIdpelanggan));
        }else if (resultCode==2000){
            tIdbarang=data.getIntExtra("idbarang",0);
            tIdkategori=data.getIntExtra("idkategori",0);
            tIdsatuan=data.getIntExtra("idsatuan",0);
            tnBarang=data.getStringExtra("barang");
            tStok=data.getStringExtra("stok");
            tHargabesar=data.getStringExtra("hargabesar");
            tHargakecil=data.getStringExtra("hargakecil");
            getBarang(Function.intToStr(tIdbarang));
        }
    }


    public void setDate(int i) {
        showDialog(i);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 1) {
            return new DatePickerDialog(this, dTerima, year, month, day);
        }
        return null;
    }
    private DatePickerDialog.OnDateSetListener dTerima = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int thn, int bln, int day) {
            Function.setText(v, R.id.edtTglOrder, Function.setDatePickerNormal(thn,bln+1,day)) ;
        }
    };

//    private void setJumlah(Integer jumlah, String biaya){
//        Function.setText(v,R.id.edtJumlah,String.valueOf(jumlah));
//        if (jumlah<1){
//            btnRemoveJ.setVisibility(View.INVISIBLE);
//        }else {
//            btnRemoveJ.setVisibility(View.VISIBLE);
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        tJumlah=0;
        Function.setText(v,R.id.edtJumlah,"0");
        Function.setText(v,R.id.edtNamaPelanggan,"0");
        setEdit(tnBarang,tHargabesar,tHargakecil);
        getSatuanData();
    }

    private void keluar(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.create();
        builder.setMessage("Anda yakin ingin keluar?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(ActivityPenjualan.this, ActivityTransaksi.class) ;
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
                startActivity(i);
            }
        }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();

    }

    @Override
    public void onBackPressed() {
        keluar();
    }

    public String convertDate(String date){
        String[] a = date.split("/") ;
        return a[2]+a[1]+a[0];
    }

    public void insertTransaksi(View view) {
        String eFaktur = Function.getText(v,R.id.edtNomorFaktur);
        String eTglT = Function.getText(v,R.id.edtTglOrder);
        String eNPelanggan = Function.getText(v,R.id.edtNamaPelanggan);
        String eNBarang = Function.getText(v,R.id.edtNamaBarang);
        String eHarga = Function.getText(v,R.id.edtHargaBarang);
        String eJumlah = Function.getText(v,R.id.edtJumlah);
        String eKet = Function.getText(v,R.id.edtKeterangan);
        if (TextUtils.isEmpty(eNBarang) || TextUtils.isEmpty(eNPelanggan) || Function.strToDouble(eHarga)==0 || Function.strToDouble(eJumlah)==0) {
            Toast.makeText(this, "Masukkan data dengan benar", Toast.LENGTH_SHORT).show();
        }else {
            String idPelanggan=String.valueOf(tIdpelanggan);
            String idBarang=String.valueOf(tIdbarang);
            Integer idorder = Integer.valueOf(eFaktur);
            Double bay = Function.strToDouble(eHarga) * Function.strToDouble(eJumlah);
            String harga="";
            String qOrderD,qOrder ;
            String[] detail ={
                    idBarang,
                    String.valueOf(idorder),
                    Satuan,
                    eHarga,
                    eJumlah,
                    eKet
            };
            String[] simpan = {
                    String.valueOf(idorder),
                    eFaktur,
                    Function.removeE(harga),
                    convertDate(eTglT),
                    idPelanggan
            } ;
            String q = Query.selectwhere("tblorder")+Query.sWhere("faktur",eFaktur);
            Cursor c = db.sq(q);
            if (c.getCount()==0){
                qOrder=Query.splitParam("INSERT INTO tblorder (idorder,faktur,total,tglorder,idpelanggan) VALUES (?,?,?,?,?)",simpan);
                qOrderD=Query.splitParam("INSERT INTO tblorderdetail (idbarang,idorder,satuanjual,hargajual,jumlah,keterangan) VALUES (?,?,?,?,?,?)",detail);
            }else {
//                qOrder=Query.splitParam("UPDATE INTO tblorder (idorder,total,tglorder,idpelanggan) VALUES (?,?,?,?)",simpan);
                qOrder="UPDATE tblorder SET " +
                        "idpelanggan=" +idPelanggan+","+
                        "tglorder="+convertDate(eTglT)+
                        " WHERE idorder=" +String.valueOf(idorder);
                qOrderD=Query.splitParam("INSERT INTO tblorderdetail (idbarang,idorder,satuanjual,hargajual,jumlah,keterangan) VALUES (?,?,?,?,?,?)",detail);
            }

            Cursor cursor = db.sq(Query.selectwhere("qbarang") + Query.sWhere("idbarang", idBarang));
            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                Spinner sp = findViewById(R.id.spSatuan);
                String stok = "",stoktetep;
                Double kurang;
                if (sp.getSelectedItemPosition() == 1) {
                    stok = Function.getString(cursor, "stok");
                    stoktetep=stok;
                    kurang=Function.strToDouble(Function.getText(v, R.id.edtJumlah));
                } else {
                    stok = Function.getString(cursor, "stok");
                    stoktetep=stok;
                    Double nilai = Function.strToDouble(Function.getString(cursor, "nilai"));
                    stok = Function.doubleToStr(Function.strToDouble(stok)*nilai);
                    kurang = Function.strToDouble(Function.getText(v, R.id.edtJumlah))/nilai;

                }
                String total=Function.doubleToStr(Function.strToDouble(stoktetep)-kurang);
                if(Function.strToDouble(stok) >= Function.strToDouble(Function.getText(v,R.id.edtJumlah))){
                    if (db.exc(qOrder)&&db.exc(qOrderD)){
                    Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show();
                    db.exc("UPDATE tblbarang SET stok="+ total + " WHERE idbarang =" + idBarang);
                    getTotal();
                    clearText();
                    loadCart();
                    }else if (db.exc(qOrderD)) {
                        Toast.makeText(this, "Sukses", Toast.LENGTH_SHORT).show();
                        db.exc("UPDATE tblbarang SET stok="+ total + " WHERE idbarang =" + idBarang);
                        getTotal();
                        clearText();
                        loadCart();
                    } else {
                        Toast.makeText(this, "Gagal", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Stok Tidak Cukup untuk Pemesanan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getBarang(String idbarang){
        String q = Query.selectwhere("tblbarang") + Query.sWhere("idbarang", idbarang) ;
        Cursor c = db.sq(q) ;
        c.moveToNext() ;

        Function.setText(v,R.id.edtNamaBarang,Function.getString(c, "barang")) ;
    }

    public void getPelanggan(String idpelanggan){
        String q = Query.selectwhere("tblpelanggan") + Query.sWhere("idpelanggan", idpelanggan) ;
        Cursor c = db.sq(q) ;
        c.moveToNext() ;
        Function.setText(v,R.id.edtNamaPelanggan,Function.getString(c, "pelanggan")) ;
    }

    private void getSatuanData(){
        Database db = new Database(this);

        ArrayList arrayList = new ArrayList();
        arrayList.add("Satuan");
        List<String> labels=arrayList;
        if (tIdbarang == 0) {
            labels= arrayList;
        }else {
            labels=db.getSatuanBarang(Function.intToStr(tIdbarang));
        }

        ArrayAdapter<String> data = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,labels);
        data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSatuan.setAdapter(data);
    }

    public void getTotal(){
        double total=0.0;
        int idorder=Integer.valueOf(Function.getText(v,R.id.edtNomorFaktur));
        Cursor c=db.sq("SELECT SUM(hargajual*jumlah) FROM tblorderdetail WHERE idorder="+String.valueOf(idorder));
        double sum=0.0;
        if (c.moveToFirst()){
            sum = c.getDouble(0);
        }
        total=total+sum;
        totalbayar=String.valueOf(total);
        Function.setText(v,R.id.tvTotalBayar,"Rp. "+Function.removeE(total));
    }
    public void loadCart(){
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recTransaksi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        ArrayList arrayList = new ArrayList();
        RecyclerView.Adapter adapter=new AdapterTransaksi(this,arrayList);
        recyclerView.setAdapter(adapter);

        String tempFaktur=Function.getText(v,R.id.edtNomorFaktur);

        String q=Query.selectwhere("qcart")+Query.sWhere("faktur",tempFaktur);
        Cursor c = db.sq(q);
        if (c.getCount()>0){
            while (c.moveToNext()){
                String campur=Function.getString(c,"idorderdetail")+"__"+
                        Function.getString(c, "keterangan") + "__" +
                        Function.getString(c, "pelanggan") + "__" +
                        Function.getString(c, "jumlah") + "__" +
                        Function.getString(c, "hargajual") + "__" +
                        Function.getString(c, "barang") + "__" +
                        Function.getString(c, "satuanbesar") + "__" +
                        Function.getString(c, "nilai") + "__" +
                        Function.getString(c, "stok") + "__" +
                        Function.getString(c, "satuanjual") + "__" +
                        Function.getString(c, "idbarang:1") + "__" +
                        Function.getString(c, "satuankecil") ;
                arrayList.add(campur);
            }
        }else{

        }
        adapter.notifyDataSetChanged();

    }
    private void clearText(){
        Function.setText(v,R.id.edtNamaBarang,"");
        Function.setText(v,R.id.edtHargaBarang,"");
        Function.setText(v,R.id.edtJumlah,"0");
        Function.setText(v,R.id.edtKeterangan,"");
    }

    public void simpan(View view) {
        String faktur=Function.getText(v,R.id.edtNomorFaktur);
        Cursor c= db.sq("SELECT * FROM qcart WHERE faktur='"+faktur+"'");
        c.moveToNext();
        isikeranjang=c.getCount();
        final Intent i = new Intent(this,ActivityBayar.class);
        i.putExtra("faktur", faktur);
        if (isikeranjang==0){
            Toast.makeText(this, "Masukkan data dengan benar", Toast.LENGTH_SHORT).show();
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.create()
                    .setTitle("Anda Yakin?");
            builder.setMessage("Anda yakin ingin menyimpan pesanan ini ?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }

    }
}

class AdapterTransaksi extends RecyclerView.Adapter<AdapterTransaksi.TransaksiViewHolder>{
    private Context ctxAdapter;
    private ArrayList<String> data;

    public AdapterTransaksi(Context ctxAdapter, ArrayList<String> data) {
        this.ctxAdapter = ctxAdapter;
        this.data = data;
    }

    @NonNull
    @Override
    public TransaksiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_penjualan,viewGroup,false);
        return new TransaksiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiViewHolder holder, int i) {
        final String[] row=data.get(i).split("__");
        String sat,ket;

        holder.pelanggan.setText(row[2]);
        holder.sBarang.setText(Function.removeE(row[3])+"  /");
        holder.harga.setText(row[5]);
        holder.barang.setText("x  "+Function.removeE(row[4]));

        if (row[9].equals("1")){
            sat = row[6];
        }else {
            sat = row[11];
        }
        holder.satuan.setText(sat);

        if (row[1].equals("")){
            ket = "-";
        } else {
            ket = row[1];
        }

        holder.keterangan.setText("Keterangan : "+ket);

        holder.hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Database db=new Database(ctxAdapter);
                AlertDialog.Builder builder=new AlertDialog.Builder(ctxAdapter);
                builder.create();
                builder.setMessage("Anda yakin ingin menghapusnya?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String q = "DELETE FROM tblorderdetail WHERE idorderdetail="+row[0];
                                String stok=row[8];
                                double kurang,nilai;
                                if (row[9].equals("1")){

                                    kurang = Function.strToDouble(row[3]);
                                } else {

                                    nilai = Function.strToDouble(row[7]);
                                    kurang = Function.strToDouble(row[3])/nilai;
                                }
                                String total=Function.doubleToStr(Function.strToDouble(stok)+kurang);
                                db.exc("UPDATE tblbarang SET stok="+ total + " WHERE idbarang =" + row[10]);
                                if (db.exc(q)){
                                    Toast.makeText(ctxAdapter, "Berhasil", Toast.LENGTH_SHORT).show();
                                    ((ActivityPenjualan)ctxAdapter).getTotal();
                                    ((ActivityPenjualan)ctxAdapter).loadCart();
                                }else {
                                    Toast.makeText(ctxAdapter, "Gagal", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class TransaksiViewHolder extends RecyclerView.ViewHolder{
        TextView pelanggan,sBarang,barang,harga,satuan,keterangan;
        ImageButton hapus;
        public TransaksiViewHolder(@NonNull View itemView) {
            super(itemView);
            pelanggan=(TextView)itemView.findViewById(R.id.tvNamaPelanggan);
            sBarang=(TextView)itemView.findViewById(R.id.sBarang);
            barang=(TextView)itemView.findViewById(R.id.tvNamaBarang);
            harga=(TextView)itemView.findViewById(R.id.tvHarga);
            satuan=(TextView)itemView.findViewById(R.id.tvSatuanKecil);
            keterangan=(TextView)itemView.findViewById(R.id.tvKeterangan);
            hapus=(ImageButton)itemView.findViewById(R.id.ibtnHapus);
        }
    }
}
