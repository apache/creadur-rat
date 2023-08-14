package org.apache.rat.configuration;

import java.util.Map;
import java.util.HashMap;

public class Readers {
    
    /*
     * --- JSON ---
     *  {
     *      families : {
     *          xxx: 'name',
     *          },
     *      licenses : [
     *      {
     *          family : 'xxx',
     *          notes: 'notes',
     *          spdx: 'spdx',
     *          fulltext: 'text',
     *          copyright: 'copyright',
     *          text = ['text','text2','text3'],
     *          }],
     *  }
     * 
     * --- RDF ---
     *
     * family:category [
     *   name='   '
     *   ] 
     *
     * license:id [
     *   family family:category
     *   notes=''
     *   spdx=''
     *   fulltext=''
     *   copyright=''
     *   text=''
     *   text=''
     *   text=''
     * ]
     *
     * --- XML ---
     * <config>
     * <family id='fxx' category='xxxxx'>
     *      name text
     * </family>
     * 
     * <license id="lxx' family='fxx' spdx='spdxtx'>
     *  <notes>notes text</notes>
     *  <fulltext>full text</fulltext>
     *  <copyright>copyright</copyright>
     *  <text>text</text>
     *  </license>
     *  </config>
     *  
     *  --- CONFIG ---
     * family.category.name=text
     * 
     * license.id.family=famid
     * license.id.notes=
     * license.id.fullText=
     * license.id.copyright=
     * license.id.text=
     * license.id.spdx=
     */

    private static Map<Format,Reader> readers;

    static {
        readers = new HashMap<>();
        readers.put( Format.CONFIG, new ConfigurationReader());
    }

    public static Reader get(String fileName) {
        return readers.get(Format.fromName(fileName));
    }
    
    private Readers() {
        // do not instantiate
    }
}
