package com.komputerkit.pointofsaletokopluskeuangan;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityLaporanPenjualan extends AppCompatActivity {

    Toolbar appbar;
    View v ;
    Config config ;
    Database db ;
    ArrayList arrayList = new ArrayList() ;
    String dari, ke, total,type ;
    Calendar calendar ;
    int year,month, day ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_penjualan);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        String title = "judul";

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));
        db = new Database(this) ;
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        type = getIntent().getStringExtra("type") ;

        final EditText eCari = (EditText) findViewById(R.id.eCari) ;
        eCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(type.equals("perjenis")){
                    arrayList.clear();
                    loadList2(eCari.getText().toString());
                } else if(type.equals("perbarang")){
                    arrayList.clear();
                    loadList(eCari.getText().toString());
                }
            }
        });

        setText();
        List<String> categories = new ArrayList<>();
          if(type.equals("perjenis")){
            title = ("Laporan per Jenis") ;
            loadList2("");
            categories = new ArrayList<String>();
            categories.add("Semua");
            categories.add("Kulakan");
            categories.add("Titipan");
        } else if (type.equals("perbarang")){
              v.findViewById(R.id.Spinner).setVisibility(View.GONE);
            title = ("Laporan Penjualan") ;
            loadList("");
            categories = new ArrayList<String>();
            categories.add("Tunai");
        }

        Spinner spinner = (Spinner) findViewById(R.id.Spinner) ;
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(type.equals("perjenis")){
                    loadList2(Function.getText(v,R.id.eCari));
                } else if(type.equals("perbarang")){
                    loadList(Function.getText(v,R.id.eCari));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        Function.btnBack(title, getSupportActionBar());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

//    public void print(View v){
//        String faktur = v.getTag().toString() ;
//        Intent i = new Intent(this, ActivityCetak2.class) ;
//        i.putExtra("faktur",faktur) ;
//        startActivity(i);
//    }

    public void setText(){
        dari = Function.setDatePicker(year,month+1,day) ;
        ke = Function.setDatePicker(year,month+1,day) ;
        String now = Function.setDatePickerNormal(year,month+1,day) ;
        Function.setText(v,R.id.eKe,now) ;
        Function.setText(v,R.id.eDari,now) ;
    }

    public void loadList2(String cari){
        String item = Function.getSpinnerItem(v,R.id.Spinner) ;
        String bayar = "" ;
        String q = "" ;
        if(item.equals("Semua")){
            q = Query.selectwhere("qorderdetail")+"bayar>0 AND ("+ Query.sLike("pelanggan",cari) +"  OR  " + Query.sLike("barang",cari) +"  OR  "+ Query.sLike("faktur",cari) +") AND "+Query.sBetween("tglorder",dari,ke) + Query.sOrderASC("tglorder");
        } else if (item.equals("Kulakan")){
            bayar="Kulakan" ;
            q = Query.selectwhere("qorderdetail")+"bayar>0 AND "+Query.sWhere("tipe",bayar)+" AND ("+ Query.sLike("pelanggan",cari) +"  OR  " + Query.sLike("barang",cari) +"  OR  "+ Query.sLike("faktur",cari) +") AND "+Query.sBetween("tglorder",dari,ke) + Query.sOrderASC("tglorder");
        } else if(item.equals("Titipan")){
            bayar="Titipan" ;
            q = Query.selectwhere("qorderdetail")+"bayar>0 AND "+Query.sWhere("tipe",bayar)+" AND ("+ Query.sLike("pelanggan",cari) +"  OR  " + Query.sLike("barang",cari) +"  OR  "+ Query.sLike("faktur",cari) +") AND "+Query.sBetween("tglorder",dari,ke) + Query.sOrderASC("tglorder");
        }
        arrayList.clear();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recUtang) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new AdapterLaporanPenjualan(this,arrayList) ;
        recyclerView.setAdapter(adapter);
        Cursor c = db.sq(q) ;
        if(c.getCount() > 0){
            double jum = 0 ;
            Function.setText(v,R.id.tjumlah,"Jumlah Data : "+Function.intToStr(c.getCount())) ;
            while(c.moveToNext()){
                String pelanggan = Function.getString(c,"pelanggan") ;
                String kembali = Function.getString(c,"barang") ;
                String faktur = Function.getString(c,"faktur") ;
                String jumlah = Function.getString(c,"jumlah") ;
                String harga = Function.getString(c,"hargajual") ;
                double total = Function.strToDouble(harga)*Function.strToDouble(jumlah) ;
                String idorderdetail = Function.getString(c,"idorderdetail") ;
                String tot = Function.getString(c,"total") ;
                String sat = Function.getString(c,"satuanjual") ;
                String idbarang = Function.getString(c, "idbarang:1");

                String campur = idorderdetail+"__"+tot+"__"+sat+"__"+idbarang+"__"+faktur +"__"+pelanggan+"__"+kembali+"__"+Function.removeE(jumlah)+" x "+Function.removeE(harga)+" = "+Function.removeE(total) +"__" +Function.dateToNormal(Function.getString(c,"tglorder")) ;
                arrayList.add(campur);
                jum += total ;
            }
            String a = Function.removeE(jum) ;
            Function.setText(v,R.id.tTotal,"Rp. "+a) ;
        } else {
            Function.setText(v,R.id.tTotal,"Rp. 0") ;
            Function.setText(v,R.id.tjumlah,"Jumlah Data : 0") ;
        }
        adapter.notifyDataSetChanged();
    }

    public void loadList(String cari){
        String q = "" ;
        if(TextUtils.isEmpty(cari)){
            q = Query.selectwhere("qorderdetail")+"bayar>0 AND "+Query.sBetween("tglorder",dari,ke) + " LIMIT 30";
        } else {
            q = Query.selectwhere("qorderdetail")+"bayar>0 AND ("+ Query.sLike("pelanggan",cari) +"  OR  " + Query.sLike("barang",cari) +"  OR  "+ Query.sLike("faktur",cari) +") AND "+Query.sBetween("tglorder",dari,ke);
        }
        arrayList.clear();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recUtang) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new AdapterLaporanPenjualan(this,arrayList) ;
        recyclerView.setAdapter(adapter);
        Cursor c = db.sq(q) ;
        if(c.getCount() > 0){
            double jum = 0 ;
            Function.setText(v,R.id.tjumlah,"Jumlah Data : "+Function.intToStr(c.getCount())) ;
            while(c.moveToNext()){
                String pelanggan = Function.getString(c,"pelanggan") ;
                String kembali = Function.getString(c,"barang") ;
                String faktur = Function.getString(c,"faktur") ;
                String jumlah = Function.getString(c,"jumlah") ;
                String harga = Function.getString(c,"hargajual") ;
                double total = Function.strToDouble(harga)*Function.strToDouble(jumlah) ;
                String idorderdetail = Function.getString(c,"idorderdetail") ;
                String tot = Function.getString(c,"total") ;
                String sat = Function.getString(c,"satuanjual") ;
                String idbarang = Function.getString(c, "idbarang:1");

                String campur = idorderdetail+"__"+tot+"__"+sat+"__"+idbarang+"__"+faktur +"__"+pelanggan+"__"+kembali+"__"+Function.removeE(jumlah)+" x "+Function.removeE(harga)+" = "+Function.removeE(total) +"__" +Function.dateToNormal(Function.getString(c,"tglorder")) ;
                arrayList.add(campur);
                jum += total ;
            }
            String a = Function.removeE(jum) ;
            Function.setText(v,R.id.tTotal,"Rp. "+a) ;
        } else {
            Function.setText(v,R.id.tTotal,"Rp. 0") ;
            Function.setText(v,R.id.tjumlah,"Jumlah Data : 0") ;
        }
        adapter.notifyDataSetChanged();
    }

    public void dateDari(View view){
        setDate(1);
    }
    public void dateKe(View view){
        setDate(2);
    }

    public void filtertgl(){
         if(type.equals("perjenis")){
            loadList2(Function.getText(v,R.id.eCari));
        } else if(type.equals("perbarang")){
            loadList(Function.getText(v,R.id.eCari));
        }
    }

    public void export(View view){
        Intent i = new Intent(this, ActivityExportExcel.class);
        i.putExtra("type","laporanpenjualan") ;
        startActivity(i);
    }

    public void clearText(){
        Function.setText(v, R.id.tjumlah , "Jumlah Data : 0");
    }

    //start date time picker
    public void setDate(int i) {
        showDialog(i);
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 1) {
            return new DatePickerDialog(this, edit1, year, month, day);
        } else if(id == 2){
            return new DatePickerDialog(this, edit2, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener edit1 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int thn, int bln, int day) {
            Function.setText(v, R.id.eDari, Function.setDatePickerNormal(thn,bln+1,day)) ;
            dari = Function.setDatePicker(thn,bln+1,day) ;
            filtertgl();
        }
    };

    private DatePickerDialog.OnDateSetListener edit2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int thn, int bln, int day) {
            Function.setText(v, R.id.eKe, Function.setDatePickerNormal(thn,bln+1,day)) ;
            ke = Function.setDatePicker(thn,bln+1,day) ;
            filtertgl();
        }
    };
    //end date time picker
}





