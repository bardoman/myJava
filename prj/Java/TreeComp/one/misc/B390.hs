<?xml ?>

<helpset>

<title>Build390 Client Users Guide</title>

<maps>
   <mapref location="B390.map" engine="Build390.Utilities.help.B390Engine"/>
</maps>

<wintype default=false>
    <name>ExternalLink</name>
    <height>70%</height>
    <width>400</width>
    <x>10</x>
    <y>10</y>
    <textfg>000000</textfg>
    <linkfg>0000cc</linkfg>
    <bg>ffffff</bg>
    <title>External Link</title>
    <toolbar>06444</toolbar>
</wintype>


<links>
</links>

<view>
   <type>oracle.help.navigator.tocNavigator.TOCNavigator</type>
   <data engine="oracle.help.engine.XMLTOCEngine">B390TOC.xml</data>
</view>

<view>
   <type>oracle.help.navigator.keywordNavigator.KeywordNavigator</type>
   <data engine="oracle.help.engine.XMLIndexEngine">B390Index.xml</data>
</view>

</helpset>

<!OHJ_WIZARD_PROPERTY MAP_OPTION ="INCLUDE" >
<!OHJ_WIZARD_PROPERTY SEARCH_ENCODING ="8859_1" >
<!OHJ_WIZARD_PROPERTY BACKUP_FILES ="TRUE" >
<!OHJ_WIZARD_PROPERTY INCLUDE_SUBDIR ="FALSE" >
<!OHJ_WIZARD_PROPERTY MAP_INCLUDE ="B390.map" >
<!OHJ_WIZARD_PROPERTY TARGET_BROWSER ="ICE Browser 5.0 (default)" >
<!OHJ_WIZARD_PROPERTY SEARCH_CASE ="FALSE" >
<!OHJ_WIZARD_PROPERTY FILE_BASENAME ="B390" >
