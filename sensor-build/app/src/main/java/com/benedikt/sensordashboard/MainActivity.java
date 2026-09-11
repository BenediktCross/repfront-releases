package com.benedikt.sensordashboard;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private final Map<Sensor, TextView> valueViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(28));
        root.setBackgroundColor(Color.rgb(245, 245, 245));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Sensor Dashboard");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        root.addView(title);

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        TextView subtitle = new TextView(this);
        subtitle.setText(sensors.size() + " Sensoren erkannt\nLive-Werte direkt vom Gerät");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        root.addView(subtitle);

        for (Sensor sensor : sensors) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dp(10));
            root.addView(card, cardParams);

            TextView name = new TextView(this);
            name.setText(sensor.getName());
            name.setTextSize(17);
            name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            name.setTextColor(Color.BLACK);
            card.addView(name);

            TextView meta = new TextView(this);
            meta.setText("Typ " + sensor.getType() + "  •  " + sensor.getVendor());
            meta.setTextSize(12);
            meta.setTextColor(Color.GRAY);
            meta.setPadding(0, dp(2), 0, dp(8));
            card.addView(meta);

            TextView values = new TextView(this);
            values.setText("Warte auf Messwerte …");
            values.setTextSize(15);
            values.setTypeface(Typeface.MONOSPACE);
            values.setTextColor(Color.rgb(35, 35, 35));
            values.setTextIsSelectable(true);
            card.addView(values);

            valueViews.put(sensor, values);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        setContentView(scroll);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView target = valueViews.get(event.sensor);
        if (target == null) return;

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < event.values.length; i++) {
            if (i > 0) text.append("   ");
            text.append(axisLabel(i, event.values.length))
                    .append(String.format(Locale.US, "%.3f", event.values[i]));
        }
        String unit = unitFor(event.sensor.getType());
        if (!unit.isEmpty()) text.append("  ").append(unit);
        target.setText(text.toString());
    }

    private String axisLabel(int index, int count) {
        if (count >= 3 && index < 3) {
            return new String[]{"X ", "Y ", "Z "}[index];
        }
        return count == 1 ? "" : "V" + (index + 1) + " ";
    }

    private String unitFor(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "m/s²";
            case Sensor.TYPE_GYROSCOPE:
                return "rad/s";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "µT";
            case Sensor.TYPE_LIGHT:
                return "lx";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case Sensor.TYPE_PROXIMITY:
                return "cm";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "°C";
            default:
                return "";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
