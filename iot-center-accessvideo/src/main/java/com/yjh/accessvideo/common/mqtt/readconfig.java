package com.yjh.platform.common.mqtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class readconfig {

    public void readfile() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = this.getClass().getResourceAsStream("/application-dev.properties");
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        properties.load(bf);
        String node_id = properties.getProperty("mqtt.node_id");
        System.out.println(node_id);
        String level = properties.getProperty("mqtt.volt_level");
        System.out.println(level);
    }

    public static void main(String[] args) throws IOException {

        new readconfig().readfile();

    }
}
