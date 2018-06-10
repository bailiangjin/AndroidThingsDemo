package com.bailiangjin.androidthingsdemo.board;

/**
 * //TODO 添加类注释
 *
 * @author bailiangjin
 * @date 2018/6/10
 */
public enum BoardEnum {
    Pi3B_INSTANCE("I2C1","SPI0.0","UART0",new String[]{"PWM0", "PWM1"}) ;


    String i2c ;
    String spi = null;
    String uart = null;
    String[] pwms;

    BoardEnum(String i2c, String spi, String uart, String[] pwms) {
        this.i2c = i2c;
        this.spi = spi;
        this.uart = uart;
        this.pwms = pwms;
    }


    public String getI2c() {
        return i2c;
    }

    public String getSpi() {
        return spi;
    }

    public String getUart() {
        return uart;
    }

    public String[] getPwms() {
        return pwms;
    }
}
