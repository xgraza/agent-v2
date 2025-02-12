package secret.agent.injector.transform;

import secret.agent.injector.logger.Logger;
import secret.agent.injector.mc.MinecraftVersion;
import secret.agent.injector.mc.MappingsReader;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * @author xgraza
 * @since 01/25/25
 */
public final class AgentClassTransformer
{
    public void init(final Instrumentation instrumentation, final MinecraftVersion version)
    {
        loadMappings(version);

        // load edited classes TODO
    }

    private void loadMappings(final MinecraftVersion version)
    {
        try
        {
            final long start = System.nanoTime();
            long end;
            MappingsReader.readMappingsFile(version.getMappings());
            end = System.nanoTime();
            Logger.info("Read mappings in {}ms", (end - start) / 1000000.0);
        } catch (IOException e)
        {
            Logger.error(e.toString());
        }
    }
}
