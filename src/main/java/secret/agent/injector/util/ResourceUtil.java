package secret.agent.injector.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author xgraza
 * @since 01/25/25
 */
public final class ResourceUtil
{
    public static String readResource(final String location) throws IOException
    {
        final StringBuilder builder = new StringBuilder();
        try (final InputStream is = ResourceUtil.class.getResourceAsStream(location))
        {
            if (is == null)
            {
                return null;
            }
            int b;
            while ((b = is.read()) != -1)
            {
                builder.append((char)b);
            }
        }
        return builder.toString();
    }
}
