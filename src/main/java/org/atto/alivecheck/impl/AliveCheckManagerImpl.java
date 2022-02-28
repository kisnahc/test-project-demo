package org.atto.alivecheck.impl;

import lombok.NoArgsConstructor;
import org.atto.alivecheck.AliveCheckManager;
import org.atto.alivecheck.HostInfo;
import org.atto.alivecheck.HostInfoMonitor;
import org.atto.alivecheck.HostInfoStore;

@NoArgsConstructor
public class AliveCheckManagerImpl implements AliveCheckManager {

    @Override
    public HostInfoMonitor hostInfoMonitor() {
        return HostInfoMonitorImpl.getInstance();
    }

    @Override
    public HostInfoStore hostInfoStore() {
        return HostInfoStoreImpl.getInstance();
    }

    @Override
    public HostInfoStore hostInfoTestStore() {
        return HostInfoStoreTestImpl.getInstance();
    }

    @Override
    public HostInfo createHostInfo(String hostName) {
        return new HostInfo(hostName);
    }



}
