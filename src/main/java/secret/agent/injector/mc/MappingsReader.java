package secret.agent.injector.mc;

import secret.agent.injector.logger.Logger;
import secret.agent.injector.util.ResourceUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xgraza
 */
public final class MappingsReader
{
    private static final Pattern NOTCH_NAMING_PATTERN = Pattern.compile("((?:field|method)_[a-zA-Z0-9_]+)");

    private static final Map<String, String> obfClassToName = new HashMap<>();
    private static final Map<String, String> obfFieldToName = new HashMap<>();
    private static final Map<String, String> obfMethodToName = new HashMap<>();

    public static String getClassNameFromObf(final String obfClassName)
    {
        return obfClassToName.getOrDefault(obfClassName, null);
    }

    public static void readMappingsFile(final String location) throws IOException
    {
        final String content = ResourceUtil.readResource(
                location + "joined.srg");
        if (content == null)
        {
            Logger.error("Failed to read %sjoined.srg", location);
            return;
        }
        final Map<String, String> srgFieldToNotch = new HashMap<>();
        final Map<String, String> srgMethodToNotch = new HashMap<>();
        for (final String line : content.split("\n"))
        {
            // MD, FD, CL
            final String[] parts = line.split(" ");
            // first part is the identifier (MD, FD, CL)
            final String identifier = parts[0];
            if (identifier.equals("PK:")) // ignore package identifiers
            {
                continue;
            }
            final String srgName = parts[1]; // this is the jumbled shit
            final String name = parts[2]; // this is the notch mapping
            switch (identifier)
            {
                case "CL:":
                {
                    // Example:
                    // "a" -> "net/minecraft/util/EnumChatFormatting"
                    Logger.info("{} is {}", srgName, name);
                    obfClassToName.put(srgName, name);
                    break;
                }
                case "FD:":
                {
                    // Example:
                    // "a/A" -> "net/minecraft/util/EnumChatFormatting/field_96303_A"
                    srgFieldToNotch.put(srgName, name);
                    break;
                }
                case "MD:":
                {
                    // Example:
                    // "a/a(I)La;" -> "net/minecraft/util/EnumChatFormatting/func_175744_a (I)Lnet/minecraft/util/EnumChatFormatting;"

                    final String srgMethodDesc = srgName + name;
                    final String notchMethodDesc = parts[3] + parts[4]; // parts[3] is the new method, parts[4] is the notch desc
                    srgMethodToNotch.put(srgMethodDesc, notchMethodDesc);
                    break;
                }
            }
        }
        readSrgFieldMappings(location, srgFieldToNotch);
        readSrgMethodMappings(location, srgMethodToNotch);
    }

    private static void readSrgMethodMappings(final String location,
                                              final Map<String, String> methodMappings)
            throws IOException
    {
        final Map<String, String> notchToName = readCSVFile(
                location + "methods.csv");
    }

    private static void readSrgFieldMappings(final String location,
                                             final Map<String, String> fieldMappings)
            throws IOException
    {
        // get all relevant information first
        final Map<String, String> notchToName = readCSVFile(
                location + "fields.csv");

        // srgField fmt = a/A
        for (final String srgField : fieldMappings.keySet())
        {
            final String notchField = fieldMappings.get(srgField);
            final Matcher matcher = NOTCH_NAMING_PATTERN.matcher(notchField);
            if (!matcher.find())
            {
                Logger.warn("Failed to parse notch field. Value: {}", notchField);
                continue;
            }
            final String notchFieldName = matcher.group();

            final String fieldName = notchToName.get(notchFieldName);
            if (fieldName == null)
            {
                Logger.warn("Field \"{}\" did not map to a name", notchFieldName);
                continue;
            }

            // Example:
            // a/A -> fancyStyling
            obfFieldToName.put(srgField, fieldName);
        }

        notchToName.clear();
    }

    private static Map<String, String> readCSVFile(final String location) throws IOException
    {
        final String content = ResourceUtil.readResource(location);
        if (content == null)
        {
            Logger.error("Failed to read %s", location);
            throw new RuntimeException("Failed to read");
        }
        // get all relevant information first
        final Map<String, String> notchToName = new HashMap<>();
        for (final String line : content.split("\n"))
        {
            if (line.equals("searge,name,side,desc") || line.isEmpty())
            {
                continue;
            }
            final String[] parts = line.split(",");
            notchToName.put(parts[0], parts[1]);
        }
        return notchToName;
    }
}
