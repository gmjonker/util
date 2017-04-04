package gmjonker.util;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static gmjonker.util.Util.eq;

public class StringNormalization
{
    private static final LambdaLogger log = new LambdaLogger(StringNormalization.class);
    
    /** 
     * - Removes accents
     * - Removes non-printable characters 
     * - Replaces non-breakable spaces with breakable spaces
     * - Trims
     * - Lowercases
     **/
    public static String normalize(String string)
    {
        if (string == null)
            return null;
////        string = string.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", ""); // remove unprintable characters
////        string = removeUnprintableCharacters(string);
////        string = string.replace("\u00A0", " "); // replace non-breakable spaces
//        string = string.replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}\\u00A0]", " "); // replace unprintable characters and non-breakable spaces
////        string = string.replaceAll("\\u00A0", " "); // replace non-breakable spaces
        string = Normalizer.normalize(string, Normalizer.Form.NFKC);
        if (string.contains("\u00A0"))
            System.out.println("wtf");
        return StringUtils.stripAccents(string.trim().toLowerCase());
    }

    /**
     * - Removes accents
     * - Trims
     * - Lowercases
     **/
    public static void normalize(@Nullable String[] strings)
    {
        if (strings != null)
            for (int i = 0; i < strings.length; i++)
                strings[i] = normalize(strings[i]);
    }

    /**
     * - Removes accents
     * - Trims
     * - Lowercases
     **/
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
    
    public static String removeUnprintableCharacters(String myString)
    {
        StringBuilder newString = new StringBuilder(myString.length());
        for (int offset = 0; offset < myString.length();)
        {
            int codePoint = myString.codePointAt(offset);
            offset += Character.charCount(codePoint);

            // Replace invisible control characters and unused code points
            switch (Character.getType(codePoint))
            {
                case Character.CONTROL:     // \p{Cc}
                case Character.FORMAT:      // \p{Cf}
                case Character.PRIVATE_USE: // \p{Co}
                case Character.SURROGATE:   // \p{Cs}
                case Character.UNASSIGNED:  // \p{Cn}
                    newString.append(' ');
                    break;
                default:
                    newString.append(Character.toChars(codePoint));
                    break;
            }
        }
        return newString.toString();
    }

    public static String removeNbsp(String string)
    {
        StringBuilder sb = new StringBuilder(string.length());
        for (char c : string.toCharArray()) {
            if (c == '\u00A0')
                sb.append(' ');
            else
                sb.append(c);
        }
        return sb.toString();
    }

    static Pattern pattern = Pattern.compile("[\\.:,\"'“”\\(\\)\\[\\]|/?!;=_*<>€]+");

    public static final Function<String, String> punctuationRemover =
            text -> {
                log.trace("text = '{}'", text);
                String ppText = pattern.matcher(text).replaceAll("");
                log.trace("ppText = '{}'", ppText);
                return ppText;
            };

    public static String replacePunctuation(String text, String replacement)
    {
        return pattern.matcher(text).replaceAll(replacement);
    }
    
    public static String replacePunctuationFast(String text, String replacement)
    {
        final String punctuation = ".:,\"'“”()[]|/?!;=_*<>€";
        final String replacementList = StringUtils.repeat(replacement, punctuation.length());
        return StringUtils.replaceChars(text, punctuation, replacement);
    }

    //    // This should solve the problem of Excel not wanting to import the CSV, but doesn't...
    //    private static String flattenToAscii(String string)
    //    {
    //        StringBuilder sb = new StringBuilder(string.length());
    //        string = Normalizer.normalize(string, Normalizer.Form.NFD);
    //        for (char c : string.toCharArray())
    //            if (c <= '\u007F')
    //                sb.append(c);
    //        return sb.toString();
    //    }

}
