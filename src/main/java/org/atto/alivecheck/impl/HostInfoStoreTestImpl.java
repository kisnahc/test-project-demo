package org.atto.alivecheck.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.atto.alivecheck.AliveStatus;
import org.atto.alivecheck.HostInfo;
import org.atto.alivecheck.HostInfoStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HostInfoStoreTestImpl implements HostInfoStore {

    private final List<HostInfo> hostInfos = new ArrayList<>();

    private static class InstanceHolder {
        private static final HostInfoStoreTestImpl INSTANCE = new HostInfoStoreTestImpl();
    }

    static HostInfoStoreTestImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private List<HostInfo> getHostInfos() {
        return hostInfos;
    }

    @Override
    public List<HostInfo> getDeadList() {
        return getHostInfos().stream()
                .filter(h -> h.getAliveStatus().equals(AliveStatus.DEAD))
                .collect(Collectors.toList());
    }

    @Override
    public HostInfo get(String hostName) {
        for (HostInfo hostInfo : hostInfos) {
            if (hostInfo.getHostName().equals(hostName)) {
                return new HostInfo(hostName, hostInfo.getAliveStatus());
            }
        }
        return null;
    }

    @Override
    public List<HostInfo> getAll() {
        return getHostInfos();
    }

    @Override
    public void add(HostInfo hostInfo) {
        hostInfos.add(hostInfo);
    }

    @Override
    public void add(List<HostInfo> hostInfo) {
        hostInfos.addAll(hostInfo);
    }

    @Override
    public void remove(HostInfo hostInfo) {
        hostInfos.remove(hostInfo);
    }

    @Override
    public void remove(List<HostInfo> hostInfo) {
        hostInfos.remove(hostInfo);
    }

    @Override
    public void clear() {
        if (!hostInfos.isEmpty()) {
            hostInfos.clear();
        }
    }

    @Override
    public void update(HostInfo hostInfo) {
        hostInfos.set(hostInfos.indexOf(hostInfo), hostInfo);
    }

}
