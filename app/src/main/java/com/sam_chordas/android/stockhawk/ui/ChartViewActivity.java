package com.sam_chordas.android.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
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
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.easing.CubicEase;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteDatabase;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Bharat on 6/1/2016.
 */
//Class and fragment to make a line chart from the WilliamChart library. Only bid values that are different are charted for a better representation.
// This will use the created  field in the Content Provider for the x axis and the stock price at that time for the y axis.
//A tooltip was implemented to view exact x and y axis.
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
                "DISTINCT " + QuoteColumns.BIDPRICE,
                QuoteColumns.CREATED,
                QuoteColumns.CHANGE,
                QuoteColumns.PERCENT_CHANGE
        };
        // these indices must match the projection
        static final int INDEX_STOCK_PRICE = 0;
        static final int INDEX_STOCK_CREATED = 1;
        static final int INDEX_STOCK_CHANGE = 2;
        static final int INDEX_STOCK_PERCENT_CHANGE = 3;

        int maxStockPrice;
        int minStockPrice;
        TextView stockNameTextView;
        TextView stockPriceTextView;
        TextView stockChangeTextView;
        TextView stockPercentChangeTextView;
        String[] stockCreated;
        Float[] stockPrices;
        private Tooltip mTip;
        TextView tooltipDate;

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
                //retrieves stock symbol from intent and formats the respective textview.
                stockSymbol = intent.getStringExtra("symbol");
                stockNameTextView.setText(String.format(getResources().getString(R.string.chartview_stock_symbol),stockSymbol.toUpperCase()));
                getStockData();
            }
            return rootView;
        }

        //function to populate the various textviews in the chartview activity as well as make the chart for stock price over time.
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

            //Logic to make only unique points added to chart. This will check if the bid price from the previous point is the same. If it is, it will ignore the point.
            while (!cursor.isAfterLast()) {

                try {
                    if(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)) != (stockPrice.get(stockPrice.size()-1))) {
                        stockPrice.add(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)));
                        stockCreatedArray.add(cursor.getString(INDEX_STOCK_CREATED));
                    }
                    else if(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)) == (stockPrice.get(stockPrice.size()-1))) {

                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    stockPrice.add(Float.parseFloat(cursor.getString(INDEX_STOCK_PRICE)));
                    stockCreatedArray.add(cursor.getString(INDEX_STOCK_CREATED));
                }
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

            final float[] floatArray = new float[stockPrices.length];
            int i = 0;
            for (Float f : stockPrices) {
                floatArray[i++] = (f != null ? f : Float.NaN);
            }

            mTip = new Tooltip(getContext(), R.layout.tooltip, R.id.value);
            mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
            mTip.setDimensions((int) Tools.fromDpToPx(100), (int) Tools.fromDpToPx(50));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

                mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

                mTip.setPivotX(Tools.fromDpToPx(65) / 2);
                mTip.setPivotY(Tools.fromDpToPx(25));
            }
            mChart.setTooltips(mTip);

            LineSet dataset = new LineSet(stockCreated, floatArray);
            dataset.setColor(getResources().getColor(R.color.md_divider_white))
                    .setDotsColor(getResources().getColor(R.color.material_blue_700))
                    .setSmooth(true)
                    .setThickness(4);
            mChart.addData(dataset);
            mChart.setBorderSpacing(Tools.fromDpToPx(15))
                    .setXLabels(YController.LabelPosition.NONE)
                    .setLabelsColor(getResources().getColor(R.color.material_gray_200))
                    .setAxisBorderValues(minStockPrice-1, maxStockPrice+1);


            String date = convertToDateProper(stockCreated[0]);
            tooltipDate = (TextView) mTip.findViewById(R.id.date);
            tooltipDate.setText(date);

            Runnable chartAction = new Runnable() {
                @Override
                public void run() {
                    mTip.prepare(mChart.getEntriesArea(0).get(0), floatArray[0]);
                    mChart.showTooltip(mTip, true);
                }
            };

            //onclick to inject the date into the tooltip
            mChart.setOnEntryClickListener(new OnEntryClickListener() {
                @Override
                public void onClick(int setIndex, int entryIndex, Rect rect) {

                    tooltipDate.setText(convertToDateProper(stockCreated[entryIndex]));
                }
            });

            Animation anim = new Animation()
                    .setEasing(new CubicEase()).setEndAction(chartAction);

            mChart.show(anim);
        }

        //converts the date from the yahoo api into a more usable one and returns it.
        private String convertToDateProper(String dateRaw) {
            String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(dateRaw);
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd " + "hh:mma", Locale.US);
                return formatter.format(date);
            }
            catch (Exception e) {
            }
            return " ";
        }
    }
}