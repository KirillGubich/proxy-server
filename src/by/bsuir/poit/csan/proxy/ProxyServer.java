package by.bsuir.poit.csan.proxy;

import by.bsuir.poit.csan.model.ClientRequest;
import by.bsuir.poit.csan.model.ServerResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyServer {

    private final int proxyPort;
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final String HTTP_URL_PART = "http://";
    private static final String SPACE_REGEX = " ";
    private static final int BUFFER_SIZE = 100000;
    private static final String HTTP_REGEX = "http://[a-z0-9а-яё:.]*";

    public ProxyServer(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start() {
        try {
            ServerSocket serverListener = new ServerSocket(proxyPort);
            System.out.println("Start listening on port: " + proxyPort);
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
            Socket server;
            if (clientRequest.getPort() < 0) {
                server = new Socket(clientRequest.getDestinationAddress(), HTTP_DEFAULT_PORT);
            } else {
                server = new Socket(clientRequest.getDestinationAddress(), clientRequest.getPort());
            }
            final InputStream fromServerStream = server.getInputStream();
            final OutputStream toServerStream = server.getOutputStream();
            sendRequestToServer(toServerStream, clientRequest.getMessage());
            ServerResponse serverResponse = extractServerResponse(fromServerStream);
            sendResponseForClient(toClientStream, serverResponse.getMessage());
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String format = now.format(formatter);
            System.out.println(format + " " + clientRequest.getDestinationAddress()
                    + " " + serverResponse.getStatusCode());
            joinStreams(fromServerStream, toClientStream);
        } catch (Exception ignored) {
        }
    }

    private byte[] replaceAbsolutePath(String request) {
        List<String> list = parseAbsolutePath(request);
        if (!list.isEmpty()) {
            String path = list.get(0);
            request = request.replace(path, "");
            return request.getBytes();
        }
        return null;
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

    private ServerResponse extractServerResponse(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[100000];
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        do {
            int len = inputStream.read(buffer);
            if (len > 0) {
                byteStream.write(buffer, 0, len);
                byteStream.flush();
                bytesRead += len;
            }
        } while ((inputStream.available() > 0) && bytesRead < 32);
        String response = new String(buffer);
        if (!response.equals("")) {
            int statusCode = extractResponseStatusCode(response);
            return new ServerResponse(buffer, statusCode);
        } else {
            return new ServerResponse(buffer, 0);
        }
    }

    private ClientRequest receiveClientRequest(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        do {
            int len = inputStream.read(buffer);
            if (len > 0) {
                byteStream.write(buffer, 0, len);
                byteStream.flush();
                bytesRead += len;
            }
        } while (inputStream.available() > 0 && bytesRead < 32);
        String destinationAddress = extractDestinationAddress(byteStream.toString());
        int port = extractPortNumber(byteStream.toString());
        byte[] request = replaceAbsolutePath(byteStream.toString());
        return new ClientRequest(request, destinationAddress, port);
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

    private List<String> parseAbsolutePath(String textToParse) {
        List<String> components = new ArrayList<>();
        Pattern pattern = Pattern.compile(ProxyServer.HTTP_REGEX);
        Matcher matcher = pattern.matcher(textToParse);
        while (matcher.find()) {
            components.add(matcher.group());
        }
        return components;
    }
}