class AdapterLaporanPenjualan extends RecyclerView.Adapter<AdapterLaporanPenjualan.ViewHolder> {
    private ArrayList<String> data;
    Context c;

    public AdapterLaporanPenjualan(Context a, ArrayList<String> kota) {
        this.data = kota;
        c = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_laporan_penjualan_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView faktur, nma, jumlah, tanggal;
        ConstraintLayout print ;

        public ViewHolder(View view) {
            super(view);

            nma = (TextView) view.findViewById(R.id.tHitung);
            tanggal = (TextView) view.findViewById(R.id.tTanggal);
            faktur = (TextView) view.findViewById(R.id.tNama);
            jumlah = (TextView) view.findViewById(R.id.tBarang);
            print = (ConstraintLayout) view.findViewById(R.id.wPrinter);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        final String[] row = data.get(i).split("__");

        viewHolder.jumlah.setText(row[6] + "\t : "+row[7]);
        viewHolder.nma.setText("Pelanggan : "+row[5]);
        viewHolder.tanggal.setText(row[8]);
        viewHolder.faktur.setText(row[4]);
        viewHolder.print.setTag(row[4]);

        viewHolder.print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, ActivityCetak2.class);
                intent.putExtra("faktur", row[4]);
                c.startActivity(intent);
            }
        });
    }
    }
