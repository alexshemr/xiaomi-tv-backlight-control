package local.ha.tvbacklight;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Bridge extends Service {
    static final String KEY = "picture_backlight";
    private ServerSocket server;
    private NetworkDiscovery discovery;
    static volatile boolean running;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int id) {
        String token = getSharedPreferences("bridge", MODE_PRIVATE).getString("token", null);
        String channel = "backlight";
        getSystemService(NotificationManager.class).createNotificationChannel(
            new NotificationChannel(channel, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW));
        startForeground(1, new Notification.Builder(this, channel)
            .setContentTitle(getString(R.string.app_name)).setSmallIcon(android.R.drawable.ic_menu_manage).build());
        if (token == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (server == null) {
            try {
                server = new ServerSocket(8765);
                running = true;
                discovery = new NetworkDiscovery(this);
                discovery.advertise(Pairing.identity(this));
                ServerSocket listener = server;
                new Thread(() -> serve(listener, token), "backlight-http").start();
            } catch (IOException error) {
                Log.e("TVBacklight", "Unable to open port 8765", error);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        running = false;
        if (discovery != null) discovery.close();
        if (server != null) {
            try { server.close(); } catch (IOException ignored) {}
        }
        super.onDestroy();
    }

    private void serve(ServerSocket listener, String token) {
        while (!listener.isClosed()) {
            try (Socket client = listener.accept()) {
                if (!client.getInetAddress().isSiteLocalAddress()) continue;
                client.setSoTimeout(1500);
                handle(client, token);
            } catch (IOException error) {
                if (!listener.isClosed()) Log.d("TVBacklight", "Connection ended", error);
            }
        }
    }

    private void handle(Socket client, String token) throws IOException {
        InputStream input = new BufferedInputStream(client.getInputStream());
        OutputStream output = client.getOutputStream();
        String[] request = line(input).split(" ");
        if (request.length != 3) {
            reply(output, "404 Not Found", "{}");
            return;
        }
        String authorization = "";
        String length = "";
        for (int count = 0; ; count++) {
            if (count == 32) throw new IOException("Too many headers");
            String header = line(input);
            if (header.isEmpty()) break;
            int colon = header.indexOf(':');
            if (colon < 1) throw new IOException("Invalid header");
            String name = header.substring(0, colon);
            String value = header.substring(colon + 1).trim();
            if (name.equalsIgnoreCase("Authorization")) authorization = value;
            if (name.equalsIgnoreCase("Content-Length")) length = value;
            if (name.equalsIgnoreCase("Transfer-Encoding")) throw new IOException("Unsupported encoding");
        }
        if (request[1].equals("/info") && request[0].equals("GET")) {
            reply(output, "200 OK", info());
            return;
        }
        if (request[1].equals("/pair") && request[0].equals("POST")) {
            if (!length.equals("6") || !Pairing.isOpen()) {
                reply(output, "403 Forbidden", "{}");
                return;
            }
            byte[] code = new byte[6];
            new DataInputStream(input).readFully(code);
            String paired = Pairing.accept(this, new String(code, StandardCharsets.US_ASCII));
            reply(output, paired == null ? "403 Forbidden" : "200 OK",
                paired == null ? "{}" : "{\"token\":\"" + paired + "\"}");
            return;
        }
        if (!request[1].equals("/backlight")) {
            reply(output, "404 Not Found", "{}");
            return;
        }
        if (!Pairing.authorized(this, authorization, token)) {
            reply(output, "401 Unauthorized", "{}");
            return;
        }
        try {
            if (request[0].equals("PUT")) {
                if (!length.matches("[1-3]")) {
                    reply(output, "400 Bad Request", "{}");
                    return;
                }
                byte[] body = new byte[Integer.parseInt(length)];
                new DataInputStream(input).readFully(body);
                String value = new String(body, StandardCharsets.US_ASCII);
                if (!value.matches("0|[1-9][0-9]?|100")) {
                    reply(output, "400 Bad Request", "{}");
                    return;
                }
                if (Backlight.write(this, Integer.parseInt(value)) < 0) {
                    reply(output, "503 Service Unavailable", "{}");
                    return;
                }
            } else if (!request[0].equals("GET")) {
                reply(output, "405 Method Not Allowed", "{}");
                return;
            }
            int current = Backlight.read(this);
            if (current < 0 || current > 100) {
                reply(output, "503 Service Unavailable", "{}");
                return;
            }
            reply(output, "200 OK", "{\"level\":" + current + "}");
        } catch (SecurityException | IllegalStateException error) {
            reply(output, "503 Service Unavailable", "{}");
        }
    }

    private String info() {
        try {
            org.json.JSONObject info = new org.json.JSONObject();
            info.put("id", Pairing.identity(this));
            info.put("model", android.os.Build.MODEL);
            info.put("api", 1);
            return info.toString();
        } catch (org.json.JSONException error) {
            throw new IllegalStateException(error);
        }
    }

    private static String line(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int count = 0; count < 1024; count++) {
            int next = input.read();
            if (next == '\r' && input.read() == '\n') {
                return new String(buffer.toByteArray(), StandardCharsets.US_ASCII);
            }
            if (next < 0 || next == '\n' || next == '\r') break;
            buffer.write(next);
        }
        throw new IOException("Invalid HTTP line");
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static void reply(OutputStream output, String status, String body) throws IOException {
        output.write(bytes("HTTP/1.1 " + status + "\r\nContent-Type: application/json\r\n"
            + "Connection: close\r\nContent-Length: " + bytes(body).length + "\r\n\r\n" + body));
    }
}


