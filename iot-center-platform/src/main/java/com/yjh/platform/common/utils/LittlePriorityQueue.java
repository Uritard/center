
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yjh.platform.common.utils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 小顶堆实现，摘抄自 org.apache.solr.util
 *
 * @author Chenfei
 * @date 2023/1/18
 * @since [产品/模块版本] （可选）
 */
public class LittlePriorityQueue<T> {
    protected int size;             // number of elements currently in the queue
    protected int currentCapacity;  // number of elements the queue can hold w/o expanding
    protected int maxSize;          // max number of elements allowed in the queue
    protected T[] heap;
    protected final T sentinel;   // represents a null return value

    private final Comparator<? super T> comparator;

    public LittlePriorityQueue(int initialSize, int maxSize, T sentinel) {
        this(initialSize, maxSize, sentinel, null);
    }

    public LittlePriorityQueue(int initialSize, int maxSize, T sentinel, Comparator<? super T> comparator) {
        this.maxSize = maxSize;
        this.sentinel = sentinel;
        this.comparator = comparator;
        initialize(initialSize);
    }

    protected void initialize(int sz) {
        int heapSize;
        if (0 == sz){
            // We allocate 1 extra to avoid if statement in top()
            heapSize = 2;
        } else {
            // NOTE: we add +1 because all access to heap is
            // 1-based not 0-based.  heap[0] is unused.
            heapSize = Math.max(sz, sz + 1); // handle overflow，处理溢出问题
        }
        heap = (T[])new Object[heapSize];
        currentCapacity = sz;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void resize(int sz) {
        int heapSize;
        if (sz > maxSize) {
            maxSize = sz;
        }
        if (0 == sz){
            // We allocate 1 extra to avoid if statement in top()
            heapSize = 2;
        } else {
            heapSize = Math.max(sz, sz + 1); // handle overflow
        }
        heap = Arrays.copyOf(heap, heapSize);
        currentCapacity = sz;
    }
    /**
     * Adds an object to a PriorityQueue in log(size) time.
     * It returns the smallest object (if any) that was
     * dropped off the heap减少 because it was full, or
     * the sentinel value.
     *
     *  This can be
     * the given parameter (in case it is smaller than the
     * full heap's minimum, and couldn't be added), or another
     * object that was previously the smallest value in the
     * heap and now has been replaced by a larger one, or null
     * if the queue wasn't yet full with maxSize elements.
     */
    public T insertWithOverflow(T element) {
        if (element == null) {
            return sentinel;
        }
        if (size < maxSize) {
            add(element);
            return sentinel;
        } else if (compareLittle(heap[1], element)) {//越大的数往里插入
            T ret = heap[1];
            heap[1] = element;
            updateTop();
            return ret;
        } else {
            return element;
        }
    }

    /** inserts the element and returns true if this element caused another element
     * to be dropped from the queue. */
    public boolean insert(T element) {
        if (element == null) {
            return false;
        }
        if (size < maxSize) {
            add(element);
            return false;
        } else if (compareLittle(heap[1], element)) {
            // long ret = heap[1];
            heap[1] = element;
            updateTop();
            return true;
        } else {
            return false;
        }
    }

    /** Removes and returns the least element of the PriorityQueue in log(size)
     time.  Only valid if size() > 0.
     */
    public T pop() {
        T result = heap[1];            // save first value
        heap[1] = heap[size];            // move last to first
        size--;
        downHeap();          // adjust heap
        return result;
    }

    /**
     * Should be called when the Object at top changes values.
     * @return the new 'top' element.
     */
    public T updateTop() {
        downHeap();
        return heap[1];
    }

    /** Returns the number of elements currently stored in the PriorityQueue. */
    public int size() {
        return size;
    }

    /** Returns the array used to hold the heap, with the smallest item at array[1]
     *  and the last (but not necessarily largest) at array[size()].  This is *not*
     *  fully sorted.
     */
    public T[] getInternalArray() {
        return heap;
    }

    /** Pops the smallest n items from the heap, placing them in the internal array at
     *  arr[size] through arr[size-(n-1)] with the smallest (first element popped)
     *  being at arr[size].  The internal array is returned.
     */
    public T[] sort(int n) {
        while (--n >= 0) {
            T result = heap[1];            // save first value
            heap[1] = heap[size];            // move last to first
            heap[size] = result;                  // place it last
            size--;
            downHeap();          // adjust heap
        }
        return heap;
    }

    /** Removes all entries from the PriorityQueue. */
    public void clear() {
        size = 0;
    }

    private void upHeap() {
        int i = size;
        T node = heap[i];        // save bottom node
        int j = i >>> 1;
        while (j > 0 && compareLittle(node, heap[j])) {
            heap[i] = heap[j];        // shift parents down
            i = j;
            j = j >>> 1;
        }
        heap[i] = node;          // install saved node
    }

    private void downHeap() {
        int i = 1;
        T node = heap[i];        // save top node
        int j = i << 1;          // find smaller child
        int k = j + 1;
        if (k <= size && compareLittle(heap[k], heap[j])) {
            j = k;
        }
        while (j <= size && compareLittle(heap[j], node)) {
            heap[i] = heap[j];        // shift up child
            i = j;
            j = i << 1;
            k = j + 1;
            if (k <= size && compareLittle(heap[k], heap[j])) {
                j = k;
            }
        }
        heap[i] = node;          // install saved node
    }

    private boolean compareLittle(T o1, T o2){
        Comparator<? super T> cpr = comparator;
        int cmp;
        if (cpr != null) {
            cmp = cpr.compare(o1, o2);
        } else {
            if (o1 == null) {
                throw new NullPointerException();
            }
            Comparable<? super T> k = (Comparable<? super T>) o1;
            cmp = k.compareTo(o2);
        }
        return cmp < 0;
    }

    /**
     * Adds an object to a PriorityQueue in log(size) time. If one tries to add
     * more objects than maxSize from initialize an
     * {@link ArrayIndexOutOfBoundsException} is thrown.
     * @return the new 'top' element in the queue.
     */
    private T add(T element) {
        if (size >= currentCapacity) {
            int newSize = Math.min(currentCapacity <<1, maxSize);
            if (newSize < currentCapacity) {
                newSize = Integer.MAX_VALUE;  // handle overflow
            }
            resize(newSize);
        }
        size++;
        heap[size] = element;//将插入元素插入在末尾，然后调整堆
        upHeap();
        return heap[1];
    }

    /**
     * Adds an object to a PriorityQueue in log(size) time. If one tries to add
     * more objects than the current capacity, an
     * {@link ArrayIndexOutOfBoundsException} is thrown.
     */
    private void addNoCheck(T element) {
        ++size;
        heap[size] = element;
        upHeap();
    }
    /** Returns the least element of the PriorityQueue in constant time. */
    public T top() {
        return heap[1];
    }

}
