## Additional license information structure

Not all archive entries have to have license information defined in their POMs. Therefore you may define such additional information in JSON file ( ``src/main/license.mapping``).

The JSON structure is defined as following

     {
       "licenses": [
           {
              "name" : "License name",
               "url"  : "License text URL",
               "files": [
                   "file name"
                ]
           }
       ]
     }

JSON license mapping example

     {
       "licenses": [
           {
               "name"  : "The LGPL license 2.1",
               "url"   : "http://www.gnu.org/licenses/lgpl-2.1.html",
               "files" : [
                             "aspectwerkz-nodeps-jdk5-2.2.1.jar"
               ]
           },
           {
               "name"  : "The BSD license",
               "url"   : "http://www.opensource.org/licenses/bsd-license.php",
               "files" : [
                             "antlr-2.7.6.jar",
                             "asm-3.1.jar",
                             "dom4j-1.6.1.jar"
               ]
           }
       ]
     }
