/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.zfoo.protocol.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author godotg
 */
public class ByteBufUtilsTest {

    @Test
    public void byteTest() {
        byte[] values = new byte[]{Byte.MIN_VALUE, -100, -2, -1, 0, 1, 2, 100, Byte.MAX_VALUE};
        ByteBuf byteBuf = Unpooled.buffer();
        for (byte value : values) {
            ByteBufUtils.writeByte(byteBuf, value);
            byte result = ByteBufUtils.readByte(byteBuf);
            Assert.assertEquals(result, value);
        }
        for (byte value : values) {
            ByteBufUtils.writeByteBox(byteBuf, value);
            byte result = ByteBufUtils.readByteBox(byteBuf);
            Assert.assertEquals(result, value);
        }
    }

    @Test
    public void shortTest() {
        short[] values = new short[]{Short.MIN_VALUE, -100, -2, -1, 0, 1, 2, 100, Short.MAX_VALUE};
        ByteBuf byteBuf = Unpooled.buffer();
        for (short value : values) {
            ByteBufUtils.writeShort(byteBuf, value);
            short result = ByteBufUtils.readShort(byteBuf);
            Assert.assertEquals(result, value);
        }

        for (short value : values) {
            ByteBufUtils.writeShortBox(byteBuf, value);
            short result = ByteBufUtils.readShortBox(byteBuf);
            Assert.assertEquals(result, value);
        }
    }

    @Test
    public void intTest() {
        int[] values = new int[]{Integer.MIN_VALUE, -99999999, -9999, -100, -2, -1, 0, 1, 2, 100, 9999, 99999999, Integer.MAX_VALUE};
        ByteBuf byteBuf = Unpooled.buffer();
        for (int value : values) {
            ByteBufUtils.writeInt(byteBuf, value);
            int result = ByteBufUtils.readInt(byteBuf);
            Assert.assertEquals(result, value);
        }

        for (int value : values) {
            ByteBufUtils.writeIntBox(byteBuf, value);
            int result = ByteBufUtils.readIntBox(byteBuf);
            Assert.assertEquals(result, value);
        }
    }

    @Test
    public void longTest() {
        long[] values = new long[]{Long.MIN_VALUE, -9999999999999999L, -9999999999999999L, -99999999, -9999, -100, -2, -1
                , 0, 1, 2, 100, 9999, 99999999, 9999999999999999L, Long.MAX_VALUE};
        ByteBuf byteBuf = Unpooled.buffer();
        for (long value : values) {
            ByteBufUtils.writeLong(byteBuf, value);
            long result = ByteBufUtils.readLong(byteBuf);
            Assert.assertEquals(result, value);
        }

        for (long value : values) {
            ByteBufUtils.writeLongBox(byteBuf, value);
            long result = ByteBufUtils.readLongBox(byteBuf);
            Assert.assertEquals(result, value);
        }
    }

    @Test
    public void stringTest() {
        String str = "hello";
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBufUtils.writeString(byteBuf, str);
        String result = ByteBufUtils.readString(byteBuf);
        Assert.assertEquals(result, str);
    }

    @Test
    public void adjustPaddingEqualTest() {
        var byteBuf = Unpooled.buffer();
        var beforeWriteIndex = byteBuf.writerIndex();
        var predictionLength = 1000;

        // padding等于0的情况
        ByteBufUtils.writeInt(byteBuf, predictionLength);
        for (int i = 0; i < predictionLength; i++) {
            ByteBufUtils.writeByte(byteBuf, (byte) 1);
        }

        byteBuf.markReaderIndex();
        var bytes1 = ByteBufUtils.readAllBytes(byteBuf);
        byteBuf.resetReaderIndex();

        ByteBufUtils.adjustPadding(byteBuf, predictionLength, beforeWriteIndex);

        byteBuf.markReaderIndex();
        var bytes2 = ByteBufUtils.readAllBytes(byteBuf);
        byteBuf.resetReaderIndex();

        Assert.assertArrayEquals(bytes1, bytes2);
    }

    @Test
    public void adjustPaddingLessTest() {
        var byteBuf = Unpooled.buffer();
        var beforeWriteIndex = byteBuf.writerIndex();
        var predictionLength = 1000;
        var length = predictionLength / 100;

        // padding等于0的情况
        ByteBufUtils.writeInt(byteBuf, predictionLength);
        for (int i = 0; i < length; i++) {
            ByteBufUtils.writeByte(byteBuf, (byte) 1);
        }


        var byteBuf1 = Unpooled.buffer();
        ByteBufUtils.writeInt(byteBuf1, length);
        for (int i = 0; i < length; i++) {
            ByteBufUtils.writeByte(byteBuf1, (byte) 1);
        }
        var bytes1 = ByteBufUtils.readAllBytes(byteBuf1);

        ByteBufUtils.adjustPadding(byteBuf, predictionLength, beforeWriteIndex);

        byteBuf.markReaderIndex();
        var bytes2 = ByteBufUtils.readAllBytes(byteBuf);
        byteBuf.resetReaderIndex();

        Assert.assertArrayEquals(bytes1, bytes2);
    }

    @Test
    public void adjustPaddingMoreTest() {
        var byteBuf = Unpooled.buffer();
        var beforeWriteIndex = byteBuf.writerIndex();
        var predictionLength = 1000;
        var length = predictionLength * 10;

        // padding等于0的情况
        ByteBufUtils.writeInt(byteBuf, predictionLength);
        for (int i = 0; i < length; i++) {
            ByteBufUtils.writeByte(byteBuf, (byte) 1);
        }


        var byteBuf1 = Unpooled.buffer();
        ByteBufUtils.writeInt(byteBuf1, length);
        for (int i = 0; i < length; i++) {
            ByteBufUtils.writeByte(byteBuf1, (byte) 1);
        }
        var bytes1 = ByteBufUtils.readAllBytes(byteBuf1);

        ByteBufUtils.adjustPadding(byteBuf, predictionLength, beforeWriteIndex);

        byteBuf.markReaderIndex();
        var bytes2 = ByteBufUtils.readAllBytes(byteBuf);
        byteBuf.resetReaderIndex();

        Assert.assertArrayEquals(bytes1, bytes2);
    }
}
