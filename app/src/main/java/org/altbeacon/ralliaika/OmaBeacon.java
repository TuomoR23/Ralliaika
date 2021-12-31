package org.altbeacon.ralliaika;

import org.altbeacon.beacon.Beacon;

public class OmaBeacon extends Beacon {
    long lastSeen = 0;

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public OmaBeacon(Beacon otherBeacon, long lastSeen) {
        super(otherBeacon);
        this.lastSeen = lastSeen;
    }
}
