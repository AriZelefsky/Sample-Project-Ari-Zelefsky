package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;
public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    //Fields inherited from MinHeap class

    public MinHeapImpl(){
        elements = (E[]) new Comparable[2];
    }

    /*
    public void printHeap(){ //For debugging only
        System.out.println("Starting Print:");
        for (E elem : elements){
            System.out.println("\t" + elem);
        }
    }
     */

    @Override
    public void reHeapify(E element){
        int index = getArrayIndex(element);

        downHeap(index); //If an element's last use time is made most recent, downHeap will bring it down to bottom of heap
        upHeap(index); //If an element's last use time is set to Long.MINVALUE to artificially make it the min of the heap, upheap will bring it all the way up
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
        throw new NoSuchElementException("Element not found in heap");
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
