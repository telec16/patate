package fr.saya.patate;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.hardware.Camera;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static final int LED_NOTIFICATION_ID = 1;
    int mem;

    SeekBar haut;
    SeekBar bas;
    RadioButton add;
    RadioButton soustract;
    RadioButton special;
    RadioGroup radioGroup;
    TextView res;
    TextView surprise;
    EditText speTxt;
    CheckBox freeze;

    Vibrator vib;
    Camera camera;
    Flash flash = new Flash();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        haut = (SeekBar) findViewById(R.id.barreHaut);
        bas = (SeekBar) findViewById(R.id.barreBas);
        add = (RadioButton) findViewById(R.id.RadButAdd);
        soustract = (RadioButton) findViewById(R.id.RadButSoustract);
        special = (RadioButton) findViewById(R.id.special);
        radioGroup = (RadioGroup) findViewById(R.id.RG);
        res = (TextView) findViewById(R.id.TxtNombre);
        surprise = (TextView) findViewById(R.id.TxtSurprise);
        speTxt = (EditText) findViewById(R.id.speTxt);
        freeze = (CheckBox) findViewById(R.id.Freeze);

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        (new Thread(flash)).start();
        camera = Camera.open();

        haut.setMax(100);
        bas.setMax(100);

        actualise(-1);
        setListener();

        RedFlashLight();
    }

    private void flashOn() {
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        camera.startPreview();
    }

    private void flashOff() {
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
    }

    private void RedFlashLight() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 1;
        notif.ledOffMS = 0;
        nm.notify(LED_NOTIFICATION_ID, notif);
    }


    void actualise(int barre) {
        int text;
        if (!(freeze.isChecked()))
        {
            text = (haut.getProgress()) + (add.isChecked() ? 1 : -1) * (bas.getProgress());
        }
        else
        {
            text = mem;
            if (barre != -1) {
                if (add.isChecked())
                {
                    ((barre == 0) ? bas : haut).setProgress(mem - (((barre == 0) ? haut : bas).getProgress()));
                }
                else if (soustract.isChecked())
                {
                    ((barre == 0) ? bas : haut).setProgress(((barre == 0) ? (haut.getProgress()-mem) : (bas.getProgress() + mem)));
                }
            }
        }


        if (text == 42)
            surprise.setText("Yolo");
        else if (text == 16)
            surprise.setText("Telec :)");
        else
            surprise.setText("");

        vib.cancel();
        if (special.isChecked()) {
            speTxt.setVisibility(View.VISIBLE);
            String tab[] = speTxt.getText().toString().split(",");
            long[] data = new long[tab.length];
            try {
                for (int i = 0; i < tab.length; i++) {
                    data[i] = Long.parseLong(tab[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (haut.getProgress() > 95)
                vib.vibrate(data, 0);
            else if (haut.getProgress() > 90)
                vib.vibrate(new long[]{0, 10000}, 0);

            if (bas.getProgress() <= 50) {
                flash.setFrequency(bas.getProgress());
                flash.toggle(true);
            } else
                flash.toggle(false);
        } else {
            flash.toggle(false);
            speTxt.setVisibility(View.INVISIBLE);
        }

        res.setText(String.valueOf(text));
    }

    void setListener() {
        haut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                actualise(0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                actualise(0);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actualise(0);
            }
        });
        bas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                actualise(1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                actualise(1);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actualise(1);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                actualise(-1);
            }
        });

        speTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                actualise(-1);
                return true;
            }
        });

        freeze.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mem = Integer.parseInt(res.getText().toString());
                actualise(-1);
            }
        });

    }


    private class Flash implements Runnable {
        private int frequency = 1;
        private boolean running = false;
        private boolean state = false;

        public Flash() {

        }

        @Override
        public void run() {

            while (true) {
                if (running) {
                    state = !state;
                    if (state)
                        flashOn();
                    else
                        flashOff();
                }
                try {
                    Thread.sleep(500 / frequency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency <= 0 ? 1 : frequency;
        }

        public void toggle(boolean running) {
            this.running = running;
            if (!running)
                flashOff();
        }
    }
}

