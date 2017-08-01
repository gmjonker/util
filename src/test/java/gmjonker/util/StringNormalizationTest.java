package gmjonker.util;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.*;

import java.text.Normalizer;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("InvisibleCharacter")
public class StringNormalizationTest
{
    @Test
    public void normalizeWithInvisibleCharacters() throws Exception
    {
        {
            String string = "";
            String normalize = StringNormalization.normalize(string);
            System.out.println(" = '" + normalize + "'");
            System.out.println("StringNormalization.isNormalized(\"\") = " + StringNormalization.isNormalized(""));
        }
        {
            String string = "description ";
            System.out.println(" = '" + string + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeJava(string) + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeXml(string) + "'");
            String normalize = StringNormalization.normalize(string);
            System.out.println(" = '" + normalize + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeJava(normalize) + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeXml(normalize) + "'");
        }
        {
            String string = "We are a beautiful, well rounded and busy naturopathic clinic in Snohomish, WA. The practice has been established here for about 15 years. Many of our patients come to us for primary care, but we also do a significant amount of hormone replacement for men and women, food sensitivity testing, IV therapy and weight loss / nutrition counseling. We have an excellent administrative team and a well stocked dispensary to support our new naturopathic physician! We are looking for a doctor with the availability to grow their practice to 4-5 days a week.  \n";
            System.out.println(" = '" + string + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeJava(string) + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeXml(string) + "'");
            String normalize = StringNormalization.normalize(string);
            System.out.println(" = '" + normalize + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeJava(normalize) + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeXml(normalize) + "'");
        }
        {
            String string = "We are a beautiful, well rounded and busy naturopathic clinic in Snohomish, WA. The practice has been established here for about 15 years. Many of our patients come to us for primary care, but we also do a significant amount of hormone replacement for men and women, food sensitivity testing, IV therapy and weight loss / nutrition counseling. We have an excellent administrative team and a well stocked dispensary to support our new naturopathic physician! We are looking for a doctor with the availability to grow their practice to 4-5 days a week.  \n";
            System.out.println(" = '" + string + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeJava(string) + "'");
            System.out.println(" = '" + StringEscapeUtils.escapeXml(string) + "'");
            String normalize = Normalizer.normalize(string, Normalizer.Form.NFKC); // Also works
            System.out.println("NFKC  = '" + normalize + "'");
            System.out.println("NFKC  = '" + StringEscapeUtils.escapeJava(normalize) + "'");
            System.out.println("NFKC  = '" + StringEscapeUtils.escapeXml(normalize) + "'");
        }
    }
    
    @Test
    public void removePunct()
    {
        String text = "Price/quality, café, verjaardag, (\"a:b\").";
        assertThat(StringNormalization.removePunctuation(text), equalTo("Price quality café verjaardag a b"));

        String text2 = "midden- en klein-bedrijf en -gedoe - --- hoi";
        String noPunct = StringNormalization.removePunctuation(text2);
        System.out.println("text2   = " + text2);
        System.out.println("noPunct = " + noPunct);
    }

}