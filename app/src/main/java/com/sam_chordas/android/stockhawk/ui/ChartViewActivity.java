package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.widget.WidgetService;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Bharat on 6/1/2016.
 */

public class ChartViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.graphfragment, new GraphFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public static class GraphFragment extends Fragment {
        private LineChartView mChart;
        private static final int URL_LOADER = 0;
        private String stockSymbol;
        private static final String[] STOCK_COLUMNS = {
                QuoteColumns._ID,
                QuoteColumns.SYMBOL,
                QuoteColumns.BIDPRICE
        };
        // these indices must match the projection
        static final int INDEX_STOCK_ID = 0;
        static final int INDEX_STOCK_SYMBOL = 1;
        static final int INDEX_STOCK_PRICE = 2;


        public GraphFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.activity_line_graph, container, false);


            mChart = (LineChartView) rootView.findViewById(R.id.linechart);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                stockSymbol = intent.getStringExtra("symbol");
                getActivity().setTitle("Trend for: " + stockSymbol.toUpperCase());
                Float[] stockPrices = getStockPrices();
                float[] floatArray = new float[stockPrices.length];
                int i = 0;

                for (Float f : stockPrices) {
                    floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
                }
                String[] stockID = getStockID();


                LineSet dataset = new LineSet(stockID, floatArray);
                dataset.setColor(Color.parseColor("#f2f2f2"))
                        .setDotsColor(Color.parseColor("#f2f2f2"))
                        .setThickness(4);
                mChart.addData(dataset);

                dataset = new LineSet(stockID, floatArray);
                dataset.setColor(Color.parseColor("#f2f2f2"))
                        .setDotsColor(Color.parseColor("#f2f2f2"))
                        .setThickness(4);
                mChart.addData(dataset);

                // Chart
                mChart.setBorderSpacing(Tools.fromDpToPx(15))
                        .setYLabels(AxisController.LabelPosition.OUTSIDE)
                        .setLabelsColor(Color.parseColor("#6a84c3"))
                        .setAxisBorderValues(0, 200)
                        .setStep(5)
                        .setXAxis(false)
                        .setYAxis(true);

                mChart.show();

            }
            return rootView;
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement


            return super.onOptionsItemSelected(item);
        }

        public Float[] getStockPrices() {
            Uri StockURI = QuoteProvider.Quotes.withSymbol(stockSymbol);
            Cursor cursor = getContext().getContentResolver().query(StockURI,
                    STOCK_COLUMNS,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            ArrayList<Float> stockPrices = new ArrayList<Float>();
            while (!cursor.isAfterLast()) {
                stockPrices.add(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)));
                cursor.moveToNext();
            }
            cursor.close();
            return stockPrices.toArray(new Float[stockPrices.size()]);
        }

        public String[] getStockID() {
            Uri StockURI = QuoteProvider.Quotes.withSymbol(stockSymbol);
            Cursor cursor = getContext().getContentResolver().query(StockURI,
                    null,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            DatabaseUtils.dumpCursor(cursor);
            ArrayList<String> stockID = new ArrayList<String>();
            while (!cursor.isAfterLast()) {
                stockID.add(cursor.getString(INDEX_STOCK_ID));
                cursor.moveToNext();
            }
            cursor.close();
            return stockID.toArray(new String[stockID.size()]);
        }

    }
}

