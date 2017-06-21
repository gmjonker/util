package gmjonker.math;

import lombok.Data;

import static gmjonker.util.FormattingUtil.asPercentage;

@Data
public class ValueConf
{
    public static final ValueConf UNKNOWN = new ValueConf(0, 0);
    
    public final double value;
    public final double confidence;

    @Override
    public String toString()
    {
        return toShortString();
    }

    public String toShortString()
    {
        return value + "/" + asPercentage(confidence);
    }
}
