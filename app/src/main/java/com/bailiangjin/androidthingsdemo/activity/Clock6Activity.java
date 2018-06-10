/*
 * @author Ray, ray@sysolve.com
 * Copyright 2018, Sysolve IoT Open Source
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bailiangjin.androidthingsdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.bailiangjin.androidthingsdemo.board.Pi3BCMCodeByPin;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Clock6Activity extends Activity {
    private static final String TAG = Clock6Activity.class.getSimpleName();

    private Gpio[] digital = new Gpio[8];
    private Gpio[] showDigital = new Gpio[11];

    public int[] digitalForDisplay = new int[11];
    public static boolean[][] DIGITAL_DISPLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager service = PeripheralManager.getInstance();

        try {

            initSegments(service);

            initNumber(service);

            initDisplayNumber();


            //自动计数
            startTimeTick();

            //将自动计数的数值显示在4位数码管上
            displayDigitals(showDigital);


        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void initDisplayNumber() {
        //we will define the digitals which segments ON
        //eg. 8: all segments ON, 1: [3],[6] ON
        DIGITAL_DISPLAY = new boolean[10][];
        DIGITAL_DISPLAY[0] = new boolean[]{true, true, true, true, true, true, false, false};
        DIGITAL_DISPLAY[1] = new boolean[]{false, true, true, false, false, false, false, false};
        DIGITAL_DISPLAY[2] = new boolean[]{true, true, false, true, true, false, true, false};
        DIGITAL_DISPLAY[3] = new boolean[]{true, true, true, true, false, false, true, false};
        DIGITAL_DISPLAY[4] = new boolean[]{false, true, true, false, false, true, true, false};
        DIGITAL_DISPLAY[5] = new boolean[]{true, false, true, true, false, true, true, false};
        DIGITAL_DISPLAY[6] = new boolean[]{true, false, true, true, true, true, true, false};
        DIGITAL_DISPLAY[7] = new boolean[]{true, true, true, false, false, false, false, false};
        DIGITAL_DISPLAY[8] = new boolean[]{true, true, true, true, true, true, true, false};
        DIGITAL_DISPLAY[9] = new boolean[]{true, true, true, true, false, true, true, false};
    }


    /**
     * 初始化段
     *
     * @param service
     * @throws IOException
     */
    private void initSegments(PeripheralManager service) throws IOException {

        /*
            定义数码管的各个显示段
            Define the digital segments GPIO like:
              --2--
            1|     |3
              --0--
            4|     |6
              --5--  .7
             */

        digital[0] = service.openGpio(Pi3BCMCodeByPin.PIN_31);
        digital[1] = service.openGpio(Pi3BCMCodeByPin.PIN_33);
        digital[2] = service.openGpio(Pi3BCMCodeByPin.PIN_35);
        digital[3] = service.openGpio(Pi3BCMCodeByPin.PIN_37);

        digital[4] = service.openGpio(Pi3BCMCodeByPin.PIN_32);
        digital[5] = service.openGpio(Pi3BCMCodeByPin.PIN_36);
        digital[6] = service.openGpio(Pi3BCMCodeByPin.PIN_38);
        digital[7] = service.openGpio(Pi3BCMCodeByPin.PIN_40);


        //set the digital segments DIRECTION_OUT and INITIALLY_HIGH
        //设置各个段默认为高电平，即不显示
        for (Gpio g : digital) {
            g.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        }

    }

    /***
     * 初始化每一个数字
     * @param service
     * @throws IOException
     */
    private void initNumber(PeripheralManager service) throws IOException {
        //定义数码管的4个数字是否显示
        showDigital[0] = service.openGpio(Pi3BCMCodeByPin.PIN_3);
        showDigital[1] = service.openGpio(Pi3BCMCodeByPin.PIN_5);
        showDigital[2] = service.openGpio(Pi3BCMCodeByPin.PIN_7);
        showDigital[3] = service.openGpio(Pi3BCMCodeByPin.PIN_11);
        showDigital[4] = service.openGpio(Pi3BCMCodeByPin.PIN_13);
        showDigital[5] = service.openGpio(Pi3BCMCodeByPin.PIN_15);
        showDigital[6] = service.openGpio(Pi3BCMCodeByPin.PIN_8);
        showDigital[7] = service.openGpio(Pi3BCMCodeByPin.PIN_10);
        showDigital[8] = service.openGpio(Pi3BCMCodeByPin.PIN_12);
        showDigital[9] = service.openGpio(Pi3BCMCodeByPin.PIN_16);
        showDigital[10] = service.openGpio(Pi3BCMCodeByPin.PIN_18);

        //设置各个数字默认为低电平，即不显示
        int i = 0;
        for (Gpio g : showDigital) {
            int direction = i >= 7 ? Gpio.DIRECTION_OUT_INITIALLY_HIGH : Gpio.DIRECTION_OUT_INITIALLY_LOW;
            g.setDirection(direction);
            g.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    return false;
                }
            });
            i++;
        }

    }


    public void startTimeTick() {

        //org.apache.commons.lang3.concurrent.BasicThreadFactory
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateTimeToUI();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

    }


    private void updateTimeToUI() {
        //do something
        digitalForDisplay = getCurrentTimeArray();
        displayDigitals(showDigital);
    }


    public int[] getCurrentTimeArray() {

        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);
        int week = rightNow.get(Calendar.DAY_OF_WEEK);
        int month = rightNow.get(Calendar.MONTH);
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        //周日 显示8 周日 是第一天 其他减一
        week = 1 == week ? 8 : week - 1;

        month = month + 1;

        int hourHigh = hour / 10;
        int hourLow = hour % 10;

        int minuteHigh = minute / 10;
        int minuetLow = minute % 10;

        int secondHigh = second / 10;
        int secondLow = second % 10;

        int monthHigh = month / 10;
        int monthLow = month % 10;

        int dayHigh = day / 10;
        int dayLow = day % 10;


        return new int[]{hourHigh, hourLow, minuteHigh, minuetLow, secondHigh, secondLow, week, monthHigh, monthLow, dayHigh, dayLow};
    }


    public void displayDigitals(final Gpio[] showDigital) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                while (true) {      //持续刷新显示
                    //依次在每位数码管上显示
                    for (int i = 0; i < showDigital.length; ++i) {

                        boolean isReverse = i >= 7 && i <= 10;


                        //先设置显示的数码管段
                        displayDigital(digitalForDisplay[i], i < 4 || i == 8, isReverse);

                        try {
                            //设置对应的数码管位开启显示
                            boolean value = isReverse ? true : false;
                            showDigital[i].setValue(value);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            //设置对应的数码管位关闭显示
                            boolean value = isReverse ? false : true;
                            showDigital[i].setValue(value);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }


    /**
     * 显示数字
     *
     * @param digital
     * @param withPoint
     */
    public void displayDigital(int digital, boolean withPoint, boolean isReverse) {
        try {
            if (digital < 0) {
                //let All Segments OFF when d<0
                for (int i = 0; i < 8; ++i) {
                    boolean value = isReverse ? true : false;
                    this.digital[i].setValue(value);
                }
            } else {
                digital = digital % 10;
                //get the ON/OFF map for the digital
                boolean[] segments = DIGITAL_DISPLAY[digital];

                //set digital segment ON/OFF
                for (int i = 0; i < 8; ++i) {
                    boolean value = segments[i];
                    value = isReverse ? !value : value;
                    //数码管显示段是低电平触发显示，所以要将segments[i]的值取反
                    this.digital[i].setValue(value);
                }

                boolean pointValue = isReverse ? !withPoint : withPoint;
                this.digital[7].setValue(pointValue);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (digital != null) {
            for (Gpio g : digital) {
                try {
                    if (g != null) {
                        g.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        if (showDigital != null) {
            for (Gpio g : showDigital) {
                try {
                    if (g != null) {
                        g.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

}
