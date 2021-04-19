package by.bsuir.poit.csan.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public enum BlacklistLoader {
    INSTANCE;

    private static final String BLACKLIST_PATH = "src/resources/blacklist.txt";

    public List<String> loadBlackList() {
        List<String> blacklist = new ArrayList<>();
        File file = new File(BLACKLIST_PATH);
        try(Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                blacklist.add(in.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Can't upload blacklist");
        }
        return blacklist;
    }
}
