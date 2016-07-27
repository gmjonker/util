package gmjonker.util;

import org.junit.*;

import java.io.IOException;

public class UtilTest
{
    @Test
    public void executeCommand() throws IOException
    {
        Util.executeCommandAndCaptureResult("git rev-parse --abbrev-ref HEAD");
    }

}