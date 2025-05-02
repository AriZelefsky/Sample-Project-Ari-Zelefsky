package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;
public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    //Fields are inherited. See MinHeap.java

    public MinHeapImpl(){
        elements = (E[]) new Comparable[2];
    }

    /*
    public void printHeap(){ //For additional testing only
        System.out.println("Starting Print:");
        for (E elem : elements){
            System.out.println("\t" + elem);
        }
    }
     */


    @Override
    public void reHeapify(E element){
        int index = getArrayIndex(element);
        //if(index == -1) throw new NoSuchElementException("Cannot reheapify that which is not in heap"); -1 no lnoger used. getarray index calls the exception itself

        //Downheap and Upheap methods each first check if given elem is in need of being moved at all
        //assuming that the only element in the heap that has been altered is this one,
        // it will either need upheaping downheaping or neither. Never both.
        downHeap(index); //If something's last use time is made most recent, downHeap will bring it down to bottom of heap
        upHeap(index); //If manually set last use time to Long.MINVALUE to artificially bring it to min, upheap will bring it all the way up
    }

    @Override
    protected int getArrayIndex(E element){
        for (int i = 1; i <= count; i++) {
            if (elements[i].equals(element)) {
                return i;
            }
        }
        System.out.println(element);
        for (E elem : elements){
            System.out.println(elem);
        }
        throw new NoSuchElementException(); //element not found in heap
    }

    @Override
    protected void doubleArraySize(){
        @SuppressWarnings("unchecked")
        E[] temp = (E[]) new Comparable[2*elements.length];
        for (int i = 1; i < elements.length; i++) {
            temp[i] = elements[i];
        }
        elements = temp;
    }




}
