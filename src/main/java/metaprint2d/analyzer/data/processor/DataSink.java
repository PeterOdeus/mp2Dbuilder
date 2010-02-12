package metaprint2d.analyzer.data.processor;

import java.io.IOException;

public abstract interface DataSink<T>
{
  public abstract void put(T paramT)
    throws IOException;

  public abstract void close()
    throws IOException;

  public abstract void flush()
    throws IOException;
}

