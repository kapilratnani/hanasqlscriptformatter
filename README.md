HANA SQLScript Formatter
----------------------

Eclipse plugin for formatting SAP HANA SQLScript. Can be used with SAP HANA Studio.

Installation
------------

Latest Binaries can be downloaded from https://sourceforge.net/projects/sqlscriptformat/files/

1. Download "hanasqlscriptformatter.zip" file from the above link.
2. Open HDBStudio 
3. Click on Help->Install New Software.
4. Click on "Add". In the "Add Repository" dialog Click on "Archive".
5. Select the downloaded archive "hanasqlscriptformatter.zip" and continue as usual.
 
Usage
-----
1. Select the procedure and press "CTRL+6"(OSX - CMD+6) or click on the toolbar icon labeled "Format HANA SQL Script".

Note : You need to select the whole procedure for the formatter to work properly. If in some case the formatter doesn't work as intended post a bug report in the issues section and as always, undo command will bring the procedure to pre-formatted state.

Known issues
------------
Doesn't works with "/**/" style comment inside "create procedure proc_name(<args>)...as" section, works as intended after that.

TODO
----
1. Add license.
2. Fix /**/ style comments inside create proc.
3. Work on better formatting of subqueries.
4. Look for alternative ways of formatting, look in the direction of generating a parser and then format using the AST.

Disclaimer
--------------
This plugin is not a part of standard SAP HANA delivery package, hence SAP HANA support is not responsible for this plugin in any way. All support queries regarding this plugin should be reported here(https://github.com/kapilratnani/hanasqlscriptformatter). If i've time, i'll fix them.
