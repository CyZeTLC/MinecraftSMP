package de.cyzetlc.smp.config;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonConfig {
    @Getter
    private JsonObject object;
    private Gson gson;
    @Getter
    private File file;

    public JsonConfig(String file) {
        if (file == null) {
            throw new NullPointerException("File ist null!");
        }

        this.file = new File(file);
        this.gson = (new GsonBuilder()).setPrettyPrinting().create();
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
                this.object = new JsonObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.object = JsonParser.parseString(new String(Files.readAllBytes(Paths.get(this.file.toURI())), String.valueOf(StandardCharsets.UTF_8))).getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonConfig(JsonObject jsonObject) {
        this.gson = (new GsonBuilder()).setPrettyPrinting().create();
        this.object = jsonObject;
    }

    public <T> T load(Class<T> clazz) {
        try {
            return this.gson.fromJson(new FileReader(this.file), clazz);
        } catch (FileNotFoundException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public void save(){
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            try {
                writer.write(this.object.toString());
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
