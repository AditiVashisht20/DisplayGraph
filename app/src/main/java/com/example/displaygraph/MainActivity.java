package com.example.displaygraph;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;

public class MainActivity extends AppCompatActivity {
        List<DataPoint> dataPoints = null;
        private GraphView graph = null;
        private final int upper_bound = 100;
        private final int timer = 10000;
        private LineGraphSeries<DataPoint> series;
        private int x=0;
        String TAG = "GraphApp";
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            graph = findViewById(R.id.graph);
            dataPoints = new ArrayList<>();


            graph.setTitle("Data Points");
            graph.getViewport().setScrollable(true);

            double max_y = 100;
            graph.getViewport().setMinX(0);
            graph.getViewport().setXAxisBoundsManual(true);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(max_y);
            graph.getGridLabelRenderer().setHumanRounding(false);
            Random random = new Random();
            for(int i = 0;i<10;i++){
                dataPoints.add(new DataPoint(x++,random.nextInt(upper_bound)));
            }
            series = new LineGraphSeries<>(getData());
            graph.addSeries(series);
            startTimer(random);
        }
        protected void startTimer(Random random) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    dataPoints.add(new DataPoint(x++,random.nextInt(upper_bound)));
                    mHandler.obtainMessage(1).sendToTarget();
                }
            }, 0, timer);
        }

        public Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this, "Data Added to the graph", Toast.LENGTH_SHORT).show();
                readExcelFile();
                addToGraph();
            }
        };
        public void readExcelFile() {
            try {
                InputStream myInput;
                AssetManager assetManager = getAssets();
                myInput = assetManager.open("mockdata.xls");
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);
                Iterator<Row> rowIter = mySheet.rowIterator();
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int colno =0;
                    String x="";
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (colno==0){
                            x = myCell.toString();
                        }
                        colno++;
                    }
                    this.dataPoints.add(new DataPoint(this.x++,Double.parseDouble(x.trim())));
                }
                addToGraph();
            } catch (Exception e) {
                Log.e(TAG, "error "+ e.toString());
            }
        }


        static class SortDataPoint implements Comparator<DataPoint>{

            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                int xComp = Integer.compare((int)o1.getX(), (int)o2.getX());
                if(xComp == 0)
                    return Integer.compare((int)o1.getY(),(int) o2.getY());
                else
                    return xComp;
            }
        }

        private DataPoint[] getData(){
            Collections.sort(dataPoints, new SortDataPoint());
            DataPoint[] dp = new DataPoint[dataPoints.size()];
            dp = dataPoints.toArray(dp);
            return dp;
        }

        private void addToGraph(){
            graph.getViewport().setMaxX(dataPoints.size()+10);
            series.resetData(getData());
        }

}