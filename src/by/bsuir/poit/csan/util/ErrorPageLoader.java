package by.bsuir.poit.csan.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum ErrorPageLoader {
    INSTANCE;

    private static final int BUFFER_LENGTH = 2048;
    private static final String PAGE_HEAD = "HTTP/1.1 403 Forbidden\r\nContent-Type: text/html\r\nContent-Length: "
            + BUFFER_LENGTH + "\r\n\r\n";
    private static final String ERROR_PAGE_PATH = "src/resources/error.html";

    public byte[] load() throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];
        try (InputStream inputStream = new FileInputStream(ERROR_PAGE_PATH);
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {
            bufferedStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] pageHead = PAGE_HEAD.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(pageHead);
        outputStream.write(buffer);
        return outputStream.toByteArray();
    }
}
