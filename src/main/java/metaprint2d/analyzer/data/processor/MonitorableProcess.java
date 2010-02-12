package metaprint2d.analyzer.data.processor;

public abstract interface MonitorableProcess
{
  public abstract boolean isDone();

  public abstract int getProgress();
}

