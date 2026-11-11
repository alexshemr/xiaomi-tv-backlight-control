package local.ha.tvbacklight;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import java.util.function.Consumer;

final class NetworkDiscovery {
    private final NsdManager manager;
    private NsdManager.RegistrationListener registration;
    private NsdManager.DiscoveryListener discovery;
    NetworkDiscovery(Context context) { manager = context.getSystemService(NsdManager.class); }
    void advertise(String id) {
        NsdServiceInfo service = new NsdServiceInfo();
        service.setServiceName("Xiaomi TV Backlight " + id.substring(0, 8));
        service.setServiceType("_xiaomi-backlight._tcp.");
        service.setPort(8765);
        service.setAttribute("id", id);
        service.setAttribute("model", Build.MODEL);
        registration = new NsdManager.RegistrationListener() {
            public void onServiceRegistered(NsdServiceInfo info) {}
            public void onRegistrationFailed(NsdServiceInfo info, int error) {}
            public void onServiceUnregistered(NsdServiceInfo info) {}
            public void onUnregistrationFailed(NsdServiceInfo info, int error) {}
        };
        manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, registration);
    }
    void search(Consumer<String> found) {
        stopSearch();
        discovery = new NsdManager.DiscoveryListener() {
            public void onDiscoveryStarted(String type) {}
            public void onServiceFound(NsdServiceInfo service) {
                manager.resolveService(service, new NsdManager.ResolveListener() {
                    public void onResolveFailed(NsdServiceInfo info, int error) {}
                    public void onServiceResolved(NsdServiceInfo info) {
                        String host = info.getHost().getHostAddress();
                        if (host.contains(":")) host = "[" + host + "]";
                        found.accept("http://" + host + ":" + info.getPort());
                    }
                });
            }
            public void onServiceLost(NsdServiceInfo service) {}
            public void onDiscoveryStopped(String type) {}
            public void onStartDiscoveryFailed(String type, int error) { stopSearch(); }
            public void onStopDiscoveryFailed(String type, int error) {}
        };
        manager.discoverServices("_home-assistant._tcp.", NsdManager.PROTOCOL_DNS_SD, discovery);
    }
    void stopSearch() {
        if (discovery != null) {
            try { manager.stopServiceDiscovery(discovery); } catch (IllegalArgumentException ignored) {}
            discovery = null;
        }
    }
    void close() {
        stopSearch();
        if (registration != null) {
            try { manager.unregisterService(registration); } catch (IllegalArgumentException ignored) {}
            registration = null;
        }
    }
}
