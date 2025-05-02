package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    private class Node {
        T t;
        Node nextNode;

        Node(T t){
            this.t = t;
        }
    }

    Node head;


    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element){
        Node newlyPushedNode = new Node(element);
        newlyPushedNode.nextNode = this.head;
        this.head = newlyPushedNode;
    }


    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop(){
        if (this.head == null) return null;

        T tToPop = this.head.t;
        this.head = this.head.nextNode;
        return tToPop;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek(){
        return (this.head == null) ? null: this.head.t;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size(){
        Node current = this.head;
        int size = 0;
        while(current != null){
            size++;
            current = current.nextNode;
        }
        return size;
    }
}



