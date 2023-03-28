package com.example.chatbuddy;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class Word {
    private String word;
    private float wordSize;
    private Rect wordRect;
    private Paint wordPaint;
    private int yOffset = 0;
    private int wordCount = 1;

    public Word(String word, int wordCount, float wordSize, int wordColor) {
        this(word, wordCount, wordSize, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), wordColor, 255);
    }

    public Word(String word, int wordCount, float wordSize, int wordColor,  int wordColorAlpha) {
        this(word, wordCount, wordSize, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), wordColor, wordColorAlpha);
    }

    public Word(String word, int wordCount, float wordSize, Typeface wordTypeFace, int wordColor) {
        this(word, wordCount, wordSize, wordTypeFace, wordColor, 255);
    }

    public Word(String word, int wordCount, float wordSize, Typeface wordTypeFace, int wordColor, int wordColorAlpha) {
        this.word = word;
        this.wordCount = wordCount;
        this.wordSize = wordSize;

        // init Paint object
        wordPaint = new Paint();
        wordPaint.setAntiAlias(true);
        wordPaint.setColor(wordColor);
        wordPaint.setAlpha(wordColorAlpha);
        wordPaint.setTextAlign(Paint.Align.LEFT);
        wordPaint.setStyle(Paint.Style.FILL);
        wordPaint.setTypeface(wordTypeFace);
        wordPaint.setTextSize(this.wordSize);

        // calculate rect
        wordRect = new Rect();
        wordPaint.getTextBounds(word, 0, word.length(), wordRect);
        yOffset = Math.abs(wordRect.top);
        wordRect.offsetTo(0,0);
    }

    public String getWord() {
        return word;
    }

    public int getWordCount() {
        return wordCount;
    }

    public Paint getWordPaint() {
        return wordPaint;
    }

    public Rect getWordRect() {
        return wordRect;
    }

    public float getX() { // x pos for drawing into canvas
        return (float) wordRect.left;
    }

    public float getY() { // y pos for drawing into canvas
        return wordRect.top + yOffset;
    }

    public void changeTextSize(float newTextSize) {
        this.wordSize = newTextSize;
        this.wordPaint.setTextSize(this.wordSize);
        this.wordPaint.getTextBounds(word, 0, word.length(), wordRect);
        yOffset = Math.abs(wordRect.top);
        wordRect.offsetTo(0,0);
    }
}
