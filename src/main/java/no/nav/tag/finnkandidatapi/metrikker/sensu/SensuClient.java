package no.nav.tag.finnkandidatapi.metrikker.sensu;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SensuClient {

    private String environmentName;
    private final String sensuHost;
    private int sensuPort;

    public SensuClient(
            String environmentName,
            String sensuHostname,
            int sensuPort
    ) {
        this.environmentName = environmentName;
        this.sensuHost = sensuHostname;
        this.sensuPort = sensuPort;
    }

    public void sendEvent(String event) {
        sendEvent(event, Collections.emptyMap(), Map.of("value", 1));
    }

    public void sendEvent(String event, Map fields) {
        sendEvent(event, Collections.emptyMap(), fields);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void sendEvent(String event, Map tags, Map fields) {
        try {
            final String dataPoint = toLineProtocol(event, addDefaultTags(tags), fields);
            String sensuEvent = createSensuEvent(event, dataPoint);
            writeToSocket(sensuEvent);
            log.debug("Sent event with output {} to InfluxDB via sensu-client", dataPoint);
        } catch (RuntimeException e) {
            log.error("Unable to send event to InfluxDB via sensu-client", e);
        }
    }

    private Map<String, Object> addDefaultTags(Map<String, Object> tags) {
        Map<String, Object> map = new HashMap<>(tags);
        map.put("application", "finn-kandidat-api");
        map.put("environment", environmentName);
        map.put("hostname", getHostname());
        return map;
    }

    protected static String createSensuEvent(String eventName, String output) {
        return "{\"name\":\"" + eventName + "\",\"type\":\"metric\",\"handlers\":[\"events_nano\"],\"output\":\"" + output + "\"}";
    }

    protected static String toLineProtocol(String measurement, Map<String, Object> tags, Map<String, Object> fields) {
        return String.format("%s%s %s %d", measurement, tags != null ? "," + toCSV(tags) : "", transformFields(fields), System.currentTimeMillis() * 1000000);
    }

    protected static String transformFields(Map<String, Object> fields) {
        if (fields != null) {
            StringBuilder fieldsString = new StringBuilder();
            for (Map.Entry<String, Object> field : fields.entrySet()) {
                String key = field.getKey();
                Object value = field.getValue();
                if (value instanceof String) {
                    fieldsString.append(",").append(key).append("=").append(escape((String) value));
                } else {
                    fieldsString.append(",").append(key).append("=").append(value);
                }
            }
            return fieldsString.substring(1);
        } else {
            throw new RuntimeException("InfluxDB datapoint fields can't be null!");
        }
    }

    private static String escape(String string) {
        return "\\\"" + string + "\\\"";
    }

    private static String toCSV(Map<String, Object> map) {
        if (map != null) {
            return Joiner.on(",").withKeyValueSeparator("=").join(map);
        } else {
            return "";
        }
    }

    private void writeToSocket(String data) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);

            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            osw.write(data, 0, data.length());
            osw.flush();
            log.debug("Wrote {} to socket with port {}", data, sensuPort);
        } catch (ConnectException e) {
            // for å slippe full stacktrace i enhetstester mm.
            log.error("Unable to connect to {}:{} {}", sensuHost, sensuPort, e.getMessage());
        } catch (IOException e) {
            log.error("Unable to write data {} to socket with port {}", data, sensuPort, e);
        }
    }

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Unknown host " + e);
            return "Unknown host";
        }
    }
}
