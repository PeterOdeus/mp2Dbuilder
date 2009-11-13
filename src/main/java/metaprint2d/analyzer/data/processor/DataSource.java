package metaprint2d.analyzer.data.processor;

import java.io.IOException;

public abstract interface DataSource<T>
{
  public abstract T getNext()
    throws Exception;

  public abstract void close()
    throws IOException;
}

// Location:           /home/podeus/az/metaprint2d-builder-app-r16427/ImportedClasses/
// Qualified Name:     metaprint2d.analyzer.data.processor.DataSource
// Java Class Version: 5 (49.0)
// JD-Core Version:    0.5.1
