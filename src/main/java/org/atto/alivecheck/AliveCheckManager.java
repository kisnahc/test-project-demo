package org.atto.alivecheck;


public interface AliveCheckManager {

    HostInfoMonitor hostInfoMonitor();

    HostInfoStore hostInfoStore();

    HostInfoStore hostInfoTestStore();

    HostInfo createHostInfo(String hostName);

}
