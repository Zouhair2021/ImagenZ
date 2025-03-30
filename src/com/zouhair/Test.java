package com.zouhair;

public class Test
{
    public static int calculerSomme(int n)
    {
        int somme = 0;
        for (int i = n - 1; i >= 0; i--)
        {
            somme += i;
        }
        return somme;
    }


    public static void main(String[] args)
    {
        System.out.println(calculerSomme(3));

    }
}