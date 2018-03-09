package observeIt;

import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

public class ObserverExample {

  /**
   * Normally this thing would be listening at a port or such.
   *
   * @param <T>
   */
  public static class Source<T> extends Observable implements Runnable {

    private T[] _data = null;

    Source(T[] data){ _data = data; }

    @Override
    public void run() {
      System.out.println(super.countObservers());
      for(T d : _data) {
        super.setChanged();
        super.notifyObservers(d);
      }
    }
  }

  public static class GenericOb<T,R> extends Observable implements Observer{
    // observer state
    private Function<T,R> _payload = null;

    GenericOb(Function<T,R> payload) {
      _payload = payload;
    }

    @Override
    public void update(Observable o, Object arg) {
      R r = _payload.apply((T) arg);
      super.setChanged();
      super.notifyObservers(r);
    }
  }

  /**
   * This function runs the following observer pipeline:
   * Source -> Print -> Increment -> Print
   */
  public static void observerPipeline(){
    Integer[] someInts = new Integer[] { 0, 1, 2 };
    Source<Integer> src = new Source<>(someInts);

    // first added observer
    Function<Integer,Integer> payload1 = t -> { System.out.println("Peek: " + t); return t;};
    GenericOb<Integer, Integer> ob1 = new GenericOb<>(payload1);

    src.addObserver(ob1);

    // second added observer
    Function<Integer,Integer> payload2 = t -> ++t;
    GenericOb<Integer,Integer> ob2 = new GenericOb<>(payload2);

    ob1.addObserver(ob2);

    Function<Integer,Integer> payload3 = t -> { System.out.println("Final value is: " + t); return t;};
    GenericOb<Integer, Integer> ob3 = new GenericOb<>(payload3);

    ob2.addObserver(ob3);

    // and finally somebody (the loop) that pushes the data because observers are actually push-based
    src.run();
  }

}