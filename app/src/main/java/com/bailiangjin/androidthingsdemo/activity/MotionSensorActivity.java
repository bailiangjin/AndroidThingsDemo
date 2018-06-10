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

public class MotionSensorActivity extends Activity {
    private static final String TAG = MotionSensorActivity.class.getSimpleName();

    Gpio motionSensorGio = null;

    public static boolean[][] DIGITAL_DISPLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager service = PeripheralManager.getInstance();

        try {

            //define a button for counter
            motionSensorGio = service.openGpio(Pi3BCMCodeByPin.PIN_7);
            motionSensorGio.setDirection(Gpio.DIRECTION_IN);//将引脚初始化为输入
            motionSensorGio.setActiveType(Gpio.ACTIVE_HIGH);//设置收到高电压是有效的结果
            //注册状态更改监听类型 EDGE_NONE（无更改，默认）EDGE_RISING（从低到高）EDGE_FALLING（从高到低）
            motionSensorGio.setEdgeTriggerType(Gpio.EDGE_BOTH);

            motionSensorGio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    try {
                        if (gpio.getValue()) {
                            Log.e("有人来了", gpio.getValue() + ":1111111111111");
                        } else {
                            Log.e("没有人", gpio.getValue() + ":222222222222");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;

                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (motionSensorGio !=null) {
            try {
                motionSensorGio.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

}
