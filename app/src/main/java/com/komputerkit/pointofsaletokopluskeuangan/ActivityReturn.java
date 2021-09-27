package com.komputerkit.pointofsaletokopluskeuangan;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class ActivityReturn extends AppCompatActivity {

    Toolbar appbar;
    View v ;
    Config config ;
    Database db ;
    ArrayList arrayList = new ArrayList() ;
    String dari, ke ;
    Calendar calendar ;
    int year,month, day ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return) ;
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        appbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(appbar);
        Function.btnBack("Return",getSupportActionBar());

        v = this.findViewById(android.R.id.content);
        config = new Config(getSharedPreferences("config",this.MODE_PRIVATE));
        db = new Database(this) ;
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

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
                arrayList.clear();
                loadList(eCari.getText().toString());
            }
        });

        setText() ;
        loadList("") ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setText(){
        dari = Function.setDatePicker(year,month+1,day) ;
        ke = Function.setDatePicker(year,month+1,day) ;
        String now = Function.setDatePickerNormal(year,month+1,day) ;
        Function.setText(v,R.id.eKe,now) ;
        Function.setText(v,R.id.eDari,now) ;
    }

    public void loadList(String cari){
        arrayList.clear();
        String q = "" ;
        if(TextUtils.isEmpty(cari)){
            q = Query.selectwhere("qorderdetail") +"bayar>0 AND "+ Query.sWhere("return", "0") + " AND " + Query.sBetween("tglorder",dari,ke) + Query.sOrderASC("tglorder");
        } else {
            q = Query.selectwhere("qorderdetail") +"bayar>0 AND "+ Query.sWhere("return", "0") + " AND (" + Query.sLike("barang",cari) + " OR " + Query.sLike("faktur",cari) + ") AND " + Query.sBetween("tglorder",dari,ke);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recUtang) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerView.Adapter adapter = new AdapterReturn(this,arrayList) ;
        recyclerView.setAdapter(adapter);
        Cursor c = db.sq(q) ;
        if(c.getCount() > 0){
//            Function.setText(v,R.id.tTotal,"Jumlah Data : "+String.valueOf(c.getCount())) ;
            while(c.moveToNext()){
                String nama = Function.getString(c,"pelanggan") ;
                String barang = Function.getString(c,"barang") ;
                String faktur = Function.getString(c,"faktur") ;
                String jumlah = Function.getString(c,"jumlah") ;
                String satuan = Function.getString(c, "satuanjual") ;
                String id = Function.getString(c,"idorderdetail") ;
                String metod="";
                String idsatuan=Function.getString(c, "idsatuan");
                Cursor san = db.sq(Query.selectwhere("tblsatuan")+" idsatuan="+idsatuan);
                san.moveToNext();
                if (satuan.equals("1")){
                    metod = Function.getString(san, "satuanbesar");
                } else {
                    metod = Function.getString(san, "satuankecil");
                }

                if(!jumlah.equals("0")){
                    String campur = nama +"__"+barang+"__"+faktur +"__"+jumlah+"__"+metod+"__"+id;
                    arrayList.add(campur);
                }
            }
        } else {

        }
        adapter.notifyDataSetChanged();
    }

    public void filtertgl(){
        loadList(Function.getText(v,R.id.eCari));
    }

    public void dateDari(View view){
        setDate(1);
    }
    public void dateKe(View view){
        setDate(2);
    }
    public void bayarutang(View view){
        String id = view.getTag().toString() ;
        Intent i = new Intent(this, ActivityReturnProses.class) ;
        i.putExtra("idorderdetail",id) ;
        startActivity(i);
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


class AdapterReturn extends RecyclerView.Adapter<AdapterReturn.ViewHolder> {
    private ArrayList<String> data;
    Context c;

    public AdapterReturn(Context a, ArrayList<String> kota) {
        this.data = kota;
        c = a;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_hutang_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView faktur, nma, jumlah, fakt;
        ConstraintLayout wadah ;

        public ViewHolder(View view) {
            super(view);

            nma = (TextView) view.findViewById(R.id.tBarang);
            faktur = (TextView) view.findViewById(R.id.tNama);
            jumlah = (TextView) view.findViewById(R.id.tTotal);
            fakt = (TextView) view.findViewById(R.id.tFaktur);
            wadah = (ConstraintLayout) view.findViewById(R.id.wUtang2) ;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        String[] row = data.get(i).split("__");

        viewHolder.fakt.setText(row[2]);
        viewHolder.jumlah.setText("Jumlah : " + Function.removeE(row[3])+" "+row[4]);
        viewHolder.nma.setText(row[1]);
        viewHolder.faktur.setText("Pelanggan : "+row[0]);
        viewHolder.wadah.setTag(row[5]);
    }
}
