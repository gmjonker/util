package gmjonker.util;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static gmjonker.util.Util.eq;

public class StringNormalization
{
    /** Return string lower cased and trimmed. **/
    public static String normalize(String string)
    {
        if (string == null)
            return null;
        return StringUtils.stripAccents(string.trim().toLowerCase());
    }

    /** Return strings lower cased and trimmed. **/
    public static void normalize(@Nullable String[] strings)
    {
        if (strings != null)
            for (int i = 0; i < strings.length; i++)
                strings[i] = normalize(strings[i]);
    }

    /** Return strings lower cased and trimmed. **/
    public static void normalize(@Nullable List<String> strings)
    {
        if (strings != null)
            for (int i = 0; i < strings.size(); i++)
                strings.set(i, normalize(strings.get(i)));
    }

    public static boolean isNormalized(String text)
    {
        return eq(text, normalize(text));
    }

    public static boolean equalsNormalized(String string1, String string2)
    {
        return eq(normalize(string1), normalize(string2));
    }

    public static boolean containsNormalized(String string1, String string2)
    {
        return (StringUtils.contains(normalize(string1), normalize(string2)));
    }
}
