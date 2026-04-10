package net.portalmod.core.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DataUtil {
    public static byte[] readInputStream(InputStream is) throws IOException {
        byte[] data = new byte[1024];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int readCount;
        while((readCount = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, readCount);
        }

        return buffer.toByteArray();
    }

    public static String computeChecksum(byte[] data) throws IOException {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static File tryCreateFolder(File folder) throws IOException {
        if(!(folder.exists() || folder.mkdirs()))
            throw new IOException("Failed to create folder: " + folder.getName());
        return folder;
    }

    public static File tryCreateFolderAndGetFile(File folder, String filename) throws IOException {
        return new File(tryCreateFolder(folder), filename);
    }

    public static byte[] loadFile(File file) throws IOException {
        byte[] data;

        try(FileInputStream fis = new FileInputStream(file)) {
            data = readInputStream(fis);
        }

        return data;
    }

    public static String loadTextFile(File file) throws IOException {
        return new String(loadFile(file), StandardCharsets.UTF_8);
    }

    public static void writeFile(File file, byte[] data) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    public static void writeTextFile(File file, String data) throws IOException {
        writeFile(file, data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] makeRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        InputStream is = null;
        byte[] data;

        try {
            connection.connect();
            if(connection.getResponseCode() != 200) {
                throw new HttpRetryException(connection.getResponseMessage(), connection.getResponseCode());
            }

            is = connection.getInputStream();
            data = readInputStream(is);
        } finally {
            if(is != null) {
                is.close();
            }
        }

        return data;
    }

    public static String makeTextRequest(String url) throws IOException {
        return new String(makeRequest(url), StandardCharsets.UTF_8);
    }
}