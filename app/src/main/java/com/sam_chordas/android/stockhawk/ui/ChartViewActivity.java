package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.YController;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import java.util.Collections;

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
        private String stockSymbol;
        private static final String[] STOCK_COLUMNS = {
                QuoteColumns._ID,
                QuoteColumns.SYMBOL,
                QuoteColumns.BIDPRICE,
                QuoteColumns.CREATED,
                QuoteColumns.CHANGE,
                QuoteColumns.PERCENT_CHANGE
        };
        // these indices must match the projection
        static final int INDEX_STOCK_ID = 0;
        static final int INDEX_STOCK_SYMBOL = 1;
        static final int INDEX_STOCK_PRICE = 2;
        static final int INDEX_STOCK_CREATED = 3;
        static final int INDEX_STOCK_CHANGE = 4;
        static final int INDEX_STOCK_PERCENT_CHANGE = 5;

        int maxStockPrice;
        int minStockPrice;
        TextView stockNameTextView;
        TextView stockPriceTextView;
        TextView stockChangeTextView;
        TextView stockPercentChangeTextView;
        String[] stockCreated;
        Float[] stockPrices;

        public GraphFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.activity_line_graph, container, false);

            mChart = (LineChartView) rootView.findViewById(R.id.linechart);
            stockNameTextView = (TextView) rootView.findViewById(R.id.stock_symbol);
            stockPriceTextView = (TextView) rootView.findViewById(R.id.bid_price);
            stockChangeTextView = (TextView) rootView.findViewById(R.id.change);
            stockPercentChangeTextView = (TextView) rootView.findViewById(R.id.percent_change);

            Intent intent = getActivity().getIntent();
            if (intent != null) {

                stockSymbol = intent.getStringExtra("symbol");
                stockNameTextView.setText(String.format(getResources().getString(R.string.chartview_stock_symbol),stockSymbol.toUpperCase()));
                getStockData();
            }
            return rootView;
        }

        public void getStockData() {
            Uri StockURI = QuoteProvider.Quotes.withSymbol(stockSymbol);
            Cursor cursor = getContext().getContentResolver().query(StockURI,
                    STOCK_COLUMNS,
                    null,
                    null,
                    null);

            cursor.moveToFirst();
            ArrayList<Float> stockPrice = new ArrayList<>();
            ArrayList<String> stockCreatedArray = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                stockPrice.add(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)));
                stockCreatedArray.add(cursor.getString(INDEX_STOCK_CREATED));
                cursor.moveToNext();
            }
            cursor.moveToLast();
            stockChangeTextView.setText(String.format(getResources().getString(R.string.chartview_change), cursor.getString(INDEX_STOCK_CHANGE)));
            stockPercentChangeTextView.setText(String.format(getResources().getString(R.string.chartview_percent_change), cursor.getString(INDEX_STOCK_PERCENT_CHANGE)));
            cursor.close();
            stockPriceTextView.setText(String.format(getResources().getString(R.string.chartview_bid_price),stockPrice.get(stockPrice.size()-1)));

            maxStockPrice = Math.round(Collections.max(stockPrice));
            minStockPrice = Math.round(Collections.min(stockPrice));

            stockPrices =  stockPrice.toArray(new Float[stockPrice.size()]);
            stockCreated = stockCreatedArray.toArray(new String[stockCreatedArray.size()]);

            float[] floatArray = new float[stockPrices.length];
            int i = 0;
            for (Float f : stockPrices) {
                floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
            }

            LineSet dataset = new LineSet(stockCreated, floatArray);
            dataset.setColor(Color.parseColor("#f2f2f2"))
                    .setDotsColor(Color.parseColor("#f2f2f2"))
                    .setThickness(1);
            mChart.addData(dataset);
            mChart.setBorderSpacing(Tools.fromDpToPx(15))
                    .setXLabels(YController.LabelPosition.NONE)
                    .setLabelsColor(Color.parseColor("#6a84c3"))
                    .setAxisBorderValues(minStockPrice-1, maxStockPrice+1);
            mChart.show();
        }

    }
}