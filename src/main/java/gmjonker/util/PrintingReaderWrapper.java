package gmjonker.util;

import java.io.IOException;
import java.io.Reader;

public class PrintingReaderWrapper extends Reader
{
    private final Reader sourceReader;

    public PrintingReaderWrapper(Reader sourceReader)
    {
        this.sourceReader = sourceReader;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        int bytes = sourceReader.read(cbuf, off, len);
        System.out.println("cbuf = " + new String(cbuf));
        return bytes;
    }

    @Override
    public void close() throws IOException
    {
        sourceReader.close();
    }
}
