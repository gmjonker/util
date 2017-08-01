package gmjonker.math;

public class ImmutableIndication extends Indication 
{
    public ImmutableIndication() {
        super();
    }

    public ImmutableIndication(double value, double confidence, String comment) {
        super(value, confidence, comment);
    }

    public ImmutableIndication(double value, double confidence) {
        super(value, confidence);
    }

    @Override
    public void setValue(double value) {
        throw new RuntimeException("setValue() called on ImmutableIndication");
    }

    @Override
    public void setConfidence(double confidence) {
        throw new RuntimeException("setConfidence() called on ImmutableIndication");
    }

    @Override
    public void setComment(String comment) {
        throw new RuntimeException("setComment() called on ImmutableIndication");
    }

    @Override
    public Indication correct() {
        throw new RuntimeException("correct() called on ImmutableIndication");
    }
}
