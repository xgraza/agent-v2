package secret.agent.injector.mc;

import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xgraza
 * @since 01/23/25
 */
public final class MinecraftProcess
{
    private static final Pattern GAME_ARGS_PATTERN = Pattern.compile(
            "--(?:username|version|gameDir)(?:\\s+|=)?([a-z0-9_:\\\\/.]+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private final MinecraftVersion version;
    private final String username;
    private final String gameDir;

    private final VirtualMachineDescriptor vm;

    public MinecraftProcess(final String input, final VirtualMachineDescriptor vm)
    {
        this.vm = vm;

        final Matcher matcher = GAME_ARGS_PATTERN.matcher(input);
        final String[] found = new String[3];
        for (int i = 0; i < found.length; ++i)
        {
            if (!matcher.find())
            {
                throw new RuntimeException("short matchings");
            }
            found[i] = matcher.group(1);
        }

        username = found[0];
        version = MinecraftVersion.getFrom(found[1]);
        gameDir = found[2];
    }

    public MinecraftVersion getVersion()
    {
        return version;
    }

    public String getUsername()
    {
        return username;
    }

    public String getGameDir()
    {
        return gameDir;
    }

    public VirtualMachineDescriptor getVm()
    {
        return vm;
    }
}
