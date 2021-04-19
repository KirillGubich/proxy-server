package by.bsuir.poit.csan.proxy;

import by.bsuir.poit.csan.model.ClientRequest;
import by.bsuir.poit.csan.model.ServerResponse;
import by.bsuir.poit.csan.parser.TextParser;
import by.bsuir.poit.csan.util.BlacklistLoader;
import by.bsuir.poit.csan.util.DateTimeKeeper;
import by.bsuir.poit.csan.util.ErrorPageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ProxyServer {

    private final int proxyPort;
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final String HTTP_URL_PART = "http://";
    private static final String SPACE_REGEX = " ";
    private static final int BUFFER_SIZE = 100000;
    private static final String HTTP_REGEX = "http://[a-z0-9а-яё:.]*";
    private static final int DATA_MIN_LENGTH = 32;

    public ProxyServer(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start() {
        try {
            ServerSocket serverListener = new ServerSocket(proxyPort);
            System.out.println("Start proxying on port: " + proxyPort);
            while (true) {
                Socket client = serverListener.accept();
                Thread thread = new Thread(() -> acceptRequest(client));
                thread.start();
            }
        } catch (Exception e) {
            System.out.println("Server is down");
        }
    }

    private void acceptRequest(Socket client) {
        try (InputStream fromClientStream = client.getInputStream();
             OutputStream toClientStream = client.getOutputStream()) {
            ClientRequest clientRequest = receiveClientRequest(fromClientStream);
            String destAddress = clientRequest.getDestinationAddress();
            if (checkBlacklist(clientRequest.getDestinationAddress())) {
                denyAccess(toClientStream, destAddress);
                return;
            }
            Socket server;
            if (clientRequest.getPort() < 0) {
                server = new Socket(destAddress, HTTP_DEFAULT_PORT);
            } else {
                server = new Socket(destAddress, clientRequest.getPort());
            }
            processClientRequest(toClientStream, clientRequest, server);
        } catch (Exception ignored) {
        }
    }

    private void processClientRequest(OutputStream toClientStream, ClientRequest clientRequest, Socket server)
            throws IOException {
        final InputStream fromServerStream = server.getInputStream();
        final OutputStream toServerStream = server.getOutputStream();
        sendRequestToServer(toServerStream, clientRequest.getMessage());
        ServerResponse serverResponse = extractServerResponse(fromServerStream);
        sendResponseForClient(toClientStream, serverResponse.getMessage());
        String currentDateTime = DateTimeKeeper.getCurrentDateTime();
        System.out.println(currentDateTime + " " + clientRequest.getDestinationAddress()
                + " " + serverResponse.getStatusCode());
        joinStreams(fromServerStream, toClientStream);
    }

    private void denyAccess(OutputStream toClientStream, String destAddress) throws IOException {
        sendErrorPage(toClientStream);
        String currentDateTime = DateTimeKeeper.getCurrentDateTime();
        System.out.println(currentDateTime + " " + destAddress + " Access denied");
    }

    private void sendErrorPage(OutputStream toClientStream) throws IOException {
        if (toClientStream == null) {
            return;
        }
        ErrorPageLoader errorPageLoader = ErrorPageLoader.INSTANCE;
        byte[] errorPage = errorPageLoader.load();
        toClientStream.write(errorPage);
    }

    private String extractDestinationAddress(String request) {
        if (request == null || request.equals("")) {
            throw new IllegalArgumentException("request is empty");
        }
        String[] strings = request
                .trim()
                .split(SPACE_REGEX);
        String destAddress = strings[1];
        destAddress = destAddress.replace(HTTP_URL_PART, "");
        String[] split = destAddress
                .split("/");
        destAddress = split[0];
        String[] secondSplit = destAddress.split(":");
        destAddress = secondSplit[0];
        return destAddress;
    }

    private int extractPortNumber(String request) {
        if (request == null) {
            return -1;
        }
        String[] strings = request.split(SPACE_REGEX);
        String destinationAddress = strings[1].replace(HTTP_URL_PART, "");
        String[] params = destinationAddress.split("/");
        String url = params[0];
        int port = -1;
        if (url.contains(":")) {
            String[] portSplit = url.split(":");
            port = Integer.parseInt(portSplit[1]);
        }
        return port;
    }

    private int extractResponseStatusCode(String response) {
        String[] strings = response.split(SPACE_REGEX);
        return Integer.parseInt(strings[1]);
    }

    private void sendRequestToServer(OutputStream outputStream, byte[] request) throws IOException {
        outputStream.write(request, 0, request.length);
        outputStream.flush();
    }

    private void sendResponseForClient(OutputStream outputStream, byte[] response) throws IOException {
        outputStream.write(response, 0, response.length);
        outputStream.flush();
    }

    private ClientRequest receiveClientRequest(InputStream inputStream) throws IOException {
        String data = readClientRequest(inputStream);
        String destinationAddress = extractDestinationAddress(data);
        int port = extractPortNumber(data);
        byte[] request = replaceAbsolutePath(data);
        return new ClientRequest(request, destinationAddress, port);
    }

    private String readClientRequest(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int len;
        do {
            len = inputStream.read(buffer);
            if (len > 0) {
                byteStream.write(buffer, 0, len);
                byteStream.flush();
            }
        } while (inputStream.available() > 0 && len < DATA_MIN_LENGTH);
        return byteStream.toString();
    }

    private ServerResponse extractServerResponse(InputStream inputStream) throws IOException {
        byte[] buffer = readServerResponse(inputStream);
        String response = new String(buffer);
        if (!response.equals("")) {
            int statusCode = extractResponseStatusCode(response);
            return new ServerResponse(buffer, statusCode);
        } else {
            return new ServerResponse(buffer, 0);
        }
    }

    private byte[] readServerResponse(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        do {
            len = inputStream.read(buffer);
        } while ((inputStream.available() > 0) && len < DATA_MIN_LENGTH);
        return buffer;
    }

    private byte[] replaceAbsolutePath(String request) {
        TextParser textParser = TextParser.INSTANCE;
        List<String> list = textParser.parseAbsolutePath(request, HTTP_REGEX);
        if (!list.isEmpty()) {
            String path = list.get(0);
            request = request.replace(path, "");
            return request.getBytes();
        }
        return null;
    }

    public void joinStreams(InputStream from, OutputStream to) throws IOException {
        if (!(from.available() > 0)) {
            return;
        }
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
        }
    }

    private boolean checkBlacklist(String url) {
        if (url == null) {
            return false;
        }
        BlacklistLoader blacklistLoader = BlacklistLoader.INSTANCE;
        List<String> blackList = blacklistLoader.loadBlackList();
        for (String site : blackList) {
            if (url.contains(site)) {
                return true;
            }
        }
        return false;
    }
}
