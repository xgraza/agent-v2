package secret.agent.injector.mc;

/**
 * @author xgraza
 * @since 01/24/25
 */
public enum MinecraftVersion
{
    _1_7_2, _1_8_8, _1_8_9;

    private static final String BASE_RESOURCE_URL = "/mappings/%s/";
    private final String mappings, id;

    MinecraftVersion()
    {
        id = name()
                .substring(1)
                .replaceAll("_", ".");
        mappings = String.format(BASE_RESOURCE_URL, id);
    }

    public String getId()
    {
        return id;
    }

    public String getMappings()
    {
        return mappings;
    }

    public static MinecraftVersion getFrom(final String version)
    {
        for (final MinecraftVersion v : values())
        {
            if (v.getId().equals(version))
            {
                return v;
            }
        }
        return null;
    }
}
