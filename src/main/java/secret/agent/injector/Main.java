package secret.agent.injector;

import com.sun.tools.attach.*;
import secret.agent.BuildConfig;
import secret.agent.injector.logger.Logger;
import secret.agent.injector.mc.MinecraftProcess;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author xgraza
 * @since 01/23/25
 */
public final class Main
{
    private static final String[] MINECRAFT_START_LOCATIONS =
            {
                    "net.minecraft.client.main.Main",
                    "net.minecraft.launchwrapper.Launch"
            };
    private static final String ATTACH_TOOLS_LOC;

    static
    {
        final String javaHome = System.getenv("JAVA_HOME");
        final String separator = System.getProperty("file.separator", "\\");
        ATTACH_TOOLS_LOC = "file:///" + javaHome + separator + "lib" + separator + "tools.jar";
    }

    public static void main(final String[] args) throws
            IOException,
            AgentLoadException,
            AgentInitializationException,
            AttachNotSupportedException
    {
        final Scanner scanner = new Scanner(System.in);

        Logger.debug("Version: {}, Git: {}/{}, Build ID: {}",
                BuildConfig.VERSION,
                BuildConfig.HASH,
                BuildConfig.BRANCH,
                BuildConfig.BUILD_NUMBER);

        addAttachToolsToRuntime();

        final List<MinecraftProcess> processes = searchForMinecraftProcesses();
        if (processes.isEmpty())
        {
            Logger.error("No minecraft processes found.");
            return;
        }
        final MinecraftProcess process = selectMinecraftProcess(processes, scanner);

        Logger.info("Found minecraft process with PID({}) on version {}",
                process.getVm().id(), process.getVersion());

        final File agentFile = getAgentJarFile();
        final VirtualMachine vm = VirtualMachine.attach(process.getVm());
        vm.loadAgent(agentFile.getAbsolutePath(), process.getVersion().getId());
        Logger.success("Loaded agent into VM {}", vm.id());

        listenForCommands(scanner);

        // once the loop in the listenForCommands is interrupted, save log files.
        Logger.info("Exiting... Writing log files to disk");
        Logger.saveLogsToFile();
        Logger.info("Goodbye!");
    }

    private static void listenForCommands(final Scanner scanner)
    {
        Logger.info("Write your commands below:");
        while (true)
        {
            final String command = scanner.nextLine();

            if (command.equalsIgnoreCase("exit")
                    || command.equalsIgnoreCase("quit"))
            {
                break;
            }
        }
    }

    private static File getAgentJarFile()
    {
        //        final File agentFile = new File(Main.class
//                .getProtectionDomain()
//                .getCodeSource()
//                .getLocation()
//                .getFile());
        return Paths.get("", "build/libs/Agent+1-1.0.0.jar").toFile();
    }

    private static MinecraftProcess selectMinecraftProcess(
            final List<MinecraftProcess> processes,
            final Scanner scanner)
    {
        if (processes.size() >= 2)
        {
            while (true)
            {
                for (int i = 0; i < processes.size(); ++i)
                {
                    final MinecraftProcess p = processes.get(i);
                    System.out.printf("%s.) PID: %s, Version: %s%n",
                            (i + 1), p.getVm().id(), p.getVersion());
                }

                System.out.print("Please choose which process you would like to inject: ");
                final int selection = scanner.nextInt();
                if (selection < 1 || selection > processes.size())
                {
                    Logger.error("Invalid process chose \"{}\"", selection);
                    continue;
                }

                return processes.get(selection - 1);
            }
        } else
        {
            return processes.get(0);
        }
    }

    private static List<MinecraftProcess> searchForMinecraftProcesses()
    {
        final List<MinecraftProcess> processes = new LinkedList<>();
        final List<VirtualMachineDescriptor> attachableVms = VirtualMachine.list();
        for (final VirtualMachineDescriptor vm : attachableVms)
        {
            final String name = vm.displayName();
            for (final String startClassName : MINECRAFT_START_LOCATIONS)
            {
                if (name.startsWith(startClassName))
                {
                    processes.add(new MinecraftProcess(name, vm));
                }
            }
        }
        return processes;
    }

    private static void addAttachToolsToRuntime()
    {
        if (isAttachToolsAttached())
        {
            Logger.info("tools.jar is already present in the classloader");
            return;
        }

        try
        {
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), new URL(ATTACH_TOOLS_LOC));
            Logger.info("Attached tools.jar to classloader {}", ATTACH_TOOLS_LOC);
        } catch (final NoSuchMethodException |
                       InvocationTargetException |
                       IllegalAccessException |
                       MalformedURLException e)
        {
            Logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private static boolean isAttachToolsAttached()
    {
        try
        {
            Class.forName("com.sun.tools.attach.VirtualMachine");
            return true;
        } catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
