package net.ripper.util;

// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 



// 
/** Fast String Utilities.
 *
 * These string utilities provide both conveniance methods and
 * performance improvements over most standard library versions. The
 * main aim of the optimizations is to avoid object creation unless
 * absolutely required.
 *
 * @author Greg Wilkins (gregw)
 */
public class StringUtil
{
    public static final String CRLF="\015\012";
    public static final String __LINE_SEPARATOR=
        System.getProperty("line.separator","\n");
    
    public static String __ISO_8859_1;
    static
    {
        String iso=System.getProperty("ISO_8859_1");
        if (iso!=null)
            __ISO_8859_1=iso;
        else
        {
            try{
                new String(new byte[]{(byte)20},"ISO-8859-1");
                __ISO_8859_1="ISO-8859-1";
            }
            catch(java.io.UnsupportedEncodingException e)
            {
                __ISO_8859_1="ISO8859_1";
            }        
        }
    }
    
    public final static String __UTF8="UTF-8";
    
    
    private static char[] lowercases = {
          '\000','\001','\002','\003','\004','\005','\006','\007',
          '\010','\011','\012','\013','\014','\015','\016','\017',
          '\020','\021','\022','\023','\024','\025','\026','\027',
          '\030','\031','\032','\033','\034','\035','\036','\037',
          '\040','\041','\042','\043','\044','\045','\046','\047',
          '\050','\051','\052','\053','\054','\055','\056','\057',
          '\060','\061','\062','\063','\064','\065','\066','\067',
          '\070','\071','\072','\073','\074','\075','\076','\077',
          '\100','\141','\142','\143','\144','\145','\146','\147',
          '\150','\151','\152','\153','\154','\155','\156','\157',
          '\160','\161','\162','\163','\164','\165','\166','\167',
          '\170','\171','\172','\133','\134','\135','\136','\137',
          '\140','\141','\142','\143','\144','\145','\146','\147',
          '\150','\151','\152','\153','\154','\155','\156','\157',
          '\160','\161','\162','\163','\164','\165','\166','\167',
          '\170','\171','\172','\173','\174','\175','\176','\177' };

    /* ------------------------------------------------------------ */
    public static boolean endsWithIgnoreCase(String s,String w)
    {
        if (w==null)
            return true;

        if (s==null)
            return false;
            
        int sl=s.length();
        int wl=w.length();
        
        if (sl<wl)
            return false;
        
        for (int i=wl;i-->0;)
        {
            char c1=s.charAt(--sl);
            char c2=w.charAt(i);
            if (c1!=c2)
            {
                if (c1<=127)
                    c1=lowercases[c1];
                if (c2<=127)
                    c2=lowercases[c2];
                if (c1!=c2)
                    return false;
            }
        }
        return true;
    }
}