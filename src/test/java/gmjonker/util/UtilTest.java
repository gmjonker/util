package gmjonker.util;

import org.junit.*;

import java.io.IOException;

import static gmjonker.util.Util.getEnvOrDefault;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilTest
{
    @Test
    public void executeCommand() throws IOException
    {
        Util.executeCommandAndCaptureResult("git rev-parse --abbrev-ref HEAD");
    }

    @Test
    public void getEnvOrDefaultt()
    {
        assertThat(getEnvOrDefault("ZXVZXCVZXCV", 123), equalTo(123));
    }

}