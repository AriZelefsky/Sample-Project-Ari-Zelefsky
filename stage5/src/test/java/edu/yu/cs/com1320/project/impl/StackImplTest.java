package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.undo.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class StackImplTest {
    Stack commandStack = new StackImpl<Command>();
    Stack intStack = new StackImpl<Integer>();
    Stack stringStack = new StackImpl<String>();
    URI uri = URI.create("https://something");
    Consumer<URI> cmr = uri1 -> {};
    Command cmd = new Command(uri, cmr);


    @Test
    void pushTest(){
        assertEquals(0, commandStack.size());
        assertEquals(0, intStack.size());

        commandStack.push(cmd);
        intStack.push(45);

        assertEquals(1, commandStack.size());
        assertEquals(1, intStack.size());
    }

    @Test
    void popTest(){
        commandStack.push(cmd);
        intStack.push(779);

        assertEquals(1, commandStack.size());
        assertEquals(1, intStack.size());

        //Ensure proper return value
        assertEquals(cmd, commandStack.pop());
        assertEquals(779, intStack.pop());

        //Ensure size is reduced
        assertEquals(0, commandStack.size());
        assertEquals(0, intStack.size());

        //Handling case with many pushes and pops
        intStack.push(779);
        intStack.push(1);
        intStack.push(1); //should have 2 ones on the stack
        intStack.push(2);
        intStack.push(3);
        assertEquals(3, intStack.pop());
        assertEquals(2, intStack.pop());
        assertEquals(1, intStack.pop());
        assertEquals(1, intStack.pop());
        intStack.push(4);
        assertEquals(4, intStack.pop());
        assertEquals(779, intStack.pop());

        //Should return null if pop an empty stack
        assertNull(intStack.pop());
    }

    @Test
    void peekTest(){
        intStack.push(1);
        assertEquals(1, intStack.peek()); //peek post push
        intStack.push(2);
        assertEquals(2, intStack.peek()); //peek post push
        intStack.push(3);
        assertEquals(3, intStack.pop());
        assertEquals(2, intStack.peek()); //peek post pop
        assertEquals(2, intStack.pop());
        assertEquals(1, intStack.peek()); //peek post pop
        intStack.pop();

        //Null when peek at empty stack
        assertNull(intStack.peek());
    }

    @Test
    void sizeTest(){
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        commandStack.push(cmd);
        assertEquals(8, commandStack.size());

        stringStack.push("a");
        stringStack.push("bb");
        stringStack.pop();
        stringStack.push("ccc");
        stringStack.push("dddd");
        stringStack.pop();
        stringStack.push("eee");
        stringStack.push("ff");

        assertEquals(4, stringStack.size());
    }


}
