package org.apache.rat.maven;

import java.util.Collections;
import java.util.List;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

public class SettingsStub
    extends Settings
{
    /** {@inheritDoc} */
    public List<Proxy> getProxies()
    {
        return Collections.EMPTY_LIST;
    }
}
