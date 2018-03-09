package observeIt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class IteratorExample {

  /**
   * This is a verbose iterator over an array. It shall only serve for
   * the purpose of describing the concept. Java libs exist that provide
   * this implementation out of the box.
   *
   * Note: in bigger systems such as Hadoop MR or Spark this iterator would retrieve data from disk or the network.
   */
  public static class Source<T> implements Iterator<T> {

    // iterator state
    private T[] _t = null;
    private int _currentIdx = 0;

    Source(T[] t){
      _t = t;
    }

    @Override
    public boolean hasNext() {
      return _currentIdx < _t.length;
    }

    @Override
    public T next() {
      // static payload for retrieving values from an array
      return _t[_currentIdx++];
    }
  }

  public static class GenericIt<T> implements Iterator<T> {
    // iterator state
    private Iterator<T> _src = null;
    private Function<T,T> _payload = null; // I could also add a function here that just changes the type.

    GenericIt(Iterator<T> src, Function<T,T> payload){
      _src = src;
      _payload = payload;
    }

    @Override
    public boolean hasNext() {
      return _src.hasNext();
    }

    @Override
    public T next() {
      T t = _src.next();
      return _payload.apply(t); // just calling the payload on the retrieved item and returning the result.
    }
  }

  /**
   * This function runs the following iterator pipeline:
   * Source -> Print -> Increment -> Print
   */
  public static void iteratorPipeline(){
    Integer[] someInts = new Integer[] { 0, 1, 2 };
    Source<Integer> src = new Source<>(someInts);

    // first added iterator
    Function<Integer,Integer> payload1 = t -> { System.out.println("Peek: " + t); return t;};
    GenericIt<Integer> it1 = new GenericIt<>(src, payload1);

    // second added iterator
    Function<Integer,Integer> payload2 = t -> ++t;
    GenericIt<Integer> it2 = new GenericIt<>(it1, payload2);

    // and finally somebody (the loop) that consumes it because iterators are actually pull-based
    while(it2.hasNext()){
      Integer i = it2.next();
      System.out.println("Final value: " + i);
    }
  }

  public static class Union<T,R> implements Iterator<R>{
    // iterator state
    private Iterator<T>[] _sources = null;
    private Function<List<T>,R> _payload = null;

    Union(Iterator<T>[] sources, Function<List<T>,R> payload){
      _sources = sources;
      _payload = payload;
    }

    @Override
    public boolean hasNext() {
      for(Iterator<T> source : _sources){
        if(!source.hasNext()) return false;
      }
      return true;
    }

    @Override
    public R next() {
      // in Java 8 this would of course be done with Streams
      List<T> items = new ArrayList<>(_sources.length);
      for(Iterator<T> source : _sources) {
        items.add(source.next());
      }
      return _payload.apply(items);
    }
  }

  /**
   * This function builds the following tree:
   * Source1 -> Print
   *                  \
   *                    Add -> Print
   *                  /
   * Source2 -> Print
   */
  public static void unifyingIterators(){
    Integer[] someInts = new Integer[] { 0, 1, 2 };
    Source<Integer> srcInts = new Source<>(someInts);

    Integer[] someOtherInts = new Integer[] { 3, 4, 5 };
    Source<Integer> sourceOtherInts = new Source<>(someOtherInts);

    // printer for ints
    Function<Integer,Integer> payloadInt1 = t -> { System.out.println("Peek: " + t); return t;};
    GenericIt<Integer> it1 = new GenericIt<>(srcInts, payloadInt1);

    // printer for more ints
    Function<Integer,Integer> payloadOtherInts1 = t -> { System.out.println("Value is: " + t); return t;};
    GenericIt<Integer> it2 = new GenericIt<>(sourceOtherInts, payloadOtherInts1);

    // union
    Iterator<Integer>[] its = new Iterator[] { it1, it2 };
    Function<List<Integer>,Integer> add = ts -> { int sum = 0; for(Integer t:ts){ sum += t; } return sum; };
    Union<Integer, Integer> itAdd = new Union(its, add);

    // and finally somebody (the loop) that consumes it because iterators are actually pull-based
    while(itAdd.hasNext()){
      Integer i = itAdd.next();
      System.out.println("Final value: " + i);
    }

  }

}