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
package org.apache.rat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.rat.document.CompositeDocumentException;
import org.apache.rat.utils.Log;

/**
 * The representation of a document being scanned.
 */
public interface Document {
    /**
     * An enumeraton of document types.
     */
    enum Type {
        /** A generated document. */
        GENERATED,
        /** An unknown document type. */
        UNKNOWN,
        /** An archive type document. */
        ARCHIVE,
        /** A notice document (e.g. LICENSE file) */
        NOTICE,
        /** A binary file */
        BINARY,
        /** A standard document */
        STANDARD;

        public static Map<String, Type> documentTypeMap;
        
        public static Type fromContentType(String documentType, Log log) {
            Type result = documentTypeMap.get(documentType);
            if (result == null) {
                log.warn(String.format("Please open a Jira ticket with the subject: 'Unknown media type %s in Document.Type'", documentType));
                return UNKNOWN;
            }
            return result;
        }

        /*
         * https://tika.apache.org/3.0.0-BETA/formats.html 
         */
        static {
            documentTypeMap = new HashMap<>();
//        org.apache.tika.parser.apple.AppleSingleFileParser       
            documentTypeMap.put("application/applefile", Type.BINARY);
            documentTypeMap.put("application/x-plist", Type.BINARY);
            documentTypeMap.put("application/x-bplist-itunes", Type.BINARY);
            documentTypeMap.put("application/x-bplist", Type.BINARY);
            documentTypeMap.put("application/x-bplist-memgraph", Type.BINARY);
            documentTypeMap.put("application/x-bplist-webarchive", Type.BINARY);
//        org.apache.tika.parser.asm.ClassParser
            documentTypeMap.put("application/java-vm", Type.BINARY);
//        org.apache.tika.parser.audio.AudioParser
            documentTypeMap.put("audio/vnd.wave", Type.BINARY);
            documentTypeMap.put("audio/x-wav", Type.BINARY);
            documentTypeMap.put("audio/basic", Type.BINARY);
            documentTypeMap.put("audio/x-aiff", Type.BINARY);
//        org.apache.tika.parser.audio.MidiParser
            documentTypeMap.put("application/x-midi", Type.BINARY);
            documentTypeMap.put("audio/midi", Type.BINARY);
//        org.apache.tika.parser.code.SourceCodeParser
            documentTypeMap.put("text/x-c++src", Type.STANDARD);
            documentTypeMap.put("text/x-groovy", Type.STANDARD);
            documentTypeMap.put("text/x-java-source", Type.STANDARD);
//        org.apache.tika.parser.crypto.Pkcs7Parser
            documentTypeMap.put("application/pkcs7-signature", Type.BINARY);
            documentTypeMap.put("application/pkcs7-mime", Type.BINARY);
//        org.apache.tika.parser.crypto.TSDParser
            documentTypeMap.put("application/timestamped-data", Type.BINARY);
//        org.apache.tika.parser.csv.TextAndCSVParser
            documentTypeMap.put("text/csv", Type.STANDARD);
            documentTypeMap.put("text/tsv", Type.STANDARD);
            documentTypeMap.put("text/plain", Type.STANDARD);
//        org.apache.tika.parser.dbf.DBFParser
            documentTypeMap.put("application/x-dbf", Type.BINARY);
//        org.apache.tika.parser.dgn.DGN8Parser
            documentTypeMap.put("image/vnd.dgn; version=8", Type.BINARY);
//        org.apache.tika.parser.dif.DIFParser
            documentTypeMap.put("application/dif+xml", Type.BINARY);
//        org.apache.tika.parser.dwg.DWGParser
            documentTypeMap.put("image/vnd.dwg", Type.BINARY);
//        org.apache.tika.parser.epub.EpubParser
            documentTypeMap.put("application/x-ibooks+zip", Type.BINARY);
            documentTypeMap.put("application/epub+zip", Type.BINARY);
//        org.apache.tika.parser.executable.ExecutableParser
            documentTypeMap.put("application/x-msdownload", Type.BINARY);
            documentTypeMap.put("application/x-sharedlib", Type.BINARY);
            documentTypeMap.put("application/x-elf", Type.BINARY);
            documentTypeMap.put("application/x-object", Type.BINARY);
            documentTypeMap.put("application/x-executable", Type.BINARY);
            documentTypeMap.put("application/x-coredump", Type.BINARY);
//        org.apache.tika.parser.external.ExternalParser
            documentTypeMap.put("video/avi", Type.BINARY);
            documentTypeMap.put("video/mpeg", Type.BINARY);
            documentTypeMap.put("video/x-msvideo", Type.BINARY);
            documentTypeMap.put("video/mp4", Type.BINARY);
//        org.apache.tika.parser.feed.FeedParser
            documentTypeMap.put("application/atom+xml", Type.STANDARD);
            documentTypeMap.put("application/rss+xml", Type.STANDARD);
//        org.apache.tika.parser.font.AdobeFontMetricParser
            documentTypeMap.put("application/x-font-adobe-metric", Type.BINARY);
//        org.apache.tika.parser.font.TrueTypeParser
            documentTypeMap.put("application/x-font-ttf", Type.BINARY);
//        org.apache.tika.parser.html.JSoupParser
            documentTypeMap.put("text/html", Type.STANDARD);
            documentTypeMap.put("application/vnd.wap.xhtml+xml", Type.STANDARD);
            documentTypeMap.put("application/x-asp", Type.STANDARD);
            documentTypeMap.put("application/xhtml+xml", Type.STANDARD);
//        org.apache.tika.parser.http.HttpParser
            documentTypeMap.put("application/x-httpresponse", Type.BINARY);
//        org.apache.tika.parser.hwp.HwpV5Parser
            documentTypeMap.put("application/x-hwp-v5", Type.BINARY);
//        org.apache.tika.parser.image.BPGParser
            documentTypeMap.put("image/bpg", Type.BINARY);
            documentTypeMap.put("image/x-bpg", Type.BINARY);
//        org.apache.tika.parser.image.HeifParser
            documentTypeMap.put("image/heic-sequence", Type.BINARY);
            documentTypeMap.put("image/heif", Type.BINARY);
            documentTypeMap.put("image/heic", Type.BINARY);
            documentTypeMap.put("image/heif-sequence", Type.BINARY);
//        org.apache.tika.parser.image.ICNSParser
            documentTypeMap.put("image/icns", Type.BINARY);
//        org.apache.tika.parser.image.ImageParser
            documentTypeMap.put("image/png", Type.BINARY);
            documentTypeMap.put("image/vnd.wap.wbmp", Type.BINARY);
            documentTypeMap.put("image/x-jbig2", Type.BINARY);
            documentTypeMap.put("image/bmp", Type.BINARY);
            documentTypeMap.put("image/x-xcf", Type.BINARY);
            documentTypeMap.put("image/gif", Type.BINARY);
            documentTypeMap.put("image/x-icon", Type.BINARY);
            documentTypeMap.put("image/x-ms-bmp", Type.BINARY);
//        org.apache.tika.parser.image.JXLParser
            documentTypeMap.put("image/jxl", Type.BINARY);
//        org.apache.tika.parser.image.JpegParser
            documentTypeMap.put("image/jpeg", Type.BINARY);
//        org.apache.tika.parser.image.PSDParser
            documentTypeMap.put("image/vnd.adobe.photoshop", Type.BINARY);
//        org.apache.tika.parser.image.TiffParser
            documentTypeMap.put("image/tiff", Type.BINARY);
//        org.apache.tika.parser.image.WebPParser
            documentTypeMap.put("image/webp", Type.BINARY);
//        org.apache.tika.parser.indesign.IDMLParser
            documentTypeMap.put("application/vnd.adobe.indesign-idml-package", Type.BINARY);
//        org.apache.tika.parser.iptc.IptcAnpaParser
            documentTypeMap.put("text/vnd.iptc.anpa", Type.BINARY);
//        org.apache.tika.parser.iwork.IWorkPackageParser
            documentTypeMap.put("application/vnd.apple.keynote", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.iwork", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.numbers", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.pages", Type.BINARY);
//        org.apache.tika.parser.iwork.iwana.IWork13PackageParser
            documentTypeMap.put("application/vnd.apple.numbers.13", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.unknown.13", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.pages.13", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.keynote.13", Type.BINARY);
//        org.apache.tika.parser.iwork.iwana.IWork18PackageParser
            documentTypeMap.put("application/vnd.apple.pages.18", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.keynote.18", Type.BINARY);
            documentTypeMap.put("application/vnd.apple.numbers.18", Type.BINARY);
//        org.apache.tika.parser.mail.RFC822Parser
            documentTypeMap.put("message/rfc822", Type.BINARY);
//        org.apache.tika.parser.mat.MatParser
            documentTypeMap.put("application/x-matlab-data", Type.BINARY);
//        org.apache.tika.parser.mbox.MboxParser
            documentTypeMap.put("application/mbox", Type.BINARY);
//        org.apache.tika.parser.microsoft.EMFParser
            documentTypeMap.put("image/emf", Type.BINARY);
//        org.apache.tika.parser.microsoft.JackcessParser
            documentTypeMap.put("application/x-msaccess", Type.BINARY);
//        org.apache.tika.parser.microsoft.MSOwnerFileParser
            documentTypeMap.put("application/x-ms-owner", Type.BINARY);
//        org.apache.tika.parser.microsoft.OfficeParser
            documentTypeMap.put("application/x-tika-msoffice-embedded; format=ole10_native", Type.BINARY);
            documentTypeMap.put("application/msword", Type.BINARY);
            documentTypeMap.put("application/vnd.visio", Type.BINARY);
            documentTypeMap.put("application/x-tika-ole-drm-encrypted", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-project", Type.BINARY);
            documentTypeMap.put("application/x-tika-msworks-spreadsheet", Type.BINARY);
            documentTypeMap.put("application/x-mspublisher", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-powerpoint", Type.BINARY);
            documentTypeMap.put("application/x-tika-msoffice", Type.BINARY);
            documentTypeMap.put("application/sldworks", Type.BINARY);
            documentTypeMap.put("application/x-tika-ooxml-protected", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-outlook", Type.BINARY);
//        org.apache.tika.parser.microsoft.OldExcelParser
            documentTypeMap.put("application/vnd.ms-excel.workspace.3", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.workspace.4", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.sheet.2", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.sheet.3", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.sheet.4", Type.BINARY);
//        org.apache.tika.parser.microsoft.TNEFParser
            documentTypeMap.put("application/vnd.ms-tnef", Type.BINARY);
            documentTypeMap.put("application/x-tnef", Type.BINARY);
            documentTypeMap.put("application/ms-tnef", Type.BINARY);
//        org.apache.tika.parser.microsoft.WMFParser
            documentTypeMap.put("image/wmf", Type.BINARY);
//        org.apache.tika.parser.microsoft.activemime.ActiveMimeParser
            documentTypeMap.put("application/x-activemime", Type.BINARY);
//        org.apache.tika.parser.microsoft.chm.ChmParser
            documentTypeMap.put("application/vnd.ms-htmlhelp", Type.BINARY);
            documentTypeMap.put("application/x-chm", Type.BINARY);
            documentTypeMap.put("application/chm", Type.BINARY);
//        org.apache.tika.parser.microsoft.onenote.OneNoteParser
            documentTypeMap.put("application/onenote; format=one", Type.BINARY);
//        org.apache.tika.parser.microsoft.ooxml.OOXMLParser
            documentTypeMap.put("application/vnd.ms-powerpoint.template.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.addin.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.sheet.binary.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-powerpoint.slide.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.drawing", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-powerpoint.slideshow.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-powerpoint.presentation.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml.slide", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.sheet.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-word.template.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-word.document.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-powerpoint.addin.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-xpsdocument", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.drawing.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.template.macroenabled.12", Type.BINARY);
            documentTypeMap.put("model/vnd.dwfx+xps", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml.template", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.stencil", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.template", Type.BINARY);
            documentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-visio.stencil.macroenabled.12", Type.BINARY);
            documentTypeMap.put("application/vnd.ms-excel.template.macroenabled.12", Type.BINARY);
//        org.apache.tika.parser.microsoft.ooxml.xwpf.ml2006.Word2006MLParser
            documentTypeMap.put("application/vnd.ms-word2006ml", Type.BINARY);
//        org.apache.tika.parser.microsoft.pst.OutlookPSTParser
            documentTypeMap.put("application/vnd.ms-outlook-pst", Type.BINARY);
//        org.apache.tika.parser.microsoft.rtf.RTFParser
            documentTypeMap.put("application/rtf", Type.STANDARD);
//        org.apache.tika.parser.microsoft.xml.SpreadsheetMLParser
            documentTypeMap.put("application/vnd.ms-spreadsheetml", Type.BINARY);
//        org.apache.tika.parser.microsoft.xml.WordMLParser
            documentTypeMap.put("application/vnd.ms-wordml", Type.BINARY);
//        org.apache.tika.parser.mif.MIFParser
            documentTypeMap.put("application/x-mif", Type.BINARY);
            documentTypeMap.put("application/vnd.mif", Type.BINARY);
            documentTypeMap.put("application/x-maker", Type.BINARY);
//        org.apache.tika.parser.mp3.Mp3Parser
            documentTypeMap.put("audio/mpeg", Type.BINARY);
//        org.apache.tika.parser.mp4.MP4Parser
            documentTypeMap.put("video/x-m4v", Type.BINARY);
            documentTypeMap.put("application/mp4", Type.BINARY);
            documentTypeMap.put("video/3gpp", Type.BINARY);
            documentTypeMap.put("video/3gpp2", Type.BINARY);
            documentTypeMap.put("video/quicktime", Type.BINARY);
            documentTypeMap.put("audio/mp4", Type.BINARY);
            documentTypeMap.put("video/mp4", Type.BINARY);
//        org.apache.tika.parser.ocr.TesseractOCRParser
            documentTypeMap.put("image/ocr-x-portable-pixmap", Type.BINARY);
            documentTypeMap.put("image/ocr-jpx", Type.BINARY);
            documentTypeMap.put("image/x-portable-pixmap", Type.BINARY);
            documentTypeMap.put("image/ocr-jpeg", Type.BINARY);
            documentTypeMap.put("image/ocr-jp2", Type.BINARY);
            documentTypeMap.put("image/jpx", Type.BINARY);
            documentTypeMap.put("image/ocr-png", Type.BINARY);
            documentTypeMap.put("image/ocr-tiff", Type.BINARY);
            documentTypeMap.put("image/ocr-gif", Type.BINARY);
            documentTypeMap.put("image/ocr-bmp", Type.BINARY);
            documentTypeMap.put("image/jp2", Type.BINARY);
//        org.apache.tika.parser.odf.FlatOpenDocumentParser
            documentTypeMap.put("application/vnd.oasis.opendocument.tika.flat.document", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.flat.presentation", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.flat.spreadsheet", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.flat.text", Type.BINARY);
//        org.apache.tika.parser.odf.OpenDocumentParser
            documentTypeMap.put("application/x-vnd.oasis.opendocument.presentation", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.chart", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.text-web", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.image", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.graphics-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.text-web", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.spreadsheet-temp", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.spreadsheet-template", Type.BINARY);
            documentTypeMap.put("application/vnd.sun.xml.writer", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.graphics-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.graphics", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.spreadsheet", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.chart", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.spreadsheet", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.image", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.text", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.text-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.formula-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.formula", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.image-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.image-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.presentation-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.presentation-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.text", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.text-template", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.chart-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.chart-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.formula-template", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.text-master", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.presentation", Type.BINARY);
            documentTypeMap.put("application/x-vnd.oasis.opendocument.graphics", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.formula", Type.BINARY);
            documentTypeMap.put("application/vnd.oasis.opendocument.text-master", Type.BINARY);
//        org.apache.tika.parser.pdf.PDFParser", Type.BINARY);
            documentTypeMap.put("application/pdf", Type.STANDARD);
//org.apache.tika.parser.pkg.CompressorParser
            documentTypeMap.put("application/zlib", Type.ARCHIVE);
            documentTypeMap.put("application/x-gzip", Type.ARCHIVE);
            documentTypeMap.put("application/x-bzip2", Type.ARCHIVE);
            documentTypeMap.put("application/x-compress", Type.ARCHIVE);
            documentTypeMap.put("application/x-java-pack200", Type.ARCHIVE);
            documentTypeMap.put("application/x-lzma", Type.ARCHIVE);
            documentTypeMap.put("application/deflate64", Type.ARCHIVE);
            documentTypeMap.put("application/x-lz4", Type.ARCHIVE);
            documentTypeMap.put("application/x-snappy", Type.ARCHIVE);
            documentTypeMap.put("application/x-brotli", Type.ARCHIVE);
            documentTypeMap.put("application/gzip", Type.ARCHIVE);
            documentTypeMap.put("application/x-bzip", Type.ARCHIVE);
            documentTypeMap.put("application/x-xz", Type.ARCHIVE);
//org.apache.tika.parser.pkg.PackageParser
            documentTypeMap.put("application/x-tar", Type.ARCHIVE);
            documentTypeMap.put("application/java-archive", Type.ARCHIVE);
            documentTypeMap.put("application/x-arj", Type.ARCHIVE);
            documentTypeMap.put("application/x-archive", Type.ARCHIVE);
            documentTypeMap.put("application/zip", Type.ARCHIVE);
            documentTypeMap.put("application/x-cpio", Type.ARCHIVE);
            documentTypeMap.put("application/x-tika-unix-dump", Type.ARCHIVE);
            documentTypeMap.put("application/x-7z-compressed", Type.ARCHIVE);
//org.apache.tika.parser.pkg.RarParser
            documentTypeMap.put("application/x-rar-compressed", Type.ARCHIVE);
//        org.apache.tika.parser.prt.PRTParser
            documentTypeMap.put("application/x-prt", Type.BINARY);
//        org.apache.tika.parser.sas.SAS7BDATParser
            documentTypeMap.put("application/x-sas-data", Type.BINARY);
//        org.apache.tika.parser.tmx.TMXParser
            documentTypeMap.put("application/x-tmx", Type.BINARY);
//        org.apache.tika.parser.video.FLVParser
            documentTypeMap.put("video/x-flv", Type.BINARY);
//        org.apache.tika.parser.wacz.WACZParser
            documentTypeMap.put("application/x-wacz", Type.BINARY);
//        org.apache.tika.parser.warc.WARCParser
            documentTypeMap.put("application/warc", Type.BINARY);
            documentTypeMap.put("application/warc+gz", Type.BINARY);
//        org.apache.tika.parser.wordperfect.QuattroProParser
            documentTypeMap.put("application/x-quattro-pro; version=9", Type.BINARY);
//        org.apache.tika.parser.wordperfect.WordPerfectParser
            documentTypeMap.put("application/vnd.wordperfect; version=5.1", Type.BINARY);
            documentTypeMap.put("application/vnd.wordperfect; version=5.0", Type.BINARY);
            documentTypeMap.put("application/vnd.wordperfect; version=6.x", Type.BINARY);
//        org.apache.tika.parser.xliff.XLIFF12Parser
            documentTypeMap.put("application/x-xliff+xml", Type.STANDARD);
//        org.apache.tika.parser.xliff.XLZParser
            documentTypeMap.put("application/x-xliff+zip", Type.ARCHIVE);
//        org.apache.tika.parser.xml.DcXMLParser
            documentTypeMap.put("application/xml", Type.STANDARD);
            documentTypeMap.put("image/svg+xml", Type.STANDARD);
//        org.apache.tika.parser.xml.FictionBookParser
            documentTypeMap.put("application/x-fictionbook+xml", Type.STANDARD);
//        org.gagravarr.tika.FlacParser
            documentTypeMap.put("audio/x-oggflac", Type.BINARY);
            documentTypeMap.put("audio/x-flac", Type.BINARY);
//        org.gagravarr.tika.OggParser
            documentTypeMap.put("audio/ogg", Type.BINARY);
            documentTypeMap.put("application/kate", Type.BINARY);
            documentTypeMap.put("application/ogg", Type.BINARY);
            documentTypeMap.put("video/daala", Type.BINARY);
            documentTypeMap.put("video/x-ogguvs", Type.BINARY);
            documentTypeMap.put("video/x-ogm", Type.BINARY);
            documentTypeMap.put("audio/x-oggpcm", Type.BINARY);
            documentTypeMap.put("video/ogg", Type.BINARY);
            documentTypeMap.put("video/x-dirac", Type.BINARY);
            documentTypeMap.put("video/x-oggrgb", Type.BINARY);
            documentTypeMap.put("video/x-oggyuv", Type.BINARY);
//        org.gagravarr.tika.OpusParser
            documentTypeMap.put("audio/opus", Type.BINARY);
            documentTypeMap.put("audio/ogg; codecs=opus", Type.BINARY);
//        org.gagravarr.tika.SpeexParser
            documentTypeMap.put("audio/ogg; codecs=speex", Type.BINARY);
            documentTypeMap.put("audio/speex", Type.BINARY);
//        org.gagravarr.tika.TheoraParser
            documentTypeMap.put("video/theora", Type.BINARY);
//        org.gagravarr.tika.VorbisParser
            documentTypeMap.put("audio/vorbis", Type.BINARY);
        };
    }

    /**
     * @return the name of the current document.
     */
    String getName();

    /**
     * Reads the contents of this document.
     * 
     * @return <code>Reader</code> not null
     * @throws IOException if this document cannot be read
     * @throws CompositeDocumentException if this document can only be read as a
     * composite archive
     */
    Reader reader() throws IOException;

    /**
     * Streams the document's contents.
     * 
     * @return a non null input stream of the document.
     * @throws IOException when stream could not be opened
     */
    InputStream inputStream() throws IOException;

    /**
     * Gets data describing this resource.
     * 
     * @return a non null MetaData object.
     */
    MetaData getMetaData();

    /**
     * Tests if this a composite document.
     * 
     * @return true if composite, false otherwise
     */
    boolean isComposite();
}
