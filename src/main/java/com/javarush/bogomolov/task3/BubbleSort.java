package com.javarush.bogomolov.task3;

import java.util.Arrays;

public class BubbleSort {
    public static int[] bubbleSort(int[] arr){

        while(true){
            boolean isSorted = true;
            for (int i = 0; i < arr.length-1; i++) {
                if(arr[i] > arr[i+1]) {
                    int bubble = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = bubble;
                }
            }
            for (int i = 0; i < arr.length-1; i++) {
                if(arr[i] > arr[i+1]) {
                    isSorted = false;
                }
            }
            if(isSorted){break;}

        }
        return arr;
    }
}
