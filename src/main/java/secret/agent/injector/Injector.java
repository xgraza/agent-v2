package secret.agent.injector;

import secret.agent.injector.logger.Logger;
import secret.agent.injector.mc.MinecraftVersion;
import secret.agent.injector.transform.AgentClassTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author xgraza
 * @since 01/23/25
 */
public final class Injector
{
    public static final AgentClassTransformer CLASS_TRANSFORMER = new AgentClassTransformer();

    public static void agentmain(final String args, final Instrumentation is)
    {
        final MinecraftVersion version = MinecraftVersion.getFrom(args);
        if (version == null)
        {
            Logger.error("Mappings were not found for version {}", args);
            return;
        }
        Logger.info("Loading mappings for version {}", version);
        CLASS_TRANSFORMER.init(is, version);
    }
}
