package by.bsuir.poit.csan.app;

import by.bsuir.poit.csan.proxy.ProxyServer;

public class Main {
    public static final int PORT = 50100;

    public static void main(String[] args) {
        ProxyServer proxyServer = new ProxyServer(PORT);
        proxyServer.start();
    }
}
