/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UpdateBalance;

import java.util.*;

/**
 *
 * @author Omar.AlFar
 */
public class RoundRobin<T>  {  
  
    private LinkedList<T> _current;  
      
      
    /** 
     * do not create the terminating elements 
     */  
    public RoundRobin() {  
        _current = new LinkedList<T>();  
          
  
    }  
      
    /** 
     * enqueues the specified object in the round-robin queue. 
     * @param value the object to add to the queue 
     */  
    public synchronized void enqueue(T value) {  
          
        _current.addLast(value);  
          
    }  
      
    /** 
     * @return the next object in the round robin queue 
     */  
    public synchronized T next() {  
        T ret = _current.removeFirst();  
        _current.addLast(ret);  
        return ret;  
    }  
      
    /** 
     * Removes the next occurrence of the specified object 
     * @param o the object to remove from the queue.  
     */  
    public synchronized void remove (Object o) {  
        _current.remove(o);  
    }  
      
    /** 
     * Removes all occurrences of the given object in the list. 
     * @param o the object to remove. 
     */  
    public synchronized void removeAllOccurences(Object o) {  
        Iterator iterator = _current.iterator();  
        while(iterator.hasNext())  
            if (iterator.next().equals(o))  
                iterator.remove();  
              
    }  
      
    public synchronized int size() {  
        return _current.size();  
    }  
      
    public synchronized void clear() {  
        _current.clear();  
    }  
          
}
