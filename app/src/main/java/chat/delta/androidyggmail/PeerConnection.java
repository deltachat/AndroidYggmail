package chat.delta.androidyggmail;

import java.util.Objects;

public class PeerConnection {
    public String source;
    public String prefix;
    public String localIP;

    public PeerConnection(String prefix, String localIP, String source) {
        this.prefix = prefix;
        this.localIP = localIP;
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerConnection that = (PeerConnection) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(localIP, that.localIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, prefix, localIP);
    }

    @Override
    public String toString() {
        return "LocalPeerConnection{" +
                "source='" + source + '\'' +
                ", prefix='" + prefix + '\'' +
                ", localIP='" + localIP + '\'' +
                '}';
    }

    public boolean isLinkLocal() {
        return localIP.startsWith("fe80:");
    }
}
