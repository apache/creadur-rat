/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.report.xml.writer.impl.base;

import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.InvalidXmlException;
import org.apache.rat.report.xml.writer.OperationNotAllowedException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Lightweight {@link IXmlWriter} implementation.</p>
 * <p>
 * Requires a wrapper to be used safely in a multithreaded
 * environment.</p>
 * <p>
 * Not intended to be subclassed. Please copy and hack!</p>
 */
public final class XmlWriter implements IXmlWriter {

    private static final byte NAME_START_MASK = 1 << 1;
    private static final byte NAME_MASK = 1 << 2;
    private static final byte NAME_BODY_CHAR = NAME_MASK;
    private static final byte NAME_START_OR_BODY_CHAR = NAME_MASK | NAME_START_MASK;

    private final static boolean[] ALLOWED_CHARACTERS = new boolean[1 << 16];

    static {
        Arrays.fill(ALLOWED_CHARACTERS, false);
        ALLOWED_CHARACTERS[0x9] = true;
        ALLOWED_CHARACTERS[0xA] = true;
        ALLOWED_CHARACTERS[0xD] = true;
        Arrays.fill(ALLOWED_CHARACTERS, 0x20, 0xD7FF, true);
        Arrays.fill(ALLOWED_CHARACTERS, 0xE000, 0xFFFD, true);
    }

    private final static byte[] CHARACTER_CODES = new byte[1 << 16];

