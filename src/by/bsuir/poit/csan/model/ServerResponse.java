package by.bsuir.poit.csan.model;

import java.util.Arrays;
import java.util.Objects;

public class ServerResponse {
    private final byte[] message;
    private final int statusCode;

    public ServerResponse(byte[] message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerResponse that = (ServerResponse) o;
        return statusCode == that.statusCode && Arrays.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(statusCode);
        result = 31 * result + Arrays.hashCode(message);
        return result;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "message=" + Arrays.toString(message) +
                ", statusCode=" + statusCode +
                '}';
    }
}
