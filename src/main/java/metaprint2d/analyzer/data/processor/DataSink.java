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

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.processor.DataSink
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
