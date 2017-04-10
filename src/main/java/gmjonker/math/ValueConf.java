package gmjonker.math;

import lombok.Data;

import static gmjonker.util.FormattingUtil.asPercentage;

@Data
public class ValueConf
{
    public final double value;
    public final double confidence;

    @Override
    public String toString()
    {
        return toShortString();
    }

    public String toShortString()
    {
        return asPercentage(value) + "/" + asPercentage(confidence);
    }
}
