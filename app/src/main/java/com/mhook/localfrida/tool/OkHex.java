package com.mhook.localfrida.tool;

import java.io.UnsupportedEncodingException;

public class OkHex {
    private byte[] mHex;
    private OkHex(String hexStr){
        mHex = OkHexUtil.toBytes(hexStr);
    }
    private OkHex(byte[] data){
        mHex =data;
    }
    public static OkHex on(){
        return new OkHex(new byte[0]);
    }
    public static OkHex on(byte data){
        return new OkHex(new byte[]{data});
    }
    public static OkHex on(int size){
        return new OkHex(new byte[size]);
    }

    public static OkHex on(String hexStr){
        return new OkHex(hexStr);
    }
    public static OkHex on(byte[] data){
        return new OkHex(data);
    }
    public static OkHex on(char[] data){
        byte newBytes[]=new byte[data.length];
        for (int i=0;i<data.length;i++){
            newBytes[i]=(byte)data[i];
        }
        return new OkHex(newBytes);
    }
    public int indexOf(String hexString){
        return indexOf(OkHexUtil.toBytes(hexString));
    }
    public int indexOf(byte[] subBytes){
        for(int i = 0; i< mHex.length; i++){
            byte onByte= mHex[i];
            if(onByte==subBytes[0]){
                boolean isEquals =true;
                for(int j=0;j<subBytes.length;j++){
                    if(mHex[i+j]!=subBytes[j]){
                        isEquals =false;
                        break;
                    }
                }
                if(isEquals)return i;
            }
        }
        return -1;
    }
    public OkHex subBytes(int index){
        return subBytes(index,mHex.length-index);
    }
    public OkHex subBytes(int index,int length){
        byte[] result=new byte[length];
        System.arraycopy(mHex,index,result,0,length);
        return OkHex.on(result);
    }
    public OkHex addBefore(String hexString){
        return addBefore(OkHexUtil.toBytes(hexString));
    }
    public OkHex addAfter(String hexString){
        return addAfter(OkHexUtil.toBytes(hexString));
    }
    public OkHex addBefore(byte[] data){
        byte[] newHex=new byte[mHex.length+data.length];
        System.arraycopy(data,0,newHex,0,data.length);
        System.arraycopy(mHex,0,newHex,data.length,mHex.length);
        mHex=newHex;
        return this;
    }
    public OkHex addAfter(byte[] data){
        byte[] newHex=new byte[mHex.length+data.length];
        System.arraycopy(mHex,0,newHex,0,mHex.length);
        System.arraycopy(data,0,newHex,mHex.length,data.length);
        mHex=newHex;
        return this;
    }
    public OkHex addBefore(OkHex enData) {
        return addBefore(enData.toBytes());
    }
    public OkHex addAfter(OkHex enData) {
        return addAfter(enData.toBytes());
    }
    public boolean equals(byte[] data) {
        if(data.length==mHex.length){
            for (int i=0;i<mHex.length;i++){
                if(mHex[i]!= data[i])return false;
            }
            return true;
        }
        return false;
    }
    public boolean equals(String hexString) {
        return equals(OkHexUtil.toBytes(hexString));
    }
    public int length(){
        return mHex.length;
    }
//    public byte[] replaceFirst(byte[] src,byte[] dst){
//        byte[] result=new byte[length];
//        System.arraycopy(mHex,index,result,0,length);
//        return result;
//    }
    public String toHexString(){
        return OkHexUtil.toHexString(mHex);
    }

    @Override
    public String toString() {
        return new String(mHex);
    }
    public String toString(String charset) throws UnsupportedEncodingException {
        return new String(mHex,charset);
    }
    public byte[] toBytes(){
        return mHex;
    }
    public OkHex printf(String tag){
        System.out.println(tag+" Length:"+mHex.length);
        System.out.println(tag+" Text:"+toString());
        System.out.println(tag+" Hex :"+toHexString());
        return this;
    }
    public void clear(){
        if (mHex!=null&&mHex.length>0){
            mHex=new byte[0];
        }
    }


    static class OkHexUtil{
        static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

        /**
         * Convert dddd byte array into its hex string equivalent.
         */
         static String toHexString(byte[] data) {
            char[] chars = new char[data.length * 2];
            for (int i = 0; i < data.length; i++) {
                chars[i * 2] = HEX_DIGITS[(data[i] >> 4) & 0xf];
                chars[i * 2 + 1] = HEX_DIGITS[data[i] & 0xf];
            }
            return new String(chars);
        }
         static byte[] toBytes(String hexString) {
             if (hexString == null || hexString.equals("")) {
                 return null;
             }
             hexString = hexString.toUpperCase();
             int length = hexString.length() / 2;
             char[] hexChars = hexString.toCharArray();
             byte[] d = new byte[length];
             for (int i = 0; i < length; i++) {
                 int pos = i * 2;
                 d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
             }
             return d;
         }
         /**
          * Convert char to byte
          *
          * @param c char
          * @return byte
          */
         private static byte charToByte(char c) {
             return (byte) "0123456789ABCDEF".indexOf(c);
         }
    }


}