    static {
        // Name ::= (Letter | '_' | ':') (NameChar)*
        CHARACTER_CODES['_'] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[':'] = NAME_START_OR_BODY_CHAR;
        // Letter ::= BaseChar | Ideographic
        // BaseChar
        Arrays.fill(CHARACTER_CODES, 0x0041, 0x005A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0061, 0x007A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x00C0, 0x00D6, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x00D8, 0x00F6, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x00F8, 0x00FF, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0100, 0x0131, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0134, 0x013E, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0141, 0x0148, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x014A, 0x017E, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0180, 0x01C3, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x01CD, 0x01F0, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x01F4, 0x01F5, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x01FA, 0x0217, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0250, 0x02A8, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x02BB, 0x02C1, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0386] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0388, 0x038A, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x038C] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x038E, 0x03A1, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x03A3, 0x03CE, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x03D0, 0x03D6, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x03DA] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x03DC] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x03DE] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x03E0] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x03E2, 0x03F3, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0401, 0x040C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x040E, 0x044F, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0451, 0x045C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x045E, 0x0481, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0490, 0x04C4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x04C7, 0x04C8, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x04CB, 0x04CC, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x04D0, 0x04EB, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x04EE, 0x04F5, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x04F8, 0x04F9, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0531, 0x0556, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0559] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0561, 0x0586, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x05D0, 0x05EA, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x05F0, 0x05F2, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0621, 0x063A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0641, 0x064A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0671, 0x06B7, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06BA, 0x06BE, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06C0, 0x06CE, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06D0, 0x06D3, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x06D5] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x06E5, 0x06E6, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0905, 0x0939, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x093D] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0958, 0x0961, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0985, 0x098C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x098F, 0x0990, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0993, 0x09A8, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09AA, 0x09B0, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x09B2] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x09B6, 0x09B9, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09DC, 0x09DD, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09DF, 0x09E1, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09F0, 0x09F1, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A05, 0x0A0A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A0F, 0x0A10, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A13, 0x0A28, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A2A, 0x0A30, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A32, 0x0A33, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A35, 0x0A36, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A38, 0x0A39, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A59, 0x0A5C, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0A5E] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0A72, 0x0A74, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A85, 0x0A8B, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0A8D] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0A8F, 0x0A91, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A93, 0x0AA8, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0AAA, 0x0AB0, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0AB2, 0x0AB3, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0AB5, 0x0AB9, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0ABD] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x0AE0] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0B05, 0x0B0C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B0F, 0x0B10, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B13, 0x0B28, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B2A, 0x0B30, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B32, 0x0B33, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B36, 0x0B39, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0B3D] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0B5C, 0x0B5D, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B5F, 0x0B61, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B85, 0x0B8A, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B8E, 0x0B90, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B92, 0x0B95, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B99, 0x0B9A, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0B9C] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0B9E, 0x0B9F, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BA3, 0x0BA4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BA8, 0x0BAA, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BAE, 0x0BB5, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BB7, 0x0BB9, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C05, 0x0C0C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C0E, 0x0C10, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C12, 0x0C28, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C2A, 0x0C33, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C35, 0x0C39, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C60, 0x0C61, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C85, 0x0C8C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C8E, 0x0C90, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C92, 0x0CA8, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CAA, 0x0CB3, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CB5, 0x0CB9, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0CDE] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0CE0, 0x0CE1, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D05, 0x0D0C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D0E, 0x0D10, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D12, 0x0D28, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D2A, 0x0D39, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D60, 0x0D61, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E01, 0x0E2E, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0E30] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0E32, 0x0E33, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E40, 0x0E45, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E81, 0x0E82, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0E84] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0E87, 0x0E88, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0E8A] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x0E8D] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0E94, 0x0E97, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E99, 0x0E9F, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0EA1, 0x0EA3, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0EA5] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x0EA7] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0EAA, 0x0EAB, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0EAD, 0x0EAE, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0EB0] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0EB2, 0x0EB3, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x0EBD] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0EC0, 0x0EC4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F40, 0x0F47, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F49, 0x0F69, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x10A0, 0x10C5, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x10D0, 0x10F6, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1100] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x1102, 0x1103, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1105, 0x1107, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1109] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x110B, 0x110C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x110E, 0x1112, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x113C] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x113E] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1140] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x114C] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x114E] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1150] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x1154, 0x1155, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1159] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x115F, 0x1161, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1163] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1165] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1167] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1169] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x116D, 0x116E, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1172, 0x1173, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1175] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x119E] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x11A8] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x11AB] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x11AE, 0x11AF, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x11B7, 0x11B8, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x11BA] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x11BC, 0x11C2, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x11EB] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x11F0] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x11F9] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x1E00, 0x1E9B, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1EA0, 0x1EF9, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F00, 0x1F15, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F18, 0x1F1D, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F20, 0x1F45, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F48, 0x1F4D, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F50, 0x1F57, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1F59] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1F5B] = NAME_START_OR_BODY_CHAR;
        CHARACTER_CODES[0x1F5D] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x1F5F, 0x1F7D, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1F80, 0x1FB4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FB6, 0x1FBC, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x1FBE] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x1FC2, 0x1FC4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FC6, 0x1FCC, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FD0, 0x1FD3, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FD6, 0x1FDB, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FE0, 0x1FEC, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FF2, 0x1FF4, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x1FF6, 0x1FFC, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x2126] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x212A, 0x212B, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x212E] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x2180, 0x2182, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x3041, 0x3094, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x30A1, 0x30FA, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x3105, 0x312C, NAME_START_OR_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0xAC00, 0xD7A3, NAME_START_OR_BODY_CHAR);
        // Ideographic
        Arrays.fill(CHARACTER_CODES, 0x4E00, 0x9FA5, NAME_START_OR_BODY_CHAR);
        CHARACTER_CODES[0x3007] = NAME_START_OR_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x3021, 0x3029, NAME_START_OR_BODY_CHAR);
        // NameChar ::= Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender
        CHARACTER_CODES['.'] = NAME_BODY_CHAR;
        CHARACTER_CODES['-'] = NAME_BODY_CHAR;
        // CombiningChar 
        Arrays.fill(CHARACTER_CODES, 0x0300, 0x0345, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0360, 0x0361, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0483, 0x0486, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0591, 0x05A1, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x05A3, 0x05B9, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x05BB, 0x05BD, NAME_BODY_CHAR);
        CHARACTER_CODES[0x05BF] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x05C1, 0x05C2, NAME_BODY_CHAR);
        CHARACTER_CODES[0x05C4] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x064B, 0x0652, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0670] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x06D6, 0x06DC, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06DD, 0x06DF, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06E0, 0x06E4, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06E7, 0x06E8, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06EA, 0x06ED, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0901, 0x0903, NAME_BODY_CHAR);
        CHARACTER_CODES[0x093C] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x093E, 0x094C, NAME_BODY_CHAR);
        CHARACTER_CODES[0x094D] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0951, 0x0954, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0962, 0x0963, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0981, 0x0983, NAME_BODY_CHAR);
        CHARACTER_CODES[0x09BC] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x09BE] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x09BF] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x09C0, 0x09C4, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09C7, 0x09C8, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09CB, 0x09CD, NAME_BODY_CHAR);
        CHARACTER_CODES[0x09D7] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x09E2, 0x09E3, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0A02] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0A3C] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0A3E] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0A3F] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0A40, 0x0A42, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A47, 0x0A48, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A4B, 0x0A4D, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A70, 0x0A71, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A81, 0x0A83, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0ABC] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0ABE, 0x0AC5, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0AC7, 0x0AC9, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0ACB, 0x0ACD, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B01, 0x0B03, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0B3C] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0B3E, 0x0B43, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B47, 0x0B48, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B4B, 0x0B4D, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B56, 0x0B57, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B82, 0x0B83, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BBE, 0x0BC2, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BC6, 0x0BC8, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BCA, 0x0BCD, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0BD7] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0C01, 0x0C03, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C3E, 0x0C44, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C46, 0x0C48, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C4A, 0x0C4D, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C55, 0x0C56, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C82, 0x0C83, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CBE, 0x0CC4, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CC6, 0x0CC8, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CCA, 0x0CCD, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CD5, 0x0CD6, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D02, 0x0D03, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D3E, 0x0D43, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D46, 0x0D48, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D4A, 0x0D4D, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0D57] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0E31] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0E34, 0x0E3A, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E47, 0x0E4E, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0EB1] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0EB4, 0x0EB9, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0EBB, 0x0EBC, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0EC8, 0x0ECD, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F18, 0x0F19, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0F35] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0F37] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0F39] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0F3E] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0F3F] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0F71, 0x0F84, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F86, 0x0F8B, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F90, 0x0F95, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0F97] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x0F99, 0x0FAD, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0FB1, 0x0FB7, NAME_BODY_CHAR);
        CHARACTER_CODES[0x0FB9] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x20D0, 0x20DC, NAME_BODY_CHAR);
        CHARACTER_CODES[0x20E1] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x302A, 0x302F, NAME_BODY_CHAR);
        CHARACTER_CODES[0x3099] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x309A] = NAME_BODY_CHAR;
        // Digit 
        Arrays.fill(CHARACTER_CODES, 0x0030, 0x0039, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0660, 0x0669, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x06F0, 0x06F9, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0966, 0x096F, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x09E6, 0x09EF, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0A66, 0x0A6F, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0AE6, 0x0AEF, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0B66, 0x0B6F, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0BE7, 0x0BEF, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0C66, 0x0C6F, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0CE6, 0x0CEF, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0D66, 0x0D6F, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0E50, 0x0E59, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0ED0, 0x0ED9, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x0F20, 0x0F29, NAME_BODY_CHAR);
        // Extender 
        CHARACTER_CODES[0x00B7] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x02D0] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x02D1] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0387] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0640] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0E46] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x0EC6] = NAME_BODY_CHAR;
        CHARACTER_CODES[0x3005] = NAME_BODY_CHAR;
        Arrays.fill(CHARACTER_CODES, 0x3031, 0x3035, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x309D, 0x309E, NAME_BODY_CHAR);
        Arrays.fill(CHARACTER_CODES, 0x30FC, 0x30FE, NAME_BODY_CHAR);

    }

    private final Writer writer;
    private final ArrayDeque elementNames;
    private final Set<CharSequence> currentAttributes = new HashSet<>();

    boolean elementsWritten = false;
    boolean inElement = false;
    boolean prologWritten = false;

    public XmlWriter(final Writer writer) {
        this.writer = writer;
        this.elementNames = new ArrayDeque<CharSequence>();
    }

    /**
     * Starts a document by writing a prolog.
     * Calling this method is optional.
     * When writing a document fragment, it should <em>not</em> be called.
     *
     * @return this object
     * @throws OperationNotAllowedException if called after the first element has been written
     *                                      or once a prolog has already been written
     */
    public IXmlWriter startDocument() throws IOException {
        if (elementsWritten) {
            throw new OperationNotAllowedException("Document already started");
        }
        if (prologWritten) {
            throw new OperationNotAllowedException("Only one prolog allowed");
        }
        writer.write("<?xml version='1.0'?>");
        prologWritten = true;
        return this;
    }

    /**
     * Writes the start of an element.
     *
     * @param elementName the name of the element, not null
     * @return this object
     * @throws InvalidXmlException          if the name is not valid for an xml element
     * @throws OperationNotAllowedException if called after the first element has been closed
     */
    public IXmlWriter openElement(final CharSequence elementName) throws IOException {
        if (elementsWritten && elementNames.isEmpty()) {
            throw new OperationNotAllowedException("Root element already closed. Cannot open new element.");
        }
        if (isInvalidName(elementName)) {
            throw new InvalidXmlException("'" + elementName + "' is not a valid element name");
        }
        elementsWritten = true;
        if (inElement) {
            writer.write('>');
        }
        writer.write('<');
        rawWrite(elementName);
        inElement = true;
        elementNames.push(elementName);
        currentAttributes.clear();
        return this;
    }

    /**
     * Writes an attribute of an element.
     * Note that this is only allowed directly after {@link #openElement(CharSequence)}
     * or {@link #attribute}.
     *
     * @param name  the attribute name, not null
     * @param value the attribute value, not null
     * @return this object
     * @throws InvalidXmlException          if the name is not valid for an xml attribute
     *                                      or if a value for the attribute has already been written
     * @throws OperationNotAllowedException if called after {@link #content(CharSequence)}
     *                                      or {@link #closeElement()} or before any call to {@link #openElement(CharSequence)}
     */
    public IXmlWriter attribute(CharSequence name, CharSequence value) throws IOException {
        if (elementNames.isEmpty()) {
            if (elementsWritten) {
                throw new OperationNotAllowedException("Root element has already been closed.");
            } else {
                throw new OperationNotAllowedException("Close called before an element has been opened.");
            }
        }
        if (isInvalidName(name)) {
            throw new InvalidXmlException("'" + name + "' is not a valid attribute name.");
        }
        if (!inElement) {
            throw new InvalidXmlException("Attributes can only be written in elements");
        }
        if (currentAttributes.contains(name)) {
            throw new InvalidXmlException("Each attribute can only be written once");
        }
        writer.write(' ');
        rawWrite(name);
        writer.write('=');
        writer.write('\'');
        writeAttributeContent(value);
        writer.write('\'');
        currentAttributes.add(name);
        return this;
    }

    private void writeAttributeContent(CharSequence content) throws IOException {
        writeEscaped(content, true);
    }

    /**
     * Writes content.
     * Calling this method will automatically
     * Note that this method does not use CDATA.
     *
     * @param content the content to write
     * @return this object
     * @throws OperationNotAllowedException if called before any call to {@link #openElement}
     *                                      or after the first element has been closed
     */
    public IXmlWriter content(CharSequence content) throws IOException {
        if (elementNames.isEmpty()) {
            if (elementsWritten) {
                throw new OperationNotAllowedException("Root element has already been closed.");
            } else {
                throw new OperationNotAllowedException("An element must be opened before content can be written.");
            }
        }
        if (inElement) {
            writer.write('>');
        }
        writeBodyContent(content);
        inElement = false;
        return this;
    }

    private void writeBodyContent(final CharSequence content) throws IOException {
        writeEscaped(content, false);
    }

    private void writeEscaped(final CharSequence content, boolean isAttributeContent) throws IOException {
        final int length = content.length();
        for (int i = 0; i < length; i++) {
            char character = content.charAt(i);
            if (character == '&') {
                writer.write("&amp;");
            } else if (character == '<') {
                writer.write("&lt;");
            } else if (character == '>') {
                writer.write("&gt;");
            } else if (isAttributeContent && character == '\'') {
                writer.write("&apos;");
            } else if (isAttributeContent && character == '\"') {
                writer.write("&quot;");
            } else if (isOutOfRange(character)) {
                writer.write('?');
            } else {
                writer.write(character);
            }
        }
    }

    private boolean isOutOfRange(final char character) {
        return !ALLOWED_CHARACTERS[character];
    }

    /**
     * Closes the last element written.
     *
     * @return this object
     * @throws OperationNotAllowedException if called before any call to {@link #openElement}
     *                                      or after the first element has been closed
     */
    public IXmlWriter closeElement() throws IOException {
        if (elementNames.isEmpty()) {
            if (elementsWritten) {
                throw new OperationNotAllowedException("Root element has already been closed.");
            } else {
                throw new OperationNotAllowedException("Close called before an element has been opened.");
            }
        }
        final CharSequence elementName = (CharSequence) elementNames.pop();
        if (inElement) {
            writer.write('/');
            writer.write('>');
        } else {
            writer.write('<');
            writer.write('/');
            rawWrite(elementName);
            writer.write('>');
        }
        writer.flush();
        inElement = false;
        return this;
    }


    /**
     * Closes all pending elements.
     * When appropriate, resources are also flushed and closed.
     * No exception is raised when called upon a document whose
     * root element has already been closed.
     *
     * @return this object
     * @throws OperationNotAllowedException if called before any call to {@link #openElement}
     */
    public IXmlWriter closeDocument() throws IOException {
        if (elementNames.isEmpty() && !elementsWritten) {
            throw new OperationNotAllowedException("Close called before an element has been opened.");
        }
        while (!elementNames.isEmpty()) {
            closeElement();
        }
        writer.flush();
        return this;
    }

    private void rawWrite(final CharSequence sequence) throws IOException {
        for (int i = 0; i < sequence.length(); i++) {
            final char charAt = sequence.charAt(i);
            writer.write(charAt);
        }
    }

    private boolean isInvalidName(final CharSequence sequence) {
        boolean result = true;
        final int length = sequence.length();
        for (int i = 0; i < length; i++) {
            char character = sequence.charAt(i);
            if (i == 0) {
                if (!isValidNameStart(character)) {
                    result = false;
                    break;
                }
            } else {
                if (!isValidNameBody(character)) {
                    result = false;
                    break;
                }
            }
        }
        return !result;
    }

    private boolean isValidNameStart(final char character) {
        final byte code = CHARACTER_CODES[character];
        return (code & NAME_START_MASK) > 0;
    }

    private boolean isValidNameBody(final char character) {
        final byte code = CHARACTER_CODES[character];
        return (code & NAME_MASK) > 0;
    }
}
