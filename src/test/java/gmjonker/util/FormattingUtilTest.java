package gmjonker.util;

import org.junit.*;

import java.util.Date;

import static gmjonker.util.FormattingUtil.getIndentation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormattingUtilTest
{
    @Test
    public void toMicroFormatABC() throws Exception
    {
        System.out.println("FormattingUtil.toMicroFormatABC(-1.1 ) = " + FormattingUtil.toMicroFormatABC(-1.1 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-1.0 ) = " + FormattingUtil.toMicroFormatABC(-1.0 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.99) = " + FormattingUtil.toMicroFormatABC(-0.99));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.9 ) = " + FormattingUtil.toMicroFormatABC(-0.9 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.8 ) = " + FormattingUtil.toMicroFormatABC(-0.8 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.7 ) = " + FormattingUtil.toMicroFormatABC(-0.7 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.6 ) = " + FormattingUtil.toMicroFormatABC(-0.6 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.5 ) = " + FormattingUtil.toMicroFormatABC(-0.5 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.4 ) = " + FormattingUtil.toMicroFormatABC(-0.4 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.3 ) = " + FormattingUtil.toMicroFormatABC(-0.3 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.2 ) = " + FormattingUtil.toMicroFormatABC(-0.2 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.1 ) = " + FormattingUtil.toMicroFormatABC(-0.1 ));
        System.out.println("FormattingUtil.toMicroFormatABC(-0.01) = " + FormattingUtil.toMicroFormatABC(-0.01));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.0 ) = " + FormattingUtil.toMicroFormatABC( 0.0 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.01) = " + FormattingUtil.toMicroFormatABC( 0.01));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.1 ) = " + FormattingUtil.toMicroFormatABC( 0.1 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.2 ) = " + FormattingUtil.toMicroFormatABC( 0.2 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.3 ) = " + FormattingUtil.toMicroFormatABC( 0.3 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.4 ) = " + FormattingUtil.toMicroFormatABC( 0.4 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.5 ) = " + FormattingUtil.toMicroFormatABC( 0.5 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.6 ) = " + FormattingUtil.toMicroFormatABC( 0.6 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.7 ) = " + FormattingUtil.toMicroFormatABC( 0.7 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.8 ) = " + FormattingUtil.toMicroFormatABC( 0.8 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.9 ) = " + FormattingUtil.toMicroFormatABC( 0.9 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 0.99) = " + FormattingUtil.toMicroFormatABC( 0.99));
        System.out.println("FormattingUtil.toMicroFormatABC( 1.0 ) = " + FormattingUtil.toMicroFormatABC( 1.0 ));
        System.out.println("FormattingUtil.toMicroFormatABC( 1.1 ) = " + FormattingUtil.toMicroFormatABC( 1.1 ));
    }

    @Test
    public void mapToString2()
    {
        DefaultingHashmap<Integer, Double> map = new DefaultingHashmap<>(new Double(2));
        map.put(1, 1.1);
        map.put(2, 2.2);
        String s = FormattingUtil.mapToString(
                map,
                integer -> integer + 2
        );
        System.out.println("s = " + s);
    }

    @Test
    public void dateToString()
    {
        System.out.println("FormattingUtil.toString(new Date()) = " + FormattingUtil.dateToString(new Date()));
    }

    @Test
    public void testGetIndentation()
    {
        assertThat(getIndentation("asdf"), equalTo(0));
        assertThat(getIndentation("  asdf"), equalTo(2));
        assertThat(getIndentation("    asdf"), equalTo(4));
        assertThat(getIndentation(""), equalTo(0));
        assertThat(getIndentation("  "), equalTo(2));
    }
}