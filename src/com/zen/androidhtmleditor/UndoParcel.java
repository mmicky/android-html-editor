package com.zen.androidhtmleditor;


import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class UndoParcel implements Parcelable
{
    public static class TextChange {
        public int start;
        public CharSequence oldtext;
        public CharSequence newtext;
    };
    
    public static class OnUndoStatusChange {
        public void run(boolean canUndo, boolean canRedo)
        {
            
        }
    }
    
    private ArrayList<TextChange> mBuffer;
    private int mCurrentSize=0;
    public static final int MAX_SIZE=512*1024;
    
    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flag)
    {
        out.writeInt( mBuffer.size() );
        for( TextChange item : mBuffer ){
            out.writeInt(item.start);
            out.writeString(item.oldtext.toString());
            out.writeString(item.newtext.toString());
        }
    }
    
    public static final Parcelable.Creator<UndoParcel> CREATOR
            = new Parcelable.Creator<UndoParcel>() {
        public UndoParcel createFromParcel(Parcel in) {
            return new UndoParcel(in);
        }
        
        public UndoParcel[] newArray(int size) {
            return new UndoParcel[size];
        }
    };
    
    /**
     * createFromParcel初始化时必须要这个函数
     * @param in
     */
    private UndoParcel(Parcel in) {
        int len = in.readInt();
        while( in.dataAvail() > 0 && len-- > 0){
            TextChange item = new TextChange();
            item.start = in.readInt();
            item.oldtext = in.readString();
            item.newtext = in.readString();
            if ( item.newtext == null ){
                item.newtext = "";
            }
            if ( item.oldtext == null ){
                item.oldtext = "";
            }
            mBuffer.add(item);
            mCurrentSize += item.newtext.length() + item.oldtext.length();
        }
    }

    public UndoParcel() {
        mBuffer = new ArrayList<TextChange>();
        mCurrentSize = 0;
    }
    
    public TextChange pop()
    {
        int size = mBuffer.size();
        if ( size > 0 ){
            TextChange item = mBuffer.get(size-1);
            mBuffer.remove(size-1);
            int removed = item.newtext.length() + item.oldtext.length();
            mCurrentSize -= removed;
            return item;
        }else{
            return null;
        }
    }

    public boolean removeLast()
    {
        int size = mBuffer.size();
        if ( size > 0 ){
            TextChange item = mBuffer.get(0);
            mBuffer.remove(0);
            mCurrentSize -= item.newtext.length() + item.oldtext.length();
            return true;
        }else{
            return false;
        }
    }

    public void push( TextChange item )
    {
        if ( item.newtext == null ){
            item.newtext = "";
        }
        if ( item.oldtext == null ){
            item.oldtext = "";
        }
        int delta = item.newtext.length() + item.oldtext.length() ;
        if ( delta < MAX_SIZE ){
            mCurrentSize += delta;
            mBuffer.add(item);
            while( mCurrentSize > MAX_SIZE ){
                if ( !removeLast() ){
                    break;
                }
            }
        }else{
            removeAll();
        }
    }
    
    public void clean()
    {
        mBuffer.clear();
        mCurrentSize = 0;
    }

    public void removeAll()
    {
        mBuffer.removeAll(mBuffer);
        mCurrentSize = 0;
    }
    
    public boolean canUndo()
    {
        return mBuffer.size()>0;
    }

}