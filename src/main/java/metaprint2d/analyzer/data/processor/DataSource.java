package metaprint2d.analyzer.data.processor;

import java.io.IOException;

import org.openscience.cdk.interfaces.IReactionSet;

public abstract interface DataSource<T>
{
  public abstract T getNext(int reactionId, IReactionSet currentReactionSet)
    throws Exception;

  public abstract void close()
    throws IOException;
}
