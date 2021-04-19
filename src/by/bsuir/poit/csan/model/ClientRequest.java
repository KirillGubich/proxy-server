package by.bsuir.poit.csan.model;

import java.util.Arrays;
import java.util.Objects;

public class ClientRequest {
    private final byte[] message;
    private final String destinationAddress;
    private final int port;

    public ClientRequest(byte[] message, String destinationAddress, int port) {
        this.message = message;
        this.destinationAddress = destinationAddress;
        this.port = port;
    }

    public byte[] getMessage() {
        return message;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientRequest that = (ClientRequest) o;
        return port == that.port && Arrays.equals(message, that.message)
                && Objects.equals(destinationAddress, that.destinationAddress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(destinationAddress, port);
        result = 31 * result + Arrays.hashCode(message);
        return result;
    }

    @Override
    public String toString() {
        return "ClientRequest{" +
                "message=" + Arrays.toString(message) +
                ", destinationAddress='" + destinationAddress + '\'' +
                ", port=" + port +
                '}';
    }
}
