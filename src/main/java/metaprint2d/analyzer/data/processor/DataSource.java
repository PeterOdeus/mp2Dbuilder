package metaprint2d.analyzer.data.processor;

import java.io.IOException;

public abstract interface DataSource<T>
{
  public abstract T getNext()
    throws Exception;

  public abstract void close()
    throws IOException;
}
