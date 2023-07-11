package dev.slne.shop.api.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ByteProcessor;

public class AttributeBuffer extends ByteBuf {

    private final ByteBuf buf;

    /**
     * @return
     * @see io.netty.util.ReferenceCounted#refCnt()
     */
    @Override
    public int refCnt() {
        return buf.refCnt();
    }

    /**
     * @return
     * @see io.netty.util.ReferenceCounted#release()
     */
    @Override
    public boolean release() {
        return buf.release();
    }

    /**
     * @param decrement
     * @return
     * @see io.netty.util.ReferenceCounted#release(int)
     */
    @Override
    public boolean release(int decrement) {
        return buf.release(decrement);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#capacity()
     */
    @Override
    public int capacity() {
        return buf.capacity();
    }

    /**
     * @param newCapacity
     * @return
     * @see io.netty.buffer.ByteBuf#capacity(int)
     */
    @Override
    public ByteBuf capacity(int newCapacity) {
        return buf.capacity(newCapacity);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#maxCapacity()
     */
    @Override
    public int maxCapacity() {
        return buf.maxCapacity();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#alloc()
     */
    @Override
    public ByteBufAllocator alloc() {
        return buf.alloc();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#order()
     * @deprecated
     */
    @Deprecated
    @Override
    public ByteOrder order() {
        return buf.order();
    }

    /**
     * @param endianness
     * @return
     * @see io.netty.buffer.ByteBuf#order(java.nio.ByteOrder)
     * @deprecated
     */
    @Deprecated
    @Override
    public ByteBuf order(ByteOrder endianness) {
        return buf.order(endianness);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#unwrap()
     */
    @Override
    public ByteBuf unwrap() {
        return buf.unwrap();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#isDirect()
     */
    @Override
    public boolean isDirect() {
        return buf.isDirect();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#asReadOnly()
     */
    @Override
    public ByteBuf asReadOnly() {
        return buf.asReadOnly();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readerIndex()
     */
    @Override
    public int readerIndex() {
        return buf.readerIndex();
    }

    /**
     * @param readerIndex
     * @return
     * @see io.netty.buffer.ByteBuf#readerIndex(int)
     */
    @Override
    public ByteBuf readerIndex(int readerIndex) {
        return buf.readerIndex(readerIndex);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#writerIndex()
     */
    @Override
    public int writerIndex() {
        return buf.writerIndex();
    }

    /**
     * @param writerIndex
     * @return
     * @see io.netty.buffer.ByteBuf#writerIndex(int)
     */
    @Override
    public ByteBuf writerIndex(int writerIndex) {
        return buf.writerIndex(writerIndex);
    }

    /**
     * @param readerIndex
     * @param writerIndex
     * @return
     * @see io.netty.buffer.ByteBuf#setIndex(int, int)
     */
    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return buf.setIndex(readerIndex, writerIndex);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readableBytes()
     */
    @Override
    public int readableBytes() {
        return buf.readableBytes();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#writableBytes()
     */
    @Override
    public int writableBytes() {
        return buf.writableBytes();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#maxWritableBytes()
     */
    @Override
    public int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#maxFastWritableBytes()
     */
    @Override
    public int maxFastWritableBytes() {
        return buf.maxFastWritableBytes();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#isReadable()
     */
    @Override
    public boolean isReadable() {
        return buf.isReadable();
    }

    /**
     * @param size
     * @return
     * @see io.netty.buffer.ByteBuf#isReadable(int)
     */
    @Override
    public boolean isReadable(int size) {
        return buf.isReadable(size);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#isWritable()
     */
    @Override
    public boolean isWritable() {
        return buf.isWritable();
    }

    /**
     * @param size
     * @return
     * @see io.netty.buffer.ByteBuf#isWritable(int)
     */
    @Override
    public boolean isWritable(int size) {
        return buf.isWritable(size);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#clear()
     */
    @Override
    public ByteBuf clear() {
        return buf.clear();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#markReaderIndex()
     */
    @Override
    public ByteBuf markReaderIndex() {
        return buf.markReaderIndex();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#resetReaderIndex()
     */
    @Override
    public ByteBuf resetReaderIndex() {
        return buf.resetReaderIndex();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#markWriterIndex()
     */
    @Override
    public ByteBuf markWriterIndex() {
        return buf.markWriterIndex();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#resetWriterIndex()
     */
    @Override
    public ByteBuf resetWriterIndex() {
        return buf.resetWriterIndex();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#discardReadBytes()
     */
    @Override
    public ByteBuf discardReadBytes() {
        return buf.discardReadBytes();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#discardSomeReadBytes()
     */
    @Override
    public ByteBuf discardSomeReadBytes() {
        return buf.discardSomeReadBytes();
    }

    /**
     * @param minWritableBytes
     * @return
     * @see io.netty.buffer.ByteBuf#ensureWritable(int)
     */
    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        return buf.ensureWritable(minWritableBytes);
    }

    /**
     * @param minWritableBytes
     * @param force
     * @return
     * @see io.netty.buffer.ByteBuf#ensureWritable(int, boolean)
     */
    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return buf.ensureWritable(minWritableBytes, force);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getBoolean(int)
     */
    @Override
    public boolean getBoolean(int index) {
        return buf.getBoolean(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getByte(int)
     */
    @Override
    public byte getByte(int index) {
        return buf.getByte(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedByte(int)
     */
    @Override
    public short getUnsignedByte(int index) {
        return buf.getUnsignedByte(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getShort(int)
     */
    @Override
    public short getShort(int index) {
        return buf.getShort(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getShortLE(int)
     */
    @Override
    public short getShortLE(int index) {
        return buf.getShortLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedShort(int)
     */
    @Override
    public int getUnsignedShort(int index) {
        return buf.getUnsignedShort(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedShortLE(int)
     */
    @Override
    public int getUnsignedShortLE(int index) {
        return buf.getUnsignedShortLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getMedium(int)
     */
    @Override
    public int getMedium(int index) {
        return buf.getMedium(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getMediumLE(int)
     */
    @Override
    public int getMediumLE(int index) {
        return buf.getMediumLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedMedium(int)
     */
    @Override
    public int getUnsignedMedium(int index) {
        return buf.getUnsignedMedium(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedMediumLE(int)
     */
    @Override
    public int getUnsignedMediumLE(int index) {
        return buf.getUnsignedMediumLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getInt(int)
     */
    @Override
    public int getInt(int index) {
        return buf.getInt(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getIntLE(int)
     */
    @Override
    public int getIntLE(int index) {
        return buf.getIntLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedInt(int)
     */
    @Override
    public long getUnsignedInt(int index) {
        return buf.getUnsignedInt(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getUnsignedIntLE(int)
     */
    @Override
    public long getUnsignedIntLE(int index) {
        return buf.getUnsignedIntLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getLong(int)
     */
    @Override
    public long getLong(int index) {
        return buf.getLong(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getLongLE(int)
     */
    @Override
    public long getLongLE(int index) {
        return buf.getLongLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getChar(int)
     */
    @Override
    public char getChar(int index) {
        return buf.getChar(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getFloat(int)
     */
    @Override
    public float getFloat(int index) {
        return buf.getFloat(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getFloatLE(int)
     */
    @Override
    public float getFloatLE(int index) {
        return buf.getFloatLE(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getDouble(int)
     */
    @Override
    public double getDouble(int index) {
        return buf.getDouble(index);
    }

    /**
     * @param index
     * @return
     * @see io.netty.buffer.ByteBuf#getDoubleLE(int)
     */
    @Override
    public double getDoubleLE(int index) {
        return buf.getDoubleLE(index);
    }

    /**
     * @param index
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, io.netty.buffer.ByteBuf)
     */
    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return buf.getBytes(index, dst);
    }

    /**
     * @param index
     * @param dst
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, io.netty.buffer.ByteBuf, int)
     */
    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return buf.getBytes(index, dst, length);
    }

    /**
     * @param index
     * @param dst
     * @param dstIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, io.netty.buffer.ByteBuf, int,
     *      int)
     */
    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return buf.getBytes(index, dst, dstIndex, length);
    }

    /**
     * @param index
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, byte[])
     */
    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return buf.getBytes(index, dst);
    }

    /**
     * @param index
     * @param dst
     * @param dstIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, byte[], int, int)
     */
    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return buf.getBytes(index, dst, dstIndex, length);
    }

    /**
     * @param index
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#getBytes(int, java.nio.ByteBuffer)
     */
    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return buf.getBytes(index, dst);
    }

    /**
     * @param index
     * @param out
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#getBytes(int, java.io.OutputStream, int)
     */
    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    /**
     * @param index
     * @param out
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#getBytes(int,
     *      java.nio.channels.GatheringByteChannel,
     *      int)
     */
    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return buf.getBytes(index, out, length);
    }

    /**
     * @param index
     * @param out
     * @param position
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#getBytes(int, java.nio.channels.FileChannel,
     *      long, int)
     */
    @Override
    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return buf.getBytes(index, out, position, length);
    }

    /**
     * @param index
     * @param length
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#getCharSequence(int, int,
     *      java.nio.charset.Charset)
     */
    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return buf.getCharSequence(index, length, charset);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setBoolean(int, boolean)
     */
    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return buf.setBoolean(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setByte(int, int)
     */
    @Override
    public ByteBuf setByte(int index, int value) {
        return buf.setByte(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setShort(int, int)
     */
    @Override
    public ByteBuf setShort(int index, int value) {
        return buf.setShort(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setShortLE(int, int)
     */
    @Override
    public ByteBuf setShortLE(int index, int value) {
        return buf.setShortLE(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setMedium(int, int)
     */
    @Override
    public ByteBuf setMedium(int index, int value) {
        return buf.setMedium(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setMediumLE(int, int)
     */
    @Override
    public ByteBuf setMediumLE(int index, int value) {
        return buf.setMediumLE(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setInt(int, int)
     */
    @Override
    public ByteBuf setInt(int index, int value) {
        return buf.setInt(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setIntLE(int, int)
     */
    @Override
    public ByteBuf setIntLE(int index, int value) {
        return buf.setIntLE(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setLong(int, long)
     */
    @Override
    public ByteBuf setLong(int index, long value) {
        return buf.setLong(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setLongLE(int, long)
     */
    @Override
    public ByteBuf setLongLE(int index, long value) {
        return buf.setLongLE(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setChar(int, int)
     */
    @Override
    public ByteBuf setChar(int index, int value) {
        return buf.setChar(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setFloat(int, float)
     */
    @Override
    public ByteBuf setFloat(int index, float value) {
        return buf.setFloat(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setFloatLE(int, float)
     */
    @Override
    public ByteBuf setFloatLE(int index, float value) {
        return buf.setFloatLE(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setDouble(int, double)
     */
    @Override
    public ByteBuf setDouble(int index, double value) {
        return buf.setDouble(index, value);
    }

    /**
     * @param index
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#setDoubleLE(int, double)
     */
    @Override
    public ByteBuf setDoubleLE(int index, double value) {
        return buf.setDoubleLE(index, value);
    }

    /**
     * @param index
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, io.netty.buffer.ByteBuf)
     */
    @Override
    public ByteBuf setBytes(int index, ByteBuf src) {
        return buf.setBytes(index, src);
    }

    /**
     * @param index
     * @param src
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, io.netty.buffer.ByteBuf, int)
     */
    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return buf.setBytes(index, src, length);
    }

    /**
     * @param index
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, io.netty.buffer.ByteBuf, int,
     *      int)
     */
    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return buf.setBytes(index, src, srcIndex, length);
    }

    /**
     * @param index
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, byte[])
     */
    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return buf.setBytes(index, src);
    }

    /**
     * @param index
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, byte[], int, int)
     */
    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return buf.setBytes(index, src, srcIndex, length);
    }

    /**
     * @param index
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#setBytes(int, java.nio.ByteBuffer)
     */
    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return buf.setBytes(index, src);
    }

    /**
     * @param index
     * @param in
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#setBytes(int, java.io.InputStream, int)
     */
    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    /**
     * @param index
     * @param in
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#setBytes(int,
     *      java.nio.channels.ScatteringByteChannel,
     *      int)
     */
    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return buf.setBytes(index, in, length);
    }

    /**
     * @param index
     * @param in
     * @param position
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#setBytes(int, java.nio.channels.FileChannel,
     *      long, int)
     */
    @Override
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return buf.setBytes(index, in, position, length);
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#setZero(int, int)
     */
    @Override
    public ByteBuf setZero(int index, int length) {
        return buf.setZero(index, length);
    }

    /**
     * @param index
     * @param sequence
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#setCharSequence(int, java.lang.CharSequence,
     *      java.nio.charset.Charset)
     */
    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return buf.setCharSequence(index, sequence, charset);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readBoolean()
     */
    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readByte()
     */
    @Override
    public byte readByte() {
        return buf.readByte();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedByte()
     */
    @Override
    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readShort()
     */
    @Override
    public short readShort() {
        return buf.readShort();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readShortLE()
     */
    @Override
    public short readShortLE() {
        return buf.readShortLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedShort()
     */
    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedShortLE()
     */
    @Override
    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readMedium()
     */
    @Override
    public int readMedium() {
        return buf.readMedium();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readMediumLE()
     */
    @Override
    public int readMediumLE() {
        return buf.readMediumLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedMedium()
     */
    @Override
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedMediumLE()
     */
    @Override
    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readInt()
     */
    @Override
    public int readInt() {
        return buf.readInt();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readIntLE()
     */
    @Override
    public int readIntLE() {
        return buf.readIntLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedInt()
     */
    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readUnsignedIntLE()
     */
    @Override
    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readLong()
     */
    @Override
    public long readLong() {
        return buf.readLong();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readLongLE()
     */
    @Override
    public long readLongLE() {
        return buf.readLongLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readChar()
     */
    @Override
    public char readChar() {
        return buf.readChar();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readFloat()
     */
    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readFloatLE()
     */
    @Override
    public float readFloatLE() {
        return buf.readFloatLE();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readDouble()
     */
    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#readDoubleLE()
     */
    @Override
    public double readDoubleLE() {
        return buf.readDoubleLE();
    }

    /**
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(int)
     */
    @Override
    public ByteBuf readBytes(int length) {
        return buf.readBytes(length);
    }

    /**
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readSlice(int)
     */
    @Override
    public ByteBuf readSlice(int length) {
        return buf.readSlice(length);
    }

    /**
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readRetainedSlice(int)
     */
    @Override
    public ByteBuf readRetainedSlice(int length) {
        return buf.readRetainedSlice(length);
    }

    /**
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        return buf.readBytes(dst);
    }

    /**
     * @param dst
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(io.netty.buffer.ByteBuf, int)
     */
    @Override
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return buf.readBytes(dst, length);
    }

    /**
     * @param dst
     * @param dstIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(io.netty.buffer.ByteBuf, int, int)
     */
    @Override
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return buf.readBytes(dst, dstIndex, length);
    }

    /**
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(byte[])
     */
    @Override
    public ByteBuf readBytes(byte[] dst) {
        return buf.readBytes(dst);
    }

    /**
     * @param dst
     * @param dstIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(byte[], int, int)
     */
    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return buf.readBytes(dst, dstIndex, length);
    }

    /**
     * @param dst
     * @return
     * @see io.netty.buffer.ByteBuf#readBytes(java.nio.ByteBuffer)
     */
    @Override
    public ByteBuf readBytes(ByteBuffer dst) {
        return buf.readBytes(dst);
    }

    /**
     * @param out
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#readBytes(java.io.OutputStream, int)
     */
    @Override
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    /**
     * @param out
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#readBytes(java.nio.channels.GatheringByteChannel,
     *      int)
     */
    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return buf.readBytes(out, length);
    }

    /**
     * @param length
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#readCharSequence(int, java.nio.charset.Charset)
     */
    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return buf.readCharSequence(length, charset);
    }

    /**
     * @param out
     * @param position
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#readBytes(java.nio.channels.FileChannel,
     *      long, int)
     */
    @Override
    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return buf.readBytes(out, position, length);
    }

    /**
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#skipBytes(int)
     */
    @Override
    public ByteBuf skipBytes(int length) {
        return buf.skipBytes(length);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeBoolean(boolean)
     */
    @Override
    public ByteBuf writeBoolean(boolean value) {
        return buf.writeBoolean(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeByte(int)
     */
    @Override
    public ByteBuf writeByte(int value) {
        return buf.writeByte(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeShort(int)
     */
    @Override
    public ByteBuf writeShort(int value) {
        return buf.writeShort(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeShortLE(int)
     */
    @Override
    public ByteBuf writeShortLE(int value) {
        return buf.writeShortLE(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeMedium(int)
     */
    @Override
    public ByteBuf writeMedium(int value) {
        return buf.writeMedium(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeMediumLE(int)
     */
    @Override
    public ByteBuf writeMediumLE(int value) {
        return buf.writeMediumLE(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeInt(int)
     */
    @Override
    public ByteBuf writeInt(int value) {
        return buf.writeInt(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeIntLE(int)
     */
    @Override
    public ByteBuf writeIntLE(int value) {
        return buf.writeIntLE(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeLong(long)
     */
    @Override
    public ByteBuf writeLong(long value) {
        return buf.writeLong(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeLongLE(long)
     */
    @Override
    public ByteBuf writeLongLE(long value) {
        return buf.writeLongLE(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeChar(int)
     */
    @Override
    public ByteBuf writeChar(int value) {
        return buf.writeChar(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeFloat(float)
     */
    @Override
    public ByteBuf writeFloat(float value) {
        return buf.writeFloat(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeFloatLE(float)
     */
    @Override
    public ByteBuf writeFloatLE(float value) {
        return buf.writeFloatLE(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeDouble(double)
     */
    @Override
    public ByteBuf writeDouble(double value) {
        return buf.writeDouble(value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#writeDoubleLE(double)
     */
    @Override
    public ByteBuf writeDoubleLE(double value) {
        return buf.writeDoubleLE(value);
    }

    /**
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        return buf.writeBytes(src);
    }

    /**
     * @param src
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(io.netty.buffer.ByteBuf, int)
     */
    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return buf.writeBytes(src, length);
    }

    /**
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(io.netty.buffer.ByteBuf, int,
     *      int)
     */
    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return buf.writeBytes(src, srcIndex, length);
    }

    /**
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(byte[])
     */
    @Override
    public ByteBuf writeBytes(byte[] src) {
        return buf.writeBytes(src);
    }

    /**
     * @param src
     * @param srcIndex
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(byte[], int, int)
     */
    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return buf.writeBytes(src, srcIndex, length);
    }

    /**
     * @param src
     * @return
     * @see io.netty.buffer.ByteBuf#writeBytes(java.nio.ByteBuffer)
     */
    @Override
    public ByteBuf writeBytes(ByteBuffer src) {
        return buf.writeBytes(src);
    }

    /**
     * @param in
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#writeBytes(java.io.InputStream, int)
     */
    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    /**
     * @param in
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#writeBytes(java.nio.channels.ScatteringByteChannel,
     *      int)
     */
    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return buf.writeBytes(in, length);
    }

    /**
     * @param in
     * @param position
     * @param length
     * @return
     * @throws IOException
     * @see io.netty.buffer.ByteBuf#writeBytes(java.nio.channels.FileChannel,
     *      long, int)
     */
    @Override
    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return buf.writeBytes(in, position, length);
    }

    /**
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#writeZero(int)
     */
    @Override
    public ByteBuf writeZero(int length) {
        return buf.writeZero(length);
    }

    /**
     * @param sequence
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#writeCharSequence(java.lang.CharSequence,
     *      java.nio.charset.Charset)
     */
    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return buf.writeCharSequence(sequence, charset);
    }

    /**
     * @param fromIndex
     * @param toIndex
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#indexOf(int, int, byte)
     */
    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return buf.indexOf(fromIndex, toIndex, value);
    }

    /**
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#bytesBefore(byte)
     */
    @Override
    public int bytesBefore(byte value) {
        return buf.bytesBefore(value);
    }

    /**
     * @param length
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#bytesBefore(int, byte)
     */
    @Override
    public int bytesBefore(int length, byte value) {
        return buf.bytesBefore(length, value);
    }

    /**
     * @param index
     * @param length
     * @param value
     * @return
     * @see io.netty.buffer.ByteBuf#bytesBefore(int, int, byte)
     */
    @Override
    public int bytesBefore(int index, int length, byte value) {
        return buf.bytesBefore(index, length, value);
    }

    /**
     * @param processor
     * @return
     * @see io.netty.buffer.ByteBuf#forEachByte(io.netty.util.ByteProcessor)
     */
    @Override
    public int forEachByte(ByteProcessor processor) {
        return buf.forEachByte(processor);
    }

    /**
     * @param index
     * @param length
     * @param processor
     * @return
     * @see io.netty.buffer.ByteBuf#forEachByte(int, int,
     *      io.netty.util.ByteProcessor)
     */
    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        return buf.forEachByte(index, length, processor);
    }

    /**
     * @param processor
     * @return
     * @see io.netty.buffer.ByteBuf#forEachByteDesc(io.netty.util.ByteProcessor)
     */
    @Override
    public int forEachByteDesc(ByteProcessor processor) {
        return buf.forEachByteDesc(processor);
    }

    /**
     * @param index
     * @param length
     * @param processor
     * @return
     * @see io.netty.buffer.ByteBuf#forEachByteDesc(int, int,
     *      io.netty.util.ByteProcessor)
     */
    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return buf.forEachByteDesc(index, length, processor);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#copy()
     */
    @Override
    public ByteBuf copy() {
        return buf.copy();
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#copy(int, int)
     */
    @Override
    public ByteBuf copy(int index, int length) {
        return buf.copy(index, length);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#slice()
     */
    @Override
    public ByteBuf slice() {
        return buf.slice();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#retainedSlice()
     */
    @Override
    public ByteBuf retainedSlice() {
        return buf.retainedSlice();
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#slice(int, int)
     */
    @Override
    public ByteBuf slice(int index, int length) {
        return buf.slice(index, length);
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#retainedSlice(int, int)
     */
    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return buf.retainedSlice(index, length);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#duplicate()
     */
    @Override
    public ByteBuf duplicate() {
        return buf.duplicate();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#retainedDuplicate()
     */
    @Override
    public ByteBuf retainedDuplicate() {
        return buf.retainedDuplicate();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#nioBufferCount()
     */
    @Override
    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#nioBuffer()
     */
    @Override
    public ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#nioBuffer(int, int)
     */
    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return buf.nioBuffer(index, length);
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#internalNioBuffer(int, int)
     */
    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return buf.internalNioBuffer(index, length);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#nioBuffers()
     */
    @Override
    public ByteBuffer[] nioBuffers() {
        return buf.nioBuffers();
    }

    /**
     * @param index
     * @param length
     * @return
     * @see io.netty.buffer.ByteBuf#nioBuffers(int, int)
     */
    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return buf.nioBuffers(index, length);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#hasArray()
     */
    @Override
    public boolean hasArray() {
        return buf.hasArray();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#array()
     */
    @Override
    public byte[] array() {
        return buf.array();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#arrayOffset()
     */
    @Override
    public int arrayOffset() {
        return buf.arrayOffset();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#hasMemoryAddress()
     */
    @Override
    public boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#memoryAddress()
     */
    @Override
    public long memoryAddress() {
        return buf.memoryAddress();
    }

    /**
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#toString(java.nio.charset.Charset)
     */
    @Override
    public String toString(Charset charset) {
        return buf.toString(charset);
    }

    /**
     * @param index
     * @param length
     * @param charset
     * @return
     * @see io.netty.buffer.ByteBuf#toString(int, int, java.nio.charset.Charset)
     */
    @Override
    public String toString(int index, int length, Charset charset) {
        return buf.toString(index, length, charset);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#hashCode()
     */
    @Override
    public int hashCode() {
        return buf.hashCode();
    }

    /**
     * @param obj
     * @return
     * @see io.netty.buffer.ByteBuf#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return buf.equals(obj);
    }

    /**
     * @param buffer
     * @return
     * @see io.netty.buffer.ByteBuf#compareTo(io.netty.buffer.ByteBuf)
     */
    @Override
    public int compareTo(ByteBuf buffer) {
        return buf.compareTo(buffer);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#toString()
     */
    @Override
    public String toString() {
        return buf.toString();
    }

    /**
     * @param increment
     * @return
     * @see io.netty.buffer.ByteBuf#retain(int)
     */
    @Override
    public ByteBuf retain(int increment) {
        return buf.retain(increment);
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#retain()
     */
    @Override
    public ByteBuf retain() {
        return buf.retain();
    }

    /**
     * @return
     * @see io.netty.buffer.ByteBuf#touch()
     */
    @Override
    public ByteBuf touch() {
        return buf.touch();
    }

    /**
     * @param hint
     * @return
     * @see io.netty.buffer.ByteBuf#touch(java.lang.Object)
     */
    @Override
    public ByteBuf touch(Object hint) {
        return buf.touch(hint);
    }

    /**
     * Create a new AttributeBuffer
     */
    public AttributeBuffer() {
        this(Unpooled.buffer());
    }

    /**
     * Create a new AttributeBuffer
     *
     * @param input the input to read from
     */
    public AttributeBuffer(byte[] input) {
        this(Unpooled.wrappedBuffer(input));
    }

    /**
     * Create a new AttributeBuffer
     *
     * @param out the output to write to
     */
    public AttributeBuffer(ByteBuf out) {
        this.buf = out;
    }

    /**
     * Reads a uuid.
     *
     * @return the uuid
     */
    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    /**
     * Writes a uuid.
     *
     * @param uuid the uuid
     */
    public void writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads a compressed int from the buffer. To do so it maximally reads 5
     * byte-sized chunks whose most significant bit dictates whether another
     * byte should be read.
     */
    @SuppressWarnings("java:S112")
    public int readVarIntFromBuffer() {
        int i = 0;
        int j = 0;

        while (true) {
            byte b = this.readByte();
            i |= (b & 127) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }

            if ((b & 128) != 128) {
                break;
            }
        }

        return i;
    }

    /**
     * Writes a compressed int to the buffer. The smallest number of bytes to
     * fit the passed int will be written. Of each such byte only 7 bits will be
     * used to describe the actual value since its most significant bit dictates
     * whether the next byte is part of that same int. Micro-optimization for
     * int values that are expected to have values below 128.
     */
    public void writeVarIntToBuffer(int input) {
        while ((input & -128) != 0) {
            this.writeByte(input & 127 | 128);
            input >>>= 7;
        }

        this.writeByte(input);
    }

    /**
     * Calculates the number of bytes required to fit the supplied int (0-5) if
     * it were to be read/written using readVarIntFromBuffer or
     * writeVarIntToBuffer
     */
    public static int getVarIntSize(int input) {
        for (int i = 1; i < 5; ++i) {
            if ((input & -1 << i * 7) == 0) {
                return i;
            }
        }

        return 5;
    }

    /**
     * Reads a string from this buffer. Expected parameter is maximum allowed
     * string length. Will throw IOException if string length exceeds this
     * value!
     *
     * @throws Exception
     */
    public String readStringFromBuffer(int maxLength) {
        int i = this.readVarIntFromBuffer();

        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i
                    + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            byte[] bytes = new byte[i];
            this.readBytes(bytes);
            String str = new String(bytes, StandardCharsets.UTF_8);

            if (str.length() > maxLength) {
                throw new DecoderException(
                        "The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
            } else {
                return str;
            }
        }
    }

    public void writeString(String string) {
        byte[] abyte = string.getBytes(StandardCharsets.UTF_8);

        if (abyte.length > 32767) {
            throw new DecoderException("String too big (was " + string.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            this.writeVarIntToBuffer(abyte.length);
            this.writeBytes(abyte);
        }
    }

    public String readStringOrNull(int maxLength) {
        if (readBoolean()) {
            return readStringFromBuffer(maxLength);
        }
        return null;
    }

    public void writeStringOrNull(String string) {
        writeBoolean(string != null);
        if (string != null) {
            writeString(string);
        }
    }

    public byte[] asArray() {
        byte[] bytes = new byte[this.readableBytes()];
        getBytes(readerIndex(), bytes);
        return bytes;
    }
}
