package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest {
    private class MutableItem implements Comparable<MutableItem> {
        int number;

        @Override
        public int compareTo(MutableItem other){
            return this.number - other.number; //Sorts in ascending order of number
        }

        public MutableItem(int initialNumber){
            this.number = initialNumber;
        }

        public void setNumber(int newNumber){
            this.number = newNumber;
        }
    }



    MinHeap<Integer> intHeap = new MinHeapImpl<>();
    MinHeap<String> stringHeap = new MinHeapImpl<>();
    MinHeap<MutableItem> mutableItemHeap = new MinHeapImpl<>();


    @BeforeEach
    void beforeEach(){

    }

/** These TEST METHODs ONLY WORKS IF MAKE getArrayIndex public temporarily
    @Test
    void getArrayIndexTest(){
        intHeap.insert(7);
        intHeap.insert(1);
        intHeap.insert(3);
        intHeap.insert(5);
        intHeap.insert(4);
        intHeap.insert(2);
        intHeap.insert(6); //Inserted 1 through seven. Order should now be /1427536

        assertEquals(intHeap.getArrayIndex(1), 1);
        assertEquals(intHeap.getArrayIndex(4), 2);
        assertEquals(intHeap.getArrayIndex(2), 3);
        assertEquals(intHeap.getArrayIndex(7), 4);
    }


    @Test
    void reheapifyTest(){
        MutableItem one = new MutableItem(1);
        MutableItem two = new MutableItem(2);
        MutableItem three = new MutableItem(3);
        MutableItem four = new MutableItem(4);
        MutableItem five = new MutableItem(5);
        MutableItem six = new MutableItem(6);
        MutableItem seven = new MutableItem(7);
        mutableItemHeap.insert(seven);
        mutableItemHeap.insert(one);
        mutableItemHeap.insert(three);
        mutableItemHeap.insert(five);
        mutableItemHeap.insert(four);
        mutableItemHeap.insert(two);
        mutableItemHeap.insert(six); //Order should currently be /1427536



        assertEquals(1, mutableItemHeap.getArrayIndex(one));
        assertEquals(2, mutableItemHeap.getArrayIndex(four));
        assertEquals(3, mutableItemHeap.getArrayIndex(two));
        assertEquals(4, mutableItemHeap.getArrayIndex(seven));

        two.setNumber(50);
        mutableItemHeap.reHeapify(two);
        seven.setNumber(1);
        mutableItemHeap.reHeapify(seven);
        //Not testing

        assertEquals(6, mutableItemHeap.getArrayIndex(two));
        assertEquals(2, mutableItemHeap.getArrayIndex(seven));
        assertEquals(1, mutableItemHeap.getArrayIndex(one));
        assertEquals(4, mutableItemHeap.getArrayIndex(four));
        assertEquals(5, mutableItemHeap.getArrayIndex(five));
        assertEquals(7, mutableItemHeap.getArrayIndex(six));

    }

 */

}
