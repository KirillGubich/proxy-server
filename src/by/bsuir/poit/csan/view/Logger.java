package by.bsuir.poit.csan.view;

import by.bsuir.poit.csan.util.DateTimeKeeper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static final String LOG_FILE_PATH = "src/resources/proxy.log";

    public void log(String message) {
        final File file = new File(LOG_FILE_PATH);
        try (FileWriter fileWriter = new FileWriter(file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            String currentDateTime = DateTimeKeeper.getCurrentDateTime();
            String logMessage = currentDateTime + " " + message;
            bufferedWriter.write(logMessage + "\n");
            System.out.println(logMessage);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
